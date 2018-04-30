package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.get_slyncy.slyncy.Model.Service.Notification.NotificationListener;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadarService;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.Model.Util.DownloadImageTask;
import com.get_slyncy.slyncy.Model.Util.SettingsDb;
import com.get_slyncy.slyncy.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;


/**
 * Created by nsshurtz on 2/15/18.
 */

public class SettingsActivity extends Activity implements DownloadImageTask.PostExecCallBack
{
    private String picUrl;

    private ImageView accountProfile;
    private Switch groupMessageSwitch;
    private static boolean synced = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Bundle bundle = getIntent().getExtras();
        TextView accountName;
        TextView accountEmail;
        TextView accountPhone;
//        TextView syncEntry;
//        TextView groupMessageEntry;
//        TextView notifEntry;
//        TextView logoutEntry;
        accountName = findViewById(R.id.account_name);
        accountEmail = findViewById(R.id.account_email);
        accountPhone = findViewById(R.id.account_phone);
        accountProfile = findViewById(R.id.account_profile);
        ImageView syncIcon = findViewById(R.id.sync_icon);
        ImageView groupIcon = findViewById(R.id.group_message_icon);
        final ImageView notificationIcon = findViewById(R.id.notification_icon);
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        ImageView notificationArrow = findViewById(R.id.notification_arrow);
//        syncEntry = findViewById(R.id.sync_entry_title);
//        groupMessageEntry = findViewById(R.id.group_message_entry);
//        notifEntry = findViewById(R.id.notification_entry);
//        logoutEntry = findViewById(R.id.logout_entry);
        groupMessageSwitch = findViewById(R.id.group_messages_switch);


        if (bundle != null)
        {
            accountName.setText(bundle.getString("name", ""));
            accountEmail.setText(bundle.getString("email", ""));
            accountPhone.setText(bundle.getString("phone", ""));
            picUrl = bundle.getString("pic");

//                    ClientCommunicator.bulkMessageUpload();

        }
        final NotificationManager notificationManager = getSystemService(NotificationManager.class);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (notificationManager != null)
                {
                    notificationManager.cancel(60);
                    Notification.Builder builder;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    {
                        builder = new Notification.Builder(getApplicationContext(), getPackageName() + "_slyncing");
                    }
                    else
                    {
                        builder = new Notification.Builder(getApplicationContext());
                    }
                    builder.setOngoing(false).setVibrate(new long[]{0L, 250L, 250L, 250L}).setSmallIcon(Icon.createWithResource(getApplicationContext(), R.drawable.notification_logo)).setColor(getColor(R.color.colorPrimary));
                    if (!intent.getBooleanExtra("successful", false))
                    {
                        builder.setContentTitle("Error").setContentText(intent.getStringExtra("reason"));
                    }
                    else
                    {
                        builder.setContentTitle("slynced").setContentText("slyncing complete");

                    }
                    notificationManager.notify(60, builder.build());
                }

            }
        }, new IntentFilter("slyncing_complete"));

        if (getSharedPreferences("authorization", MODE_PRIVATE).contains("synced"))
        {
            if (!getSharedPreferences("authorization", MODE_PRIVATE).getBoolean("synced", true))
            {
                ClientCommunicator.deleteMessages(this);
                getSharedPreferences("authorization", MODE_PRIVATE).edit().putBoolean("synced", true).commit();
                Notification.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null)
                {
                    NotificationChannel channel = new NotificationChannel(getPackageName() + "_slyncing", "slyncing Progress", NotificationManager.IMPORTANCE_DEFAULT);
                    channel.enableLights(true);
                    channel.setLightColor(getColor(R.color.colorPrimary));
                    channel.setShowBadge(true);
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                    builder = new Notification.Builder(this, getPackageName() + "_slyncing");
                }
                else
                {
                    builder = new Notification.Builder(this);
                }
                builder.setContentTitle("slyncing...").setContentText("slyncy is currently slyncing.").setOngoing(true).setSmallIcon(Icon.createWithResource(this, R.drawable.notification_logo)).setColor(getColor(R.color.colorPrimary));
                final Notification notification = builder.build();
                if (notificationManager != null) notificationManager.notify(60, notification);
            }
        }

//        accountProfile.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_24dp));
        syncIcon.setImageDrawable(getDrawable(R.drawable.ic_sync_black_24dp));
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_group_black_24dp));
        notificationIcon.setImageDrawable(getDrawable(R.drawable.ic_notifications_black_24dp));
        logoutIcon.setImageDrawable(getDrawable(R.drawable.ic_logout_black_24dp));
        notificationArrow.setImageDrawable(getDrawable(R.drawable.ic_navigate_next_black_24dp));

        syncIcon.setColorFilter(getColor(R.color.colorFontDark));
        groupIcon.setColorFilter(getColor(R.color.colorFontDark));
        logoutIcon.setColorFilter(getColor(R.color.colorFontDark));
        notificationArrow.setColorFilter(getColor(R.color.colorFontDark));
        notificationIcon.setColorFilter(getColor(R.color.colorFontDark));

        File file = new File(getCacheDir() + "/profilePic.jpg");
        if (file.exists())
        {
            accountProfile.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        }
        else if (picUrl != null)
        {
            DownloadImageTask task = new DownloadImageTask(getCacheDir().getPath(), this);
            task.execute(picUrl);
        }
        boolean groupMMSEnabled = SettingsDb.getGroupMessageSettings(getApplicationContext());
        groupMessageSwitch.setChecked(groupMMSEnabled);
        groupMessageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SettingsDb.setGroupMessageSettings(getApplicationContext(), isChecked);
            }
        });
        NotificationListener.start(getBaseContext());
        Intent intent = new Intent(this, SmsMmsRadarService.class);
        startService(intent);
    }

    public void launchNotifFilter(View view)
    {
        Intent intent = new Intent(this, NotificationFilterActivity.class);
        startActivity(intent);
    }

    public void toggleSetting(View view)
    {
        groupMessageSwitch.setChecked(!groupMessageSwitch.isChecked());
    }

    public void callBack()
    {
        File file = new File(getCacheDir() + "/profilePic.jpg");
        if (file.exists())
        {
            accountProfile.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        }
        else
        {
            accountProfile.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_24dp));
            accountProfile.setColorFilter(getColor(R.color.white));
        }
    }

    public void signOut(View v)
    {
        // Firebase sign out

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();

        SharedPreferences sharedPreferences = getSharedPreferences("authorization", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("email");
        editor.remove("phone");
        editor.remove("name");
        editor.remove("pic");
        editor.commit();
        SmsMmsRadar.stopSmsMmsRadarService(this);

        File file = new File(getCacheDir() + "/profilePic.jpg");
        if (file.exists())
        {
            file.delete();
        }
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    public void resync(View view)
    {
        final NotificationManager notificationManager = getSystemService(NotificationManager.class);



        AlertDialog alertDialog = new AlertDialog.Builder(this).setIcon(getDrawable(R.drawable.ic_warn)).setTitle("Warning!").setMessage("This will erase all messages on the Slyncy servers.\nIt is irreversible").setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        }).setPositiveButton("Proceed", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ClientCommunicator.deleteMessages(getApplicationContext());
                Notification.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null)
                {
                    NotificationChannel channel = new NotificationChannel(getPackageName() + "_slyncing", "slyncing Progress", NotificationManager.IMPORTANCE_DEFAULT);
                    channel.enableLights(true);
                    channel.setLightColor(getColor(R.color.colorPrimary));
                    channel.setShowBadge(true);
                    channel.enableVibration(true);
                    notificationManager.createNotificationChannel(channel);
                    builder = new Notification.Builder(SettingsActivity.this, getPackageName() + "_slyncing");
                }
                else
                {
                    builder = new Notification.Builder(SettingsActivity.this);
                }
                builder.setContentTitle("slyncing...").setContentText("slyncy is currently slyncing.").setOngoing(true).setSmallIcon(Icon.createWithResource(SettingsActivity.this, R.drawable.notification_logo)).setColor(getColor(R.color.colorPrimary));
                final Notification notification = builder.build();
                if (notificationManager != null) notificationManager.notify(60, notification);
            }
        }).create();
        Drawable icon = getDrawable(R.drawable.ic_warn);
//        icon.setColorFilter(getColor(R.color.colorPrimaryDark), PorterDuff.Mode.OVERLAY);
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorFontDark));
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorFontDark));
        alertDialog.setIcon(icon);
    }
}
