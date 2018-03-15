package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by undermark5 on 3/14/18.
 */
@Entity
public class NewSms
{
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "_id")
    public int msgId;

    @ColumnInfo(name = "read")
    public int read = 0;

    public NewSms(int uid, int msgId)
    {
        this.uid = uid;
        this.msgId = msgId;
        this.read = 0;
    }

    @Ignore
    public NewSms(int uid, int msgId, int read)
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
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewSms newSms = (NewSms) o;
        return uid == newSms.uid &&
                msgId == newSms.msgId;
    }

    @Override
    public int hashCode()
    {
        return 41 * Integer.valueOf(msgId).hashCode() + 41 * Integer.valueOf(uid).hashCode();
    }
}
