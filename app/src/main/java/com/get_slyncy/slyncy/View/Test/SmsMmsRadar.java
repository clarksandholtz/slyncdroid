package com.get_slyncy.slyncy.View.Test;

/**
 * Created by undermark5 on 3/12/18.
 */


import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.get_slyncy.slyncy.BuildConfig;
import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.tuenti.smsradar.*;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

public class SmsMmsRadar extends Service
{

    String substr;
    int k;
    private static Queue<String> inComingMms;
    private static Queue<String> outGoingMms;
    private static Queue<String> inComingSms;
    private static Queue<String> outGoingSms;
    private static Queue<String> markedMms;
    private static Queue<String> markedSms;
    private static boolean observerRegistered = false;
    private ContentResolver contentResolver;
    private SMSObserver smsObserver;
    private SmsStorage smsStorage;
    private MmsStorage mmsStorage;
    private static final int SMS_MAX_AGE_MILLIS = 5000;
    private static final int MMS_MAX_AGE_MILLIS = 5000;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.v("Debug", " service created.........");
    }

    public void registerObserver()
    {
        observerRegistered = true;
        contentResolver = getContentResolver();
        if (inComingMms == null)
            inComingMms = new ArrayDeque<>();
        if (outGoingMms == null)
            outGoingMms = new ArrayDeque<>();
        if (inComingSms == null)
            inComingSms = new ArrayDeque<>();
        if (outGoingSms == null)
            outGoingSms = new ArrayDeque<>();
        if (markedSms == null)
            markedSms = new ArrayDeque<>();
        if (markedMms == null)
            markedMms = new ArrayDeque<>();
        smsObserver = new SMSObserver(new Handler());
        contentResolver.registerContentObserver(Uri.parse("content://mms-sms/conversations/"),
                true, smsObserver);
        Log.v("Debug", " in registerObserver method.........");
    }

    //start the service and register observer for lifetime
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v("Debug", "Service has been started..");
        Toast.makeText(getApplicationContext(),
                "Service has been started.. ",
                Toast.LENGTH_SHORT).show();
        if (!observerRegistered)
        {
            registerObserver();
        }
        if (smsStorage == null)
        {
            SharedPreferences preferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);
            smsStorage = new SharedPreferencesSmsStorage(preferences, getApplicationContext());

        }
        if (mmsStorage == null)
        {
            SharedPreferences preferences = getSharedPreferences("mms_preferences", MODE_PRIVATE);
            mmsStorage = new SharedPreferencesMmsStorage(preferences, getApplicationContext());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterObserver();
    }

    private void unregisterObserver()
    {
        if (contentResolver != null)
        {
            contentResolver.unregisterContentObserver(smsObserver);
        }
        if (smsObserver != null)
        {
            smsObserver = null;
            observerRegistered = false;
        }
        if (BuildConfig.DEBUG) Log.v("MsgRadar", "Unregistered SMS Observer");
    }

    //    public static void removeReadMms(String msgId)
//    {
//        markedMms.remove(msgId);
//        inComingMms.remove(msgId);
//    }

    class SMSObserver extends ContentObserver
    {

        public SMSObserver(Handler handler)
        {
            super(handler);
        }

        //will be called when database get change
        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            Log.v("Debug", "Observer has been started..");

  /*first of all we need to decide message is Text or MMS type.*/
            final String[] projection = new String[]{
                    "_id", "ct_t", "read", "date"};
            Uri mainUri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI;
            Cursor mainCursor = contentResolver.
                    query(mainUri, projection,
                            null, null, "normalized_date desc");
            mainCursor.moveToFirst();


            String msgContentType = mainCursor.getString(mainCursor.
                    getColumnIndex("ct_t"));
            String msgId = mainCursor.getString(mainCursor.getColumnIndex("_id"));
            String msgDate = mainCursor.getString(mainCursor.getColumnIndex("date"));
            int read = mainCursor.getInt(mainCursor.getColumnIndex("read"));
            if (msgContentType != null)
            {
                // it's MMS
                Log.v("Debug", "it's MMS");

                //now we need to decide MMS message is sent or received
                Uri mUri = Uri.parse("content://mms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("msg_box"));

                if (type == 1)
                {
                    if (inComingMms.contains(msgId) && mmsStorage.isUnread(Integer.valueOf(Integer.valueOf(msgId))))
                    {
                        if (read == 1 && mmsStorage.isUnread(Integer.valueOf(msgId)))
                        {
                            mmsStorage.addNewMessage(Integer.valueOf(msgId));
                        }
                        //markAsRead
                    }
                    else if (shouldParseMms(Integer.valueOf(msgId), msgDate))
                    {
                        //it's received MMS
                        mmsStorage.updateLastMmsIntercepted(Integer.valueOf(msgId));
                        inComingMms.add(msgId);
                        if (inComingMms.size() > 10)
                        {
                            inComingMms.remove();
                        }
                        Log.v("Debug", "it's received MMS");
                        getReceivedMMSinfo();
                    }
                }
                else if (type == 2)
                {
                    if (!outGoingMms.contains(msgId))
                    {
                        //it's sent MMS
                        outGoingMms.add(msgId);

                        if (outGoingMms.size() > 10)
                        {
                            outGoingMms.remove();
                        }
                        Log.v("Debug", "it's Sent MMS");
                        getSentMMSinfo();
                    }
                }
            }
            else
            {
                // it's SMS
                Log.v("Debug", "it's SMS");

                //now we need to decide SMS message is sent or received
                Uri mUri = Uri.parse("content://sms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("type"));

                if (type == 1)
                {
                    //it's received SMS
                    if (inComingSms.contains(msgId))
                    {
                        if (read == 1 && smsStorage.isUnread(Integer.valueOf(msgId)))
                        {
                            smsStorage.addSms(Integer.valueOf(msgId));
                            if (markedSms.size() > 10)
                            {
                                markedSms.remove();
                            }
                        }
                    }
                    else if (shouldParseSms(Integer.valueOf(msgId), msgDate))
                    {
                        updateLastSmsParsed(Integer.valueOf(msgId));
                        inComingSms.add(msgId);
                        if (inComingSms.size() > 10)
                        {
                            inComingSms.remove();
                        }
                        Log.v("Debug", "it's received SMS");
                        getReceivedSMSinfo();
                    }
                }
                else if (type == 2)
                {
                    //it's sent SMS
                    if (!outGoingSms.contains(msgId))
                    {
                        outGoingSms.add(msgId);
                        if (outGoingSms.size() > 10)
                        {
                            outGoingSms.remove();
                        }
                        Log.v("Debug", "it's sent SMS");
                        getSentSMSinfo();
                    }
                }
            }//message content type block closed


        }//on changed closed

        private boolean shouldParseMms(int mmsId, String msgDate)
        {
            boolean isFirstMmsParsed = isFirstMmsParsed();
            boolean isOld = isOldMms(msgDate);
            boolean shouldParseId = shouldParseMmsId(mmsId);
            return (isFirstMmsParsed && !isOld) || (!isFirstMmsParsed && shouldParseId);
        }

        private boolean isFirstMmsParsed()
        {
            return mmsStorage.isFirstMmsIntercepted();
        }

        private boolean shouldParseMmsId(int mmsId)
        {
            if (smsStorage.isFirstSmsIntercepted()) {
                return false;
            }

            int lastMmsIdIntercepted = mmsStorage.getLastMmsIntercepted();
            return mmsId > lastMmsIdIntercepted;
        }

        private void updateLastSmsParsed(int smsId) {
            smsStorage.updateLastSmsIntercepted(smsId);
        }

        private boolean shouldParseSms(int smsId, String smsDate)
        {
            boolean isFirstSmsParsed = isFirstSmsParsed();
            boolean isOld = isOldSms(smsDate);
            boolean shouldParseId = shouldParseSmsId(smsId);
            return (isFirstSmsParsed && !isOld) || (!isFirstSmsParsed && shouldParseId);
        }

        private boolean isFirstSmsParsed() {
            return smsStorage.isFirstSmsIntercepted();
        }

        private boolean shouldParseSmsId(int smsId)
        {
            if (smsStorage.isFirstSmsIntercepted()) {
                return false;
            }
            int lastSmsIdIntercepted = smsStorage.getLastSmsIntercepted();
            return smsId > lastSmsIdIntercepted;
        }

        private boolean isOldMms(String mmsDate)
        {
            Date now = new Date();
            return now.getTime() - (Long.valueOf(mmsDate) * 1000) > MMS_MAX_AGE_MILLIS;
        }

        private boolean isOldSms(String smsDate)
        {

            Date now = new Date();
            return now.getTime() - Long.valueOf(smsDate) > SMS_MAX_AGE_MILLIS;
        }


 /*now Methods start to getting details for sent-received SMS*/


        //method to get details about received SMS..........
        private void getReceivedSMSinfo()
        {
            Uri uri = Uri.parse("content://sms/");
            String str = "";
            Cursor cursor = contentResolver.query(uri, new String[]{"*"},
                    null, null, null);
            cursor.moveToFirst();
            SlyncyMessage message = MessageDbUtility.getSmsMessage(cursor);
//            cursor.moveToNext();
//
//            // 1 = Received, etc.
//            int type = cursor.getInt(cursor.
//                    getColumnIndex("type"));
//            String msg_id = cursor.getString(cursor.
//                    getColumnIndex("_id"));
//            String phone = cursor.getString(cursor.
//                    getColumnIndex("address"));
//            String dateVal = cursor.getString(cursor.
//                    getColumnIndex("date"));
//            String body = cursor.getString(cursor.
//                    getColumnIndex("body"));
//            Date date = new Date(Long.valueOf(dateVal));

            str = "Received SMS: \n phone is: " + message.getSender();
            str += "\n SMS time stamp is:" + message.getDate();
            str += "\n SMS body is: " + message.getBody();
            str += "\n id is : " + message.getId();


            Log.v("Debug", "Received SMS phone is: " + message.getSender());
            Log.v("Debug", "SMS time stamp is:" + message.getDate());
            Log.v("Debug", "SMS body is: " + message.getBody());
            Log.v("Debug", "SMS id is: " + message.getId());

            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about SMS Received Message :) ");

        }

        //method to get details about Sent SMS...........
        private void getSentSMSinfo()
        {
            Uri uri = Uri.parse("content://sms/sent");
            String str = "";
            Cursor cursor = contentResolver.query(uri, null,
                    null, null, null);
            cursor.moveToNext();

            // 2 = sent, etc.
            int type = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String msg_id = cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            String body = cursor.getString(cursor.
                    getColumnIndex("body"));
            Date date = new Date(Long.valueOf(dateVal));

            str = "Sent SMS: \n phone is: " + phone;
            str += "\n SMS type is: " + type;
            str += "\n SMS time stamp is:" + date;
            str += "\n SMS body is: " + body;
            str += "\n id is : " + msg_id;


            Log.v("Debug", "sent SMS phone is: " + phone);
            Log.v("Debug", "SMS type is: " + type);
            Log.v("Debug", "SMS time stamp is:" + date);
            Log.v("Debug", "SMS body is: " + body);
            Log.v("Debug", "SMS id is: " + msg_id);

            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Sent SMS Message :) ");
        }


  /*now Methods start to getting details for sent-received MMS.*/

        // 1. method to get details about Received (inbox)  MMS...
        private void getReceivedMMSinfo()
        {
            SlyncyMessage message = new SlyncyMessage();
            Uri uri = Uri.parse("content://mms/inbox");
            String str = "";
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToNext();

            String mms_id = cursor.getString(cursor.getColumnIndex("_id"));
            message.setId(mms_id);
            message.setNumbers(MessageDbUtility.getMmsAddresses(message, getContentResolver()));
            message.setDate(cursor.getLong(cursor.getColumnIndex("date")) * 1000);
            message.setRead(cursor.getInt(cursor.getColumnIndex("read")) == 1);
            MessageDbUtility.parseMmsParts(message, getContentResolver(), null);
            // 2 = sent, etc.
            int mtype = cursor.getInt(cursor.
                    getColumnIndex("msg_box"));
            message.setUserSent(mtype == 2);

            if (!ClientCommunicator.uploadSingleMessage(message))
            {
//                inComingMms.remove(mms_id); //failed to upload so should try again. eventually replaced by job
                //schedule job to upload later.
            }
            else
            {
                Log.i("SlyncyMMSMonitor", "MMS Uploaded");
            }

            Log.v("Debug", "sent MMS phone is: " + message.getSender());
            Log.v("Debug", "MMS type is: " + mtype);
            Log.v("Debug", "MMS time stamp is:" + message.getDate());
            Log.v("Debug", "MMS body is: " + message.getBody());
            Log.v("Debug", "MMS id is: " + message.getId());
            if (message.getImages().size() > 0)
            {
                Log.v("Debug", "MMS image name: " + message.getImages().get(0).getName());
            }
            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Received MMS Message :) ");
        }

        /* .......methods to get details about Sent MMS.... */
        private void getSentMMSinfo()
        {

            SlyncyMessage message = new SlyncyMessage();
            Uri uri = Uri.parse("content://mms/sent");
            String str = "";
            Cursor cursor = getContentResolver().query(uri, null, null,null, null);
            cursor.moveToNext();

            String mms_id = cursor.getString(cursor.getColumnIndex("_id"));
            message.setId(mms_id);
            message.setNumbers(MessageDbUtility.getMmsAddresses(message, getContentResolver()));
            message.setDate(cursor.getLong(cursor.getColumnIndex("date")) * 1000);
            message.setRead(cursor.getInt(cursor.getColumnIndex("read")) == 1);
            MessageDbUtility.parseMmsParts(message, getContentResolver(), null);
            // 2 = sent, etc.
            int mtype = cursor.getInt(cursor.getColumnIndex("msg_box"));
            message.setUserSent(mtype == 2);

            if (!ClientCommunicator.uploadSingleMessage(message))
            {
                outGoingMms.remove(mms_id); //failed to upload so should try again. eventually replaced by job
                //schedule job to upload later.
            }
            else
            {
                Log.i("SlyncyMMSMonitor", "MMS Uploaded");
            }

            str = "Sent MMS: \n phone is: " + message.getSender();
            str += "\n MMS type is: " + mtype;
            str += "\n MMS time stamp is:" + message.getDate();
            str += "\n MMS body is: " + message.getBody();
            str += "\n id is : " + mms_id;

            Log.v("Debug", "sent MMS phone is: " + message.getSender());
            Log.v("Debug", "MMS type is: " + mtype);
            Log.v("Debug", "MMS time stamp is:" + message.getDate());
            Log.v("Debug", "MMS body is: " + message.getBody());
            Log.v("Debug", "MMS id is: " + mms_id);
            if (message.getImages().size() > 0)
            {
                Log.v("Debug", "MMS image name: " + message.getImages().get(0).getName());
            }
            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Sent MMS Message :) ");

        }


//        //method to get Text body from Sent MMS............
//        private String getSentMmsText(String id)
//        {
//
//            Uri partURI = Uri.parse("content://mms/sent" + id);
//            InputStream is = null;
//            StringBuilder sb = new StringBuilder();
//            try
//            {
//                is = getContentResolver().openInputStream(partURI);
//                if (is != null)
//                {
//                    InputStreamReader isr = new InputStreamReader(is,
//                            "UTF-8");
//                    BufferedReader reader = new BufferedReader(isr);
//                    String temp = reader.readLine();
//                    while (temp != null)
//                    {
//                        sb.append(temp);
//                        temp = reader.readLine();
//                    }
//                }
//            }
//            catch (IOException e)
//            {
//            }
//            finally
//            {
//                if (is != null)
//                {
//                    try
//                    {
//                        is.close();
//                    }
//                    catch (IOException e)
//                    {
//
//                    }
//                }
//            }
//            return sb.toString();
//
//        }
//
//        //method to get image from sent MMS............
//        private Bitmap getSentMmsImage(String id)
//        {
//
//            Uri partURI = Uri.parse("content://mms/sent" + id);
//            InputStream is = null;
//            Bitmap bitmap = null;
//            try
//            {
//                is = getContentResolver().
//                        openInputStream(partURI);
//                bitmap = BitmapFactory.decodeStream(is);
//            }
//            catch (IOException e)
//            {
//            }
//            finally
//            {
//                if (is != null)
//                {
//                    try
//                    {
//                        is.close();
//                    }
//                    catch (IOException e)
//                    {
//                    }
//                }
//            }
//            return bitmap;
//
//        }
    }//smsObserver class closed
}