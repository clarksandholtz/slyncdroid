package com.get_slyncy.slyncy.Model;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.klinker.android.send_message.ApnUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class Data extends Observable {

    // Do not change from private
    private Data() {
        // TODO: Pull persistent data from storage
        mCellMessages = new MessageList();
        mSlyncyMessages = new MessageList();
    }

    private Settings mSettings;
    private MessageList mCellMessages;
    private MessageList mSlyncyMessages;

    Settings getSettings() {
        return mSettings;
    }

    public void updateCellSettings(final Context context) {
        if (mSettings == null) {
            mSettings = Settings.get(context);
        }

        if (TextUtils.isEmpty(mSettings.getMmsc()) &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ApnUtils.initDefaultApns(context, new ApnUtils.OnApnFinishedListener() {
                @Override
                public void onFinished() {
                    mSettings = Settings.get(context, true);
                }
            });
        }
    }

    public MessageList getCellMessages() {
        return mCellMessages;
    }

    public MessageList getSlyncyMessages() {
        return mSlyncyMessages;
    }

    // ***************** Singleton business ********************* //

    private static volatile Data instance = null;
    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }
}
