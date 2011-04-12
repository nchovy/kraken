package org.krakenapps.datasource;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSource implements DataSource {
	private Logger logger = LoggerFactory.getLogger(AbstractDataSource.class.getName());
	private volatile Object data = null;

	private Properties props;
	private Set<DataSourceEventListener> listeners;

	public AbstractDataSource() {
		props = new Properties();
		listeners = Collections.newSetFromMap(new ConcurrentHashMap<DataSourceEventListener, Boolean>());
	}

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void update(Object data) {
		Object old = this.data;
		this.data = data;

		dispatchUpdate(old, data);
	}

	private void dispatchUpdate(Object oldData, Object newData) {
		if (oldData != null && newData != null && oldData.equals(newData))
			return;

		for (DataSourceEventListener callback : listeners) {
			try {
				callback.onUpdate(this, oldData, newData);
			} catch (Exception e) {
				logger.warn("kraken-datasource: update should not throws any exception", e);
			}
		}
	}

	@Override
	public void addListener(DataSourceEventListener listener) {
		if (listener == null)
			return;

		listeners.add(listener);
	}

	@Override
	public void removeListener(DataSourceEventListener listener) {
		if (listener == null)
			return;

		listeners.remove(listener);
	}
}
