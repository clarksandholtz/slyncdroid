/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Telephony;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.MarkReadJobService;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

import java.util.Date;


/**
 * ContentObserver created to handle the mms content provider changes. This entity will be called each time the
 * system changes the mms content provider state.
 * <p/>
 * SmsObserver analyzes the change and studies if the protocol used is null or not to identify if the mms is incoming
 * or outgoing.
 * <p/>
 * SmsObserver will analyze the mms inbox and sent content providers to get the mms information and will notify
 * smsListener.
 * <p/>
 * The content observer will be called each time the mms content provider be updated. This means that all
 * the mms state changes will be notified. For example, when the mms state change from SENDING to SENT state.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class MmsObserver extends ContentObserver
{

    private static final Uri MMS_URI = Uri.parse("content://mms/");
    private static final Uri MMS_SENT_URI = Uri.parse("content://mms/sent");
    private static final Uri MMS_INBOX_URI = Uri.parse("content://mms/inbox");
    private static final String PROTOCOL_COLUMN_NAME = "msg_box";
    private static final String SMS_ORDER = "date DESC";

    private ContentResolver contentResolver;
    private MmsCursorParser mmsCursorParser;
    private JobScheduler jobScheduler;
    private String packageName;

    public MmsObserver(ContentResolver contentResolver, Handler handler, MmsCursorParser mmsCursorParser, JobScheduler jobScheduler, String packageName)
    {
        super(handler);
        this.contentResolver = contentResolver;
        this.mmsCursorParser = mmsCursorParser;
        this.jobScheduler = jobScheduler;
        this.packageName = packageName;
    }


    @Override
    public boolean deliverSelfNotifications()
    {
        return true;
    }

    @Override
    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Cursor cursor = null;
        try
        {
            cursor = getMmsContentObserverCursor();
            if (cursor != null && cursor.moveToFirst())
            {
                int protocol = cursor.getInt(cursor.getColumnIndex(PROTOCOL_COLUMN_NAME));
                if (protocol == 1)
                {
                    processReadMms();
                }
                processMms(protocol);
            }
        }
        finally
        {
            close(cursor);
        }
//        Telephony.Mms.Sent.CONTENT_URI;
    }

    private void processReadMms()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cursor cursor = null;
                try
                {
                    cursor = contentResolver.query(Telephony.Mms.Inbox.CONTENT_URI, new String[]{"*"}, "read = 1", null, "date desc");
                    if (cursor != null && cursor.moveToFirst())
                    {
                        int msgId = cursor.getInt(cursor.getColumnIndex("_id"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));
                        int threadId = cursor.getInt(cursor.getColumnIndex("thread_id"));
                        if (MmsCursorParser.shouldParseMms(msgId, new Date(date * 1000)))
                        {
                            if (!ClientCommunicator.markThreadAsRead(threadId))
                            {
                                if (jobScheduler != null)
                                {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    {
                                        jobScheduler.enqueue(new JobInfo.Builder("slyncy_sync_read_service".hashCode(), new ComponentName(packageName, MarkReadJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build(), new JobWorkItem(new Intent().putExtra("threadId", threadId)));
                                    }
                                    else
                                    {
                                        PersistableBundle bundle = new PersistableBundle();
                                        bundle.putInt("threadId", threadId);
                                        jobScheduler.schedule(new JobInfo.Builder("slyncy_sync_read_service".hashCode(), new ComponentName(packageName, MarkReadJobService.class.toString())).setPersisted(true).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setExtras(bundle).build());
                                    }
                                }
                            } MmsCursorParser.updateLastMmsRead(msgId);
                        }
                    }
                }
                finally
                {
                    close(cursor);
                }
            }
        }).start();
    }

    private void processMms(final int protocol)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cursor mmsCursor = null;
                try
                {
                    mmsCursor = getMmsCursor(protocol);
                    SlyncyMessage mms = parseMms(mmsCursor);
                    if (mms != null)
                    {
                        mms.setUserSent(protocol == 2);
                    }
                    notifyMmsListener(mms);
                }
                finally
                {
                    close(mmsCursor);
                }
            }
        }).start();
    }

    private void notifyMmsListener(SlyncyMessage mms)
    {
        if (mms != null && SmsMmsRadar.mmsListener != null)
        {
            if (mms.isUserSent())
            {
                SmsMmsRadar.mmsListener.onMmsSent(mms);
            }
            else
            {
                SmsMmsRadar.mmsListener.onMmsReceived(mms);
            }
        }
    }

    private Cursor getMmsCursor(int protocol)
    {
        return getMmsDetailsCursor(protocol);
    }

    private Cursor getMmsDetailsCursor(int protocol)
    {
        Cursor mmsCursor;
        if (isProtocolForOutgoingMms(protocol))
        {
            //SMS Sent
            mmsCursor = getMmsDetailsCursor(MmsContext.MMS_SENT.getUri());
        }
        else
        {
            //SMSReceived
            mmsCursor = getMmsDetailsCursor(MmsContext.MMS_RECEIVED.getUri());
        }
        return mmsCursor;
    }

    private Cursor getMmsContentObserverCursor()
    {
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return contentResolver.query(MMS_URI, projection, selection, selectionArgs, sortOrder);
    }

    private boolean isProtocolForOutgoingMms(int protocol)
    {
        return protocol == 2;
    }

    private Cursor getMmsDetailsCursor(Uri mmsUri)
    {

        return mmsUri != null ? this.contentResolver.query(mmsUri, null, null, null, SMS_ORDER) : null;
    }

    private SlyncyMessage parseMms(Cursor cursor)
    {
        return mmsCursorParser.parse(cursor, contentResolver);
    }

    private void close(Cursor cursor)
    {
        if (cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
    }

    /**
     * Represents the MMS origin.
     */
    private enum MmsContext
    {
        MMS_SENT
                {
                    @Override
                    Uri getUri()
                    {
                        return MMS_SENT_URI;
                    }
                }, MMS_RECEIVED
            {
                @Override
                Uri getUri()
                {
                    return MMS_INBOX_URI;
                }
            };

        abstract Uri getUri();
    }
}