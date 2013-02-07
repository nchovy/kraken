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

import java.util.Date;
import java.util.Properties;

public class LoggerSpecification {
	private String namespace;
	private String name;
	private String description;
	private long logCount;
	private boolean isPassive;
	private Date lastLogDate;
	private Properties config;

	public LoggerSpecification(String namespace, String name, String description, long logCount, Date lastLogDate,
			Properties config) {
		this(namespace, name, description, logCount, lastLogDate, config, false);
	}

	public LoggerSpecification(String namespace, String name, String description, long logCount, Date lastLogDate,
			Properties config, boolean isPassive) {
		this.namespace = namespace;
		this.name = name;
		this.description = description;
		this.logCount = logCount;
		this.lastLogDate = lastLogDate;
		this.config = config;
		this.isPassive = isPassive;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public long getLogCount() {
		return logCount;
	}

	public boolean isPassive() {
		return isPassive;
	}

	public Date getLastLogDate() {
		return lastLogDate;
	}

	public Properties getConfig() {
		return config;
	}

}
