package org.krakenapps.logdb;

import java.util.Collection;

public interface DataSourceRegistry {
	Collection<DataSource> getAll();

	DataSource get(String nodeGuid, String name);

	void update(DataSource ds);

	void remove(DataSource ds);

	void addListener(DataSourceEventListener listener);

	void removeListener(DataSourceEventListener listener);
}
