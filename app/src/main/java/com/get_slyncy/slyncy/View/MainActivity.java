package com.get_slyncy.slyncy.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.get_slyncy.slyncy.Model.Service.Autostart;
import com.get_slyncy.slyncy.Model.Service.SlyncyService;
import com.get_slyncy.slyncy.Model.Util.Data;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.View.Test.SmsRadar;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase
        FirebaseApp.initializeApp(getApplicationContext());

        // Check if permissions are needed
        if (PermissionActivity.needPermissionRequest(this)) {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        // Init settings
        Data.getInstance().updateCellSettings(this);

        // Setup notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(getPackageName() + "notif"
                    ,"Basic Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // Start the background service
        Intent serviceIntent = new Intent(this, SlyncyService.class);
        startService(serviceIntent);
    }

    public void launchSettings(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void launchLogin(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void launchSmsRadar(View view)
    {
        Intent intent = new Intent(this, SmsRadar.class);
        startActivity(intent);
    }
}

