/*
 * Created by nsshurtz on 2/7/18.
 *
 */

package com.get_slyncy.slyncy.Model;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MMSPart;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.smil.SmilHelper;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.klinker.android.send_message.Transaction.DEFAULT_EXPIRY_TIME;
import static com.klinker.android.send_message.Transaction.DEFAULT_PRIORITY;


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
    private File mDownloadFile;
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
                List<MMSPart> parts = buildMmsParts(context, message.getParts(), message.getImages(), message.getText());
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

    private static List<MMSPart> buildMmsParts(Context context, List<Message.Part> parts, Bitmap[] images, String text) {
        ArrayList<MMSPart> data = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            // turn bitmap into byte array to be stored
            byte[] imageBytes = Message.bitmapToByteArray(images[i]);
            // TODO: Fix image compression. Message size should be under 300kb.
//            byte[] imageBytes = compressImage(images[i]);

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

    private static byte[] compressImage(Bitmap original) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = original;

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
//        ExifInterface exif;
//        try {
//            exif = new ExifInterface(filePath);
//
//            int orientation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION, 0);
//            Log.d("EXIF", "Exif: " + orientation);
//            Matrix matrix = new Matrix();
//            if (orientation == 6) {
//                matrix.postRotate(90);
//                Log.d("EXIF", "Exif: " + orientation);
//            } else if (orientation == 3) {
//                matrix.postRotate(180);
//                Log.d("EXIF", "Exif: " + orientation);
//            } else if (orientation == 8) {
//                matrix.postRotate(270);
//                Log.d("EXIF", "Exif: " + orientation);
//            }
//            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
//                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
//                    true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        FileOutputStream out = null;
//        String filename = getFilename();
//        try {
//            out = new FileOutputStream(filename);
//
////          write the compressed bitmap at the destination specified by filename.
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        return Message.bitmapToByteArray(scaledBitmap);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static byte[] getBytes(Context context, String[] recipients, String subject, List<MMSPart> parts) {

        final SendReq sendRequest = new SendReq();

        // create send request addresses
        for (int i = 0; i < recipients.length; i++) {
            final EncodedStringValue[] phoneNumbers = EncodedStringValue.extract(recipients[i]);

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
