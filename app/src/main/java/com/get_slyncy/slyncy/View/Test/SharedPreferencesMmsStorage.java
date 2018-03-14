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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SmsStorage implementation based on shared preferences.
 *
 * @author Pedro Vicente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
class SharedPreferencesMmsStorage implements MmsStorage {

	private static final String LAST_MMS_PARSED = "last_mms_parsed";
	private static final int DEFAULT_MMS_PARSED_VALUE = -1;

	private SharedPreferences preferences;
	private NewMsgDatabaseManager dbManager;

	SharedPreferencesMmsStorage(SharedPreferences preferences, Context context) {
		if (preferences == null) {
			throw new IllegalArgumentException("SharedPreferences param can't be null");
		}

		dbManager = NewMsgDatabaseManager.getInstance(context);
		this.preferences = preferences;
	}

	@Override
	public void updateLastMmsIntercepted(int smsId) {
		Editor editor = preferences.edit();
		editor.putInt(LAST_MMS_PARSED, smsId);
		editor.commit();
	}

	@Override
	public int getLastMmsIntercepted() {
		return preferences.getInt(LAST_MMS_PARSED, DEFAULT_MMS_PARSED_VALUE);
	}

	@Override
	public boolean isFirstMmsIntercepted() {
		return getLastMmsIntercepted() == DEFAULT_MMS_PARSED_VALUE;
	}

	@Override
	public void addNewMessage(int mmsId)
	{
		dbManager.getDb().NewMesNewMsgsDao().addMms(new NewMms(mmsId,mmsId));
	}

	@Override
	public boolean isUnread(int mmsId)
	{
		return dbManager.getDb().NewMesNewMsgsDao().getMms().contains(mmsId);
	}

    @Override
    public void removeMessage(int mmsId)
    {
        dbManager.getDb().NewMesNewMsgsDao().removeMms(new NewMms(mmsId,mmsId));
    }
}