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
package com.tuenti.smsradar;

/**
 * Represents a sms stored in Android sms Content Provider.
 * <p/>
 * Address field is the equivalent to the MT/MO author MSISDN in telco terminology.
 * <p/>
 * Review MSISDN standard for more information: http://en.wikipedia.org/wiki/MSISDN
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
public class Sms {

	private final String address;
	private final long date;
	private final String msg;
	private final SmsType type;
	private final long threadId;


	public Sms(String address, long date, String msg, SmsType type, long threadId) {
		this.address = address;
		this.date = date;
		this.msg = msg;
		this.type = type;
		this.threadId = threadId;
	}

	public String getAddress() {
		return address;
	}

	public long getDate() {
		return date;
	}

	public String getMsg() {
		return msg;
	}

	public SmsType getType() {
		return type;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Sms)) return false;

		Sms sms = (Sms) o;

		return (address != null ? address.equals(sms.address) : sms.address == null) && (date == sms.getDate()) && (msg != null ? msg
				.equals(sms.msg) : sms.msg == null) && threadId == sms.threadId && type == sms.type;
	}

	@Override
	public int hashCode() {
		int result = address != null ? address.hashCode() : 0;
		result = 31 * result + Long.valueOf(date).hashCode();
		result = 31 * result + (msg != null ? msg.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + Long.valueOf(threadId).hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Sms{" +
				"address='" + address + '\'' +
				", date='" + date + '\'' +
				", msg='" + msg + '\'' +
				", type=" + type +
				'}';
	}

    public long getThreadId()
    {
        return threadId;
    }
}
