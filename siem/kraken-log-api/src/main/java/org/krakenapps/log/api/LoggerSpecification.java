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
