/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.winapi;

import java.util.Calendar;
import java.util.Date;

public class EventLog {
	static {
		System.loadLibrary("winapi");
	}

	private int recordNumber;
	private int eventId;
	private EventType eventType;
	private Date generated;
	private Date written;
	private String providerName;
	private String eventCategory;
	private byte[] userSid;
	private String user;
	private String message;
	private byte[] data;

	private EventLog(int recordNumber, int eventId, EventType eventType, int generatedTime, int writtenTime,
			String sourceName, String eventCategory, byte[] userSid, String user, String message, byte[] data) {
		this.recordNumber = recordNumber;
		this.eventId = eventId;
		this.eventType = eventType;
		this.generated = toDate(generatedTime);
		this.written = toDate(writtenTime);
		this.providerName = sourceName;
		this.eventCategory = eventCategory;
		this.userSid = userSid;
		this.user = user;
		this.message = message != null ? message.trim() : null;
		this.data = data;
	}

	private Date toDate(int seconds) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1970);
		c.set(Calendar.MONDAY, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}

	public int getRecordNumber() {
		return recordNumber;
	}

	public int getEventId() {
		return eventId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Date getGenerated() {
		return generated;
	}

	public Date getWritten() {
		return written;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getEventCategory() {
		return eventCategory;
	}

	public byte[] getUserSid() {
		return userSid;
	}

	public String getUser() {
		return user;
	}

	public String getMessage() {
		return message;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		String str = "";
		str += "----- Log -----\r\n";
		str += "RecordNumber  : " + recordNumber + "\r\n";
		str += "EventId       : " + eventId + "\r\n";
		str += "EventType     : " + eventType + "\r\n";
		str += "Generated     : " + generated + "\r\n";
		str += "Written       : " + written + "\r\n";
		str += "Providername  : " + providerName + "\r\n";
		str += "EventCategory : " + eventCategory + "\r\n";
		str += "UserSID       : " + (userSid == null ? "null" : userSid.length) + "\r\n";
		str += "User          : " + user + "\r\n";
		str += "Message       : " + message + "\r\n";
		if (data != null) {
			str += "Data          : " + data.length + " bytes\r\n";
			str += "\t";
			for (byte b : data)
				str += String.format("%02x", b);
			str += "\r\n";
		}

		return str;
	}
}
