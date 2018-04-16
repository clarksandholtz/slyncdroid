package com.get_slyncy.slyncy.Model.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

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
        disabledByDefault
                .add("com.google.android.apps.messaging"); //We already have sms notifications in the telephony plugin
        disabledByDefault
                .add("com.google.android.googlequicksearchbox"); //Google Now notifications re-spawn every few minutes
    }

    private final Context ourContext;
    private SQLiteDatabase ourDatabase;
    private DbHelper ourHelper;

    public SettingsDb(Context c)
    {
        ourContext = c;
    }

    public static String getServerIP(Context c)
    {
        return c.getSharedPreferences("IP", Context.MODE_PRIVATE).getString("ip", "http://45.56.24.120:4000/");
    }

    public static void setServerIP(Context c, String ip)
    {
        c.getSharedPreferences("IP", Context.MODE_PRIVATE).edit().putString("ip", ip).commit();
    }

    public static boolean initGroupMessageSettings(File fileDir)
    {
        File groupSettings = new File(fileDir, "groupSettings");
        boolean newVal = true;
        if (groupSettings.exists())
        {
            try (Scanner scanner = new Scanner(groupSettings))
            {
                newVal = Boolean.parseBoolean(scanner.nextLine());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try (FileWriter writer = new FileWriter(groupSettings))
            {
                writer.write(String.valueOf(newVal));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return newVal;
    }

    public static boolean getGroupMessageSettings(File fileDir)
    {
        boolean newVal = true;
        File groupSettings = new File(fileDir, "groupSettings");
        try (Scanner scanner = new Scanner(groupSettings))
        {
            newVal = Boolean.parseBoolean(scanner.nextLine());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return newVal;
    }

    public static void setGroupMessageSettings(File fileDir, boolean newVal)
    {
        File groupSettings = new File(fileDir, "groupSettings");
        try (FileWriter writer = new FileWriter(groupSettings))
        {
            writer.write(String.valueOf(newVal));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void open()
    {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
    }

    public void close()
    {
        ourHelper.close();
    }

    public void setEnabled(String packageName, boolean isEnabled)
    {
        String[] columns = new String[]{KEY_IS_ENABLED};
        Cursor res = ourDatabase
                .query(DATABASE_TABLE, columns, KEY_PACKAGE_NAME + " =? ", new String[]{packageName}, null, null, null);

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
        Cursor res = ourDatabase
                .query(DATABASE_TABLE, columns, KEY_PACKAGE_NAME + " =? ", new String[]{packageName}, null, null, null);
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
            db.execSQL(
                    "CREATE TABLE " + DATABASE_TABLE + "(" + KEY_PACKAGE_NAME + " TEXT PRIMARY KEY NOT NULL, " + KEY_IS_ENABLED + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2)
        {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }
}
