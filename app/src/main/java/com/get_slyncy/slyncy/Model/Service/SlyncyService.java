package com.get_slyncy.slyncy.Model.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.Util.RecipientIdCache;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class SlyncyService extends Service {

    private static final String TAG = "SlyncyService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Toast.makeText(this, "SlyncyService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startid) {
//        Intent intents = new Intent(getBaseContext(),hello.class);
//        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intents);
//        initializeSlyncy();
        Toast.makeText(this, "SlyncyService Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
    }

    private void initializeSlyncy() {
        RecipientIdCache.init(this);
        MessageDbUtility.queryAllThreads(this);
    }
}
