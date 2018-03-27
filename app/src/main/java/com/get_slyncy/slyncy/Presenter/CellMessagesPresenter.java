package com.get_slyncy.slyncy.Presenter;

import android.app.Activity;
import android.content.Context;

import com.get_slyncy.slyncy.Model.CellMessaging.MessageSender;
import com.get_slyncy.slyncy.Model.DTO.CellMessage;
import com.get_slyncy.slyncy.Model.Util.Data;
import com.get_slyncy.slyncy.View.Test.TestActivity;


/**
 * Created by tylerbowers on 1/27/18.
 */

public class CellMessagesPresenter
{

//    private Activity mView;
    private Context mContext;

    public CellMessagesPresenter(TestActivity view)
    {
//        mView = view;
        mContext = view.getApplicationContext();
    }

    public void sendMessage(CellMessage message)
    {
        MessageSender.sendSMSMessage(message, mContext);
    }

    public void refreshMessages()
    {

    }

    public void sendMMS(CellMessage message)
    {
        MessageSender.sendMMSMessage(message, mContext);
    }

    public void initCellSettings()
    {
        Data.getInstance().updateCellSettings(mContext);
    }
}
