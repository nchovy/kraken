package org.krakenapps.datasource;

import java.util.Properties;

public interface DataSource {
	/**
	 * path includes instance name.
	 */
	String getPath();

	/**
	 * Time series, Object, and so on.
	 */
	String getType();

	/**
	 * Instance configurations. for example, host, nic name, etc.
	 */
	Properties getProperties();

	/**
	 * Data composed by common data types. Caller can find parser using type
	 * information, and use data.
	 */
	Object getData();

	/**
	 * Update data and invoke callbacks
	 */
	void update(Object data);

	/**
	 * Add event listener. UI element can receive updated data.
	 */
	void addListener(DataSourceEventListener listener);

	/**
	 * Remove event listener.
	 */
	void removeListener(DataSourceEventListener listener);
}
