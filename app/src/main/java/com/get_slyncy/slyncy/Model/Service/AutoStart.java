package com.get_slyncy.slyncy.Model.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadarService;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;

//import com.get_slyncy.slyncy.View.Test.SmsMmsRadar;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class AutoStart extends BroadcastReceiver
{

    private static final String TAG = "Slyncy AutoStart";

    @Override
    public void onReceive(Context context, Intent arg1)
    {
        Intent intent = new Intent(context, SmsMmsRadarService.class);

        if (context.getSharedPreferences("authorization", Context.MODE_PRIVATE).contains("token"))
        {
            ClientCommunicator.setAuthToken(context);
            Log.i(TAG, "Started");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent);
            else
                context.startService(intent);

        }
    }
}
