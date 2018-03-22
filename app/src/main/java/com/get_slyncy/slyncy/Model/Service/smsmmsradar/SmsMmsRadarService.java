/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.get_slyncy.slyncy.Model.Service.smsmmsradar;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;

import com.apollographql.apollo.ApolloClient;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.View.LoginActivity;
import com.get_slyncy.slyncy.View.PersistentNotifActivity;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.MmsCursorParser;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.MmsObserver;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.MmsStorage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.SharedPreferencesMmsStorage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.SharedPreferencesSmsStorage;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.SmsCursorParser;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.SmsObserver;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.SmsStorage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Service created to handle the SmsContentObserver registration. This service has the responsibility of register and
 * unregister the content observer in sms content provider when it's created and destroyed.
 * <p/>
 * The SmsContentObserver will be registered over the CONTENT_SMS_URI to be notified each time the system update the
 * sms content provider.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class SmsMmsRadarService extends Service
{

    private static final String CONTENT_SMS_URI = "content://sms";
    private static final String CONTENT_MMS_URI = "content://mms";
    private static final int ONE_SECOND = 1000;


    private ContentResolver contentResolver;
    private SmsObserver smsObserver;
    private MmsObserver mmsObserver;
    private AlarmManager alarmManager;
    private TimeProvider timeProvider;
    private boolean initialized;


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (!initialized)
        {
            initializeService();
        }
                Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(getPackageName() + "_persistent", "Persistent",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.getNotificationChannels().contains(channel))
            {
                notificationManager.createNotificationChannel(channel);
            }
            builder = new Notification.Builder(this, getPackageName() + "_persistent");

        }
        else
        {
            builder = new Notification.Builder(this);
        }
        Intent notifIntent = new Intent(this, PersistentNotifActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Icon icon = Icon.createWithResource(this, R.drawable.ic_stat_name);
        icon.setTint(getColor(R.color.colorPrimary));
        startForeground(getPackageName().hashCode(), builder.setSmallIcon(icon).setColor(getColor(R.color.colorPrimary))
                .setContentText("Slyncy ForegroundService").setContentTitle("SLYNCY FOREGROUND SERVICE").build());
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        finishService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        restartService();
    }

    private void initializeService()
    {
        initialized = true;
        initializeDependencies();
        registerSmsContentObserver();
        registerMmsContentObserver();
        ClientCommunicator.subscribeToNewMessages();
    }

    private void initializeDependencies()
    {
        if (!areDependenciesInitialized())
        {
            initializeContentResolver();
            initializeSmsObserver();
            initializeMmsObserver();
        }
    }

    private boolean areDependenciesInitialized()
    {
        return contentResolver != null && smsObserver != null;
    }

    private void initializeSmsObserver()
    {
        Handler handler = new Handler();
        SmsCursorParser smsCursorParser = initializeSmsCursorParser();
        this.smsObserver = new SmsObserver(contentResolver, handler, smsCursorParser);

    }

    private void initializeMmsObserver()
    {
        Handler handler = new Handler();
        MmsCursorParser mmsCursorParser = initializeMmsCursorParser();
        this.mmsObserver = new MmsObserver(contentResolver, handler, mmsCursorParser);
    }

    private SmsCursorParser initializeSmsCursorParser()
    {
        SharedPreferences preferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);
        SmsStorage smsStorage = new SharedPreferencesSmsStorage(preferences);
        return new SmsCursorParser(smsStorage, getTimeProvider());
    }

    private MmsCursorParser initializeMmsCursorParser()
    {
        SharedPreferences preferences = getSharedPreferences("mms_preferences", MODE_PRIVATE);
        MmsStorage mmsStorage = new SharedPreferencesMmsStorage(preferences);
        return new MmsCursorParser(mmsStorage, getTimeProvider());
    }

    private void initializeContentResolver()
    {
        this.contentResolver = getContentResolver();
    }

    private void finishService()
    {
        initialized = false;
        unregisterSmsContentObserver();
    }

    private void registerMmsContentObserver()
    {
        boolean notifyForDescendants = true;
        contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_URI, notifyForDescendants, mmsObserver);
    }

    private void registerSmsContentObserver()
    {
        Uri smsUri = Uri.parse(CONTENT_SMS_URI);
        boolean notifyForDescendants = true;
        contentResolver.registerContentObserver(smsUri, notifyForDescendants, smsObserver);
    }

    private void unregisterSmsContentObserver()
    {
        contentResolver.unregisterContentObserver(smsObserver);
    }

    private void restartService()
    {
        Intent intent = new Intent(this, SmsMmsRadarService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        long now = getTimeProvider().getDate().getTime();
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, now + ONE_SECOND, pendingIntent);
    }

    private TimeProvider getTimeProvider()
    {
        return timeProvider != null ? timeProvider : new TimeProvider();
    }

    private AlarmManager getAlarmManager()
    {
        return alarmManager != null ? alarmManager : (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    
    
    /*
     * Test methods. This methods has been created to modify the service dependencies in test runtime because
     * without dependency injection we can't provide this entities.
     */

    void setSmsObserver(SmsObserver smsObserver)
    {
        this.smsObserver = smsObserver;
    }

    void setContentResolver(ContentResolver contentResolver)
    {
        this.contentResolver = contentResolver;
    }

    void setAlarmManager(AlarmManager alarmManager)
    {
        this.alarmManager = alarmManager;
    }

    void setTimeProvider(TimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
    }
}
