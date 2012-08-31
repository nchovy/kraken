package org.krakenapps.rrd.impl;

import org.krakenapps.rrd.ArchiveConfig;
import org.krakenapps.rrd.DataSourceConfig;

public class RrdUtil {
	public static final double epsilon = 1e-08;

	public static boolean doubleListEqual(double[] l1, double[] l2) {
		if (l1.length != l2.length)
			return false;

		for (int i = 0; i < l1.length; ++i) {
			if (!doubleEqual(l1[i], l2[i]))
				return false;
		}
		return true;
	}

	public static boolean doubleEqual(double v1, double v2) {
		return (Double.isNaN(v1) && Double.isNaN(v2)) || Math.abs(v1 - v2) < epsilon;
	}

	public static ArchiveConfig archiveToConfig(Archive ar) {
		return new ArchiveConfig(ar.getCf(), ar.getXff(), (int) ar.getPdpPerRow(), ar.getRowCapacity());
	}

	public static DataSourceConfig dataSourceToConfig(DataSource ds) {
		return new DataSourceConfig(ds.getName(), ds.getType(), ds.getMinimalHeartbeat(), ds.getMin(), ds.getMax());
	}
}
