package com.get_slyncy.slyncy.Model.DTO;

import android.content.Context;
import android.text.TextUtils;

import com.get_slyncy.slyncy.Model.Util.RecipientIdCache;

import java.util.ArrayList;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class ContactList extends ArrayList<Contact> {

    public String[] getNamesAsArray() {
        ArrayList<String> names = new ArrayList<>();
        for (Contact c : this) {
            names.add(c.getmName());
        }
        return (String[]) names.toArray();
    }

    public String[] getNumbersAsArray() {
        ArrayList<String> numbers = new ArrayList<>();
        for (Contact c : this) {
            numbers.add(c.getmNumber());
        }
        return (String[]) numbers.toArray();
    }

    /**
     * Returns a ContactList for the corresponding recipient ids passed in. This method will
     * create the contact if it doesn't exist, and would inject the recipient id into the contact.
     */
    public static ContactList getByIds(String spaceSepIds, Context context) {
        ContactList list = new ContactList();
        for (RecipientIdCache.Entry entry : RecipientIdCache.getAddresses(spaceSepIds)) {
            if (entry != null && !TextUtils.isEmpty(entry.number)) {
                String phone = entry.number;
                Contact contact = new Contact(phone, "");
                contact.setmRecipientId(entry.id);
                list.add(contact);
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ContactList other = (ContactList)obj;
            // If they're different sizes, the contact
            // set is obviously different.
            if (size() != other.size()) {
                return false;
            }

            // Make sure all the individual contacts are the same.
            for (Contact c : this) {
                if (!other.contains(c)) {
                    return false;
                }
            }

            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
