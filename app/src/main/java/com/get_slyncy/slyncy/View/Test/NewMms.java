package com.get_slyncy.slyncy.View.Test;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;
import java.util.Set;

/**
 * Created by undermark5 on 3/14/18.
 */

public class NewMms
{

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "_id")
    public int msgId;

    public NewMms(int uid, int msgId)
    {
        this.uid = uid;
        this.msgId = msgId;
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
}
