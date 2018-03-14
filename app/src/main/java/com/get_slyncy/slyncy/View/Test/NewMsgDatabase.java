package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by undermark5 on 3/14/18.
 */
@Database(entities = {NewSms.class, NewMms.class}, version = 1)
public abstract class NewMsgDatabase extends RoomDatabase
{
    public abstract NewMsgsDao NewMesNewMsgsDao();
}
