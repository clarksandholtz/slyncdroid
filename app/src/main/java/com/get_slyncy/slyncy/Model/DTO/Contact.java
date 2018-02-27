package com.get_slyncy.slyncy.Model.DTO;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class Contact {

    private static final String TAG = "Contact";

    private String mNumber;
    private String mName;
    private Bitmap mAvatar;

    public Contact(String number) {
        mNumber = number;
        if (number == null || number.length() < 1) {
            return;
        }
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

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Contact c = (Contact) obj;

        if (!this.getmName().equalsIgnoreCase(c.getmName())) {
            return false;
        }

        return true;
    }
}
