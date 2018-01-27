/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.get_slyncy.slyncy.Model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;

import com.get_slyncy.slyncy.R;

/**
 * Needed to make default sms app for testing
 */
public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");
        String sender = "";
        String body = "";

        for (int i = 0; i < smsExtra.length; ++i) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            body += sms.getMessageBody();
            if (sms.getDisplayOriginatingAddress() != null) {
                sender = sms.getDisplayOriginatingAddress();
            }
            else {
                sender = sms.getEmailFrom();
            }
        }

        // TODO: Fix incoming notifications
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification notification = new Notification.Builder(context)
                .setContentText(body)
                .setContentTitle(sender)
                .setSmallIcon(R.drawable.ic_alert)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(resultPendingIntent)
                .build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, notification);

        CellMessage message = CellMessage.newIncomingMessage(body, sender);
        MessageReceiver.getInstance().addNewMessage(message);
    }
}
