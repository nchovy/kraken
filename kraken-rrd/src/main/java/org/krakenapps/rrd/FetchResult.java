package org.krakenapps.rrd;

import java.util.*;

public interface FetchResult {
	public interface Row 
	{
		Date getDate();
		long getTime();
		double[] getColumns();
		double getColumn(int columnIndex);
	}
	
	RrdRawImpl getRRD();
	List<Row> getRows();
}
