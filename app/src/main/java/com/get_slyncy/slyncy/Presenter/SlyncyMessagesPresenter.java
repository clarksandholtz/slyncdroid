package com.get_slyncy.slyncy.Presenter;

import android.app.Activity;
import android.content.Context;

import com.get_slyncy.slyncy.Model.IMessage;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class SlyncyMessagesPresenter implements IMessagesPresenter {

    private Activity mView;
    private Context mContext;

//    public SlyncyMessagesPresenter(SlyncyMessagesActivity view) {
//        mView = view;
//        mContext = mView.getApplicationContext();
//    }


    @Override
    public void sendMessage(IMessage message) {

    }

    @Override
    public void refreshMessages() {

    }
}
