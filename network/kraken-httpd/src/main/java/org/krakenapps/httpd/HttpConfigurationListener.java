package org.krakenapps.httpd;

public interface HttpConfigurationListener {
	/**
	 * @param fieldName
	 *            http configuration field name. "maxContentLength" or
	 *            "idleTimeout"
	 * @param value
	 *            config value (Integer only)
	 */
	void onSet(String fieldName, Object value);
}
