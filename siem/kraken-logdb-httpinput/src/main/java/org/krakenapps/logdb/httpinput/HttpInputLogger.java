/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.httpinput;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;

public class HttpInputLogger extends AbstractLogger {
	private String token;

	public HttpInputLogger(LoggerFactory factory, LoggerSpecification spec) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), factory, spec.getConfig());
		this.token = spec.getConfig().getProperty("token");
		setPassive(true);
	}

	@Override
	protected void runOnce() {
	}

	public String getToken() {
		return token;
	}

	public void write(Log log) {
		super.write(log);
	}
}
