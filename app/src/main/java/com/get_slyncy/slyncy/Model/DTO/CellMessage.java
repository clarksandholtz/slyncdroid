package com.get_slyncy.slyncy.Model.DTO;

import android.graphics.Bitmap;

import com.klinker.android.send_message.Message;

/**
 * Created by tylerbowers on 1/27/18.
 */

public class CellMessage extends Message {

    public boolean isMms() {
        if (this.getImages().length > 0) {
            return true;
        }
        else if (this.getAddresses().length > 1) {
            return true;
        }
        else if (this.getSubject() != null && this.getSubject().length() > 0) {
            return true;
        }
        else return false;
    }

    public String[] getRecipients() {
        String[] addresses = this.getAddresses();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < addresses.length; i++) {
//            sb.append(addresses[i]);
//            if (i != addresses.length - 1) {
//                sb.append(" ");
//            }
//        }
//        return sb.toString();
        return addresses;
    }

    public Bitmap[] getPictures() {
        return this.getImages();
    }


    // ******************** OUTGOING INIT METHODS ******************************** //

    public static CellMessage newCellMessage(String text, String address) {
        CellMessage msg = new CellMessage(text, address);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String address, String subject) {
        CellMessage msg = new CellMessage(text, address, subject);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses) {
        CellMessage msg = new CellMessage(text, addresses);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses, String subject) {
        CellMessage msg = new CellMessage(text, addresses, subject);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String address, Bitmap image) {
        CellMessage msg = new CellMessage(text, address, image);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String address, Bitmap image, String subject) {
        CellMessage msg = new CellMessage(text, address, image, subject);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses, Bitmap image) {
        CellMessage msg = new CellMessage(text, addresses, image);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses, Bitmap image, String subject) {
        CellMessage msg = new CellMessage(text, addresses, image, subject);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String address, Bitmap[] images) {
        CellMessage msg = new CellMessage(text, address, images);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses, Bitmap[] images) {
        CellMessage msg = new CellMessage(text, addresses, images);
        return msg;
    }

    public static CellMessage newCellMessage(String text, String[] addresses, Bitmap[] images, String subject) {
        CellMessage msg = new CellMessage(text, addresses, images, subject);
        return msg;
    }

    private CellMessage(String text, String address) {
        super(text, address);
    }

    private CellMessage(String text, String address, String subject) {
        super(text, address, subject);
    }

    private CellMessage(String text, String[] addresses) {
        super(text, addresses);
    }

    private CellMessage(String text, String[] addresses, String subject) {
        super(text, addresses, subject);
    }

    private CellMessage(String text, String address, Bitmap image) {
        super(text, address, image);
    }

    private CellMessage(String text, String address, Bitmap image, String subject) {
        super(text, address, image, subject);
    }

    private CellMessage(String text, String[] addresses, Bitmap image) {
        super(text, addresses, image);
    }

    private CellMessage(String text, String[] addresses, Bitmap image, String subject) {
        super(text, addresses, image, subject);
    }

    private CellMessage(String text, String address, Bitmap[] images) {
        super(text, address, images);
    }

    private CellMessage(String text, String address, Bitmap[] images, String subject) {
        super(text, address, images, subject);
    }

    private CellMessage(String text, String[] addresses, Bitmap[] images) {
        super(text, addresses, images);
    }

    private CellMessage(String text, String[] addresses, Bitmap[] images, String subject) {
        super(text, addresses, images, subject);
    }
}
