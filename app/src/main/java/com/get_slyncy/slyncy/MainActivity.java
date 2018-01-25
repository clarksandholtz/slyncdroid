/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.get_slyncy.slyncy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.klinker.android.logger.Log;
import com.klinker.android.logger.OnLogListener;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private Settings settings;

    private Button mSetDefaultAppButton;
    private Button mSelectApnsButtons;
    private EditText mFromField;
    private EditText mToField;
    private EditText mMessageField;
    private ImageView mImageToSend;
    private Button mSelectImageButton;
    private Button mRemoveImageButton;
    private Button mSendButton;
    private RecyclerView log;
    private String mPicturePath;

    private LogAdapter logAdapter;

    private static final int RESULT_LOADED_IMAGE = 464;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("request_permissions", true) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initSettings();
        initViews();
        initActions();
        initLogging();

        BroadcastUtils.sendExplicitBroadcast(this, new Intent(), "test action");
    }

    private void initSettings() {
        settings = Settings.get(this);

        if (TextUtils.isEmpty(settings.getMmsc()) &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            initApns();
        }
    }

    private void initApns() {
        ApnUtils.initDefaultApns(this, new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                settings = Settings.get(MainActivity.this, true);
            }
        });
    }

    private void initViews() {
        mSetDefaultAppButton = (Button) findViewById(R.id.set_as_default);
        mSelectApnsButtons = (Button) findViewById(R.id.apns);
        mFromField = (EditText) findViewById(R.id.from);
        mToField = (EditText) findViewById(R.id.to);
        mMessageField = (EditText) findViewById(R.id.message);
        mImageToSend = (ImageView) findViewById(R.id.image);
        mSendButton = (Button) findViewById(R.id.send);
        log = (RecyclerView) findViewById(R.id.log);
        mSelectImageButton = (Button) findViewById(R.id.button_attach_image);
        mRemoveImageButton = (Button) findViewById(R.id.button_remove_image);
    }

    private void initActions() {
        if (Utils.isDefaultSmsApp(this)) {
            mSetDefaultAppButton.setVisibility(View.GONE);
        } else {
            mSetDefaultAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDefaultSmsApp();
                }
            });
        }

        mSelectApnsButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initApns();
            }
        });

        mFromField.setText(Utils.getMyPhoneNumber(this));
        mToField.setText(Utils.getMyPhoneNumber(this));

        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageToSend();
            }
        });

        mRemoveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        log.setHasFixedSize(false);
        log.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(new ArrayList<String>());
        log.setAdapter(logAdapter);
    }

    private void initLogging() {
        Log.setDebug(true);
        Log.setPath("messenger_log.txt");
        Log.setLogListener(new OnLogListener() {
            @Override
            public void onLogged(String tag, String message) {
                //logAdapter.addItem(tag + ": " + message);
            }
        });
    }

    private void setDefaultSmsApp() {
        mSetDefaultAppButton.setVisibility(View.GONE);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                getPackageName());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOADED_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mPicturePath = cursor.getString(columnIndex);
            cursor.close();
            mImageToSend.setImageBitmap(BitmapFactory.decodeFile(mPicturePath));
            mImageToSend.setEnabled(true);
            mImageToSend.setImageAlpha(255);
        }
    }

    private void selectImageToSend() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOADED_IMAGE);
    }

    private void removeImage() {
        mImageToSend.setEnabled(false);
        mImageToSend.setImageAlpha(0);
    }

    public void sendMessage() {
        mMessageField.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(settings.getMmsc());
                sendSettings.setProxy(settings.getMmsProxy());
                sendSettings.setPort(settings.getMmsPort());
                sendSettings.setUseSystemSending(true);

                Transaction transaction = new Transaction(MainActivity.this, sendSettings);

                Message message = new Message(mMessageField.getText().toString(), mToField.getText().toString());

                if (mImageToSend.isEnabled()) {
                    message.setImage(BitmapFactory.decodeFile(mPicturePath));
                }

                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            }
        }).start();
    }

}
