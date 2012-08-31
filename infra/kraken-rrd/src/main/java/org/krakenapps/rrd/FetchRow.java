package org.krakenapps.rrd;

import java.util.Date;
import java.util.Map;

public interface FetchRow {
	Date getDate();

	long getTimeInSec();

	double[] getColumns();

	Map<DataSourceConfig, Double> getColumnsMap();

	double getColumn(int columnIndex);

	double getColumn(String dataSourceName);
}