package com.get_slyncy.slyncy.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.get_slyncy.slyncy.Model.Service.SlyncyService;
import com.get_slyncy.slyncy.Model.Util.Data;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.View.Test.SmsRadar;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase
        FirebaseApp.initializeApp(getApplicationContext());

        // Check if permissions are needed
        if (PermissionActivity.needPermissionRequest(this))
        {
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
                    , "Basic Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Start the background service
//        Intent serviceIntent = new Intent(this, SlyncyService.class);
//        startService(serviceIntent);
//        Intent intent = new Intent(this, LoginActivity.class);
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

    public void applyIpChange(View view)
    {
        EditText ipField = findViewById(R.id.ip_field);
        String ip = ipField.getText().toString();
        getSharedPreferences("IP", MODE_PRIVATE).edit().putString("ip", ip).commit();
    }
}

