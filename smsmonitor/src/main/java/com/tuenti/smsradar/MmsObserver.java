package com.tuenti.smsradar;

import android.database.ContentObserver;
import android.os.Handler;

class MmsObserver extends ContentObserver
{

    public MmsObserver(Handler h) {
        super(h);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}