package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.get_slyncy.slyncy.R;

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        accountName = findViewById(R.id.account_name);
        accountEmail = findViewById(R.id.account_email);
        accountPhone = findViewById(R.id.account_phone);


        syncEntry = findViewById(R.id.sync_entry_title);
        groupMessageEntry = findViewById(R.id.group_message_entry);
        notifEntry = findViewById(R.id.notification_entry);
        logoutEntry = findViewById(R.id.logout_entry);

        accountProfile = findViewById(R.id.account_profile);
        syncIcon = findViewById(R.id.sync_icon);
        groupIcon = findViewById(R.id.group_message_icon);
        notifIcon = findViewById(R.id.notification_icon);
        logoutIcon = findViewById(R.id.logout_icon);
        notifArrow = findViewById(R.id.notification_arrow);
        accountProfile.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_24dp));
        syncIcon.setImageDrawable(getDrawable(R.drawable.ic_sync_black_24dp));
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_group_black_24dp));
        notifIcon.setImageDrawable(getDrawable(R.drawable.ic_notifications_black_24dp));
        logoutIcon.setImageDrawable(getDrawable(R.drawable.ic_logout_black_24dp));
        notifArrow.setImageDrawable(getDrawable(R.drawable.ic_navigate_next_black_24dp));
        accountProfile.setColorFilter(getColor(R.color.white));
        syncIcon.setColorFilter(getColor(R.color.colorFontDark));
        groupIcon.setColorFilter(getColor(R.color.colorFontDark));
        logoutIcon.setColorFilter(getColor(R.color.colorFontDark));
        notifArrow.setColorFilter(getColor(R.color.colorFontDark));
        notifIcon.setColorFilter(getColor(R.color.colorFontDark));
    }

    public void launchNotifFilter(View view)
    {
        Intent intent = new Intent(this, NotificationFilterActivity.class);
        startActivity(intent);
    }
}
