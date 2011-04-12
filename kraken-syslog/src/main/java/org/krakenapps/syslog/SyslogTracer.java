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
package org.krakenapps.syslog;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.api.ScriptContext;
import org.krakenapps.filter.DefaultFilter;
import org.krakenapps.filter.Message;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.DefaultMessageSpec;

public class SyslogTracer extends DefaultFilter {
	private ScriptContext context;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSSZ");

	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@Override
	public MessageSpec[] getInputMessageSpecs() {
		return new MessageSpec[] { new DefaultMessageSpec("kraken.syslog", 1, 0) };
	}

	@Override
	public void process(Message message) {
		Date date = (Date) message.get("date");
		InetAddress remoteAddr = (InetAddress) message.get("remote_ip");
		int remotePort = (Integer) message.get("remote_port");
		String msg = (String) message.get("message");
		int facility = (Integer) message.get("facility");
		int severity = (Integer) message.get("severity");

		context.printf("[%s] (%s:%s) => [fc:%d, sv:%d] %s\n", dateFormat.format(date),
				remoteAddr, remotePort, facility, severity, msg);
	}
}
