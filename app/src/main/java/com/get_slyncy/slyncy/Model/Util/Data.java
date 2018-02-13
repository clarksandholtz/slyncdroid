package com.get_slyncy.slyncy.Model.Util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.klinker.android.send_message.ApnUtils;

import java.util.Observable;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class Data extends Observable {

    // Do not change from private
    private Data() {
        // TODO: Pull persistent data from storage
//        mCellMessages = new MsgThread();
//        mSlyncyMessages = new MsgThread();
    }

    private Settings mSettings;

    public Settings getSettings() {
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

    public MsgsCache getmMessageCache() {
        return MsgsCache.getInstance();
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
