package com.get_slyncy.slyncy.Model.Util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class Data extends Observable {

    // Do not change from private
    private Data() {
        mMessages = new HashMap<>();
    }

    private Settings mSettings;
    private Map<Integer, SlyncyMessageThread> mMessages;

    public Settings getSettings() {
        return mSettings;
    }

    public void updateCellSettings(final Context context) {
        if (mSettings == null) {
            mSettings = Settings.get(context);
        }

        this.mSettings.setmMyPhoneNumber(Utils.getMyPhoneNumber(context));

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

    public Map<Integer, SlyncyMessageThread> getmMessages() {
        return mMessages;
    }

    public void setmMessages(Map<Integer, SlyncyMessageThread> mMessages) {
        this.mMessages = mMessages;
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
