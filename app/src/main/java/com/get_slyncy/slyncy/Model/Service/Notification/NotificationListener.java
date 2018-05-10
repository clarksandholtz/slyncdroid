package com.get_slyncy.slyncy.Model.Service.Notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.Model.Util.SettingsDb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import apollographql.apollo.SendNotificationMutation;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationListener implements NotificationReceiver.NotificationListener, NotificationReceiver.InstanceCallback
{

    private SettingsDb settings;
    private Context context;

    private NotificationListener(Context context)
    {
        this.settings = new SettingsDb(context);
        this.context = context;
    }

    public static void start(Context context)
    {
        NotificationReceiver.RunCommand(context, new NotificationListener(context));
    }


    @Override
    public void onServiceStart(NotificationReceiver service)
    {
        service.addListener(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification statusBarNotification)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                Notification notification = statusBarNotification.getNotification();
                if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) != 0 || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0 || (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0)
                {
                    //This is not a notification we want!
                    return;
                }
                settings.open();
                if (!settings.isEnabled(statusBarNotification.getPackageName()))
                {
                    settings.close();
                    return;
                }
                settings.close();

                String key = getNotificationKeyCompat(statusBarNotification);
                String packageName = statusBarNotification.getPackageName();
                String appName = appNameLookup(context, packageName);

                if ("com.facebook.orca".equals(packageName) && (statusBarNotification.getId() == 10012) && "Messenger".equals(appName) && notification.tickerText == null)
                {
                    //HACK: Hide weird Facebook empty "Messenger" notification that is actually not shown in the phone
                    return;
                }

                if ("com.android.systemui".equals(packageName) && "low_battery".equals(statusBarNotification.getTag()))
                {
                    //HACK: Android low battery notification are posted again every few seconds. Ignore them, as we already have a battery indicator.
                    return;
                }

//        NetworkPackage np = new NetworkPackage(PACKAGE_TYPE_NOTIFICATION);

//                if (packageName.equals(context.getPackageName()))
//                {
//                    //Make our own notifications silent :)
//                    //do we need to notify for our own notifications?
//                    return;
//                }

                try
                {
                    Bitmap appIcon = notification.getSmallIcon() == null ? null : drawableToBitmap(notification.getSmallIcon().loadDrawable(context));

                    if (appIcon != null)
                    {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        if (appIcon.getWidth() > 128)
                        {
                            appIcon = Bitmap.createScaledBitmap(appIcon, 96, 96, true);
                        }
                        appIcon.compress(Bitmap.CompressFormat.PNG, 90, outStream);
                        byte[] bitmapData = outStream.toByteArray();

//                np.setPayload(bitmapData);
//
//                np.set("payloadHash", getChecksum(bitmapData));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("NotificationsPlugin", "Error retrieving icon");
                }

//        RepliableNotification rn = extractRepliableNotification(statusBarNotification);
//        if (rn.pendingIntent != null)
//        {
//            np.set("requestReplyId", rn.id);
//            pendingIntents.put(rn.id, rn);
//        }
//
//        np.set("id", key);
//        np.set("appName", appName == null ? packageName : appName);
//        np.set("ticker", getTickerText(notification));
//        np.set("title", getNotificationTitle(notification));
//        np.set("text", getNotificationText(notification));
//        np.set("time", Long.toString(statusBarNotification.getPostTime()));

                Log.d("Notification id", key);
                Log.d("Notification appName", appName == null ? packageName : appName);
                Log.d("Notification ticker", getTickerText(notification));
                Log.d("Notification title", getNotificationTitle(notification));
                Log.d("Notification text", getNotificationText(notification));
                Log.d("Notification time", Long.toString(statusBarNotification.getPostTime()));
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
                String date = df.format(new Date(statusBarNotification.getPostTime()));
                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
                {
                    @Override
                    public Response intercept(Chain chain) throws IOException
                    {
                        Request orig = chain.request();
                        Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                        builder.header("Authorization", "Bearer " + ClientCommunicator.getAuthToken()).addHeader("Cache-Control", "no-cache");
                        return chain.proceed(builder.build());
                    }
                }).writeTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).connectTimeout(2, TimeUnit.SECONDS).build();
                ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(SettingsDb.getServerIP(context)).build();
                client.mutate(new SendNotificationMutation(key, appName, getTickerText(notification), getNotificationTitle(notification), getNotificationText(notification), date, false)).enqueue(null);
            }
        });

        thread.start();

//        device.sendPackage(np);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification)
    {

    }

    @Override
    public void onListenerConnected(NotificationReceiver service)
    {

    }

    private static String getNotificationKeyCompat(StatusBarNotification statusBarNotification)
    {
        String result;
        // first check if it's one of our remoteIds
        String tag = statusBarNotification.getTag();
        if (tag != null && tag.startsWith("kdeconnectId:")) result = Integer.toString(statusBarNotification.getId());
        else
        {
            result = statusBarNotification.getKey();
        }

        return result;
    }

    public static String appNameLookup(Context context, String packageName)
    {

        try
        {

            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);

            return pm.getApplicationLabel(ai).toString();

        }
        catch (final PackageManager.NameNotFoundException e)
        {

            e.printStackTrace();
            Log.e("AppsHelper", "Could not resolve name " + packageName);

            return null;

        }

    }

    private String getNotificationTitle(Notification notification)
    {
        final String TITLE_KEY = "android.title";
        final String TEXT_KEY = "android.text";
        String title = "";

        if (notification != null)
        {
            try
            {
                Bundle extras = notification.extras;
                title = extras.getString(TITLE_KEY);
            }
            catch (Exception e)
            {
                Log.w("NotificationPlugin", "problem parsing notification extras for " + notification.tickerText);
                e.printStackTrace();
            }
        }

        return title;
    }

    private String getNotificationText(Notification notification)
    {
        final String TEXT_KEY = "android.text";
        String text = "";

        if (notification != null)
        {
            try
            {
                Bundle extras = notification.extras;
                Object extraTextExtra = extras.get(TEXT_KEY);
                if (extraTextExtra != null) text = extraTextExtra.toString();
            }
            catch (Exception e)
            {
                Log.w("NotificationPlugin", "problem parsing notification extras for " + notification.tickerText);
                e.printStackTrace();
            }
        }
        return text;
    }

    /**
     * Returns the ticker text of the notification.
     * If device android version is KitKat or newer, the title and text of the notification is used
     * instead the ticker text.
     */
    private String getTickerText(Notification notification)
    {
        final String TITLE_KEY = "android.title";
        final String TEXT_KEY = "android.text";
        String ticker = "";

        if (notification != null)
        {
            try
            {
                Bundle extras = notification.extras;
                String extraTitle = extras.getString(TITLE_KEY);
                String extraText = null;
                Object extraTextExtra = extras.get(TEXT_KEY);
                if (extraTextExtra != null) extraText = extraTextExtra.toString();

                if (extraTitle != null && extraText != null && !extraText.isEmpty())
                {
                    ticker = extraTitle + ": " + extraText;
                }
                else if (extraTitle != null)
                {
                    ticker = extraTitle;
                }
                else if (extraText != null)
                {
                    ticker = extraText;
                }
            }
            catch (Exception e)
            {
                Log.w("NotificationPlugin", "problem parsing notification extras for " + notification.tickerText);
                e.printStackTrace();
            }

            if (ticker.isEmpty())
            {
                ticker = (notification.tickerText != null) ? notification.tickerText.toString() : "";
            }
        }

        return ticker;
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null)
            {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
        {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        }
        else
        {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
