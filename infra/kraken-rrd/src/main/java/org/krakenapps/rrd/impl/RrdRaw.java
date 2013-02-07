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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.krakenapps.rrd.ArchiveConfig;
import org.krakenapps.rrd.ConsolidateFunc;
import org.krakenapps.rrd.DataSourceConfig;
import org.krakenapps.rrd.FetchResult;
import org.krakenapps.rrd.RrdConfig;
import org.krakenapps.rrd.io.PersistentLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RrdRaw {
	private Logger logger = LoggerFactory.getLogger(RrdRaw.class);

	private long step;
	private long lastUpdate;
	private int rowCapacity = 0;

	private List<Archive> archives = new ArrayList<Archive>();
	private List<DataSource> dataSources = new ArrayList<DataSource>();

	public RrdRaw(RrdConfig config) {
		this.step = config.getStep();
		this.lastUpdate = config.getStartTime() / 1000L;

		if (config.getArchives().isEmpty())
			throw new IllegalArgumentException("no archives in config");
		for (ArchiveConfig rraConfig : config.getArchives()) {
			archives.add(new Archive(this, rraConfig));
			rowCapacity += rraConfig.getRowCapacity();
		}

		if (config.getDataSources().isEmpty())
			throw new IllegalArgumentException("no data sources in config");
		for (DataSourceConfig dsConfig : config.getDataSources())
			dataSources.add(DataSource.createInstance(this, dsConfig, lastUpdate));
	}

	public RrdRaw(PersistentLayer persLayer) {
		try {
			persLayer.open();
			int version = persLayer.readByte();
			if (version != 1)
				throw new IllegalStateException();

			step = persLayer.readLong();
			lastUpdate = persLayer.readLong();

			int archiveSize = persLayer.readInt();
			if (archiveSize == 0)
				throw new IllegalArgumentException("no archives in config");

			for (int i = 0; i < archiveSize; i++) {
				Archive archive = new Archive(this, persLayer);
				archives.add(archive);
				rowCapacity += archive.getRowCapacity();
			}

			int dataSourceSize = persLayer.readInt();
			if (dataSourceSize == 0)
				throw new IllegalArgumentException("no data sources in config");

			for (int i = 0; i < dataSourceSize; i++)
				dataSources.add(DataSource.createFromPersLayer(this, persLayer));
		} catch (Exception e) {
			logger.error("kraken rrd: rrdraw read failed", e);
			throw new IllegalStateException(e);
		} finally {
			try {
				persLayer.close();
			} catch (IOException e) {
			}
		}
	}

	public List<ArchiveConfig> getArchiveConfigs() {
		List<ArchiveConfig> result = new ArrayList<ArchiveConfig>();
		for (Archive ar : archives)
			result.add(RrdUtil.archiveToConfig(ar));
		return result;
	}

	public List<DataSourceConfig> getDataSourceConfigs() {
		List<DataSourceConfig> result = new ArrayList<DataSourceConfig>();
		for (DataSource ds : dataSources)
			result.add(RrdUtil.dataSourceToConfig(ds));
		return result;
	}

	public void addDataSource(DataSourceConfig dsConfig) {
		for (DataSource ds : dataSources) {
			if (ds.getName().equals(dsConfig.getName()))
				throw new IllegalArgumentException("duplicate name");
		}
		DataSource ds = DataSource.createInstance(this, dsConfig, lastUpdate);
		dataSources.add(ds);
	}

	public void removeDataSource(String name) {
		Iterator<DataSource> it = dataSources.iterator();
		while (it.hasNext()) {
			if (it.next().getName().equals(name)) {
				it.remove();
				return;
			}
		}
		throw new IllegalArgumentException("datasource not found");
	}

	public void update(Date time, Map<String, Double> values) {
		Double[] v = new Double[dataSources.size()];
		int i = 0;
		for (DataSource ds : dataSources) {
			Double value = values.get(ds.getName());
			v[i++] = (value != null) ? value : Double.NaN;
		}
		update(time, v);
	}

	public void update(Date time, Double[] values) {
		long lTime = time.getTime() / 1000L;
		if (values.length != dataSources.size())
			throw new IndexOutOfBoundsException("the size of values is not match with the number of data sources");
		if (lTime < lastUpdate)
			throw new IllegalArgumentException(String.format("time %1$d is not after lastUpdate %2$d", lTime, lastUpdate));

		processExpiredCdp(lTime);

		int i = 0;
		for (DataSource ds : dataSources)
			ds.update(lTime, lastUpdate, values[i++]);

		for (Archive ar : archives)
			ar.onRrdUpdated(lTime);

		this.lastUpdate = lTime;
	}

	public void processExpiredCdp(long lTime) {
		for (Archive ar : archives)
			ar.processExpiredCdp(lTime);
	}

	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		long lStart = start.getTime() / 1000L;
		long lEnd = end.getTime() / 1000L;
		Archive archive = selectArchive(f, resolution);
		assert (archive != null);
		return new FetchResult(this, archive, lStart, lEnd);
	}

	// find longest step in archives of which step is smaller than required resolution.
	// TODO: this implementation is different from original 'rrdtool fetch'.
	private Archive selectArchive(ConsolidateFunc f, long resolution) {
		Archive ret = null;
		long r = 0;
		for (Archive ar : archives) {
			long archiveResolution = step * ar.getPdpPerRow();
			if (r < archiveResolution && archiveResolution <= resolution) {
				ret = ar;
				r = archiveResolution;
			}
		}

		return (ret != null) ? ret : archives.get(0);
	}

	public long getStep() {
		return step;
	}

	public void updateArchives(long time, long lastUpdate, DataSource dataSource, double checkpointValue, double newValue) {
		for (Archive ar : archives)
			ar.update(time, lastUpdate, dataSource, checkpointValue);
	}

	public Date getLastUpdateDate() {
		return new Date(lastUpdate * 1000L);
	}

	public void dump(OutputStream os) {
		PrintWriter writer = new PrintWriter(os);
		writer.printf("step: %d\n", this.step);
		writer.printf("lastUpdate: %d\n", this.lastUpdate);
		writer.flush();

		writer.printf("\n= datasources =\n");
		for (DataSource ds : dataSources) {
			ds.dump(writer);
			writer.flush();
		}

		writer.printf("\n= archives =\n");
		for (Archive ar : archives) {
			ar.dump(writer);
			writer.flush();
		}
	}

	public int length() {
		int len = 17;
		len += 4;
		for (DataSource ds : dataSources)
			len += ds.length();
		len += 4;
		for (Archive ar : archives)
			len += ar.length();
		return len;
	}

	public void write(PersistentLayer persLayer) {
		try {
			persLayer.open(false);
			persLayer.writeByte(1); // version
			persLayer.writeLong(this.step);
			persLayer.writeLong(this.lastUpdate);

			persLayer.writeInt(archives.size());
			for (Archive ar : archives)
				ar.writeToPersLayer(persLayer);

			persLayer.writeInt(dataSources.size());
			for (DataSource ds : dataSources)
				ds.writeToPersLayer(persLayer);
		} catch (IOException e) {
			logger.error("kraken rrd: rrdraw write failed", e);
		} finally {
			try {
				persLayer.close();
			} catch (IOException e) {
			}
		}
	}

	public int getRowCapacity() {
		return rowCapacity;
	}

	public List<Archive> getArchives() {
		return archives;
	}

	public List<DataSource> getDataSources() {
		return dataSources;
	}

	@Override
	public boolean equals(Object obj) {
		RrdRaw rhs = (RrdRaw) obj;

		if (this.step != rhs.step)
			return false;
		if (this.lastUpdate != rhs.lastUpdate)
			return false;

		if (this.dataSources.size() != rhs.dataSources.size())
			return false;
		if (this.archives.size() != rhs.archives.size())
			return false;

		int dataSourceSize = this.dataSources.size();
		for (int i = 0; i < dataSourceSize; ++i) {
			if (!this.dataSources.get(i).equals(rhs.dataSources.get(i)))
				return false;
		}

		int archiveSize = this.archives.size();
		for (int i = 0; i < archiveSize; ++i) {
			if (!this.archives.get(i).equals(rhs.archives.get(i)))
				return false;
		}

		return true;
	}
}