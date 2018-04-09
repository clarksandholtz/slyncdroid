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
package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms;


import android.app.job.JobInfo;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;


/**
 * ContentObserver created to handle the sms content provider changes. This entity will be called each time the
 * system changes the sms content provider state.
 * <p/>
 * SmsObserver analyzes the change and studies if the protocol used is null or not to identify if the sms is incoming
 * or outgoing.
 * <p/>
 * SmsObserver will analyze the sms inbox and sent content providers to get the sms information and will notify
 * smsListener.
 * <p/>
 * The content observer will be called each time the sms content provider be updated. This means that all
 * the sms state changes will be notified. For example, when the sms state change from SENDING to SENT state.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class SmsObserver extends ContentObserver
{

    private static final Uri SMS_URI = Uri.parse("content://sms/");
    private static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
    private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
    private static final String PROTOCOL_COLUM_NAME = "protocol";
    private static final String SMS_ORDER = "date DESC";

    private ContentResolver contentResolver;
    private SmsCursorParser smsCursorParser;

    public SmsObserver(ContentResolver contentResolver, Handler handler, SmsCursorParser smsCursorParser)
    {
        super(handler);
        this.contentResolver = contentResolver;
        this.smsCursorParser = smsCursorParser;
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
            cursor = getSmsContentObserverCursor();
            if (cursor != null && cursor.moveToFirst())
            {
                String protocol = cursor.getString(cursor.getColumnIndex(PROTOCOL_COLUM_NAME));
                if (!isProtocolForOutgoingSms(protocol))
                {
                    processReadSms();
                }
                processSms(protocol);
            }
        }
        finally
        {
            close(cursor);
        }
    }

    private void processReadSms()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cursor cursor = null;
                try
                {
                    cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, new String[]{"*"}, "read = 1", null, "date desc");
                    if (cursor != null && cursor.moveToFirst())
                    {
                        int msgId = cursor.getInt(cursor.getColumnIndex("_id"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));
                        int threadId = cursor.getInt(cursor.getColumnIndex("thread_id"));
                        if (SmsCursorParser.shouldParseReadSms(msgId))
                        {
                            if (!ClientCommunicator.markThreadAsRead(threadId))
                            {
//                                JobInfo ji =
//                                new MarkReadJob(threadId);
                            }
                            SmsCursorParser.updateLastSmsRead(msgId);
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

    private void processSms(final String protocol)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Cursor smsCursor = null;
                try
                {
                    smsCursor = getSmsCursor(protocol);
                    SlyncyMessage sms = parseSms(smsCursor);
                    notifySmsListener(sms);
                }
                finally
                {
                    close(smsCursor);
                }
            }
        }).start();
    }

    private void notifySmsListener(SlyncyMessage sms)
    {
        if (sms != null && SmsMmsRadar.smsListener != null)
        {
            if (sms.isUserSent())
            {
                SmsMmsRadar.smsListener.onSmsSent(sms);
            }
            else
            {
                SmsMmsRadar.smsListener.onSmsReceived(sms);
            }
        }
    }

    private Cursor getSmsCursor(String protocol)
    {
        return getSmsDetailsCursor(protocol);
    }

    private Cursor getSmsDetailsCursor(String protocol)
    {
        Cursor smsCursor;
        if (isProtocolForOutgoingSms(protocol))
        {
            //SMS Sent
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_SENT.getUri());
        }
        else
        {
            //SMSReceived
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_RECEIVED.getUri());
        }
        return smsCursor;
    }

    private Cursor getSmsContentObserverCursor()
    {
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return contentResolver.query(SMS_URI, projection, selection, selectionArgs, sortOrder);
    }

    private boolean isProtocolForOutgoingSms(String protocol)
    {
        return protocol == null;
    }

    private Cursor getSmsDetailsCursor(Uri smsUri)
    {

        return smsUri != null ? this.contentResolver.query(smsUri, null, null, null, SMS_ORDER) : null;
    }

    private SlyncyMessage parseSms(Cursor cursor)
    {
        return smsCursorParser.parse(cursor);
    }

    private void close(Cursor cursor)
    {
        if (cursor != null)
        {
            cursor.close();
        }
    }

    /**
     * Represents the SMS origin.
     */
    private enum SmsContext
    {
        SMS_SENT
                {
                    @Override
                    Uri getUri()
                    {
                        return SMS_SENT_URI;
                    }
                }, SMS_RECEIVED
            {
                @Override
                Uri getUri()
                {
                    return SMS_INBOX_URI;
                }
            };

        abstract Uri getUri();
    }
}