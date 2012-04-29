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
package org.krakenapps.syslog;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Syslog {
	private Date date;
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private int facility;
	private int severity;
	private String message;

	public Syslog(Date date, InetSocketAddress remote, int facility, int severity, String message) {
		this.date = date;
		this.remoteAddress = remote;
		this.facility = facility;
		this.severity = severity;
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress local) {
		this.localAddress = local;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remote) {
		this.remoteAddress = remote;
	}

	public int getFacility() {
		return facility;
	}

	public void setFacility(int facility) {
		this.facility = facility;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("date=%s, remote=%s, facility=%d, severity=%d, msg=%s", dateFormat.format(date), remoteAddress,
				facility, severity, message);
	}

}
