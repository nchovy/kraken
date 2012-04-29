/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.syslog.impl;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.api.ScriptContext;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;

public class SyslogTracer implements SyslogListener {
	private ScriptContext context;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@Override
	public void onReceive(Syslog syslog) {
		Date date = syslog.getDate();
		InetAddress remoteAddr = syslog.getRemoteAddress().getAddress();
		int remotePort = syslog.getRemoteAddress().getPort();
		String msg = syslog.getMessage();
		int facility = syslog.getFacility();
		int severity = syslog.getSeverity();

		context.printf("[%s] (%s:%s) => [fc:%d, sv:%d] %s\n", dateFormat.format(date), remoteAddr, remotePort, facility,
				severity, msg);
	}
}
