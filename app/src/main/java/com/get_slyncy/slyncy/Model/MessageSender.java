package com.get_slyncy.slyncy.Model;

import android.content.Context;

import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Settings;


/**
 * Created by tylerbowers on 1/27/18.
 */

public class MessageSender {

    public static void sendSMSMessage(final CellMessage message, final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(Data.getInstance().getSettings().getMmsc());
                sendSettings.setProxy(Data.getInstance().getSettings().getMmsProxy());
                sendSettings.setPort(Data.getInstance().getSettings().getMmsPort());
                sendSettings.setUseSystemSending(true);

                Transaction transaction = new Transaction(context, sendSettings);
                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            }
        }).start();
    }

    // Later to be used for sending files, text, etc to server
//    public static void sendSlyncyMessage(IMessage message) {
//
//    }

    public static void sendMMSMessage(CellMessage message, Context context)
    {
        MmsSender.sendMms(message, context);
    }
}
