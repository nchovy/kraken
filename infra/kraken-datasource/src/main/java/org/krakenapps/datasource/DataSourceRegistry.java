package org.krakenapps.datasource;

import java.util.Collection;
import java.util.Map.Entry;

public interface DataSourceRegistry {
	Collection<String> getSubKeys(String path);

	Collection<Entry<String, DataSource>> getChildren(String path);

	Collection<Entry<String, DataSource>> query(String query);

	DataSource getDataSource(String path);

	void addDataSource(DataSource dataSource);

	void removeDataSource(String path);

	void addListener(DataSourceRegistryEventListener listener);

	void removeListener(DataSourceRegistryEventListener listener);
}
