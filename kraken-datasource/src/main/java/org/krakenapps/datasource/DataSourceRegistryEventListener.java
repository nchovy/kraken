package org.krakenapps.datasource;

public interface DataSourceRegistryEventListener {
	void addedDataSource(DataSource dataSource);

	void removedDataSource(DataSource dataSource);
}
