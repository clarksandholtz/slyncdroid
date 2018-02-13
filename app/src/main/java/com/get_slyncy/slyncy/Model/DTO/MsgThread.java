package com.get_slyncy.slyncy.Model.DTO;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class MsgThread {

    private static final String TAG = "MsgThread";

    private Context mContext;

    private long mID;

    private ContactList mRecipients;
    private long mDate;
    private int mMessageCount;
    private String mLatestSnippet;
    private boolean mHasUnreadMsgs;
    private boolean mHasError;

    public MsgThread (Context context, long threadId) {
        Log.v(TAG, "Conversation constructor threadId: " + threadId);
        mContext = context;
//        if (!loadFromThreadId(threadId)) {
//            mRecipients = new ContactList();
//            mThreadId = 0;
//        }
    }

    public long getmID() {
        return mID;
    }

    public void setmID(long mID) {
        this.mID = mID;
    }

    public ContactList getmRecipients() {
        return mRecipients;
    }

    public void setmRecipients(ContactList mRecipients) {
        this.mRecipients = mRecipients;
    }

    public long getmDate() {
        return mDate;
    }

    public void setmDate(long mDate) {
        this.mDate = mDate;
    }

    public int getmMessageCount() {
        return mMessageCount;
    }

    public void setmMessageCount(int mMessageCount) {
        this.mMessageCount = mMessageCount;
    }

    public String getmLatestSnippet() {
        return mLatestSnippet;
    }

    public void setmLatestSnippet(String mLatestSnippet) {
        this.mLatestSnippet = mLatestSnippet;
    }

    public boolean ismHasUnreadMsgs() {
        return mHasUnreadMsgs;
    }

    public void setmHasUnreadMsgs(boolean mHasUnreadMsgs) {
        this.mHasUnreadMsgs = mHasUnreadMsgs;
    }

    public boolean ismHasError() {
        return mHasError;
    }

    public void setmHasError(boolean mHasError) {
        this.mHasError = mHasError;
    }
}
