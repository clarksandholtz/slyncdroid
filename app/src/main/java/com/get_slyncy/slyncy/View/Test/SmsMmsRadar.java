package com.get_slyncy.slyncy.View.Test;

/**
 * Created by undermark5 on 3/12/18.
 */


import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;

public class SmsMmsRadar extends Service
{

    String substr;
    int k;
    private static HashSet<String> inCommingMms;
    private static HashSet<String> outGoingMms;
    private static HashSet<String> inCommingSms;
    private static HashSet<String> outGoingSms;
    private ContentResolver contentResolver;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.v("Debug", " service created.........");
    }

    public void registerObserver()
    {

        contentResolver = getContentResolver();
        inCommingMms = new HashSet<>();
        outGoingMms = new HashSet<>();
        inCommingSms = new HashSet<>();
        outGoingSms = new HashSet<>();
        contentResolver.registerContentObserver(Uri.parse("content://mms-sms/conversations/"),
                true, new SMSObserver(new Handler()));
        Log.v("Debug", " in registerObserver method.........");
    }

    //start the service and register observer for lifetime
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v("Debug", "Service has been started..");
        Toast.makeText(getApplicationContext(),
                "Service has been started.. ",
                Toast.LENGTH_SHORT).show();
        registerObserver();

        return START_STICKY;
    }

    class SMSObserver extends ContentObserver
    {

        public SMSObserver(Handler handler)
        {
            super(handler);
        }

        //will be called when database get change
        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            Log.v("Debug", "Observer has been started..");

  /*first of all we need to decide message is Text or MMS type.*/
            final String[] projection = new String[]{
                    "_id", "ct_t"};
            Uri mainUri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI;
            Cursor mainCursor = contentResolver.
                    query(mainUri, projection,
                            null, null, "normalized_date desc");
            mainCursor.moveToFirst();


            String msgContentType = mainCursor.getString(mainCursor.
                    getColumnIndex("ct_t"));
            String msgId = mainCursor.getString(mainCursor.getColumnIndex("_id"));
            if (msgContentType != null)
            {
                // it's MMS
                Log.v("Debug", "it's MMS");

                //now we need to decide MMS message is sent or received
                Uri mUri = Uri.parse("content://mms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("msg_box"));

                if (type == 1)
                {
                    if (inCommingMms.contains(msgId))
                    {
                        //look if read, if so, notify server
                    }
                    else
                    {

                    }
                    //it's received MMS
                    Log.v("Debug", "it's received MMS");
                    getReceivedMMSinfo();
                }
                else if (type == 2)
                {
                    //it's sent MMS
                    Log.v("Debug", "it's Sent MMS");
                    getSentMMSinfo();
                }

            }
            else
            {
                // it's SMS
                Log.v("Debug", "it's SMS");

                //now we need to decide SMS message is sent or received
                Uri mUri = Uri.parse("content://sms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("type"));

                if (type == 1)
                {
                    //it's received SMS
                    Log.v("Debug", "it's received SMS");
                    getReceivedSMSinfo();
                }
                else if (type == 2)
                {
                    //it's sent SMS
                    Log.v("Debug", "it's sent SMS");
                    getSentSMSinfo();
                }
            }//message content type block closed


        }//on changed closed


 /*now Methods start to getting details for sent-received SMS*/


        //method to get details about received SMS..........
        private void getReceivedSMSinfo()
        {
            Uri uri = Uri.parse("content://sms/inbox");
            String str = "";
            Cursor cursor = contentResolver.query(uri, null,
                    null, null, null);
            cursor.moveToNext();

            // 1 = Received, etc.
            int type = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String msg_id = cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            String body = cursor.getString(cursor.
                    getColumnIndex("body"));
            Date date = new Date(Long.valueOf(dateVal));

            str = "Received SMS: \n phone is: " + phone;
            str += "\n SMS type is: " + type;
            str += "\n SMS time stamp is:" + date;
            str += "\n SMS body is: " + body;
            str += "\n id is : " + msg_id;


            Log.v("Debug", "Received SMS phone is: " + phone);
            Log.v("Debug", "SMS type is: " + type);
            Log.v("Debug", "SMS time stamp is:" + date);
            Log.v("Debug", "SMS body is: " + body);
            Log.v("Debug", "SMS id is: " + msg_id);

            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about SMS Received Message :) ");

        }

        //method to get details about Sent SMS...........
        private void getSentSMSinfo()
        {
            Uri uri = Uri.parse("content://sms/sent");
            String str = "";
            Cursor cursor = contentResolver.query(uri, null,
                    null, null, null);
            cursor.moveToNext();

            // 2 = sent, etc.
            int type = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String msg_id = cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            String body = cursor.getString(cursor.
                    getColumnIndex("body"));
            Date date = new Date(Long.valueOf(dateVal));

            str = "Sent SMS: \n phone is: " + phone;
            str += "\n SMS type is: " + type;
            str += "\n SMS time stamp is:" + date;
            str += "\n SMS body is: " + body;
            str += "\n id is : " + msg_id;


            Log.v("Debug", "sent SMS phone is: " + phone);
            Log.v("Debug", "SMS type is: " + type);
            Log.v("Debug", "SMS time stamp is:" + date);
            Log.v("Debug", "SMS body is: " + body);
            Log.v("Debug", "SMS id is: " + msg_id);

            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Sent SMS Message :) ");
        }


  /*now Methods start to getting details for sent-received MMS.*/

        // 1. method to get details about Received (inbox)  MMS...
        private void getReceivedMMSinfo()
        {
            SlyncyMessage message = new SlyncyMessage();
            Uri uri = Uri.parse("content://mms/inbox");
            String str = "";
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToNext();

            String mms_id = cursor.getString(cursor.getColumnIndex("_id"));
            message.setId(mms_id);
            message.setNumbers(MessageDbUtility.getMmsAddresses(message, getContentResolver()));
            message.setDate(cursor.getLong(cursor.getColumnIndex("date")) * 1000);
            message.setRead(cursor.getInt(cursor.getColumnIndex("read")) == 1);
            MessageDbUtility.parseMmsParts(message, getContentResolver(), null);
            // 2 = sent, etc.
            int mtype = cursor.getInt(cursor.
                    getColumnIndex("msg_box"));
            message.setUserSent(mtype == 2);

            Log.v("Debug", "sent MMS phone is: " + message.getSender());
            Log.v("Debug", "MMS type is: " + mtype);
            Log.v("Debug", "MMS time stamp is:" + message.getDate());
            Log.v("Debug", "MMS body is: " + message.getBody());
            Log.v("Debug", "MMS id is: " + message.getId());
            if (message.getImages().size() > 0)
            {
                Log.v("Debug", "MMS image name: " + message.getImages().get(0).getName());
            }
            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Received MMS Message :) ");
        }


        //method to get Text body from Received MMS.........
        private String getReceivedMmsText(String id)
        {
            Uri partURI = Uri.parse("content://mms/inbox" + id);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try
            {
                is = getContentResolver().openInputStream(partURI);
                if (is != null)
                {
                    InputStreamReader isr = new InputStreamReader(is,
                            "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null)
                    {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return sb.toString();
        }

        //method to get image from Received MMS..............
        private Bitmap getReceivedMmsImage(String id)
        {


            Uri partURI = Uri.parse("content://mms/inbox" + id);
            InputStream is = null;
            Bitmap bitmap = null;
            try
            {
                is = getContentResolver().
                        openInputStream(partURI);
                bitmap = BitmapFactory.decodeStream(is);
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return bitmap;

        }

        //Storing image on SD Card
        private void storeMmsImageOnSDcard(Bitmap bitmap)
        {
            try
            {

                substr = "A " + k + ".PNG";
                String extStorageDirectory = Environment.
                        getExternalStorageDirectory().toString();
                File file = new File(extStorageDirectory, substr);
                OutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG,
                        100, outStream);
                outStream.flush();
                outStream.close();

                Toast.makeText(getApplicationContext(), "SlyncyImage Saved",
                        Toast.LENGTH_LONG).show();
                Log.v("Debug", "SlyncyImage seved sucessfully");
            }
            catch (FileNotFoundException e)
            {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        e.toString(),
                        Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        e.toString(),
                        Toast.LENGTH_LONG).show();
            }
            k++;
        }


        /* .......methods to get details about Sent MMS.... */
        private void getSentMMSinfo()
        {

            SlyncyMessage message = new SlyncyMessage();
            Uri uri = Uri.parse("content://mms/sent");
            String str = "";
            Cursor cursor = getContentResolver().query(uri, null, null,null, null);
            cursor.moveToNext();

            String mms_id = cursor.getString(cursor.getColumnIndex("_id"));
            message.setId(mms_id);
            message.setNumbers(MessageDbUtility.getMmsAddresses(message, getContentResolver()));
            message.setDate(cursor.getLong(cursor.getColumnIndex("date")) * 1000);
            message.setRead(cursor.getInt(cursor.getColumnIndex("read")) == 1);
            MessageDbUtility.parseMmsParts(message, getContentResolver(), null);
            // 2 = sent, etc.
            int mtype = cursor.getInt(cursor.getColumnIndex("msg_box"));
            message.setUserSent(mtype == 2);

            str = "Sent MMS: \n phone is: " + message.getSender();
            str += "\n MMS type is: " + mtype;
            str += "\n MMS time stamp is:" + message.getDate();
            str += "\n MMS body is: " + message.getBody();
            str += "\n id is : " + mms_id;

            Log.v("Debug", "sent MMS phone is: " + message.getSender());
            Log.v("Debug", "MMS type is: " + mtype);
            Log.v("Debug", "MMS time stamp is:" + message.getDate());
            Log.v("Debug", "MMS body is: " + message.getBody());
            Log.v("Debug", "MMS id is: " + mms_id);
            if (message.getImages().size() > 0)
            {
                Log.v("Debug", "MMS image name: " + message.getImages().get(0).getName());
            }
            Toast.makeText(getBaseContext(), str,
                    Toast.LENGTH_SHORT).show();
            Log.v("Debug", "RDC : So we got all informaion " +
                           "about Sent MMS Message :) ");

        }


        //method to get Text body from Sent MMS............
        private String getSentMmsText(String id)
        {

            Uri partURI = Uri.parse("content://mms/sent" + id);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try
            {
                is = getContentResolver().openInputStream(partURI);
                if (is != null)
                {
                    InputStreamReader isr = new InputStreamReader(is,
                            "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null)
                    {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return sb.toString();

        }

        //method to get image from sent MMS............
        private Bitmap getSentMmsImage(String id)
        {

            Uri partURI = Uri.parse("content://mms/sent" + id);
            InputStream is = null;
            Bitmap bitmap = null;
            try
            {
                is = getContentResolver().
                        openInputStream(partURI);
                bitmap = BitmapFactory.decodeStream(is);
            }
            catch (IOException e)
            {
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return bitmap;

        }

    }//smsObserver class closed
}