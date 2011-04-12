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
package org.krakenapps.log.api;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SimpleLog implements Log {
	private Date date;
	private String loggerName;
	private Map<String, Object> params;

	public SimpleLog(Date date, String loggerName, Map<String, Object> params) {
		this.date = date;
		this.loggerName = loggerName;
		this.params = params;
	}

	public SimpleLog(Date date, String loggerName, String type, String message) {
		this.date = date;
		this.loggerName = loggerName;
		this.params = new HashMap<String, Object>();
		this.params.put("msg", message);
		if (type != null)
			this.params.put("logtype", type);
	}

	public SimpleLog(Date date, String loggerName, String type, Map<String, Object> params) {
		this(date, loggerName, type, null, params);
	}

	public SimpleLog(Date date, String loggerName, String type, String message, Map<String, Object> params) {
		this.date = date;
		this.loggerName = loggerName;
		this.params = params;
		if (message != null)
			this.params.put("msg", message);
		if (type != null)
			this.params.put("logtype", type);
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public String getMessage() {
		if (params.containsKey("msg"))
			return params.get("msg").toString();
		return null;
	}

	@Override
	public Map<String, Object> getParams() {
		return Collections.unmodifiableMap(params);
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("date=%s, logger=%s, msg=%s", dateFormat.format(date), loggerName, getMessage());
	}
}
