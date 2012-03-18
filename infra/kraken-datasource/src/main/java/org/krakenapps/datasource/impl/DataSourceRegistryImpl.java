package org.krakenapps.datasource.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.datasource.DataSource;
import org.krakenapps.datasource.DataSourceRegistry;
import org.krakenapps.datasource.DataSourceRegistryEventListener;
import org.krakenapps.util.DirectoryMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "data-source-registry")
@Provides
public class DataSourceRegistryImpl implements DataSourceRegistry {
	private Logger logger = LoggerFactory.getLogger(DataSourceRegistryImpl.class.getName());
	private DirectoryMap<DataSource> dir;
	private Set<DataSourceRegistryEventListener> callbacks;

	public DataSourceRegistryImpl() {
		dir = new DirectoryMap<DataSource>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<DataSourceRegistryEventListener, Boolean>());
	}

	@Override
	public Collection<String> getSubKeys(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return dir.keySet(path);
	}

	@Override
	public Collection<Entry<String, DataSource>> getChildren(String parentPath) {
		if (!parentPath.startsWith("/")) {
			parentPath = "/" + parentPath;
		}
		return dir.getItems(parentPath);
	}

	@Override
	public Collection<Entry<String, DataSource>> query(String query) {
		if (!query.startsWith("/")) {
			query = "/" + query;
		}
		return Collections.unmodifiableCollection(dir.entrySet(query));
	}

	@Override
	public DataSource getDataSource(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return dir.get(path);
	}

	@Override
	public void addDataSource(DataSource dataSource) {
		if (dataSource.getPath() == null)
			return;
		if (!dataSource.getPath().startsWith("/")) {
			dir.putIfAbsent("/" + dataSource.getPath(), dataSource);
		} else {
			dir.putIfAbsent(dataSource.getPath(), dataSource);
		}

		// invoke callbacks
		for (DataSourceRegistryEventListener callback : callbacks) {
			try {
				callback.addedDataSource(dataSource);
			} catch (Exception e) {
				logger.error("kraken datasource: registry event listener should not throw any exception", e);
			}
		}
	}

	@Override
	public void removeDataSource(String path) {
		if (path == null)
			return;
		if (!path.startsWith("/"))
			path = "/" + path;

		DataSource dataSource = dir.remove(path);
		if (dataSource == null)
			return;

		// invoke callbacks
		for (DataSourceRegistryEventListener callback : callbacks) {
			try {
				callback.removedDataSource(dataSource);
			} catch (Exception e) {
				logger.error("kraken datasource: registry event listener should not throw any exception", e);
			}
		}
	}

	@Override
	public void addListener(DataSourceRegistryEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeListener(DataSourceRegistryEventListener listener) {
		callbacks.remove(listener);
	}

}
