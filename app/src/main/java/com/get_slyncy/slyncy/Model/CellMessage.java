package com.get_slyncy.slyncy.Model;

import android.graphics.Bitmap;

import com.klinker.android.send_message.Message;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class CellMessage extends Message implements IMessage {

    private String mMessageId;
    private String mSender;
    private String mRecipient;
    private String mBody;
    private int mDirection;
    private MESSAGE_STATUS mStatus;

    private Bitmap mImage;

    public CellMessage() {
    }

    @Override
    public String getSender() {
        return mSender;
    }

    @Override
    public String getRecipient() {
        return mRecipient;
    }

    @Override
    public void setSender(String sender) {
        mSender = sender;
    }

    @Override
    public void setRecipient(String recipient) {
        mRecipient = recipient;
    }

    @Override
    public boolean hasBody() {
        return mBody != null && mBody.length() > 0;
    }

    @Override
    public String getBody() {
        return mBody;
    }

    @Override
    public boolean hasImageAttachment() {
        return mImage != null;
    }

    @Override
    public Bitmap getImageAttachment() {
        return mImage;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) {
        this.mDirection = direction;
    }

    public MESSAGE_STATUS getStatus() {
        return mStatus;
    }

    public void setStatus(MESSAGE_STATUS Status) {
        this.mStatus = mStatus;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String MessageId) {
        this.mMessageId = mMessageId;
    }
    
    // ******************** INCOMING INIT METHODS ******************************** //
    // TODO: Group Messaging. Commented out

    public static CellMessage newIncomingMessage(String text, String address) {
        CellMessage msg = new CellMessage(text, address);
        msg.setDirection(CellMessage.INCOMING);
        msg.setSender(address);
        return msg;
    }

    public static CellMessage newIncomingMessage(String text, String address, String subject) {
        CellMessage msg = new CellMessage(text, address, subject);
        msg.setDirection(CellMessage.INCOMING);
        msg.setSender(address);
        return msg;
    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses) {
//        CellMessage msg = new CellMessage(text, addresses);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, subject);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

    public static CellMessage newIncomingMessage(String text, String address, Bitmap image) {
        CellMessage msg = new CellMessage(text, address, image);
        msg.setDirection(CellMessage.INCOMING);
        msg.setSender(address);
        return msg;
    }

    public static CellMessage newIncomingMessage(String text, String address, Bitmap image, String subject) {
        CellMessage msg = new CellMessage(text, address, image, subject);
        msg.setDirection(CellMessage.INCOMING);
        msg.setSender(address);
        return msg;
    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses, Bitmap image) {
//        CellMessage msg = new CellMessage(text, addresses, image);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses, Bitmap image, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, image, subject);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

    public static CellMessage newIncomingMessage(String text, String address, Bitmap[] images) {
        CellMessage msg = new CellMessage(text, address, images);
        msg.setDirection(CellMessage.INCOMING);
        msg.setSender(address);
        return msg;
    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses, Bitmap[] images) {
//        CellMessage msg = new CellMessage(text, addresses, images);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

//    public static CellMessage newIncomingMessage(String text, String[] addresses, Bitmap[] images, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, images, subject);
//        msg.setDirection(CellMessage.INCOMING);
//        return msg;
//    }

    // ******************** OUTGOING INIT METHODS ******************************** //

    public static CellMessage newOutgoingMessage(String text, String address) {
        CellMessage msg = new CellMessage(text, address);
        msg.setDirection(CellMessage.OUTGOING);
        msg.setRecipient(address);
        return msg;
    }

    public static CellMessage newOutgoingMessage(String text, String address, String subject) {
        CellMessage msg = new CellMessage(text, address, subject);
        msg.setDirection(CellMessage.OUTGOING);
        msg.setRecipient(address);
        return msg;
    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses) {
//        CellMessage msg = new CellMessage(text, addresses);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, subject);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

    public static CellMessage newOutgoingMessage(String text, String address, Bitmap image) {
        CellMessage msg = new CellMessage(text, address, image);
        msg.setDirection(CellMessage.OUTGOING);
        msg.setRecipient(address);
        return msg;
    }

    public static CellMessage newOutgoingMessage(String text, String address, Bitmap image, String subject) {
        CellMessage msg = new CellMessage(text, address, image, subject);
        msg.setDirection(CellMessage.OUTGOING);
        msg.setRecipient(address);
        return msg;
    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses, Bitmap image) {
//        CellMessage msg = new CellMessage(text, addresses, image);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses, Bitmap image, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, image, subject);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

    public static CellMessage newOutgoingMessage(String text, String address, Bitmap[] images) {
        CellMessage msg = new CellMessage(text, address, images);
        msg.setDirection(CellMessage.OUTGOING);
        msg.setRecipient(address);
        return msg;
    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses, Bitmap[] images) {
//        CellMessage msg = new CellMessage(text, addresses, images);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

//    public static CellMessage newOutgoingMessage(String text, String[] addresses, Bitmap[] images, String subject) {
//        CellMessage msg = new CellMessage(text, addresses, images, subject);
//        msg.setDirection(CellMessage.OUTGOING);
//        return msg;
//    }

    public CellMessage(String text, String address) {
        super(text, address);
    }

    public CellMessage(String text, String address, String subject) {
        super(text, address, subject);
    }

    public CellMessage(String text, String[] addresses) {
        super(text, addresses);
    }

    public CellMessage(String text, String[] addresses, String subject) {
        super(text, addresses, subject);
    }

    public CellMessage(String text, String address, Bitmap image) {
        super(text, address, image);
    }

    public CellMessage(String text, String address, Bitmap image, String subject) {
        super(text, address, image, subject);
    }

    public CellMessage(String text, String[] addresses, Bitmap image) {
        super(text, addresses, image);
    }

    public CellMessage(String text, String[] addresses, Bitmap image, String subject) {
        super(text, addresses, image, subject);
    }

    public CellMessage(String text, String address, Bitmap[] images) {
        super(text, address, images);
    }

    public CellMessage(String text, String address, Bitmap[] images, String subject) {
        super(text, address, images, subject);
    }

    public CellMessage(String text, String[] addresses, Bitmap[] images) {
        super(text, addresses, images);
    }

    public CellMessage(String text, String[] addresses, Bitmap[] images, String subject) {
        super(text, addresses, images, subject);
    }

    public static final int OUTGOING = 1;
    public static final int INCOMING = 0;
}
