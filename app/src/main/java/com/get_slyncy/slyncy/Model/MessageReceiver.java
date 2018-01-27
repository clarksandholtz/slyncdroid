package com.get_slyncy.slyncy.Model;

import android.util.Log;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class MessageReceiver {

    private MessageReceiver(){}

    void addNewMessage(IMessage message) {
        Log.d("MessageReceiver", "message received from " + message.getSender());
        if (message.getClass() == CellMessage.class) {
            Data.getInstance().getCellMessages().addMessage(message.getSender(), message);
        }
//        else if(message.getClass() == SlyncyMessage.class) {
//
//        }
    }

    // ***************** Singleton business ********************* //

    private static volatile MessageReceiver instance = null;
    public static MessageReceiver getInstance() {
        if (instance == null) instance = new MessageReceiver();
        return instance;
    }
}
