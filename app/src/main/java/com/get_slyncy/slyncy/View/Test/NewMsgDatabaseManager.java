package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by undermark5 on 3/14/18.
 */

class NewMsgDatabaseManager
{
    private static NewMsgDatabaseManager ourInstance = null;
    private NewMsgDatabase db;

    public static NewMsgDatabaseManager getInstance(Context context)
    {
        if (ourInstance == null)
        {
            ourInstance = new NewMsgDatabaseManager(Room.databaseBuilder(context, NewMsgDatabase.class, context.getPackageName().concat("_db")).build());
        }
        return ourInstance;
    }

    private NewMsgDatabaseManager(NewMsgDatabase db)
    {
        this.db = db;
    }

    public NewMsgDatabase getDb()
    {
        return db;
    }
}
