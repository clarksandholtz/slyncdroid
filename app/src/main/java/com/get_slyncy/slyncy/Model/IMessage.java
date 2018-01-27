package com.get_slyncy.slyncy.Model;

import android.graphics.Bitmap;

/**
 * Created by tylerbowers on 1/27/18.
 */

public interface IMessage {

    String getSender();
    String getRecipient();
    void setSender(String sender);
    void setRecipient(String recipient);

    boolean hasBody();
    String getBody();

    boolean hasImageAttachment();
    Bitmap getImageAttachment();

    // add other attachment types e.g. multiple images, contact obj, audio, ...
}
