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
package com.get_slyncy.slyncy.Model.Service.smsmmsradar;

import android.content.Context;
import android.content.Intent;

import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Mms.MmsListener;
import com.get_slyncy.slyncy.Model.Service.smsmmsradar.Sms.SmsListener;


/**
 * Main library class. This class has to be used to initialize or stop the sms interceptor service.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class SmsMmsRadar
{

	public static SmsListener smsListener;
	public static MmsListener mmsListener;

	/**
	 * Starts the service and store the listener to be notified when a new incoming or outgoing sms be processed
	 * inside the SMS content provider
	 *
	 * @param context used to start the service
	 * @param smsListener to notify when the sms content provider gets a new sms
	 */
	public static void initializeSmsRadarService(Context context, SmsListener smsListener, MmsListener mmsListener) {
		SmsMmsRadar.smsListener = smsListener;
		SmsMmsRadar.mmsListener = mmsListener;
		Intent intent = new Intent(context, SmsMmsRadarService.class);
		context.startService(intent);
	}


	/**
	 * Stops the service and remove the SmsListener added when the SmsMmsRadar was initialized
	 *
	 * @param context used to stop the service
	 */
	public static void stopSmsRadarService(Context context) {
		SmsMmsRadar.smsListener = null;
		SmsMmsRadar.mmsListener = null;
		Intent intent = new Intent(context, SmsMmsRadarService.class);
		context.stopService(intent);
	}
}
