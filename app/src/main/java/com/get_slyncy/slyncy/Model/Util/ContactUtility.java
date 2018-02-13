package com.get_slyncy.slyncy.Model.Util;

/**
 * Created by tylerbowers on 2/12/18.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.Threads;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

//import net.everythingandroid.smspopup.BuildConfig;
//import net.everythingandroid.smspopup.R;
//import net.everythingandroid.smspopup.provider.SmsMmsMessage;
//import net.everythingandroid.smspopup.provider.SmsPopupContract.ContactNotifications;
//import net.everythingandroid.smspopup.provider.SmsPopupContract.Messages;
//import net.everythingandroid.smspopup.receiver.SmsReceiver;
//import net.everythingandroid.smspopup.util.ManagePreferences.Defaults;

import com.get_slyncy.slyncy.BuildConfig;
import com.get_slyncy.slyncy.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
        import java.util.regex.Pattern;

@TargetApi(VERSION_CODES.KITKAT)
public class ContactUtility {

    private static final String TAG = "ContactUtility";

    // Content URIs for SMS app, these may change in future SDK
    public static final Uri MMS_SMS_CONTENT_URI = MmsSms.CONTENT_URI;
    public static final Uri THREAD_ID_CONTENT_URI =
            Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "threadID");
    public static final Uri CONVERSATION_CONTENT_URI = Threads.CONTENT_URI;
    public static final String SMSTO_SCHEMA = "smsto:";
    private static final String UNREAD_CONDITION = TextBasedSmsColumns.READ + "=0";

    public static final Uri SMS_CONTENT_URI = Sms.CONTENT_URI;
    public static final Uri SMS_INBOX_CONTENT_URI = Inbox.CONTENT_URI;

    public static final Uri MMS_CONTENT_URI = Mms.CONTENT_URI;
    public static final Uri MMS_INBOX_CONTENT_URI = Mms.Inbox.CONTENT_URI;

    public static final String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
    public static final int READ_THREAD = 1;

    // The max size of either the width or height of the contact photo
    public static final int CONTACT_PHOTO_MAXSIZE = 1024;

    private static final String[] AUTHOR_CONTACT_INFO =
            { "Adam K <smspopup@everythingandroid.net>" };
    private static final String[] AUTHOR_CONTACT_INFO_DONATE =
            { "Adam K <smspopup+donate@everythingandroid.net>" };

    public static final Uri DONATE_MARKET_URI =
            Uri.parse("market://details?id=net.everythingandroid.smspopupdonate");
    public static final Uri DONATE_WEB_MARKET_URI =
            Uri.parse("https://play.google.com/store/apps/details?id=net.everythingandroid.smspopupdonate");

    public static boolean hasHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasICS() {
        // Can use static final constants like ICS, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    /**
     * Looks up a contacts display name by contact lookup key - if not found,
     * the address (phone number) will be formatted and returned instead.
     * @param context Context.
     * @param lookupKey Contact lookup key.
     * @param contactId
     * @return Contact name or null if not found.
     */
    public static ContactIdentification getPersonNameByLookup(Context context, String lookupKey,
                                                              String contactId) {

        // Check for id, if null return the formatting phone number as the name
        if (lookupKey == null) {
            return null;
        }

        Uri.Builder builder = Contacts.CONTENT_LOOKUP_URI.buildUpon();
        builder.appendPath(lookupKey);
        if (contactId != null) {
            builder.appendPath(contactId);
        }
        Uri uri = builder.build();

        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[] { Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME },
                null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    final String newId = cursor.getString(0);
                    final String newLookup = cursor.getString(1);
                    final String contactName = cursor.getString(2);
                    if (BuildConfig.DEBUG) Log.v(TAG, "Contact Display Name: " + contactName);
                    return new ContactIdentification(newId, newLookup, contactName);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return null;
    }

    /*
     * Class to hold contact lookup info (as of Android 2.0+ we need the id and lookup key)
     */
    public static class ContactIdentification {
        public String contactId = null;
        public String contactLookup = null;
        public String contactName = null;

        public ContactIdentification(String _contactId, String _contactLookup, String _contactName) {
            contactId = _contactId;
            contactLookup = _contactLookup;
            contactName = _contactName;
        }
    }

    /**
     * Looks up a contacts id, given their address (phone number in this case). Returns null if not
     * found
     */
    public static ContactIdentification getPersonIdFromPhoneNumber(
            Context context, String address) {

        if (address == null) {
            return null;
        }

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
                    new String[] { PhoneLookup._ID, PhoneLookup.DISPLAY_NAME, PhoneLookup.LOOKUP_KEY },
                    null, null, null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getPersonIdFromPhoneNumber(): " + e.toString());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getPersonIdFromPhoneNumber(): " + e.toString());
            return null;
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String contactId = String.valueOf(cursor.getLong(0));
                    String contactName = cursor.getString(1);
                    String contactLookup = cursor.getString(2);

                    if (BuildConfig.DEBUG)
                        Log.v(TAG, "Found person: " + contactId + ", " + contactName + ", "
                                + contactLookup);
                    return new ContactIdentification(contactId, contactLookup, contactName);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return null;
    }

    /**
     * Looks up a contacts id, given their email address. Returns null if not found
     */
    public static ContactIdentification getPersonIdFromEmail(Context context, String email) {
        if (email == null)
            return null;

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    Uri.withAppendedPath(
                            Email.CONTENT_LOOKUP_URI,
                            Uri.encode(extractAddrSpec(email))),
                    new String[] { Email.CONTACT_ID, Email.DISPLAY_NAME_PRIMARY, Email.LOOKUP_KEY },
                    null, null, null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getPersonIdFromEmail(): " + e.toString());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getPersonIdFromEmail(): " + e.toString());
            return null;
        }

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {

                    String contactId = String.valueOf(cursor.getLong(0));
                    String contactName = cursor.getString(1);
                    String contactLookup = cursor.getString(2);

                    if (BuildConfig.DEBUG)
                        Log.v(TAG, "Found person: " + contactId + ", " + contactName + ", "
                                + contactLookup);
                    return new ContactIdentification(contactId, contactLookup, contactName);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    /**
     *
     * Looks up a contact photo by contact id, returns a Bitmap array that represents their photo
     * (or null if not found or there was an error.
     *
     * I do my own scaling and validation of sizes - Android supports any size for contact photos
     * and some apps are adding huge photos to contacts. Doing the scaling myself allows me more
     * control over how things play out in those cases.
     *
     * @param context
     *            the context
     * @param contactUri
     *            contact uri
     * @param thumbSize
     *            the max size the thumbnail can be
     * @return Bitmap of the contacts photo (null if none or an error)
     */
    public static Bitmap getPersonPhoto(Context context, final Uri contactUri,
                                        final int thumbSize) {

        if (contactUri == null) {
            return null;
        }

        // First let's just check the dimensions of the contact photo
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // The height and width are stored in 'options' but the photo itself is not loaded
        loadContactPhoto(context, contactUri, options);

        // Raw height and width of contact photo
        final int height = options.outHeight;
        final int width = options.outWidth;

        if (BuildConfig.DEBUG)
            Log.v(TAG, "Contact photo size = " + height + "x" + width);

        // If photo is too large or not found get out
        if (height > CONTACT_PHOTO_MAXSIZE || width > CONTACT_PHOTO_MAXSIZE ||
                width == 0 || height == 0) {
            return null;
        }

        // This time we're going to do it for real
        options.inJustDecodeBounds = false;

        int newHeight = thumbSize;
        int newWidth = thumbSize;

        // If we have an abnormal photo size that's larger than thumbsize then sample it down
        boolean sampleDown = false;

        if (height > thumbSize || width > thumbSize) {
            sampleDown = true;
        }

        // If the dimensions are not the same then calculate new scaled dimenions
        if (height < width) {
            if (sampleDown) {
                options.inSampleSize = Math.round(height / thumbSize);
            }
            newHeight = Math.round(thumbSize * height / width);
        } else {
            if (sampleDown) {
                options.inSampleSize = Math.round(width / thumbSize);
            }
            newWidth = Math.round(thumbSize * width / height);
        }

        // Fetch the real contact photo (sampled down if needed)
        Bitmap contactBitmap = null;
        try {
            contactBitmap = loadContactPhoto(context, contactUri, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory when loading contact photo");
        }

        // Not found or error, get out
        if (contactBitmap == null)
            return null;

        // Bitmap scaled to new height and width
        return Bitmap.createScaledBitmap(contactBitmap, newWidth, newHeight, true);
    }

    public static Bitmap getPersonPhoto(Context context, Uri contactUri) {
        if (context == null) {
            return null;
        }
//        final Resources res = context.getResources();
//        final int thumbSize = (int) res.getDimension(R.dimen.contact_thumbnail_size);
//        final int thumbBorder = (int) res.getDimension(R.dimen.contact_thumbnail_border);
//        return getPersonPhoto(context, contactUri, thumbSize - thumbBorder);
        return null;
    }

    /**
     * Opens an InputStream for the person's photo and returns the photo as a Bitmap. If the
     * person's photo isn't present returns the placeholderImageResource instead.
     *
     * @param context
     *            the Context
     * @param contactUri
     *            the uri of the person
     * @param options
     *            the decoding options, can be set to null
     */
    @SuppressLint("NewApi")
    public static Bitmap loadContactPhoto(Context context, Uri contactUri,
                                          BitmapFactory.Options options) {

        if (contactUri == null) {
            return null;
        }

        final InputStream stream;
        if (ContactUtility.hasICS()) {
            stream = Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    contactUri, true);
        } else {
            stream = Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    contactUri);
        }

        return stream != null ? BitmapFactory.decodeStream(stream, null, options) : null;
    }

    /**
     *
     * Tries to locate the message thread id given the address (phone or email) of the message
     * sender.
     *
     * @param context
     *            a context to use
     * @param address
     *            phone number or email address of sender
     * @return the thread id (or 0 if there was a problem)
     */
    public static long findThreadIdFromAddress(Context context, String address) {
        if (address == null)
            return 0;

        String THREAD_RECIPIENT_QUERY = "recipient";

        Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, address);

        long threadId = 0;

        Cursor cursor = null;
        try {

            cursor = context.getContentResolver().query(
                    uriBuilder.build(),
                    new String[] { Contacts._ID },
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                threadId = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return threadId;
    }

//    public static void setMessageRead(Context context, long messageId, int messageType) {
//        setMessageRead(context, messageId, messageType, 0);
//    }

    /**
     * Marks a specific message as read
     */
//    public static void setMessageRead(
//            Context context, long messageId, int messageType, long timestamp) {
//
//        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean markRead = myPrefs.getBoolean(
//                context.getString(R.string.pref_markread_key),
//                Defaults.PREFS_MARK_READ);
//
//        if (ContactUtility.hasKitKat()) {
//            Cursor cursor = null;
//
//            try {
//                cursor = context.getContentResolver().query(
//                        Messages.buildMessageUri(String.valueOf(messageId)),
//                        new String[] { Messages._ID, Messages.READ }, null, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    if ("0".equals(cursor.getString(1))) {
//                        context.getContentResolver().update(
//                                Messages.buildMessageRead(String.valueOf(messageId)),
//                                null, null, null);
//                    }
//                } else {
//                    ContentValues readValues = new ContentValues(1);
//                    readValues.put(Messages._ID, messageId);
//                    readValues.put(Messages.READ, 1);
//                    readValues.put(Messages.TIMESTAMP, timestamp);
//                    context.getContentResolver().insert(
//                            Messages.buildMessageUri(String.valueOf(messageId)), readValues);
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//
//            // Now delete any old messages from the local database
//            String time = String.valueOf(System.currentTimeMillis() -
//                    Messages.STALE_READ_MESSAGES_TIME);
//            int count = context.getContentResolver().delete(Messages.CONTENT_URI,
//                    Messages.ADDED + "< ?", new String[] { time });
//            if (BuildConfig.DEBUG) {
//                Log.v(TAG, "Cleaned local read message database - " + count);
//            }
//
//            return;
//        }
//
//        if (!markRead) {
//            return;
//        }
//
//        if (messageId > 0) {
//            ContentValues values = new ContentValues(1);
//            values.put("read", READ_THREAD);
//
//            Uri messageUri;
//
//            if (SmsMmsMessage.MESSAGE_TYPE_MMS == messageType) {
//                // Used to use URI of MMS_CONTENT_URI and it wasn't working, not sure why
//                // this is diff to SMS
//                messageUri = Uri.withAppendedPath(MMS_INBOX_CONTENT_URI, String.valueOf(messageId));
//            } else if (SmsMmsMessage.MESSAGE_TYPE_SMS == messageType) {
//                messageUri = Uri.withAppendedPath(SMS_CONTENT_URI, String.valueOf(messageId));
//            } else {
//                return;
//            }
//
//            // Log.v(TAG, "messageUri for marking message read: " + messageUri.toString());
//
//            ContentResolver cr = context.getContentResolver();
//            int result;
//            try {
//                result = cr.update(messageUri, values, null, null);
//            } catch (Exception e) {
//                result = 0;
//            }
//            if (BuildConfig.DEBUG)
//                Log.v(TAG, String.format("message id = %s marked as read, result = %s", messageId,
//                        result));
//        }
//    }

    /**
     * Marks a specific message thread as read - all messages in the thread will be marked read
     */
//    public static void setThreadRead(Context context, long threadId) {
//        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean markRead = myPrefs.getBoolean(
//                context.getString(R.string.pref_markread_key),
//                Defaults.PREFS_MARK_READ);
//
//        if (!markRead)
//            return;
//
//        if (threadId > 0) {
//            ContentValues values = new ContentValues(1);
//            values.put("read", READ_THREAD);
//
//            ContentResolver cr = context.getContentResolver();
//            int result = 0;
//            try {
//                result = cr.update(
//                        ContentUris.withAppendedId(CONVERSATION_CONTENT_URI, threadId),
//                        values, null, null);
//            } catch (Exception e) {
//                if (BuildConfig.DEBUG)
//                    Log.v(TAG, "error marking thread read");
//            }
//            if (BuildConfig.DEBUG)
//                Log.v(TAG, "thread id " + threadId + " marked as read, result = " + result);
//        }
//    }

    /**
     * Tries to locate the message id (from the system database), given the message thread id, the
     * timestamp of the message and the type of message (sms/mms)
     */
//    public static long findMessageId(Context context, long threadId, long timestamp,
//                                     String body, int messageType) {
//
//        long id = 0;
//        String selection = "body = " + DatabaseUtils.sqlEscapeString(body != null ? body : "");
//        selection += " and " + UNREAD_CONDITION;
//        final String sortOrder = "date DESC";
//        final String[] projection = new String[] { "_id", "date", "thread_id", "body" };
//
//        if (threadId > 0) {
//            if (BuildConfig.DEBUG)
//                Log.v(TAG, "Trying to find message ID");
//            if (SmsMmsMessage.MESSAGE_TYPE_MMS == messageType) {
//                // It seems MMS timestamps are stored in a seconds, whereas SMS timestamps are in
//                // millis
//                selection += " and date = " + (timestamp / 1000);
//            }
//
//            Cursor cursor = context.getContentResolver().query(
//                    ContentUris.withAppendedId(CONVERSATION_CONTENT_URI, threadId),
//                    projection,
//                    selection,
//                    null,
//                    sortOrder);
//
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    id = cursor.getLong(0);
//                    if (BuildConfig.DEBUG)
//                        Log.v(TAG, "Message id found = " + id);
//                    // Log.v(TAG, "Timestamp = " + cursor.getLong(1));
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//
//        if (BuildConfig.DEBUG && id == 0) {
//            Log.v(TAG, "Message id could not be found");
//        }
//
//        return id;
//    }

    /**
     * Tries to delete a message from the system database, given the thread id, the timestamp of the
     * message and the message type (sms/mms).
     */
//    public static void deleteMessage(Context context, long messageId,
//                                     long threadId, int messageType) {
//
//        if (messageId > 0) {
//            if (BuildConfig.DEBUG)
//                Log.v(TAG, "id of message to delete is " + messageId);
//
//            // We need to mark this message read first to ensure the entire thread is marked as read
//            setMessageRead(context, messageId, messageType);
//
//            // Construct delete message uri
//            Uri deleteUri;
//
//            if (SmsMmsMessage.MESSAGE_TYPE_MMS == messageType) {
//                deleteUri = Uri.withAppendedPath(MMS_CONTENT_URI, String.valueOf(messageId));
//            } else if (SmsMmsMessage.MESSAGE_TYPE_SMS == messageType) {
//                deleteUri = Uri.withAppendedPath(SMS_CONTENT_URI, String.valueOf(messageId));
//            } else {
//                return;
//            }
//
//            int count = 0;
//            try {
//                count = context.getContentResolver().delete(deleteUri, null, null);
//            } catch (Exception e) {
//                if (BuildConfig.DEBUG)
//                    Log.v(TAG, "deleteMessage(): Problem deleting message - " + e.toString());
//            }
//
//            if (BuildConfig.DEBUG)
//                Log.v(TAG, "Messages deleted: " + count);
//            if (count == 1) {
//                // TODO: should only set the thread read if there are no more unread messages
//                // setThreadRead(context, threadId);
//            }
//        }
//    }
//
//    public static ArrayList<SmsMmsMessage> getUnreadMessages(Context context) {
//        final ArrayList<SmsMmsMessage> messages = getUnreadSms(context);
//        final ArrayList<SmsMmsMessage> mmsMessages = getUnreadMms(context);
//        final HashSet<Long> localReadMessages = getLocalReadMessages(context);
//
//        if (messages == null && mmsMessages == null) {
//            return null;
//        } else if (messages != null && mmsMessages == null) {
//            removeLocalReadMessages(messages, localReadMessages);
//            return messages;
//        } else if (messages == null && mmsMessages != null) {
//            removeLocalReadMessages(mmsMessages, localReadMessages);
//            return mmsMessages;
//        }
//        messages.addAll(mmsMessages);
//        Collections.sort(messages, new Comparator<SmsMmsMessage>() {
//            @Override
//            public int compare(SmsMmsMessage lhs, SmsMmsMessage rhs) {
//                if (lhs.getTimestamp() < rhs.getTimestamp()) {
//                    return -1;
//                }
//                return 1;
//            }
//        });
//        removeLocalReadMessages(messages, localReadMessages);
//        messages.trimToSize();
//        return messages;
//    }

    /**
     * Fetches a list of unread messages from the system database
     *
     * @param context
     *            app context
     *
     * @return ArrayList of SmsMmsMessage
     */
//    public static ArrayList<SmsMmsMessage> getUnreadSms(Context context) {
//        ArrayList<SmsMmsMessage> messages = null;
//
//        final String[] projection =
//                new String[] { "_id", "thread_id", "address", "date", "body" };
//        String selection = UNREAD_CONDITION + " and date>0 and body is not null and body != ''";
//        String[] selectionArgs = null;
//        final String sortOrder = "date ASC";
//
//        // Create cursor
//        Cursor cursor = context.getContentResolver().query(
//                SMS_INBOX_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder);
//
//        long messageId;
//        long threadId;
//        String address;
//        long timestamp;
//        String body;
//        SmsMmsMessage message;
//
//        if (cursor != null) {
//            try {
//                int count = cursor.getCount();
//                if (count > 0) {
//                    messages = new ArrayList<SmsMmsMessage>(count);
//                    while (cursor.moveToNext()) {
//                        messageId = cursor.getLong(0);
//                        threadId = cursor.getLong(1);
//                        address = cursor.getString(2);
//                        timestamp = cursor.getLong(3);
//                        body = cursor.getString(4);
//
//                        if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(body)
//                                && timestamp > 0) {
//                            message = new SmsMmsMessage(
//                                    context, address, body, timestamp, threadId,
//                                    count, messageId, SmsMmsMessage.MESSAGE_TYPE_SMS);
//                            message.setNotify(false);
//                            messages.add(message);
//                        }
//                    }
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//        return messages;
//    }

//    public static ArrayList<SmsMmsMessage> getUnreadMms(Context context) {
//        ArrayList<SmsMmsMessage> messages = null;
//
//        final String[] projection = new String[] { "_id", "thread_id", "date", "sub", "sub_cs" };
//        String selection = UNREAD_CONDITION;
//        String[] selectionArgs = null;
//        final String sortOrder = "date ASC";
//        int count = 0;
//
////        if (ignoreThreadId > 0) {
////            selection += " and thread_id != ?";
////            selectionArgs = new String[] { String.valueOf(ignoreThreadId) };
////        }
//
//        Cursor cursor = context.getContentResolver().query(
//                MMS_INBOX_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder);
//
//        SmsMmsMessage message;
//        if (cursor != null) {
//            try {
//                count = cursor.getCount();
//                if (count > 0) {
//                    messages = new ArrayList<SmsMmsMessage>(count);
//                    while (cursor.moveToNext()) {
//                        long messageId = cursor.getLong(0);
//                        long threadId = cursor.getLong(1);
//                        long timestamp = cursor.getLong(2) * 1000;
//                        String subject = cursor.getString(3);
//
//                        message = new SmsMmsMessage(context, messageId, threadId, timestamp,
//                                subject, count, SmsMmsMessage.MESSAGE_TYPE_MMS);
//                        message.setNotify(false);
//                        messages.add(message);
//                    }
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//        return messages;
//    }

//    private static HashSet<Long> getLocalReadMessages(Context context) {
//        HashSet<Long> messageIds = null;
//        Cursor cursor = null;
//
//        try {
//            String[] projection = new String[] { Messages._ID };
//            String selection = Messages.READ + "=1";
//            cursor = context.getContentResolver().query(Messages.CONTENT_URI, projection, selection,
//                    null, null);
//            if (cursor != null) {
//                int count = cursor.getCount();
//                if (count > 0) {
//                    messageIds = new HashSet<Long>(count);
//                    while (cursor.moveToNext()) {
//                        messageIds.add(cursor.getLong(0));
//                    }
//                }
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return messageIds;
//    }

//    private static boolean removeLocalReadMessages(ArrayList<SmsMmsMessage> messages,
//                                                   HashSet<Long> readIds) {
//        if (messages == null || readIds == null) {
//            return false;
//        }
//
//        Iterator<SmsMmsMessage> iterator = messages.iterator();
//        SmsMmsMessage message;
//        while (iterator.hasNext()) {
//            message = iterator.next();
//            if (readIds.contains(message.getMessageId())) {
//                iterator.remove();
//            }
//        }
//        return true;
//    }
    /**
     * Return current unread message count from system db (sms and mms)
     *
     * @param context
     * @return unread sms+mms message count
     */
    public static int getUnreadMessagesCount(Context context) {
//        final ArrayList<SmsMmsMessage> messages = getUnreadMessages(context);
//        if (messages != null) {
//            return messages.size();
//        }
        return 0;
    }

    /**
     *
     * @param context
//     * @param ignoreThreadId
     * @return
     */
//    public static SmsMmsMessage getMmsDetails(Context context, long ignoreThreadId) {
//
//        final String[] projection = new String[] { "_id", "thread_id", "date", "sub", "sub_cs" };
//        String selection = UNREAD_CONDITION;
//        String[] selectionArgs = null;
//        final String sortOrder = "date DESC";
//        int count = 0;
//
//        if (ignoreThreadId > 0) {
//            selection += " and thread_id != ?";
//            selectionArgs = new String[] { String.valueOf(ignoreThreadId) };
//        }
//
//        Cursor cursor = context.getContentResolver().query(
//                MMS_INBOX_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder);
//
//        if (cursor != null) {
//            try {
//                count = cursor.getCount();
//                if (count > 0) {
//                    cursor.moveToFirst();
//                    long messageId = cursor.getLong(0);
//                    long threadId = cursor.getLong(1);
//                    long timestamp = cursor.getLong(2) * 1000;
//                    String subject = cursor.getString(3);
//
//                    return new SmsMmsMessage(context, messageId, threadId, timestamp,
//                            subject, count, SmsMmsMessage.MESSAGE_TYPE_MMS);
//                }
//
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//        return null;
//    }
//
//    public static SmsMmsMessage getMmsDetails(Context context) {
//        return getMmsDetails(context, 0);
//    }

    public static String getMmsAddress(Context context, long messageId) {
        final String[] projection = new String[] { "address", "contact_id", "charset", "type" };
//        final String selection = "type=137"; // "type="+ PduHeaders.FROM,
        final String selection = null;

        Uri.Builder builder = MMS_CONTENT_URI.buildUpon();
        builder.appendPath(String.valueOf(messageId)).appendPath("addr");

        Cursor cursor = context.getContentResolver().query(
                builder.build(),
                projection,
                selection,
                null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Apparently contact_id is always empty in this table so we can't get it from
                    // here

                    // Just return the address
                    return cursor.getString(0);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return context.getString(android.R.string.unknownName);
    }

    public static final Pattern NAME_ADDR_EMAIL_PATTERN =
            Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");

    public static final Pattern QUOTED_STRING_PATTERN =
            Pattern.compile("\\s*\"([^\"]*)\"\\s*");

    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static boolean isEmailAddress(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    private static String extractAddrSpec(String address) {
        Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);

        if (match.matches()) {
            return match.group(2);
        }
        return address;
    }

    private static String getEmailDisplayName(String displayString) {
        Matcher match = QUOTED_STRING_PATTERN.matcher(displayString);
        if (match.matches()) {
            return match.group(1);
        }
        return displayString;
    }
}