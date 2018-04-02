package com.get_slyncy.slyncy.Model.Util;

import android.content.Context;

/**
 * Created by tylerbowers on 4/2/18.
 */

public class NotificationsUtility {

    private static NotificationsUtility instance;

    private Context mContext;

    private NotificationsUtility(){}

    private static NotificationsUtility getInstance() {
        if (instance == null) {
            instance = new NotificationsUtility();
        }

        return instance;
    }

    public static void init(Context context) {
        instance = getInstance();

        instance.mContext = context;

        new Thread(new Runnable() {
            public void run() {
                // do things
            }
        }, "NotificationsUtility.init").start();
    }

    // notification watcher

    // thread (or service?) that monitors notifications

    // send to server upon notify

    // update server upon notification clear
}
