package com.get_slyncy.slyncy.Presenter;

import com.get_slyncy.slyncy.Model.CellMessage;
import com.get_slyncy.slyncy.Model.IMessage;

/**
 * Created by tylerbowers on 1/27/18.
 */

public interface IMessagesPresenter {

    void sendMessage(IMessage message);
    void refreshMessages();

    void sendMMS(IMessage message);
}
