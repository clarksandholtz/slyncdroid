/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.get_slyncy.slyncy.View;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.get_slyncy.slyncy.View.Test.TestActivity;

public class PermissionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions(new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED
        }, 0);
    }

    public static boolean needPermissionRequest(Context context) {
        int smsReadPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_SMS);
        int smsSendPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS);
        int smsReceivePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECEIVE_SMS);
        int mmsPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECEIVE_MMS);
        int phonePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);

        if (smsReadPermission == -1 ||
                smsSendPermission == -1 ||
                smsReceivePermission == -1 ||
                mmsPermission == -1 ||
                phonePermission == -1)
        {
            return true;
        }
        else return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean("request_permissions", false)
                .apply();

        startActivity(new Intent(this, TestActivity.class));
        finish();
    }

}
