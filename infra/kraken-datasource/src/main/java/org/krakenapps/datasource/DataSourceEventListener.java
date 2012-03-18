package org.krakenapps.datasource;

public interface DataSourceEventListener {
	void onUpdate(DataSource source, Object oldData, Object newData);
}
