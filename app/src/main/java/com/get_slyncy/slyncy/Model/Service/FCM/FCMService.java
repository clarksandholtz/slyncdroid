package com.get_slyncy.slyncy.Model.Service.FCM;


import android.app.NotificationManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by nsshurtz on 4/16/18.
 */

public class FCMService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
//        NotificationManager nm = getSystemService(NotificationManager.class);
//        if (remoteMessage.getNotification() != null)
//        {
//            RemoteMessage.Notification notification = remoteMessage.getNotification();
//
//            nm.notify();
//        }
    }
}
