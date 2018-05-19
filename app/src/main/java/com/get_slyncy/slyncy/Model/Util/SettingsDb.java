package com.get_slyncy.slyncy.Model.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Telephony;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * Created by nsshurtz on 2/15/18.
 */

public class SettingsDb
{
    static final private HashSet<String> disabledByDefault = new HashSet<>();
    private static final String KEY_PACKAGE_NAME = "com_get_slyncy_slyncy";
    private static final String KEY_IS_ENABLED = "isEnabled";
    private static final String DATABASE_NAME = "Applications";
    private static final String DATABASE_TABLE = "Applications";
    private static final int DATABASE_VERSION = 2;

    static
    {
        disabledByDefault.add("com.google.android.apps.messaging"); //We already have sms notifications in the telephony plugin
        disabledByDefault.add("com.google.android.googlequicksearchbox"); //Google Now notifications re-spawn every few minutes
    }

    private final Context ourContext;
    private SQLiteDatabase ourDatabase;
    private DbHelper ourHelper;

    private static int count = 0;


    public SettingsDb(Context c)
    {
        ourContext = c;
        disabledByDefault.add(Telephony.Sms.getDefaultSmsPackage(c));
    }

    public static String getServerIP(Context c)
    {
        String addr = c.getSharedPreferences("IP", Context.MODE_PRIVATE).getString("ip", "http://45.56.24.120:4000/");
        try
        {
            URL url = new URL(addr);
        }
        catch (MalformedURLException e)
        {
            addr = "http://45.56.24.120:4000";
        }
        return addr;
    }

    public static void setServerIP(Context c, String ip)
    {
        c.getSharedPreferences("IP", Context.MODE_PRIVATE).edit().putString("ip", ip).commit();
    }


    public static boolean getGroupMessageSettings(Context context)
    {
        return context.getSharedPreferences("groupMessage", Context.MODE_PRIVATE).getBoolean("groupMms", true);
    }

    public static void setGroupMessageSettings(Context context, boolean newVal)
    {
        context.getSharedPreferences("groupMessage", Context.MODE_PRIVATE).edit().putBoolean("groupMms", newVal).commit();
    }

    Semaphore sem = new Semaphore(1);
    public void open()
    {
        sem.acquireUninterruptibly();

        if (count == 0)
        {
            ourHelper = new DbHelper(ourContext);
            ourDatabase = ourHelper.getWritableDatabase();
        }
        count++;
        sem.release();
    }

    public void close()
    {
        sem.acquireUninterruptibly();
        if (count == 1)
        {
            count--;
            ourHelper.close();
        }
        sem.release();
    }

    public void setEnabled(String packageName, boolean isEnabled)
    {
        String[] columns = new String[]{KEY_IS_ENABLED};
        Cursor res = ourDatabase.query(DATABASE_TABLE, columns, KEY_PACKAGE_NAME + " =? ", new String[]{packageName}, null, null, null);

        ContentValues cv = new ContentValues();
        cv.put(KEY_IS_ENABLED, isEnabled ? "true" : "false");
        if (res.getCount() > 0)
        {
            ourDatabase.update(DATABASE_TABLE, cv, KEY_PACKAGE_NAME + "=?", new String[]{packageName});
        }
        else
        {
            cv.put(KEY_PACKAGE_NAME, packageName);
            ourDatabase.insert(DATABASE_TABLE, null, cv);
        }
        res.close();
    }

    public boolean isEnabled(String packageName)
    {
        String[] columns = new String[]{KEY_IS_ENABLED};
        Cursor res = ourDatabase.query(DATABASE_TABLE, columns, KEY_PACKAGE_NAME + " =? ", new String[]{packageName}, null, null, null);
        boolean result;
        if (res.getCount() > 0)
        {
            res.moveToFirst();
            result = (res.getString(res.getColumnIndex(KEY_IS_ENABLED))).equals("true");
        }
        else
        {
            result = getDefaultStatus(packageName);
        }

        res.close();
        return result;
    }

    private boolean getDefaultStatus(String packageName)
    {
        return !disabledByDefault.contains(packageName);
    }

    private static class DbHelper extends SQLiteOpenHelper
    {

        DbHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + "(" + KEY_PACKAGE_NAME + " TEXT PRIMARY KEY NOT NULL, " + KEY_IS_ENABLED + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2)
        {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }
}
