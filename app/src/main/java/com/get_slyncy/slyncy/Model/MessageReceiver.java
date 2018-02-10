package com.get_slyncy.slyncy.Model;

import android.util.Log;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class MessageReceiver {

    private MessageReceiver(){}

    // ***************** Singleton business ********************* //

    private static volatile MessageReceiver instance = null;
    public static MessageReceiver getInstance() {
        if (instance == null) instance = new MessageReceiver();
        return instance;
    }
}
