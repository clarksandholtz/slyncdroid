package com.get_slyncy.slyncy.Model.Util;

import com.apollographql.apollo.ApolloClient;
import com.get_slyncy.slyncy.Model.DTO.Contact;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;

import java.util.Map;

/**
 * Created by tylerbowers on 2/27/18.
 */

public class ClientCommunicator {

    public ClientCommunicator() {
    }

    public boolean bulkMessageUpload() {

//        ApolloClient client = ApolloClient.builder().serverUrl("10.37.80.161:4000").build();

        Map<Integer, SlyncyMessageThread> messages = Data.getInstance().getmMessages();
        for (Map.Entry<Integer, SlyncyMessageThread> iter : messages.entrySet()) {
            for (SlyncyMessage message : iter.getValue().getMessages()) {

                boolean userSent;
                String sender = message.getSender();
                if (Data.getInstance().getMyPhoneNumber().contains(sender)) {
                    userSent = true;
                }
                else userSent = false;

                // Put each contact's phone number in a space separated string
                Contact[] contacts = message.getContacts().toArray(new Contact[0]);
                StringBuilder sb = new StringBuilder();
                for (Contact contact : contacts) {
                    sb.append(contact.getmNumber() + " ");
                }
                String address = sb.toString();

                boolean read = message.isRead();
                String body = message.getBody();
                int threadId = message.getThreadId();
                boolean error = false;
                String msgId = message.getId();
                String image;
                if (message.getImages().size() > 0) {
                    image = message.getImages().get(0);
                }
                long date = message.getDate();
            }

        }

        return true;
    }
}
