package org.krakenapps.log.api;

import java.util.Date;
import java.util.Properties;

public class LoggerSpecification {
	private String namespace;
	private String name;
	private String description;
	private long logCount;
	private Date lastLogDate;
	private Properties config;

	public LoggerSpecification(String namespace, String name, String description, long logCount, Date lastLogDate,
			Properties config) {
		this.namespace = namespace;
		this.name = name;
		this.description = description;
		this.logCount = logCount;
		this.lastLogDate = lastLogDate;
		this.config = config;
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

	public Date getLastLogDate() {
		return lastLogDate;
	}

	public Properties getConfig() {
		return config;
	}

}
