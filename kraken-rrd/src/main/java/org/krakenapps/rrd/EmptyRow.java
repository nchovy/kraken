package org.krakenapps.rrd;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class EmptyRow implements FetchResult.Row {
	private long time;
	private int columnSize;
	private static HashMap<Integer, double[]> cache = new HashMap<Integer, double[]>();

	public EmptyRow(long time, int columnSize) {
		this.time = time;
		this.columnSize = columnSize;
	}

	@Override
	public Date getDate() {
		return new Date(time * 1000);
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public double[] getColumns() {
		return getEmptyColumns(columnSize);
	}

	private static double[] getEmptyColumns(int cs) {
		if (!cache.containsKey(cs)) {
			cache.put(cs, new double[cs]);
			Arrays.fill(cache.get(cs), Double.NaN);
		}
		return cache.get(cs);
	}

	@Override
	public double getColumn(int columnIndex) {
		return Double.NaN;
	}

}
