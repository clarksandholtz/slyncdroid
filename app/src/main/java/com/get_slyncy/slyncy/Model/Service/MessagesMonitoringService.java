package com.get_slyncy.slyncy.Model.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.get_slyncy.slyncy.BuildConfig;
import com.get_slyncy.slyncy.Model.Util.ContactUtility;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class MessagesMonitoringService extends Service {

    private static final String TAG = "MessagesMonService";

    private static Uri uriSMS = Uri.parse("content://mms-sms/conversations/");
    // private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    // private static final Uri SMS_INBOX_CONTENT_URI =
    // Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

    private ContentResolver crMsg;
    private MsgContentObserver observerMsg = null;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        if (BuildConfig.DEBUG) Log.v(TAG, "SmsMonitorService created");
        registerMsgsObserver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterMsgsObserver();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * Registers the observerMsg for SMS changes
     */
    private void registerMsgsObserver() {
        if (observerMsg == null) {
            observerMsg = new MsgContentObserver(new Handler());
            crMsg = getContentResolver();
            crMsg.registerContentObserver(uriSMS, true, observerMsg);
            if (BuildConfig.DEBUG) Log.v(TAG, "SMS Observer registered.");
        }
    }

    /**
     * Unregisters the observerMsg for call log changes
     */
    private void unregisterMsgsObserver() {
        if (crMsg != null) {
            crMsg.unregisterContentObserver(observerMsg);
        }
        if (observerMsg != null) {
            observerMsg = null;
        }
        if (BuildConfig.DEBUG) Log.v(TAG,"Unregistered SMS Observer");
    }

    private class MsgContentObserver extends ContentObserver {
        public MsgContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Cursor c = context.getContentResolver().query(SMS_CONTENT_URI,
            // null, "read = 0", null, null);
            int count = ContactUtility.getUnreadMessagesCount(context);
            if (BuildConfig.DEBUG) Log.v(TAG, "getUnreadCount = " + count);
            if (count == 0) {
//                ManageNotification.clearAll(context);
                finishStartingService(MessagesMonitoringService.this);
            } else {
                // TODO: do something with count>0, maybe refresh the
                // notification
            }
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
