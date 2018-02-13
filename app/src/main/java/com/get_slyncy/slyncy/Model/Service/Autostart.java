package com.get_slyncy.slyncy.Model.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class Autostart extends BroadcastReceiver {

    private static final String TAG = "Autostart";

    @Override
    public void onReceive(Context context, Intent arg1) {
        Intent intent = new Intent(context, SlyncyService.class);
        context.startService(intent);
        Log.i(TAG, "Started");

        // TODO: @Tyler: Hookup the MessagesMonitoringService
    }
}
