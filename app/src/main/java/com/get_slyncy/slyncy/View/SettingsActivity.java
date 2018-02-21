package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/regular.ttf");
        TextView accountName = findViewById(R.id.account_name);
        TextView accountEmail = findViewById(R.id.account_email);
        TextView accountPhone = findViewById(R.id.account_phone);
        accountName.setTypeface(typeface);
        accountEmail.setTypeface(typeface);
        accountPhone.setTypeface(typeface);
        accountName.setTextColor(getColor(R.color.colorFontLight));
        accountEmail.setTextColor(getColor(R.color.colorFontLight));
        accountPhone.setTextColor(getColor(R.color.colorFontLight));

        TextView syncEntry = findViewById(R.id.sync_entry_title);
        TextView groupMessageEntry = findViewById(R.id.group_message_entry);
        TextView notifEntry = findViewById(R.id.notification_entry);
        TextView logoutEntry = findViewById(R.id.logout_entry);
        syncEntry.setTypeface(typeface);
        groupMessageEntry.setTypeface(typeface);
        notifEntry.setTypeface(typeface);
        logoutEntry.setTypeface(typeface);
        syncEntry.setTextColor(getColor(R.color.colorFontDark));
        groupMessageEntry.setTextColor(getColor(R.color.colorFontDark));
        notifEntry.setTextColor(getColor(R.color.colorFontDark));
        logoutEntry.setTextColor(getColor(R.color.colorFontDark));



        ImageView accountProfile = findViewById(R.id.account_profile);
        ImageView syncIcon = findViewById(R.id.sync_icon);
        ImageView groupIcon = findViewById(R.id.group_message_icon);
        ImageView notifIcon = findViewById(R.id.notification_icon);
        ImageView logoutIcon = findViewById(R.id.logout_icon);
        ImageView notifArrow = findViewById(R.id.notification_arrow);
        accountProfile.setImageDrawable(getDrawable(R.drawable.ic_account_circle_black_24dp));
        syncIcon.setImageDrawable(getDrawable(R.drawable.ic_sync_black_24dp));
        groupIcon.setImageDrawable(getDrawable(R.drawable.ic_group_black_24dp));
        notifIcon.setImageDrawable(getDrawable(R.drawable.ic_notifications_black_24dp));
        logoutIcon.setImageDrawable(getDrawable(R.drawable.ic_logout_black_24dp));
        notifArrow.setImageDrawable(getDrawable(R.drawable.ic_navigate_next_black_24dp));
    }

    public void launchNotifFilter(View view)
    {
        Intent intent = new Intent(this, NotificationFilterActivity.class);
        startActivity(intent);
    }
}
