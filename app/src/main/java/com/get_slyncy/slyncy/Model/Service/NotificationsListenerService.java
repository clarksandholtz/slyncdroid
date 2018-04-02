package com.get_slyncy.slyncy.Model.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by tylerbowers on 4/2/18.
 */

public class NotificationsListenerService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver notificationReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        notificationReceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
        registerReceiver(notificationReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new  Intent("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new Intent("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                NotificationsListenerService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NotificationsListenerService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.get_slyncy.slycny.Service.NOTIFICATIONS_LISTENER");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }
}
