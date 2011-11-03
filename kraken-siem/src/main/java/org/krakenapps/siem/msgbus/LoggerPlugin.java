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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.msgbus.Marshaler;
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

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void createLogger(Request req, Response resp) {
		int orgId = req.getOrgId();
		String loggerName = req.getString("logger");
		String parserFactoryName = req.getString("parser");
		Properties parserOptions = new Properties();

		Map<String, String> m = (Map<String, String>) req.get("parser_options");
		if (m != null)
			for (String key : m.keySet())
				parserOptions.put(key, m.get(key));

		logServer.createManagedLogger(orgId, loggerName, parserFactoryName, parserOptions);
	}

	@MsgbusMethod
	public void removeLogger(Request req, Response resp) {
		String loggerName = req.getString("logger");
		ManagedLogger logger = logServer.getManagedLogger(loggerName);
		logServer.removeManagedLogger(logger.getId());
	}

	private class LogAdder implements LogSearchCallback {
		private List<Map<String, Object>> logs;

		public LogAdder(List<Map<String, Object>> logs) {
			this.logs = logs;
		}

		@Override
		public void interrupt() {
		}

		@Override
		public boolean isInterrupted() {
			return false;
		}

		@Override
		public void onLog(Log log) {
			logs.add(marshalLog(log));
		}
	}

	private Map<String, Object> marshalLog(Log log) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("table_name", log.getTableName());
		m.put("date", dateFormat.format(log.getDate()));
		m.put("id", log.getId());
		Map<String, Object> data = new HashMap<String, Object>();
		for (String key : log.getData().keySet()) {
			Object value = log.getData().get(key);
			if (value instanceof Date)
				data.put(key, dateFormat.format(value));
			else
				data.put(key, value);
		}
		m.put("data", data);
		m.put("index_tokens", log.getIndexTokens());
		return m;
	}

}
