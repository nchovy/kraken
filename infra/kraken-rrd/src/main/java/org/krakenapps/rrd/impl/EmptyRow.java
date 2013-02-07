/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.rrd.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.rrd.DataSourceConfig;
import org.krakenapps.rrd.FetchRow;

public class EmptyRow implements FetchRow {
	private long time;
	private RrdRaw rrd;
	private static HashMap<Integer, double[]> cache = new HashMap<Integer, double[]>();

	public EmptyRow(long time, RrdRaw rrd) {
		this.time = time;
		this.rrd = rrd;
	}

	@Override
	public Date getDate() {
		return new Date(time * 1000);
	}

	@Override
	public long getTimeInSec() {
		return time;
	}

	@Override
	public double[] getColumns() {
		return getEmptyColumns(rrd.getDataSources().size());
	}

	@Override
	public Map<DataSourceConfig, Double> getColumnsMap() {
		Map<DataSourceConfig, Double> ret = new HashMap<DataSourceConfig, Double>();
		for (DataSource ds : rrd.getDataSources())
			ret.put(RrdUtil.dataSourceToConfig(ds), Double.NaN);
		return ret;
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

	@Override
	public double getColumn(String dataSourceName) {
		return Double.NaN;
	}
}
