package com.get_slyncy.slyncy.Model.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.get_slyncy.slyncy.BuildConfig;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class MessagesMonitoringService extends Service
{

    private static final String TAG = "MessagesMonService";

    private static Uri uriSMS = Uri.parse("content://mms-sms/conversations/");
    // private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    // private static final Uri SMS_INBOX_CONTENT_URI =
    // Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

    private static final String MSG_ID = "_id";
    private static final String THREAD_ID = "thread_id";
    private static final String DATE = "date";
    private static final String MMS_TYPE = "ct";
    private static final String TYPE = "type";
    private static final String READ = "read";
    private static final String TEXT = "text";
    private static final String ADDRESS = "address";
    private static final String BODY = "body";

    private ContentResolver crMsg;
    private MsgContentObserver observerMsg = null;
    private Context context;

    /**
     * Start the service to process that will run the content observerMsg
     */
    public static void beginStartingService(Context context)
    {
        if (BuildConfig.DEBUG) Log.v(TAG, "beginStartingService()");
        context.startService(new Intent(context, MessagesMonitoringService.class));
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service)
    {
        if (BuildConfig.DEBUG) Log.v(TAG, "SmsMonitorService: finishStartingService()");
        service.stopSelf();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
        if (BuildConfig.DEBUG) Log.v(TAG, "SmsMonitorService created");
        registerMsgsObserver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        unregisterMsgsObserver();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    /*
     * Registers the observerMsg for SMS changes
     */
    private void registerMsgsObserver()
    {
        if (observerMsg == null)
        {
            observerMsg = new MsgContentObserver(new Handler());
            crMsg = getContentResolver();
            crMsg.registerContentObserver(uriSMS, true, observerMsg);
            if (BuildConfig.DEBUG) Log.v(TAG, "SMS Observer registered.");
        }
    }

    /**
     * Unregisters the observerMsg for call log changes
     */
    private void unregisterMsgsObserver()
    {
        if (crMsg != null)
        {
            crMsg.unregisterContentObserver(observerMsg);
        }
        if (observerMsg != null)
        {
            observerMsg = null;
        }
        if (BuildConfig.DEBUG) Log.v(TAG, "Unregistered SMS Observer");
    }

    private class MsgContentObserver extends ContentObserver
    {
        public MsgContentObserver(Handler handler)
        {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            Uri uriSMSURI = Uri.parse("content://mms-sms/conversations");
            Cursor c = context.getContentResolver().query(uriSMSURI,
             null, null,null, "normalized_date desc");

            c.moveToFirst();
            updateReadStatus(c);
//            Log.d(TAG, c.getString(c.getColumnIndex("body")));
        }
    }

    private void updateReadStatus(Cursor c) {
        if (c.getInt(c.getColumnIndex(READ)) == 1) {
            String messageId = c.getString(c.getColumnIndex(MSG_ID));
            int threadId = c.getInt(c.getColumnIndex(THREAD_ID));
            String body = c.getString(c.getColumnIndex(BODY));
            Log.d(TAG, "Message: " + messageId + ", ThreadId: " + threadId + " with body \'" + body + "\' marked as read");
            ClientCommunicator.markThreadAsRead(threadId);
        }
    }

    /**
     * Start the service to process that will run the content observerMsg
     */
    public static void beginStartingService(Context context) {
        if (BuildConfig.DEBUG) Log.v(TAG, "beginStartingService()");
        context.startService(new Intent(context, MessagesMonitoringService.class));
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service) {
        if (BuildConfig.DEBUG) Log.v(TAG, "SmsMonitorService: finishStartingService()");
        service.stopSelf();
    }

}
