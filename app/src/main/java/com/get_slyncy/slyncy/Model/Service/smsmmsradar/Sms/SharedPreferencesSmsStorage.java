/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
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
package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.concurrent.Semaphore;

/**
 * SmsStorage implementation based on shared preferences.
 *
 * @author Pedro Vicente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class SharedPreferencesSmsStorage implements SmsStorage {

	private static final String LAST_SMS_PARSED = "last_sms_parsed";
    private static final String LAST_SMS_READ = "last_sms_read";
	private static final int DEFAULT_SMS_PARSED_VALUE = -1;
	private static final Semaphore sem = new Semaphore(1, true);
    private static final Semaphore sem1 = new Semaphore(1, true);

	private SharedPreferences preferences;

	public SharedPreferencesSmsStorage(SharedPreferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("SharedPreferences param can't be null");
		}

		this.preferences = preferences;
	}

	@SuppressLint("ApplySharedPref")
    @Override
	public void updateLastSmsIntercepted(int smsId) {
	    sem.acquireUninterruptibly();
		Editor editor = preferences.edit();
		editor.putInt(LAST_SMS_PARSED, smsId);
		editor.commit();
		sem.release();
	}

	@Override
	public int getLastSmsIntercepted() {
	    sem.acquireUninterruptibly();
        int val = preferences.getInt(LAST_SMS_PARSED, DEFAULT_SMS_PARSED_VALUE);
	    sem.release();
		return val;
	}

	@Override
	public boolean isFirstSmsIntercepted() {
		return getLastSmsIntercepted() == DEFAULT_SMS_PARSED_VALUE;
	}

	@Override
	public boolean isFirstSmsRead()
	{
		return getLastSmsRead() == DEFAULT_SMS_PARSED_VALUE;
	}

	@Override
	public int getLastSmsRead()
	{
		sem1.acquireUninterruptibly();
        int val = preferences.getInt(LAST_SMS_READ, DEFAULT_SMS_PARSED_VALUE);
        sem1.release();
        return val;
	}

	@SuppressLint("ApplySharedPref")
    @Override
	public void updateLastSmsRead(int smsId)
	{
        sem1.acquireUninterruptibly();
        Editor editor = preferences.edit();
        editor.putInt(LAST_SMS_READ, smsId);
        editor.commit();
        sem1.release();
	}
}
