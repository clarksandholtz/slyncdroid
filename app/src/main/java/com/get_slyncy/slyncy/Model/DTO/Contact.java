package com.get_slyncy.slyncy.Model.DTO;

import android.graphics.Bitmap;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class Contact {

    private String mNumber;
    private String mName;
    private Bitmap mAvatar;
    private long mRecipientId;

    public Contact(String number, String name) {
        mName = name;
        mNumber = number;
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmNumber(String mNumber) {
        this.mNumber = mNumber;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public Bitmap getmAvatar() {
        return mAvatar;
    }

    public void setmAvatar(Bitmap mAvatar) {
        this.mAvatar = mAvatar;
    }

    public long getmRecipientId() {
        return mRecipientId;
    }

    public void setmRecipientId(long mRecipientId) {
        this.mRecipientId = mRecipientId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Contact c = (Contact) obj;

        if (!this.getmName().equalsIgnoreCase(c.getmName())) {
            return false;
        }

        if (!this.getmNumber().equals(c.getmNumber())) {
            return false;
        }

        return true;
    }
}
