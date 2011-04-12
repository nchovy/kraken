package org.krakenapps.filter.exception;

/**
 * Unchecked exception thrown when the filter can not accept current
 * configurations.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class ConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * property name or internal configuration name
	 */
	private String configName;

	/**
	 * cause of the error
	 */
	private String errorMessage;

	public ConfigurationException(String configName) {
		this.configName = configName;
	}

	public ConfigurationException(String name, String errorMessage) {
		this.configName = name;
		this.errorMessage = errorMessage;
	}

	public String getConfigurationName() {
		return configName;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
