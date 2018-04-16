package com.get_slyncy.slyncy.Model.CellMessaging;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.get_slyncy.slyncy.Model.DTO.Contact;
import com.get_slyncy.slyncy.Model.DTO.SlyncyImage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.Model.Util.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by tylerbowers on 2/26/18.
 */

public class MessageDbUtility
{

    private static final String TAG = "MessageDbUtility";
    private static final int MAX_MESSAGES_PER_THREAD = 5;
    private static final int MAX_THREADS = 10;
    private static final int ADDTL_MMS_THREADS = 5;
    private static final int MAX_IMGS_PER_THREAD = 5;
    private static final String MSG_ID = "_id";
    private static final String THREAD_ID = "thread_id";
    private static final String DATE = "date";
    private static final String MMS_TYPE = "ct";
    private static final String TYPE = "type";
    private static final String READ = "read";
    private static final String TEXT = "text";
    private static final String ADDRESS = "address";
    private static final String BODY = "body";
    private static final int SENDER = 137;
    private static MessageDbUtility instance;
    private static boolean isGettingMessages;
    private Context mContext;
//    private static ContentResolver mResolver;
    private Map<Integer, SlyncyMessageThread> mThreadList;

    private MessageDbUtility()
    {
        mThreadList = new HashMap<>();
    }

    public static void init(Context context)
    {
        instance = getInstance();

        instance.mContext = context;
//        mResolver = context.getContentResolver();
        new Thread(new Runnable()
        {
            public void run()
            {
                instance.getMessagesBulk();
                instance.mThreadList = new HashMap<>();
            }
        }, "MessageDbUtility.init").start();
    }

    public static void getMessagesBulk(final Context context)
    {
        instance = getInstance();

        instance.mContext = context;
//        mResolver = context.getContentResolver();
        new Thread(new Runnable()
        {
            public void run()
            {
                instance.getMessagesBulk();
                instance.mThreadList = new HashMap<>();

                ClientCommunicator.bulkMessageUpload(LocalBroadcastManager.getInstance(context), context);
            }
        }, "MessageDbUtility.init").start();
    }

    private static MessageDbUtility getInstance()
    {
        if (instance == null)
        {
            instance = new MessageDbUtility();
        }

        return instance;
    }

    public static void parseMmsParts(SlyncyMessage message, ContentResolver resolver,
                                     Map<Integer, SlyncyMessageThread> threadList)
    {
        if (threadList == null)
        {
            threadList = new HashMap<>();
            SlyncyMessageThread thread = new SlyncyMessageThread();
            thread.addMessage(message);
            threadList.put(message.getThreadId(), thread);
        }
        Uri uri = Uri.parse("content://mms/part");
        String mmsId = "mid = " + message.getId();
        Cursor c = resolver.query(uri, null, mmsId, null, null);
        StringBuilder body = new StringBuilder();
        if (c == null)
        {
            return;
        }
        while (c.moveToNext())
        {

            String pid = c.getString(c.getColumnIndex(MSG_ID));
            String type = c.getString(c.getColumnIndex(MMS_TYPE));
            if ("text/plain".equals(type))
            {
                body.append((c.getString(c.getColumnIndex(TEXT))));
            }
            else if (type.contains("image"))
            {
                if (threadList.get(message.getThreadId()).getImageCount() < MAX_IMGS_PER_THREAD)
                {
                    SlyncyImage image = getMmsImg(pid, resolver);
                    if (image != null)
                        message.addImage(image);
                    threadList.get(message.getThreadId()).incrementImageCount();
                }
                else
                {
                    Log.d(TAG, "Skipping image, thread full");
                }
            }
        }
        c.close();
        message.setBody(body.toString());
    }

    public static SlyncyImage getMmsImg(String id, ContentResolver resolver)
    {
        Uri uri = Uri.parse("content://mms/part/" + id);
        InputStream in = null;
        Bitmap bitmap = null;
        byte[] bytes = null;

        try
        {
            in = resolver.openInputStream(uri);
            byte[] buf = new byte[1024];
            int len = 0;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while ((len = in.read(buf)) != -1)
            {
                os.write(buf, 0, len);
            }
            bytes = os.toByteArray();
            if (in != null)
                in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        Cursor cursor = resolver.query(Uri.parse("content://mms/part/" + id), null, null, null, null);
        if (cursor != null && cursor.moveToFirst())
        {
            String fullName = cursor.getString(cursor.getColumnIndex("name"));
            String type;
            if (fullName == null || fullName.matches("image_(\\p{Digit})+"))
            {
                String[] temp = cursor.getString(cursor.getColumnIndex("ct")).split("/");
                type = "." + temp[temp.length - 1];
            }
            else
            {
                type = "." + fullName.split("\\.")[1];
            }
            cursor.close();
            return new SlyncyImage(UUID.randomUUID().toString() + type,
                    android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT).replace("\n", ""));
        }
        return null;

    }

    public static List<String> getMmsAddresses(SlyncyMessage message, Cursor c)
    {
//        String id = message.getId();
//        String sel = new String("msg_id=" + id);
//        String uriString = "content://mms/" + id + "/addr";
//        Uri uri = Uri.parse(uriString);
//        Cursor c = resolver.query(uri, null, sel, null, null);
        List<String> numberList = new ArrayList<>();
        while (c.moveToNext())
        {

            String number = c.getString(c.getColumnIndex(ADDRESS));
            if (!(number.contains("insert")))
            {
                numberList.add(number);
            }

            int type = c.getInt(c.getColumnIndex(TYPE));
            if (type == SENDER)
            {
                message.setSender(number);
            }
        }
        c.close();
        return numberList;
    }

    public static Map<String, String> fetchPhoneContacts(ContentResolver cr)
    {
        Map<String, String> contactMap = new HashMap<>();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0)
        {
            while (cur != null && cur.moveToNext())
            {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext())
                    {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        Log.i(TAG, "Name: " + name);
//                        Log.i(TAG, "Phone Number: " + phoneNo);
                        contactMap.put(phoneNo, name);
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null)
        {
            cur.close();
        }

        return contactMap;
    }

    public static String fetchContactNameByNumber(String number, ContentResolver rs)
    {
        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = rs.query(contactUri, projection, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.v(TAG, "Contact found for " + number + ". Contact name: " + name);
            }
            else
            {
                Log.v(TAG, "No Contact found for " + number);
                name = number;
            }
            cursor.close();
        }

        return name;
    }

    private void getMessagesBulk()
    {
        // TODO: Do we need to prevent against multiple concurrent requests?
//        if (isGettingMessages) {
//            Log.d(TAG, "Already loading messages.");
//            return;
//        }
        isGettingMessages = true;
        Log.i(TAG, "Loading messages...");
        long startTime = System.nanoTime();
        getSMS();
        getMMS();
        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1000000000.0;
        Log.i(TAG, "Done loading messages. Took " + elapsedTime + " seconds");

        Log.i(TAG, "Attaching contacts to messages...");
        startTime = System.nanoTime();
        getContacts();
        endTime = System.nanoTime();
        elapsedTime = (endTime - startTime) / 1000000000.0;
        Log.i(TAG, "Done attaching contacts. Took " + elapsedTime + " seconds");

        Data.getInstance().setmMessages(mThreadList);
        isGettingMessages = false;
    }

    private void getMMS()
    {
        Uri uri = Uri.parse("content://mms");
        String[] proj = {"*"};
        ContentResolver cr = mContext.getContentResolver();

        Cursor c = cr.query(uri, proj, null, null, null);

        if (c != null && c.moveToFirst())
        {
            do
            {

                boolean makeMessage = false;
                int threadId = c.getInt(c.getColumnIndex(THREAD_ID));
                if (mThreadList.get(threadId) == null && mThreadList.size() < MAX_THREADS + ADDTL_MMS_THREADS)
                {
                    SlyncyMessageThread thread = new SlyncyMessageThread();
                    thread.setThreadId(threadId);
                    mThreadList.put(threadId, thread);

                    makeMessage = true;
                }
                else if (mThreadList.get(threadId) != null
                         && mThreadList.get(threadId).getMessageCount() < MAX_MESSAGES_PER_THREAD + ADDTL_MMS_THREADS)
                {
                    makeMessage = true;
                }

                if (makeMessage)
                {
                    ///*
                    SlyncyMessage message = getMmsMessage(c, mContext.getContentResolver());
                    /*new SlyncyMessage();
                    message.setId(c.getString(c.getColumnIndex(MSG_ID)));
                    message.setThreadId(threadId);
                    message.setDate(c.getLong(c.getColumnIndex(DATE)));
                    int readStatus = c.getInt(c.getColumnIndex(READ));
                    if (readStatus == 1) message.setRead(true);
                    else message.setRead(false);
                    List<String> numbers = getMmsAddresses(message);
                    message.setNumbers(numbers);//*/
                    mThreadList.get(threadId).addMessage(message);
                    mThreadList.get(threadId).setNumbers(message.getNumbers());//*/
//                    parseMmsParts(message);
                }

            } while (c.moveToNext());
        }

        c.close();

    }

    private void parseMmsParts(SlyncyMessage message)
    {
        parseMmsParts(message, mContext.getContentResolver(), mThreadList);
    }

    private SlyncyImage getMmsImg(String id)
    {
        return getMmsImg(id, mContext.getContentResolver());
    }

    private List<String> getMmsAddresses(SlyncyMessage message)
    {
        String id = message.getId();
        String sel = "msg_id=" + id;
        String uriString = "content://mms/" + id + "/addr";
        Uri uri = Uri.parse(uriString);
        Cursor c = mContext.getContentResolver().query(uri, null, sel, null, null);
        return getMmsAddresses(message, c);
    }

    private void getSMS()
    {
        Uri uri = Uri.parse("content://sms");
        String[] proj = {"*"};
        ContentResolver cr = mContext.getContentResolver();

        Cursor c = cr.query(uri, proj, null, null, null);

        if (c.moveToFirst())
        {
            do
            {

                boolean makeMessage = false;
                int threadId = c.getInt(c.getColumnIndex(THREAD_ID));
                if (mThreadList.get(threadId) == null && mThreadList.size() < MAX_THREADS)
                {
                    SlyncyMessageThread thread = new SlyncyMessageThread();
                    thread.setThreadId(threadId);
                    mThreadList.put(threadId, thread);

                    makeMessage = true;
                }
                else if (mThreadList.get(threadId) != null
                         && mThreadList.get(threadId).getMessageCount() < MAX_MESSAGES_PER_THREAD)
                {
                    makeMessage = true;
                }

                if (makeMessage)
                {
                    SlyncyMessage message = getSmsMessage(c);
                    message.setThreadId(threadId);
                    mThreadList.get(threadId).addMessage(message);
                    mThreadList.get(threadId).setNumbers(message.getNumbers());
                }

            } while (c.moveToNext());
        }
        c.close();
    }

    public static SlyncyMessage getMmsMessage(Cursor c, ContentResolver resolver)
    {
        SlyncyMessage message = new SlyncyMessage();
        message.setId(c.getString(c.getColumnIndex(MSG_ID)));
        String id = message.getId();
        String sel = "msg_id=" + id;
        String uriString = "content://mms/" + id + "/addr";
        Uri uri = Uri.parse(uriString);
        Cursor addrC = resolver.query(uri, null, sel, null, null);
        message.setThreadId(c.getInt(c.getColumnIndex("thread_id")));
        message.setDate(c.getLong(c.getColumnIndex(DATE)));
        int readStatus = c.getInt(c.getColumnIndex(READ));
        if (readStatus == 1) message.setRead(true);
        else message.setRead(false);
        List<String> numbers = getMmsAddresses(message, addrC);
        message.setNumbers(numbers);
        parseMmsParts(message, resolver, null);
//        mThreadList.get(threadId).addMessage(message);
//        mThreadList.get(threadId).setNumbers(numbers);
        return message;
    }

    public static SlyncyMessage getSmsMessage(Cursor c)
    {
        SlyncyMessage message = new SlyncyMessage();
        message.setThreadId(c.getInt(c.getColumnIndex("thread_id")));
        message.setId(c.getString(c.getColumnIndex(MSG_ID)));
        message.setDate(c.getLong(c.getColumnIndex(DATE)));
        message.setBody(c.getString(c.getColumnIndex(BODY)));
        int readStatus = c.getInt(c.getColumnIndex("read"));
        message.setRead(readStatus == 1);
        ArrayList<String> numbers = new ArrayList<>();
        String number = c.getString(c.getColumnIndex(ADDRESS));
        numbers.add(number);
        message.setNumbers(numbers);
        int type = c.getInt(c.getColumnIndex(TYPE));
        if (type == 2) message.setSender(Data.getInstance().getSettings().getmMyPhoneNumber());
        if (type == 1) message.setSender(number);
        message.setUserSent(type == 2);
        return message;
    }

    private void getContacts()
    {

        Map<String, String> contactMap = fetchPhoneContacts();

        for (Map.Entry<Integer, SlyncyMessageThread> threadIter : mThreadList.entrySet())
        {

            for (String number : threadIter.getValue().getNumbers())
            {

                // don't add your own phone number
                if (number.substring(2).equals(Data.getInstance().getSettings().getmMyPhoneNumber())
                    || number.equals(Data.getInstance().getSettings().getmMyPhoneNumber()))
                {
                    continue;
                }

                Contact contact = new Contact(number);

                if (contactMap.containsKey(number))
                {
                    contact.setName(contactMap.get(number));
                }
                else
                {
                    contact.setName(fetchContactNameByNumber(number));
                }

                threadIter.getValue().addContact(contact);
            }
            threadIter.getValue().setNumbers(null);
        }
    }

    private Map<String, String> fetchPhoneContacts()
    {
        return fetchPhoneContacts(mContext.getContentResolver());
    }

    public String fetchContactNameByNumber(String number)
    {
        return fetchContactNameByNumber(number, mContext.getContentResolver());
    }

}
