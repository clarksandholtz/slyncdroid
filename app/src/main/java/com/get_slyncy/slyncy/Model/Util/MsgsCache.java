package com.get_slyncy.slyncy.Model.Util;

import android.util.Log;

import com.android.mms.logs.LogTag;
import com.get_slyncy.slyncy.Model.DTO.MsgThread;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class MsgsCache {
    private static MsgsCache sInstance = new MsgsCache();
    private static final boolean DEBUG = false;

    public static MsgsCache getInstance() {
        return sInstance;
    }

    private final HashSet<MsgThread> mCache;

    private MsgsCache() {
        mCache = new HashSet<MsgThread>(10);
    }

    /**
     * Return the conversation with the specified thread ID, or
     * null if it's not in cache.
     */
    public static MsgThread get(long threadId) {
        synchronized (sInstance) {
            if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                LogTag.debug("Conversation get with threadId: " + threadId);
            }
            for (MsgThread c : sInstance.mCache) {
                if (DEBUG) {
                    LogTag.debug("Conversation get() threadId: " + threadId +
                            " c.getThreadId(): " + c.getmID());
                }
                if (c.getmID() == threadId) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Return the conversation with the specified recipient
     * list, or null if it's not in cache.
     */
//        static MsgThread get(ContactList list) {
//            synchronized (sInstance) {
//                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
//                    LogTag.debug("Conversation get with ContactList: " + list);
//                }
//                for (MsgThread c : sInstance.mCache) {
//                    if (c.getmRecipients().equals(list)) {
//                        return c;
//                    }
//                }
//            }
//            return null;
//        }

    /**
     * Put the specified conversation in the cache.  The caller
     * should not place an already-existing conversation in the
     * cache, but rather update it in place.
     */
    public static void put(MsgThread c) {
        synchronized (sInstance) {
            // We update cache entries in place so people with long-
            // held references get updated.
            if (Log.isLoggable(LogTag.THREAD_CACHE, Log.DEBUG)) {
                Log.d(LogTag.THREAD_CACHE, "Conversation.MsgsCache.put: conv= " + c + ", hash: " + c.hashCode());
            }

            if (sInstance.mCache.contains(c)) {
                if (DEBUG) {
                    dumpCache();
                }
                throw new IllegalStateException("cache already contains " + c +
                        " threadId: " + c.getmID());
            }
            sInstance.mCache.add(c);
        }
    }

    /**
     * Replace the specified conversation in the cache. This is used in cases where we
     * lookup a conversation in the cache by threadId, but don't find it. The caller
     * then builds a new conversation (from the cursor) and tries to add it, but gets
     * an exception that the conversation is already in the cache, because the hash
     * is based on the recipients and it's there under a stale threadId. In this function
     * we remove the stale entry and add the new one. Returns true if the operation is
     * successful
     */
    public static boolean replace(MsgThread c) {
        synchronized (sInstance) {
            if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                LogTag.debug("Conversation.MsgsCache.put: conv= " + c + ", hash: " + c.hashCode());
            }

            if (!sInstance.mCache.contains(c)) {
                if (DEBUG) {
                    dumpCache();
                }
                return false;
            }
            // Here it looks like we're simply removing and then re-adding the same object
            // to the hashset. Because the hashkey is the conversation's recipients, and not
            // the thread id, we'll actually remove the object with the stale threadId and
            // then add the the conversation with updated threadId, both having the same
            // recipients.
            sInstance.mCache.remove(c);
            sInstance.mCache.add(c);
            return true;
        }
    }

    public static void remove(long threadId) {
        synchronized (sInstance) {
            if (DEBUG) {
                LogTag.debug("remove threadid: " + threadId);
                dumpCache();
            }
            for (MsgThread c : sInstance.mCache) {
                if (c.getmID() == threadId) {
                    sInstance.mCache.remove(c);
                    return;
                }
            }
        }
    }

    public static void dumpCache() {
        synchronized (sInstance) {
            LogTag.debug("Conversation dumpCache: ");
            for (MsgThread c : sInstance.mCache) {
                LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
            }
        }
    }

    /**
     * Remove all conversations from the cache that are not in
     * the provided set of thread IDs.
     */
    public static void keepOnly(Set<Long> threads) {
        synchronized (sInstance) {
            Iterator<MsgThread> iter = sInstance.mCache.iterator();
            while (iter.hasNext()) {
                MsgThread c = iter.next();
                if (!threads.contains(c.getmID())) {
                    iter.remove();
                }
            }
        }
        if (DEBUG) {
            LogTag.debug("after keepOnly");
            dumpCache();
        }
    }
}
