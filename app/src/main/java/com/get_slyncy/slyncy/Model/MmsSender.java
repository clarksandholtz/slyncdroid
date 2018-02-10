/*
 * Created by nsshurtz on 2/7/18.
 *
 */

package com.get_slyncy.slyncy.Model;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.google.android.mms.ContentType;
import com.google.android.mms.MMSPart;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.smil.SmilHelper;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class MmsSender
{
    private static final String TAG = "MmsSender";
    private static final String TEXT_PART_FILENAME = "text_0.txt";

    public static final String EXTRA_NOTIFICATION_URL = "notification_url";

    private static final String ACTION_MMS_SENT = "com.example.android.apis.os.MMS_SENT_ACTION";
    private static final String ACTION_MMS_RECEIVED = "com.example.android.apis.os.MMS_RECEIVED_ACTION";
    private static final String sSmilText =
            "<smil>" +
                    "<head>" +
                    "<layout>" +
                    "<root-layout/>" +
                    "<region height=\"100%%\" id=\"Text\" left=\"0%%\" top=\"0%%\" width=\"100%%\"/>" +
                    "</layout>" +
                    "</head>" +
                    "<body>" +
                    "<par dur=\"8000ms\">" +
                    "<text src=\"%s\" region=\"Text\"/>" +
                    "</par>" +
                    "</body>" +
                    "</smil>";


    private static File mSendFile;
    private static Random mRandom = new Random();

    public static void sendMms(final CellMessage message, final Context context)
    {
        // Make the MMS file
        final String fileName = "send." + String.valueOf(Math.abs(mRandom.nextLong())) + ".dat";
        mSendFile = new File(context.getCacheDir(), fileName);

        // Making RPC call in non-UI thread
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable()
        {
            @Override
            public void run()
            {

                Log.d(TAG, "Building Mms...");
                List<MMSPart> parts = buildMmsParts(message.getParts(), message.getImages(), message.getText());
                final byte[] pdu = getBytes(context, message.getRecipients(), message.getSubject(), parts);

                Uri writerUri = (new Uri.Builder())
                        .authority(context.getPackageName() + ".MmsFileProvider")
                        .path(fileName)
                        .scheme(ContentResolver.SCHEME_CONTENT)
                        .build();

                final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, 0, new Intent(ACTION_MMS_SENT), 0);
                FileOutputStream writer = null;
                Uri contentUri = null;
                try
                {
                    writer = new FileOutputStream(mSendFile);
                    writer.write(pdu);
                    contentUri = writerUri;
                }
                catch (final IOException e)
                {
                    Log.e(TAG, "Error writing send file", e);
                }
                finally
                {
                    if (writer != null)
                    {
                        try
                        {
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                if (contentUri != null)
                {
                    SmsManager.getDefault().sendMultimediaMessage(context,
                            contentUri, null/*locationUrl*/, null/*configOverrides*/,
                            pendingIntent);
                    Log.d(TAG, "Message sent.");
                }
                else
                {
                    Log.e(TAG, "Error sending Mms");
                    try
                    {
                        pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR);
                    }
                    catch (PendingIntent.CanceledException ex)
                    {
                        Log.e(TAG, "Mms pending intent cancelled?", ex);
                    }
                }
            }
        });
    }

    private static List<MMSPart> buildMmsParts(List<Message.Part> parts, Bitmap[] images, String text) {
        ArrayList<MMSPart> data = new ArrayList<>();

        for (Bitmap image : images) {
            // turn bitmap into byte array to be stored
            byte[] imageBytes = MmsUtils.bitmapToByteArray(image);
            // TODO: Fix image compression. Message size should be under 300kb.
//            byte[] imageBytes = MmsUtils.compressImage(images[i]);

            MMSPart part = new MMSPart();
            part.MimeType = "image/jpeg";
            part.Name = "image_" + System.currentTimeMillis();
            part.Data = imageBytes;
            data.add(part);
        }

        // add any extra media according to their mimeType set in the message
        //      eg. videos, audio, contact cards, location maybe?
        if (parts != null) {
            for (Message.Part p : parts) {
                MMSPart part = new MMSPart();
                if (p.getName() != null) {
                    part.Name = p.getName();
                } else {
                    part.Name = p.getContentType().split("/")[0];
                }
                part.MimeType = p.getContentType();
                part.Data = p.getMedia();
                data.add(part);
            }
        }

        if (text != null && !text.equals("")) {
            // add text to the end of the part and send
            MMSPart part = new MMSPart();
            part.Name = "text";
            part.MimeType = "text/plain";
            part.Data = text.getBytes();
            data.add(part);
        }

        return data;
    }

    private static byte[] getBytes(Context context, String[] recipients, String subject, List<MMSPart> parts) {

        final SendReq sendRequest = new SendReq();

        // create send request addresses
        for (String recipient : recipients) {
            final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipient);

            if (phoneNumbers != null && phoneNumbers.length > 0) {
                sendRequest.addTo(phoneNumbers[0]);
            }
        }

        if (subject != null) {
            sendRequest.setSubject(new EncodedStringValue(subject));
        }

        sendRequest.setDate(Calendar.getInstance().getTimeInMillis() / 1000L);

        try {
            sendRequest.setFrom(new EncodedStringValue(Utils.getMyPhoneNumber(context)));
        } catch (Exception e) {
            Log.e(TAG, "error getting from address", e);
        }

        final PduBody pduBody = new PduBody();

        // assign parts to the pdu body which contains sending data
        long size = 0;
        if (parts != null) {
            for (int i = 0; i < parts.size(); i++) {
                MMSPart part = parts.get(i);
                if (part != null) {
                    try {
                        PduPart partPdu = new PduPart();
                        partPdu.setName(part.Name.getBytes());
                        partPdu.setContentType(part.MimeType.getBytes());

                        if (part.MimeType.startsWith("text")) {
                            partPdu.setCharset(CharacterSets.UTF_8);
                        }
                        // Set Content-Location.
                        partPdu.setContentLocation(part.Name.getBytes());
                        int index = part.Name.lastIndexOf(".");
                        String contentId = (index == -1) ? part.Name
                                : part.Name.substring(0, index);
                        partPdu.setContentId(contentId.getBytes());
                        partPdu.setData(part.Data);

                        pduBody.addPart(partPdu);
                        size += ((2 * part.Name.getBytes().length) + part.MimeType.getBytes().length + part.Data.length + contentId.getBytes().length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(pduBody), out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(out.toByteArray());
        pduBody.addPart(0, smilPart);

        sendRequest.setBody(pduBody);

        Log.v(TAG, "setting message size to " + size + " bytes");
        sendRequest.setMessageSize(size);

        // add everything else that could be set
//        sendRequest.setPriority(PduHeaders.PRIORITY_NORMAL);
//        sendRequest.setDeliveryReport(PduHeaders.VALUE_NO);
//        sendRequest.setExpiry(1000 * 60 * 60 * 24 * 7);
//        sendRequest.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
//        sendRequest.setReadReport(PduHeaders.VALUE_NO);

        // create byte array which will actually be sent
        final PduComposer composer = new PduComposer(context, sendRequest);
        byte[] bytesToSend = null;

        try {
            bytesToSend = composer.make();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return bytesToSend;
    }
}
