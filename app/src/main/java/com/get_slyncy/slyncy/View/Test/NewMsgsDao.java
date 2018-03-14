package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import java.util.Set;

/**
 * Created by undermark5 on 3/14/18.
 */

@Dao
public interface NewMsgsDao
{
    @Query("select * from NewMms")
    Set<Integer> getMms();

    @Query("select * from NewSms")
    Set<Integer> getSms();

    @Insert
    void addMms(NewMms mms);

    @Insert
    void addSms(NewSms mms);

    @Delete
    void removeMms(NewMms mms);
}
