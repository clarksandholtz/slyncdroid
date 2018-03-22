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
package com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms;

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
public class SharedPreferencesMmsStorage implements MmsStorage {

	private static final String LAST_MMS_PARSED = "last_mms_parsed";
	private static final String LAST_MMS_READ = "last_mms_read";
	private static final int DEFAULT_MMS_PARSED_VALUE = -1;
	private static final Semaphore sem = new Semaphore(1, true);
	private static final Semaphore sem1 = new Semaphore(1, true);
	private SharedPreferences preferences;

	public SharedPreferencesMmsStorage(SharedPreferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("SharedPreferences param can't be null");
		}

		this.preferences = preferences;
	}

	@SuppressLint("ApplySharedPref")
    @Override
	public void updateLastMmsIntercepted(int mmsId) {
		sem.acquireUninterruptibly();
		Editor editor = preferences.edit();
		editor.putInt(LAST_MMS_PARSED, mmsId);
		editor.commit();
		sem.release();
	}

	@Override
	public int getLastMmsIntercepted() {
		sem.acquireUninterruptibly();
		int val = preferences.getInt(LAST_MMS_PARSED, DEFAULT_MMS_PARSED_VALUE);
		sem.release();
		return val;
	}

	@Override
	public boolean isFirstMmsIntercepted() {
		return getLastMmsIntercepted() == DEFAULT_MMS_PARSED_VALUE;
	}

    @Override
    public boolean isFirstMmsRead()
    {
        return getLastMmsIntercepted() == DEFAULT_MMS_PARSED_VALUE;
    }

    @Override
    public int getLastMmsRead()
    {
        sem1.acquireUninterruptibly();
        int val = preferences.getInt(LAST_MMS_READ, DEFAULT_MMS_PARSED_VALUE);
        sem1.release();
        return val;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void updateLastMmsRead(int mmsId)
    {
        sem1.acquireUninterruptibly();
        Editor editor = preferences.edit();
        editor.putInt(LAST_MMS_READ, mmsId);
        editor.commit();
        sem1.release();
    }
}
