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


import android.content.ContentResolver;
import android.database.Cursor;


import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.TimeProvider;

import java.util.Date;

import static com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility.*;


/**
 * Works as cursor parser to get mms info from a cursor obtained from mms inbox/sent content provider.
 * <p/>
 * This entity will be called from MmsObserver with a cursor created over mms inbox or mms sent content provider to
 * extract the mms information and return an Mms object with the most important info we can get from mms content
 * provider.
 * <p/>
 * This entity can't be stateless because the MmsObserver it's called more than one time when the mms
 * content provider receive a incoming or outgoing mms. MmsCursorParser keep a reference of the last mms id parsed
 * and use it to parse only the correct incoming or outgoing mms. This implementation is based on a
 * lastMmsIdProcessed var that is updated each time an mms it's parsed.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class MmsCursorParser
{

    private static final String ADDRESS_COLUMN_NAME = "address";
    private static final String DATE_COLUMN_NAME = "date";
    private static final String BODY_COLUMN_NAME = "body";
    private static final String TYPE_COLUMN_NAME = "m_type";
    private static final String ID_COLUMN_NAME = "_id";
    private static final String THREAD_ID_COLUMN_NAME = "thread_id";
    private static final int MMS_MAX_AGE_MILLIS = 5000;

    private static MmsStorage mmsStorage;
    private static TimeProvider timeProvider;

    public MmsCursorParser(MmsStorage mmsStorage, TimeProvider timeProvider)
    {
        MmsCursorParser.mmsStorage = mmsStorage;
        MmsCursorParser.timeProvider = timeProvider;
    }

    SlyncyMessage parse(Cursor cursor, ContentResolver resolver)
    {

        if (!canHandleCursor(cursor) || !cursor.moveToNext())
        {
            return null;
        }

        SlyncyMessage mmsParsed = extractMmsInfoFromCursor(cursor, resolver);

        int mmsId = cursor.getInt(cursor.getColumnIndex(ID_COLUMN_NAME));
        String date = cursor.getString(cursor.getColumnIndex(DATE_COLUMN_NAME));
        Date mmsDate = new Date(Long.parseLong(date) * 1000);

        if (shouldParseMms(mmsId, mmsDate))
        {
            updateLastMmsParsed(mmsId);
        }
        else
        {
            mmsParsed = null;
        }

        return mmsParsed;
    }

    private void updateLastMmsParsed(int mmsId)
    {
        mmsStorage.updateLastMmsIntercepted(mmsId);
    }

    public static void updateLastMmsRead(int mmsId)
    {
        mmsStorage.updateLastMmsRead(mmsId);
    }

    public static boolean shouldParseMms(int mmsId, Date mmsDate)
    {
        boolean isFirstMmsParsed = isFirstMmsParsed();
        boolean isOld = isOld(mmsDate);
        boolean shouldParseId = shouldParseMmsId(mmsId);
        return (isFirstMmsParsed && !isOld) || (!isFirstMmsParsed && shouldParseId);
    }

    private static boolean isOld(Date mmsDate)
    {
        Date now = timeProvider.getDate();
        return now.getTime() - mmsDate.getTime() > MMS_MAX_AGE_MILLIS;
    }

    private static boolean shouldParseMmsId(int mmsId)
    {
        if (mmsStorage.isFirstMmsIntercepted())
        {
            return false;
        }
        int lastMmsIdIntercepted = mmsStorage.getLastMmsIntercepted();
        return mmsId > lastMmsIdIntercepted;
    }

    private static boolean isFirstMmsParsed()
    {
        return mmsStorage.isFirstMmsIntercepted();
    }

    private SlyncyMessage extractMmsInfoFromCursor(Cursor cursor, ContentResolver resolver)
    {
        //        String address = cursor.getString(cursor.getColumnIndex(ADDRESS_COLUMN_NAME));
//        long date = cursor.getLong(cursor.getColumnIndex(DATE_COLUMN_NAME));
//        String msg = cursor.getString(cursor.getColumnIndex(BODY_COLUMN_NAME));
//        String type = cursor.getString(cursor.getColumnIndex(TYPE_COLUMN_NAME));
//        long threadId = cursor.getLong(cursor.getColumnIndex(THREAD_ID_COLUMN_NAME));
//        return new Mms(address, date, msg, SmsType.fromValue(Integer.parseInt(type)), threadId)
        return getMmsMessage(cursor, resolver);
    }


    private boolean canHandleCursor(Cursor cursor)
    {
        return cursor != null && cursor.getCount() > 0;
    }

}
