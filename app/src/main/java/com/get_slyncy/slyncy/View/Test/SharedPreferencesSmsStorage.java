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
package com.get_slyncy.slyncy.View.Test;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SmsStorage implementation based on shared preferences.
 *
 * @author Pedro Vicente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
class SharedPreferencesSmsStorage implements SmsStorage {

	private static final String LAST_SMS_PARSED = "last_sms_parsed";
	private static final int DEFAULT_SMS_PARSED_VALUE = -1;

	private SharedPreferences preferences;
	private NewMsgDatabaseManager dbManager;

	SharedPreferencesSmsStorage(SharedPreferences preferences, Context context) {
		if (preferences == null) {
			throw new IllegalArgumentException("SharedPreferences param can't be null");
		}
		dbManager = NewMsgDatabaseManager.getInstance(context);
		this.preferences = preferences;
	}

	@Override
	public void updateLastSmsIntercepted(int smsId) {
		Editor editor = preferences.edit();
		editor.putInt(LAST_SMS_PARSED, smsId);
		editor.commit();
	}

	@Override
	public int getLastSmsIntercepted() {
		return preferences.getInt(LAST_SMS_PARSED, DEFAULT_SMS_PARSED_VALUE);
	}

	@Override
	public boolean isFirstSmsIntercepted() {
		return getLastSmsIntercepted() == DEFAULT_SMS_PARSED_VALUE;
	}

	@Override
	public void addSms(int smsId)
	{
		dbManager.getDb().NewMesNewMsgsDao().addSms(new NewSms(smsId, smsId));
	}

	@Override
	public boolean isUnread(int smsId)
	{
		return !dbManager.getDb().NewMesNewMsgsDao().getSms().contains(smsId);
	}
}
