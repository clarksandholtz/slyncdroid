package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;
import java.util.Set;

/**
 * Created by undermark5 on 3/14/18.
 */
@Entity
public class NewMms
{
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "_id")
    public int msgId;

    @ColumnInfo(name = "read")
    public int read;

    public NewMms(int uid, int msgId)
    {
        this.uid = uid;
        this.msgId = msgId;
        this.read = 0;
    }

    @Ignore
    public NewMms(int uid, int msgId, int read)
    {
        this.uid = uid;
        this.msgId = msgId;
        this.read = read;
    }

    public int getUid()
    {
        return uid;
    }

    public void setUid(int uid)
    {
        this.uid = uid;
    }

    public int getMsgId()
    {
        return msgId;
    }

    public void setMsgId(int msgId)
    {
        this.msgId = msgId;
    }

    public int getRead()
    {
        return read;
    }

    public void setRead(int read)
    {
        this.read = read;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewMms newMms = (NewMms) o;
        return uid == newMms.uid &&
                msgId == newMms.msgId;
    }

    @Override
    public int hashCode()
    {
        return 41 * Integer.valueOf(msgId).hashCode() + 41 * Integer.valueOf(uid).hashCode();
    }
}
