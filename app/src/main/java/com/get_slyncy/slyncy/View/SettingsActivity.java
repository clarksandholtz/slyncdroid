package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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
        ImageView notificationIcon = findViewById(R.id.notification_icon);
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
            if (bundle.containsKey("sync"))
            {
                if (bundle.getBoolean("sync"))
                {
                    ClientCommunicator.bulkMessageUpload();
                }
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
        boolean groupMMSEnabled = SettingsDb.initGroupMessageSettings(getFilesDir());
        groupMessageSwitch.setChecked(groupMMSEnabled);
        groupMessageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SettingsDb.setGroupMessageSettings(getFilesDir(), isChecked);
            }
        });

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>()
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
}
