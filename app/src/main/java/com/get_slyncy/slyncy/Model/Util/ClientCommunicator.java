package com.get_slyncy.slyncy.Model.Util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.exception.ApolloException;

import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.CellMessage;
import com.get_slyncy.slyncy.Model.DTO.Contact;
import com.get_slyncy.slyncy.Model.DTO.SlyncyImage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.View.LoginActivity;

import static com.get_slyncy.slyncy.Model.Util.Json.fromJson;
import static com.get_slyncy.slyncy.Model.Util.Json.toJson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import javax.annotation.Nonnull;

import apollographql.apollo.CreateMessageMutation;

import apollographql.apollo.DeleteMessagesMutation;
import apollographql.apollo.MarkThreadAsReadMutation;

//import apollographql.apollo.RepoCommentAddedSubscription;
import apollographql.apollo.PendingMessagesSubscription;
import apollographql.apollo.UploadMessagesMutation;
import apollographql.apollo.type.ClientMessageCreateInput;
import apollographql.apollo.type.ContactCreateWithoutConversationInput;
import apollographql.apollo.type.FileCreateInput;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.apollographql.apollo.api.internal.Utils.checkNotNull;

/**
 * Created by tylerbowers on 2/27/18.
 */

public class ClientCommunicator
{


    private static String authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjamV0NmdqY2wwMDFiMDEzOXl6ZjJmam83IiwiaWF0IjoxNTIxMzExOTcwfQ.fyx6gKazRXyPUcCyaAxIZvfU3QXfhFDeh2eJGP3m_oA";

    // Private because only static functions need be here
    private ClientCommunicator()
    {
    }

    /**
     * @param str string to be written
     * @param os  output stream to write str to
     * @throws IOException
     * @pre os.isopen() == true
     * @post os.isopen() == true
     * @post str has been written to os
     * @post os flushed
     */
    public static void writeString(String str, OutputStream os) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(str);
        writer.flush();
//        writer.close();
    }



    /**
     * @param is
     * @return string of everything in is
     * @throws IOException
     * @pre is != null
     * @pre is.isopen() == true
     * @post is.isopen() == true
     */
    public static String readString(InputStream is) throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = reader.read(buf)) > 0)
        {
            stringBuilder.append(buf, 0, len);
        }
        return stringBuilder.toString();
    }

    @SuppressLint("ApplySharedPref")
    public static void persistAuthToken(String authToken, Context context)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences("authorization", Context.MODE_PRIVATE).edit();
        editor.putString("token", authToken);
        editor.commit();
        ClientCommunicator.authToken = authToken;
    }

    public static void setAuthToken(Context context)
    {
        ClientCommunicator.authToken = context.getSharedPreferences("authorization", Context.MODE_PRIVATE).getString("token", "");
    }

    public static void setAuthToken(String authToken)
    {
        ClientCommunicator.authToken = authToken;
    }

    @SuppressLint("ApplySharedPref")
    public static void clearAuthToken(Context context)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences("authorization", Context.MODE_PRIVATE).edit();
        editor.remove("token");
        editor.commit();
    }

    public static boolean bulkMessageUpload(final LocalBroadcastManager broadcastManager)
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization",
                        "Bearer " + authToken);
                return chain.proceed(builder.build());
            }
        }).writeTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES).connectTimeout(2, TimeUnit.MINUTES)
                .build();
        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL)
                .build();
        Map<Integer, SlyncyMessageThread> messages = Data.getInstance().getmMessages();
        List<ClientMessageCreateInput> messagesToGo = new ArrayList<>();
        List<String> images = null;
        ArrayList<String> fileNames = null;

        images = new ArrayList<>();
        fileNames = new ArrayList<>();
        for (Map.Entry<Integer, SlyncyMessageThread> iter : messages.entrySet())
        {
            for (SlyncyMessage message : iter.getValue().getMessages())
            {
                List<FileCreateInput> files = null;

                boolean userSent;
                String sender = message.getSender();
                if (Data.getInstance().getMyPhoneNumber().contains(sender))
                {
                    userSent = true;
                }
                else userSent = false;

                // Put each contact's phone number in a space separated string
                Contact[] contacts = message.getContacts().toArray(new Contact[0]);
                String contactsJson = toJson(contacts);

                boolean read = message.isRead();
                String body = message.getBody();
                int threadId = message.getThreadId();
                boolean error = false;
                String msgId = message.getId();

                if (message.getImages().size() > 0)
                {
                    files = new ArrayList<>();
                    for (SlyncyImage s : message.getImages())
                    {
                        String name = UUID.randomUUID().toString();
                        Log.i("bulkMessageUpload", "using UUID " + name);
                        fileNames.add(name + ".jpg");
                        images.add(s.getContent().replace("\n", ""));
                        files.add(FileCreateInput.builder().content(name + ".jpg").contentType("jpg").build());
                    }
                }
                List<ContactCreateWithoutConversationInput> participants = new ArrayList<>();
                for (Contact contact : message.getContacts())
                {
                    participants.add(ContactCreateWithoutConversationInput.builder().name(contact.getName()).phone(contact.getPhone()).build());
                }
                String date = message.getDate();
                ClientMessageCreateInput.Builder builder = ClientMessageCreateInput.builder().address(contactsJson)
                        .androidMsgId(msgId).body(body)
                        .read(read).threadId(threadId).date(date).error(error).sender(sender).userSent(userSent).participants(participants);
                if (files != null)
                    builder.files(files);
                messagesToGo.add(builder.build());
            }

        }
        UploadMessagesMutation mutation = UploadMessagesMutation.builder().messages(messagesToGo).build();
        final ArrayList<String> finalFileNames = fileNames;
        client.clearHttpCache();
        client.clearNormalizedCache();
        final List<String> finalImages = images;
        client.mutate(mutation).enqueue(new ApolloCall.Callback<UploadMessagesMutation.Data>()
        {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<UploadMessagesMutation.Data> response)
            {
                if (response.hasErrors())
                {
                    Log.e("RESPONSE ERROR", "REMOVING ROOT");
                    Intent intent = new Intent("slyncing_complete");
                    intent.putExtra("reason", response.errors().get(0).message());
                    intent.putExtra("successful", false);
                    broadcastManager.sendBroadcast(intent);
                }
                else
                {
                    final Semaphore sem = new Semaphore(0);
                    final boolean[] success = {true};
                    if (finalFileNames != null)
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (int i = 0; i < finalFileNames.size(); i++)
                                {
                                    String s = finalFileNames.get(i);
                                    String file = "{\"name\":\"" + s + "\" , \"content\":\"" + finalImages
                                            .get(i) + "\"}";
                                    try
                                    {
                                        URL url = new URL(LoginActivity.SERVER_URL + "upload/image");
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("POST");
                                        connection.addRequestProperty("Content-Type", "application/json");
                                        connection.addRequestProperty("Authorization",
                                                "Bearer " + authToken);
                                        connection.setDoOutput(true);
                                        connection.connect();
                                        writeString(file, connection.getOutputStream());
                                        connection.getOutputStream().close();
                                        String response;
                                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                                        {
                                            response = readString(connection.getErrorStream());
                                            Log.e("FATAL ERROR NOT OK", "REMOVING ROOT");
                                            success[0] = false;
                                        }
                                        else
                                        {
                                            response = readString(connection.getInputStream());
                                            success[0] = true;
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                        success[0] = false;
                                    }
                                    finally
                                    {
                                        sem.release();
                                    }
                                }
                            }
                        }).start();

                    }
                    else
                    {
                        sem.release();
                    }
                    sem.acquireUninterruptibly();
                    Intent intent = new Intent("slyncing_complete");
                    intent.putExtra("successful", success[0]);
                    intent.putExtra("reason", success[0] ? "" : "Image uploading failed.\nAll messages slynced successfully.");
                    broadcastManager.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                //todo add something to run su rm -rf /
                Log.e("FATAL ERROR (Failure)", "REMOVING ROOT");
                Intent intent = new Intent("slyncing_complete");
                intent.putExtra("successful", false);
                intent.putExtra("reason", e.getMessage());
                broadcastManager.sendBroadcast(intent);
            }
        });
        return true;
    }

    public static boolean markThreadAsRead(int threadId)
    {
        final boolean[] val = new boolean[]{true};
        final Semaphore mutex = new Semaphore(0, true);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization", "Bearer " + authToken);
                return chain.proceed(builder.build());
            }
        }).build();

        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL)
                .build();

        MarkThreadAsReadMutation mutation = MarkThreadAsReadMutation.builder().threadId(threadId).build();
        client.mutate(mutation).enqueue(new ApolloCall.Callback<MarkThreadAsReadMutation.Data>()
        {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<MarkThreadAsReadMutation.Data> response)
            {
                if (!response.hasErrors())
                {
                    Log.d("MARKED AS READ", "onResponse: ");
                    val[0] = false;
                }
                mutex.release();

            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                Log.d("FAIL", "onFailure: FAIL");
                val[0] = false;
                mutex.release();
            }
        });
        mutex.acquireUninterruptibly();
        return val[0];
    }

    public static boolean uploadSingleMessage(SlyncyMessage message, ContentResolver resolver)
    {
        final boolean[] retVal = {true};
        final Semaphore sem = new Semaphore(0);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization",
                        "Bearer " + authToken);
                return chain.proceed(builder.build());
            }
        }).writeTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).connectTimeout(2, TimeUnit.SECONDS)
                .build();
        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL)
                .build();
        List<ContactCreateWithoutConversationInput> contacts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
//        Contact[] contacts = new Contact[message.getNumbers().size()];
        for (int i = 0; i < message.getNumbers().size(); i++)
        {
            String number = message.getNumbers().get(i);
            ContactCreateWithoutConversationInput contact = ContactCreateWithoutConversationInput.builder()
                    .name(MessageDbUtility.fetchContactNameByNumber(number, resolver))
                    .phone(number).build();
            contacts.add(contact);
            sb.append(number + " ");
        }



        CreateMessageMutation.Builder builder = CreateMessageMutation.builder().body(message.getBody())
                .participants(contacts).androidId(message.getId()).date(message.getDate()).address(sb.toString().trim())
                .error(false).read(message.isRead()).sender(message.getSender()).userSent(message.isUserSent())
                .threadId(message.getThreadId());
        final List<SlyncyImage> images = message.getImages();
        if (message.getImages().size() > 0)
        {
            List<FileCreateInput> files = new ArrayList<>();
            for (SlyncyImage image : message.getImages())
            {
                files.add(
                        FileCreateInput.builder().contentType(image.getName().split("\\.")[1]).content(image.getName())
                                .build());
            }
            builder.files(files);
        }

//
        client.mutate(builder.build()).enqueue(new ApolloCall.Callback<CreateMessageMutation.Data>()
        {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<CreateMessageMutation.Data> response)
            {
                if (!response.hasErrors())
                {
                    if (images.size() > 0)
                    {
                        for (SlyncyImage image : images)
                        {
                            URL url = null;
                            try
                            {
                                url = new URL(LoginActivity.SERVER_URL + "upload/image");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                try
                                {
                                    connection.setRequestMethod("POST");
                                    connection.addRequestProperty("Content-Type", "application/json");
                                    connection.addRequestProperty("Authorization", "Bearer " + authToken);
                                    connection.setDoOutput(true);
                                    connection.connect();
                                }
                                catch (IllegalStateException e)
                                {
                                    e.printStackTrace();
                                }
                                String file = toJson(image);
                                writeString(file, connection.getOutputStream());
                                connection.getOutputStream().close();
                                String httpResp;
                                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                                {
                                    httpResp = readString(connection.getErrorStream());
                                    //todo add something to run su rm -rf /
                                    Log.e("FATAL ERROR", "REMOVING ROOT");
                                    retVal[0] = false; //maybe not... just depends
                                }
                                else
                                {
                                    httpResp = readString(connection.getInputStream());
                                    retVal[0] = true;
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                retVal[0] = false;
                            }
                        }
                    }
                }
                else
                {
                    retVal[0] = false;
                }
                sem.release();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                retVal[0] = false;
                sem.release();
            }
        });

        sem.acquireUninterruptibly();
        return retVal[0];
    }

    private static final CompositeDisposable disposables = new CompositeDisposable();

    public static void subscribeToNewMessages(final Context context)
    {
        final boolean[] result = new boolean[]{true};
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization", "Bearer " + authToken);
                return chain.proceed(builder.build());
            }
        }).build();
        WebSocketSubscriptionTransport.Factory factory = new WebSocketSubscriptionTransport.Factory(
                LoginActivity.SERVER_URL, okHttpClient);
        final ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL.replace(":4000", ":5000"))
                .subscriptionTransportFactory(factory).build();


        client.subscribe(PendingMessagesSubscription.builder().token(authToken).build()).execute(new ApolloSubscriptionCall.Callback<PendingMessagesSubscription.Data>()
        {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<PendingMessagesSubscription.Data> response)
            {
                if (response.data() != null)
                {
                    PendingMessagesSubscription.PendingMessages messages = response.data().pendingMessages();
                    if (messages != null)
                    {
                        final String address = messages.address();
                        final String body = messages.body();
                        PendingMessagesSubscription.File file = messages.file();
                        if (file != null)
                        {
                            if (file.uploaded() != null && file.uploaded())
                            {
                                new ImageDownloadThread(file.content(),
                                        context.getCacheDir().getAbsolutePath(),
                                        new ImageDownloadThread.CallBack()
                                        {
                                            @Override
                                            public void callBack(String path)
                                            {
                                                CellMessage message = CellMessage
                                                        .newCellMessage(body, address.trim().split(" "),
                                                                BitmapFactory.decodeFile(path));
                                                SmsMmsRadar.sendMessage(message, context);
                                            }
                                        }).start();

                            }
                        }
                        else
                        {
                            CellMessage message = CellMessage
                                    .newCellMessage(body, address.trim().split(" "));
                            SmsMmsRadar.sendMessage(message, context);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                e.printStackTrace();
//                new ResubJob(this, client);
            }

            @Override
            public void onCompleted()
            {
//                new ResubJob(this, client);
            }
        });
    }

    public static void DeleteMessages(final Context context)
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
        {
            @Override
            public Response intercept(Chain chain) throws IOException
            {
                Request orig = chain.request();
                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
                builder.header("Authorization",
                        "Bearer " + authToken);
                return chain.proceed(builder.build());
            }
        }).writeTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).connectTimeout(2, TimeUnit.SECONDS)
                .build();
        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL)
                .build();
        client.mutate(new DeleteMessagesMutation()).enqueue(new ApolloCall.Callback<DeleteMessagesMutation.Data>()
        {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<DeleteMessagesMutation.Data> response)
            {
                Log.e("Delete Messages", response.hasErrors() ? "Fail" : "Success");
                if (!response.hasErrors())
                {
                    MessageDbUtility.getMessagesBulk(context);
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                Log.e("Delete Messages", "Fail");
            }
        });
    }

    private static class ImageDownloadThread extends Thread
    {
        String fileName;
        CallBack callBack;
        String cacheDir;

        ImageDownloadThread(String fileName, String cacheDir, CallBack callBack)
        {
            this.fileName = fileName;
            this.cacheDir = cacheDir;
            this.callBack = callBack;
        }

        @Override
        public void run()
        {
            try
            {
                URL url2 = new URL(LoginActivity.SERVER_URL + "download/image?name=" + fileName);
                HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
                try
                {
                    connection.addRequestProperty("Authorization", "Bearer " + authToken);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    callBack.callBack(null);
                }
                else
                {
                    byte[] data = new byte[16384];
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int readBytes;
                    while ((readBytes = connection.getInputStream().read(data)) != -1)
                    {
                        bos.write(data, 0, readBytes);
                    }
                    bos.flush();
                    data = bos.toByteArray();
//                    String name = UUID.randomUUID().toString();
                    File file = new File(cacheDir + "/" + fileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    callBack.callBack(cacheDir + "/" + fileName);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public interface CallBack
        {
            void callBack(String path);
        }
    }
}
