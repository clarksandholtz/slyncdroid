package com.get_slyncy.slyncy.Model.Util;

import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.get_slyncy.slyncy.Model.DTO.Contact;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;
import com.get_slyncy.slyncy.View.LoginActivity;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import apollographql.apollo.UploadMessagesMutation;
import apollographql.apollo.type.ClientMessageCreateInput;
import apollographql.apollo.type.FileCreateInput;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tylerbowers on 2/27/18.
 */

public class ClientCommunicator {

    // Private because only static functions need be here
    private ClientCommunicator() {
    }

    public static boolean bulkMessageUpload() {


        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjamVleDA0YW4wMDBoMDEzMGpodjhma3RyIiwiaWF0IjoxNTIwMjk4MzQ5fQ.95UYNvydLOzA1loIuhPzkQaJDIvQEwF2YMb3a9ndHQ8");
                return chain.proceed(builder.build());
            }
        }).build();
        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL).build();
        Map<Integer, SlyncyMessageThread> messages = Data.getInstance().getmMessages();
        List<ClientMessageCreateInput> messagesToGo = new ArrayList<>();
        List<FileCreateInput> files = null;
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

                if (message.getImages().size() > 0)
                {
                    files = new ArrayList<>();
                    for (String s : message.getImages())
                    {
                        files.add(FileCreateInput.builder().content(s).contentType("jpg").build());
                    }
                }
                String date = message.getDate();

                messagesToGo.add(ClientMessageCreateInput.builder().address(address).androidMsgId(msgId).body(body)
                        .read(read).threadId(threadId).date(date).error(error).sender(sender)
                        .userSent(userSent).build());
            }

        }
        UploadMessagesMutation mutation = UploadMessagesMutation.builder().messages(messagesToGo).build();
        client.mutate(mutation).enqueue(null);
        return true;
    }

    public static boolean markThreadAsRead(String threadId) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjamVleDA0YW4wMDBoMDEzMGpodjhma3RyIiwiaWF0IjoxNTIwMjk4MzQ5fQ.95UYNvydLOzA1loIuhPzkQaJDIvQEwF2YMb3a9ndHQ8");
                return chain.proceed(builder.build());
            }
        }).build();

        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL).build();


//        MarkThreadAsRead mutation = UploadMessagesMutation.builder().messages(messagesToGo).build();
//        client.mutate(mutation).enqueue(null);
        return true;
    }
}
