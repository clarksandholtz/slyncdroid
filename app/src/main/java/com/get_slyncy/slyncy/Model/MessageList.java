package com.get_slyncy.slyncy.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class MessageList {

    private Map<String, List<IMessage>> mMessages;

    public MessageList() {
        mMessages = new HashMap<>();
    }

    boolean addMessage(String contact, IMessage message) {
        if (mMessages.containsKey(contact)) {
            return mMessages.get(contact).add(message);
        }
        else {
            List<IMessage> newMsgThread = new ArrayList<>();
            newMsgThread.add(message);
            return mMessages.put(contact, newMsgThread) != null;
        }
    }

    public List<IMessage> getContactMessages(String contact) {
        return mMessages.get(contact);
    }

    public List<String> getThreads() {
        // TODO: figure out a way to return threads by most-recent message
        return null;
    }
}
