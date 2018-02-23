package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.get_slyncy.slyncy.Model.Util.SettingsDb;
import com.get_slyncy.slyncy.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;


/**
 * Created by nsshurtz on 2/15/18.
 */

public class SettingsActivity extends Activity
{
    private TextView accountName;
    private TextView accountEmail;
    private TextView accountPhone;
    private TextView syncEntry;
    private TextView groupMessageEntry;
    private TextView notifEntry;
    private TextView logoutEntry;

    private ImageView accountProfile;
    private ImageView syncIcon;
    private ImageView groupIcon;
    private ImageView notifIcon;
    private ImageView logoutIcon;
    private ImageView notifArrow;
    private Switch groupMessageSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Bundle bundle = getIntent().getExtras();
        accountName = findViewById(R.id.account_name);
        accountEmail = findViewById(R.id.account_email);
        accountPhone = findViewById(R.id.account_phone);
        accountProfile = findViewById(R.id.account_profile);
        syncIcon = findViewById(R.id.sync_icon);
        groupIcon = findViewById(R.id.group_message_icon);
        notifIcon = findViewById(R.id.notification_icon);
        logoutIcon = findViewById(R.id.logout_icon);
        notifArrow = findViewById(R.id.notification_arrow);
        syncEntry = findViewById(R.id.sync_entry_title);
        groupMessageEntry = findViewById(R.id.group_message_entry);
        notifEntry = findViewById(R.id.notification_entry);
        logoutEntry = findViewById(R.id.logout_entry);
        groupMessageSwitch = findViewById(R.id.group_messages_switch);


        if (bundle != null)
        {
            accountName.setText(bundle.getString("name"));
            accountEmail.setText(bundle.getString("email"));
            accountPhone.setText(bundle.getString("phone") == null ? "" : bundle.getString("phone"));
        }

        accountProfile.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_24dp));
        syncIcon.setImageDrawable(getDrawable(R.drawable.ic_sync_black_24dp));
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_group_black_24dp));
        notifIcon.setImageDrawable(getDrawable(R.drawable.ic_notifications_black_24dp));
        logoutIcon.setImageDrawable(getDrawable(R.drawable.ic_logout_black_24dp));
        notifArrow.setImageDrawable(getDrawable(R.drawable.ic_navigate_next_black_24dp));

        syncIcon.setColorFilter(getColor(R.color.colorFontDark));
        groupIcon.setColorFilter(getColor(R.color.colorFontDark));
        logoutIcon.setColorFilter(getColor(R.color.colorFontDark));
        notifArrow.setColorFilter(getColor(R.color.colorFontDark));
        notifIcon.setColorFilter(getColor(R.color.colorFontDark));

        File file = new File(getCacheDir() + "/profilePic.jpg");
        if (file.exists())
        {
            accountProfile.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        }
        else
        {
            accountProfile.setColorFilter(getColor(R.color.white));
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

    private static  class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        final ThreadLocal<ImageView> bmImage = new ThreadLocal<>();

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage.set(bmImage);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.get().setImageBitmap(result);
        }
    }

    public void signOut(View v) {
        // Firebase sign out

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();

        File file = new File(getCacheDir() + "/profilePic.jpg");
        if (file.exists())
        {
            file.delete();
        }

        // Google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }
}
