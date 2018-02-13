package com.get_slyncy.slyncy.Model.CellMessaging;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import com.android.mms.logs.LogTag;
import com.get_slyncy.slyncy.Model.DTO.ContactList;
import com.get_slyncy.slyncy.Model.DTO.MsgThread;
import com.get_slyncy.slyncy.Model.Util.MsgsCache;

import java.util.HashSet;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class MessageDbUtility {
    private static final String TAG = "MessageDbUtility";
    private static final boolean DEBUG = false;

    private static boolean sLoadingThreads;

    public static final Uri MMS_SMS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations/");
    public static final Uri sAllThreadsUri = Telephony.Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    public static final String[] ALL_THREADS_PROJECTION = {
            Telephony.Threads._ID, Telephony.Threads.DATE, Telephony.Threads.MESSAGE_COUNT, Telephony.Threads.RECIPIENT_IDS,
            Telephony.Threads.SNIPPET, Telephony.Threads.SNIPPET_CHARSET, Telephony.Threads.READ, Telephony.Threads.ERROR,
            Telephony.Threads.HAS_ATTACHMENT
    };

    public static final int ID = 0;
    public static final int DATE = 1;
    public static final int MESSAGE_COUNT = 2;
    public static final int RECIPIENT_IDS = 3;
    public static final int SNIPPET = 4;
    public static final int SNIPPET_CS = 5;
    public static final int READ = 6;
    public static final int ERROR = 7;
    public static final int HAS_ATTACHMENT = 8;

    public static void queryAllThreads(Context context) {
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] queryAllThreads: begin");
        }

        // check if threads are already being loaded
        synchronized (MsgsCache.getInstance()) {
            if (sLoadingThreads) {
                return;
            }
            sLoadingThreads = true;
        }

        // Keep track of what threads are now on disk so we
        // can discard anything removed from the cache.
        HashSet<Long> threadsOnDisk = new HashSet<>();

        // Query for all conversations.
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, null);
        try {
            if (c != null) {
                while (c.moveToNext()) {
                    long threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);

                    // Try to find this thread ID in the cache.
                    MsgThread conv;
                    synchronized (MsgsCache.getInstance()) {
                        conv = MsgsCache.get(threadId);
                    }

                    if (conv == null) {
                        // Make a new Conversation and put it in
                        // the cache if necessary.
                        conv = new MsgThread(context, threadId);
                        fillFromCursor(context, conv, c, true);
                        try {
                            synchronized (MsgsCache.getInstance()) {
                                MsgsCache.put(conv);
                            }
                        } catch (IllegalStateException e) {
                            LogTag.error("Tried to add duplicate Conversation to MsgsCache" +
                                    " for threadId: " + threadId + " new conv: " + conv);
                            if (!MsgsCache.replace(conv)) {
                                LogTag.error("queryAllThreads cache.replace failed on " + conv);
                            }
                        }
                    } else {
                        // Or update in place so people with references
                        // to conversations get updated too.
                        fillFromCursor(context, conv, c, true);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            synchronized (MsgsCache.getInstance()) {
                sLoadingThreads = false;
            }
        }

        // Purge the cache of threads that no longer exist on disk.
        MsgsCache.keepOnly(threadsOnDisk);

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] queryAllThreads: finished");
            MsgsCache.dumpCache();
        }
    }

    /**
     * Fill the specified conversation with the values from the specified
     * cursor, possibly setting recipients to empty if value allowQuery
     * is false and the recipient IDs are not in cache.  The cursor should
     * be one made via startQueryForAll.
     */
    private static void fillFromCursor(Context context, MsgThread conv,
                                       Cursor c, boolean allowQuery) {
        synchronized (conv) {
            conv.setmID(c.getLong(ID));
            conv.setmDate(c.getLong(DATE));
            conv.setmMessageCount(c.getInt(MESSAGE_COUNT));

            // Replace the snippet with a default value if it's empty.
//            String snippet = SmsHelper.cleanseMmsSubject(context,
//                    SmsHelper.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS));
//            if (TextUtils.isEmpty(snippet)) {
//                snippet = context.getString(R.string.no_subject_view);
//            }
//            conv.mSnippet = snippet;

            conv.setmHasUnreadMsgs(c.getInt(READ) == 0);
            conv.setmHasError((c.getInt(ERROR) != 0));
//            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
        }
        // Fill in as much of the conversation as we can before doing the slow stuff of looking
        // up the contacts associated with this conversation.
        String recipientIds = c.getString(RECIPIENT_IDS);
        ContactList recipients = ContactList.getByIds(recipientIds, context);
        synchronized (conv) {
            conv.setmRecipients(recipients);
        }

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.DEBUG)) {
            Log.d(LogTag.THREAD_CACHE, "fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }

    public static void queryForThread() {

    }

    /**
     * Private cache for the use of the various forms of Conversation.get.
     */

}
