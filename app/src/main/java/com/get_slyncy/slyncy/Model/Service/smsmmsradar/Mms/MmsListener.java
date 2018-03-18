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

import com.get_slyncy.slyncy.Model.DTO.SlyncyMessage;
/**
 * This interface has to be implemented to be notified when an sms be received or sent.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public interface MmsListener
{

	/**
	 * Invoked when an incoming sms is intercepted.
	 *
	 * @param mms intercepted.
	 */
	public void onMmsSent(SlyncyMessage mms);

	/**
	 * Invoked when an outgoing sms is intercepted.
	 *
	 * @param mms
	 */

	public void onMmsReceived(SlyncyMessage mms);

}
