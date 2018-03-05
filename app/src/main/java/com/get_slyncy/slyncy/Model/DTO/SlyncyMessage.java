package com.get_slyncy.slyncy.Model.DTO;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tylerbowers on 2/26/18.
 */

public class SlyncyMessage {

    private String id;
    private int threadId;
    private long date;
    private String dispDate;
    private List<String> numbers;
    private List<Contact> contacts;
    private String sender;
    private String body;
    private List<String> images;
    private boolean isRead;

    public SlyncyMessage() {
        numbers = new ArrayList<>();
        contacts = new ArrayList<>();
        images = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        this.dispDate = msToDate(this.date);
    }

    public String getDispDate() {
        return dispDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public boolean addNumber(String number) { return this.numbers.add(number); }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public boolean addContact(Contact contact) {
        return contacts.add(contact);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean addImage(String image) {

        if (image == null)
            return false;

        return images.add(image);
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String msToDate(long mss) {

        long time = mss;

        long sec = ( time / 1000 ) % 60;
        time = time / 60000;

        long min = time % 60;
        time = time / 60;

        long hour = time % 24 - 5;
        time = time / 24;

        long day = time % 365;
        time = time / 365;

        long yr = time + 1970;

        day = day - ( time / 4 );
        long mo = getMonth(day);
        day = getDay(day);

        return String.valueOf(yr) + "/" + String.valueOf(mo) + "/" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(min) + ":" + String.valueOf(sec);
    }
    public long getMonth(long day) {
        long[] calendar = {31,28,31,30,31,30,31,31,30,31,30,31};
        for(int i = 0; i < 12; i++) {
            if(day < calendar[i]) {
                return i + 1;
            } else {
                day = day - calendar[i];
            }
        }
        return 1;
    }
    public long getDay(long day) {
        long[] calendar = {31,28,31,30,31,30,31,31,30,31,30,31};
        for(int i = 0; i < 12; i++) {
            if(day < calendar[i]) {
                return day;
            } else {
                day = day - calendar[i];
            }
        }
        return day;
    }
}
