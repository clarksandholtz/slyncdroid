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

package com.get_slyncy.slyncy.View.Test;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.get_slyncy.slyncy.Model.CellMessaging.MessageDbUtility;
import com.get_slyncy.slyncy.Model.DTO.CellMessage;
import com.get_slyncy.slyncy.Presenter.CellMessagesPresenter;
import com.get_slyncy.slyncy.R;
import com.get_slyncy.slyncy.View.LogAdapter;
import com.get_slyncy.slyncy.View.PermissionActivity;
import com.klinker.android.send_message.Utils;

import java.util.ArrayList;

public class TestActivity extends Activity
{

    private static final int RESULT_LOADED_IMAGE = 464;

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

    private CellMessagesPresenter mPresenter;
    private LogAdapter logAdapter;

    private String mPicturePath;
    private boolean mHasImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Check if permissions are needed
        if (PermissionActivity.needPermissionRequest(this))
        {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_test);

        mPresenter = new CellMessagesPresenter(this);

        initSettings();
        initViews();
        initActions();
        initMessaging();

//        BroadcastUtils.sendExplicitBroadcast(this, new Intent(), "test action");
    }

    private void initMessaging()
    {
//        MessageDbUtility.init(this);
    }

    private void initSettings()
    {
        mHasImage = false;
        mPresenter.initCellSettings();
    }

    private void initViews()
    {
        mSetDefaultAppButton = (Button) findViewById(R.id.set_as_default);
//        mSelectApnsButtons = (Button) findViewById(R.id.apns);
        mFromField = (EditText) findViewById(R.id.from);
        mToField = (EditText) findViewById(R.id.to);
        mMessageField = (EditText) findViewById(R.id.message);
        mImageToSend = (ImageView) findViewById(R.id.image);
        mSendButton = (Button) findViewById(R.id.send);
        log = (RecyclerView) findViewById(R.id.log);
        mSelectImageButton = (Button) findViewById(R.id.button_attach_image);
        mRemoveImageButton = (Button) findViewById(R.id.button_remove_image);
    }

    private void initActions()
    {
        if (Utils.isDefaultSmsApp(this))
        {
            mSetDefaultAppButton.setVisibility(View.GONE);
        }
        else
        {
            mSetDefaultAppButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    setDefaultSmsApp();
                }
            });
        }

        mFromField.setText(Utils.getMyPhoneNumber(this));
        mToField.setText(Utils.getMyPhoneNumber(this));

        mSelectImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectImageToSend();
            }
        });

        mRemoveImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                removeImage();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });

        log.setHasFixedSize(false);
        log.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(new ArrayList<String>());
        log.setAdapter(logAdapter);
    }

    private void setDefaultSmsApp()
    {
        mSetDefaultAppButton.setVisibility(View.GONE);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                getPackageName());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOADED_IMAGE && resultCode == RESULT_OK && null != data)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mPicturePath = cursor.getString(columnIndex);
            cursor.close();
            mImageToSend.setImageBitmap(BitmapFactory.decodeFile(mPicturePath));
            mHasImage = true;
            mImageToSend.setImageAlpha(255);
        }
    }

    private void selectImageToSend()
    {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOADED_IMAGE);
    }

    private void removeImage()
    {
        mHasImage = false;
        mImageToSend.setImageAlpha(0);
        mPicturePath = null;
    }

    public void sendMessage()
    {

        CellMessage message = CellMessage
                .newCellMessage(mMessageField.getText().toString(), mToField.getText().toString());
        mPresenter.sendMessage(message);
        mMessageField.setText("");
    }

    public void forceMMS(View view)
    {
        CellMessage message = CellMessage
                .newCellMessage(mMessageField.getText().toString(), mToField.getText().toString());
        if (mHasImage)
        {
            message.setImage(BitmapFactory.decodeFile(mPicturePath));
        }
        mPresenter.sendMMS(message);
        mMessageField.setText("");
    }
}
