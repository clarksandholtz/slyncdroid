package com.get_slyncy.slyncy.View.Test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.IMmsListener;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.ISmsListener;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.SmsMmsRadar;

//import apollographql.apollo.UploadMessagesMutation;
//import apollographql.apollo.type.CustomType;
//import apollographql.apollo.type.MessageCreateInput;

/**
 * Created by undermark5 on 2/27/18.
 */

public class SmsRadar extends Activity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        Intent serviceIntent = new Intent(this, SmsMmsRadar.class);
//        startService(serviceIntent);
//        ClientCommunicator.subscribeToNewMessages();
        SmsMmsRadar.initializeSmsRadarServiceAndStart(this, new ISmsListener()
        {
            @Override
            public void onSmsSent(SlyncyMessage sms)
            {
                if (sms != null)
                {
                    showSmsSToast(sms);
                    ClientCommunicator.uploadSingleMessage(sms, getApplicationContext());
                }
            }

            @Override
            public void onSmsReceived(SlyncyMessage sms)
            {
                if (sms != null)
                {
                    showSmsRToast(sms);
                    ClientCommunicator.uploadSingleMessage(sms, getApplicationContext());
                }
            }
        }, new IMmsListener()
        {
            @Override
            public void onMmsSent(SlyncyMessage mms)
            {
                if (mms != null)
                {
                    showSmsSToast(mms);
                    ClientCommunicator.uploadSingleMessage(mms, getApplicationContext());
                }
            }

            @Override
            public void onMmsReceived(SlyncyMessage mms)
            {
                if (mms != null)
                {
                    showSmsRToast(mms);
                    ClientCommunicator.uploadSingleMessage(mms, getApplicationContext());
                }
            }
        });
    }

    private void showSmsSToast(SlyncyMessage sms)
    {
        Looper.prepare();
        Toast.makeText(this, getSmsToastText(sms, "S"), Toast.LENGTH_SHORT).show();
    }

    private void showSmsRToast(SlyncyMessage sms)
    {
        Looper.prepare();
        Toast.makeText(this, getSmsToastText(sms, "R"), Toast.LENGTH_SHORT).show();
    }


    private String getSmsToastText(SlyncyMessage sms, String type)
    {
//        ApolloClient.builder().
//        CustomTypeAdapter<Date> customTypeAdapter = new CustomTypeAdapter<Date>()
//        {
//            @Override
//            public Date decode(@Nonnull String value)
//            {
//                return new Date(Long.parseLong(value) * 1000);
//            }
//
//            @Nonnull
//            @Override
//            public String encode(@Nonnull Date value)
//            {
//                return String.valueOf(value.getTime());
//            }
//        };
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor()
//        {
//            @Override
//            public Response intercept(Chain chain) throws IOException
//            {
//                Request orig = chain.request();
//                Request.Builder builder = orig.newBuilder().method(orig.method(), orig.body());
//                builder.header("Authorization",
//                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjamV0NmdqY2wwMDFiMDEzOXl6ZjJmam83IiwiaWF0IjoxNTIxMTU4NDg1fQ.-pAAuNQCGkcLUX-WcQbXmXNg1xsYQtivMOCoNP7eMlY");
//                return chain.proceed(builder.build());
//            }
//        }).build();
//        ApolloClient client = ApolloClient.builder().okHttpClient(okHttpClient).serverUrl(LoginActivity.SERVER_URL)
//                .build();
        StringBuilder stringBuilder = new StringBuilder();
        switch (type)
        {
            case "R":
                stringBuilder.append("New SMS from ");
//                Log.d("SMS in:",  new Date(Long.parseLong(sms.getDate()) * 1000).toString());
//                client.mutate(CreateMessageMutation.builder()
//                        .address(sms.getAddress()).userSent(false)
//                        .body(sms.getMsg()).date(sms.getDate())
//                        .error(false).files(null).read(false).threadId(sms.getThreadId()).build()).enqueue(null);
                break;
            case "S":
                stringBuilder.append("New SMS to ");
//                client.mutate(CreateMessageMutation.builder()
//                        .address(sms.getAddress()).userSent(true)
//                        .body(sms.getMsg()).date(sms.getDate())
//                        .error(false).files(null).read(true).threadId(sms.getThreadId()).build()).enqueue(null);
                break;
        }
        stringBuilder.append(MessageDbUtility.fetchContactNameByNumber(sms.getSender(), getContentResolver()))
                .append("\n").append("MSG: ").append(sms.getBody());
        Log.d("MSGRadar", stringBuilder.toString());
        return stringBuilder.toString();
    }
}
