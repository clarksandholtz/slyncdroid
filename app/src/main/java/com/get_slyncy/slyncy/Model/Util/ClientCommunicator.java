package com.get_slyncy.slyncy.Model.Util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.CellMessage;
import com.get_slyncy.slyncy.Model.DTO.Contact;
import com.get_slyncy.slyncy.Model.DTO.SlyncyImage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessageThread;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;
import com.get_slyncy.slyncy.View.LoginActivity;
import com.google.gson.Gson;

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
     * @param o object to be serialized into json
     * @return string of the serialized object
     * @pre none
     */
    public static String toJson(Object o)
    {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> classOfT)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, classOfT);
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

    public static void setAuthToken(String authToken)
    {
        ClientCommunicator.authToken = authToken;
    }

    public static boolean bulkMessageUpload()
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

                String date = message.getDate();
                ClientMessageCreateInput.Builder builder = ClientMessageCreateInput.builder().address(contactsJson)
                        .androidMsgId(msgId).body(body)
                        .read(read).threadId(threadId).date(date).error(error).sender(sender).userSent(userSent);
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
                    Log.e("FATAL ERROR", "REMOVING ROOT");
                }
                else
                {
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
                                            Log.e("FATAL ERROR", "REMOVING ROOT");
                                        }
                                        else
                                        {
                                            response = readString(connection.getInputStream());
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                    }
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                //todo add something to run su rm -rf /
                Log.e("FATAL ERROR", "REMOVING ROOT");
            }
        });
        return true;
    }

    public static boolean markThreadAsRead(int threadId)
    {
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
                    Log.d("MARKEd AS READ", "onResponse: ");
            }

            @Override
            public void onFailure(@Nonnull ApolloException e)
            {
                Log.d("FAIL", "onFailure: FAIL");
            }
        });
        return true;
    }

    public static boolean uploadSingleMessage(SlyncyMessage message, Context context)
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
                    .name(MessageDbUtility.fetchContactNameByNumber(number, context.getContentResolver()))
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

//        client.subscribe(PendingMessagesSubscription.builder().build()).execute(
//                new ApolloSubscriptionCall.Callback<PendingMessagesSubscription.Data>()
//                {
//                    @Override
//                    public void onResponse(
//                            @Nonnull com.apollographql.apollo.api.Response<PendingMessagesSubscription.Data> response)
//                    {
//
//                    }
//
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e)
//                    {
//
//                    }
//
//                    @Override
//                    public void onCompleted()
//                    {
//
//                    }
//                });
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
                                    connection.addRequestProperty("Authorization",
                                            "Bearer " + authToken);
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


        ApolloSubscriptionCall<PendingMessagesSubscription.Data> subscription = client
                .subscribe(PendingMessagesSubscription.builder().token(authToken).build());
        disposables.add(Rx2Apollo.from(subscription).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread()).subscribeWith(
                        new DisposableSubscriber<com.apollographql.apollo.api.Response<PendingMessagesSubscription.Data>>()
                        {
                            @Override
                            public void onNext(
                                    com.apollographql.apollo.api.Response<PendingMessagesSubscription.Data> dataResponse)
                            {
                                Log.d("OnNext", "subscription");
                                if (dataResponse.data() != null)
                                {
                                    PendingMessagesSubscription.PendingMessages messages = dataResponse.data().pendingMessages();
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
                            public void onError(Throwable t)
                            {
                                Log.d("error", "subscription");

                            }

                            @Override
                            public void onComplete()
                            {
                                Log.d("complete", "subscription");
                            }
                        }));
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
