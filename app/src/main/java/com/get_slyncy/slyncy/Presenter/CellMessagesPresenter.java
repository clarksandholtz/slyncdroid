package com.get_slyncy.slyncy.Presenter;

import android.app.Activity;
import android.content.Context;

import com.get_slyncy.slyncy.Model.CellMessage;
import com.get_slyncy.slyncy.Model.Data;
import com.get_slyncy.slyncy.Model.MessageSender;
import com.get_slyncy.slyncy.View.MainActivity;


/**
 * Created by tylerbowers on 1/27/18.
 */

public class CellMessagesPresenter {

    private Activity mView;
    private Context mContext;

    public CellMessagesPresenter(MainActivity view) {
        mView = view;
        mContext = mView.getApplicationContext();
    }

    public void sendMessage(CellMessage message) {
        MessageSender.sendSMSMessage(message, mContext);
    }

    public void refreshMessages() {

    }

    public void sendMMS(CellMessage message)
    {
        MessageSender.sendMMSMessage(message, mContext);
    }

    public void initCellSettings() {
        Data.getInstance().updateCellSettings(mContext);
    }
}
