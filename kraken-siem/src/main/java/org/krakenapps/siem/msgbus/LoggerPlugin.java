/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.msgbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.siem.LogFileScannerRegistry;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.model.ManagedLogger;

@Component(name = "siem-logger-plugin")
@MsgbusPlugin
public class LoggerPlugin {
	@Requires
	private LogServer logServer;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LogFileScannerRegistry scannerRegistry;

	@Requires
	private MessageBus msgbus;

	@MsgbusMethod
	public void getLoggers(Request req, Response resp) {
		List<Map<String, Object>> loggers = new ArrayList<Map<String, Object>>();
		for (ManagedLogger logger : logServer.getManagedLoggers()) {
			Map<String, Object> m = logger.marshal();
			Logger l = loggerRegistry.getLogger(logger.getFullName());
			m.put("logger", org.krakenapps.log.api.msgbus.Marshaler.marshal(l));
			loggers.add(m);
		}

		resp.put("loggers", loggers);
	}

	@MsgbusMethod
	public void getLogFileScanners(Request req, Response resp) {
		resp.put("scanners", Marshaler.marshal(scannerRegistry.getScanners()));
	}

	@MsgbusMethod
	public void ensureLoggerOperation(Request req, Response resp) throws InterruptedException {
		Message m = new Message();
		m.setMethod("org.krakenapps.log.api.msgbus.LoggerPlugin.ensureLoggerOperation");
		m.setParameters(req.getParams());

		String loggerFullname = req.getString("logger");
		msgbus.dispatch(req.getSession(), m);

		int i = 0;
		while (true) {
			if (loggerRegistry.getLogger(loggerFullname) != null)
				break;

			Thread.sleep(100);
			i++;
		}

		if (i > 20)
			throw new MsgbusException("siem", "ensure-logger-timeout");

		if (logServer.getManagedLogger(loggerFullname) == null)
			createLogger(req, resp);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void createLogger(Request req, Response resp) {
		String orgDomain = req.getOrgDomain();
		String loggerName = req.getString("logger");
		Map<String, String> metadata = (Map<String, String>) req.get("metadata");

		ManagedLogger ml = new ManagedLogger();
		ml.setOrgDomain(orgDomain);
		ml.setFullName(loggerName);
		ml.setMetadata(metadata);

		logServer.createManagedLogger(ml);
	}

	@MsgbusMethod
	public void removeLogger(Request req, Response resp) {
		String loggerName = req.getString("logger");
		ManagedLogger ml = logServer.getManagedLogger(loggerName);
		logServer.removeManagedLogger(ml);
	}
}
