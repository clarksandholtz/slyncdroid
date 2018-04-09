package com.get_slyncy.slyncy.Model.CellMessaging;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class MessageReceiver
{

    private static volatile MessageReceiver instance = null;

    // ***************** Singleton business ********************* //

    private MessageReceiver()
    {
    }

    public static MessageReceiver getInstance()
    {
        if (instance == null) instance = new MessageReceiver();
        return instance;
    }
}
