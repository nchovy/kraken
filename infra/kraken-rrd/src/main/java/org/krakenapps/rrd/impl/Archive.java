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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.krakenapps.rrd.ArchiveConfig;
import org.krakenapps.rrd.ConsolidateFunc;
import org.krakenapps.rrd.DataSourceConfig;
import org.krakenapps.rrd.FetchRow;
import org.krakenapps.rrd.exception.InvalidStateException;
import org.krakenapps.rrd.exception.ParameterAssertionFailedException;
import org.krakenapps.rrd.io.PersistentLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Archive {
	private Logger logger = LoggerFactory.getLogger(Archive.class);

	private RrdRaw raw;

	private ConsolidateFunc func;
	private int pdpPerRow;
	private int rowCapacity;
	private double xff;

	private ArchiveRow[] rows;
	private int rowsStart;
	private int rowsSize;

	public Archive(RrdRaw raw, ArchiveConfig rraConfig) {
		this.raw = raw;
		this.func = rraConfig.getCf();
		this.pdpPerRow = rraConfig.getSteps();
		this.xff = rraConfig.getXff();
		this.rowCapacity = rraConfig.getRowCapacity();

		this.rows = new ArchiveRow[rowCapacity];
		this.rowsStart = 0;
		this.rowsSize = 0;

		// fill empty rows
		long cdpDuration = pdpPerRow * raw.getStep();
		long expectedLastUpdateTime = raw.getLastUpdateDate().getTime() / 1000L / cdpDuration * cdpDuration;
		for (rowsSize = 0; rowsSize < rowCapacity; rowsSize++)
			rows[rowsSize] = new ArchiveRow(rowsIndex(rowsSize), expectedLastUpdateTime - rowsSize * cdpDuration);
	}

	public Archive(RrdRaw rrd, PersistentLayer persLayer) throws IOException {
		this.raw = rrd;
		this.func = persLayer.readEnum(ConsolidateFunc.class);
		this.pdpPerRow = persLayer.readInt();
		this.xff = persLayer.readDouble();
		this.rowCapacity = persLayer.readInt();

		if (pdpPerRow == 0)
			throw new ParameterAssertionFailedException("pdpPerRow != 0", pdpPerRow);
		if (rowCapacity == 0)
			throw new ParameterAssertionFailedException("size != 0", pdpPerRow);

		rowsSize = persLayer.readInt();
		rowsStart = persLayer.readInt();

		long lastUpdate = rrd.getLastUpdateDate().getTime() / 1000;
		long cdpDuration = rrd.getStep() * pdpPerRow;
		long lastRowTime = lastUpdate / cdpDuration * cdpDuration;
		rows = new ArchiveRow[rowCapacity];
		for (int i = 0; i < rowsSize; i++) {
			int rowIndex = persLayer.readInt();
			int logicalIndex = (rowIndex >= rowsStart) ? (rowIndex - rowsStart) : (rowsSize - (rowsStart - rowIndex));
			rows[i] = new ArchiveRow(rowIndex, lastRowTime - cdpDuration * logicalIndex);
		}
	}

	public ConsolidateFunc getCf() {
		return func;
	}

	public double getXff() {
		return xff;
	}

	public void onRrdUpdated(long currentTime) {
		long cdpSeconds = pdpPerRow * raw.getStep();

		long archiveLastUpdate = getLastUpdate();
		if ((archiveLastUpdate / cdpSeconds * cdpSeconds + cdpSeconds) <= currentTime) {
			long expectedCommitTime = currentTime / cdpSeconds * cdpSeconds;
			commitCdp(expectedCommitTime);
		}
	}

	protected long getLastUpdate() {
		return rows[rowsIndex(0)].getTimeInSec();
	}

	public void commitCdp(long t) {
		ArchiveRow row = getNewRow(t);
		if (row == null)
			throw new InvalidStateException();

		for (DataSource ds : raw.getDataSources()) {
			ConsolidatedDataPoint cdp = ds.getCdp(this);
			double cdpValue = cdp.getCdpValue(t);
			row.setColumn(ds, cdpValue);
			cdp.prepareNewCdp(cdpValue);
		}

		logger.debug("kraken rrd: archive {}, {}: {}\n", new Object[] { this.pdpPerRow, this.func.toString(), this.rows.length });
	}

	public void processExpiredCdp(long currentTime) {
		long archiveLastUpdate = getLastUpdate();
		long cdpSeconds = raw.getStep() * pdpPerRow;
		long previousCdpExpiringTime = archiveLastUpdate + cdpSeconds * 2;

		if (currentTime >= previousCdpExpiringTime) {
			commitCdp(archiveLastUpdate + cdpSeconds);

			// add empty rows
			long lastCdpExpiringTime = currentTime / cdpSeconds * cdpSeconds - cdpSeconds;
			int ecc = (int) ((lastCdpExpiringTime - previousCdpExpiringTime) / cdpSeconds);
			if (ecc > rows.length)
				previousCdpExpiringTime = lastCdpExpiringTime - cdpSeconds * rows.length;

			for (long iTime = previousCdpExpiringTime; iTime <= lastCdpExpiringTime; iTime += cdpSeconds) {
				ArchiveRow newRow = (ecc > rows.length) ? getNewRowNoCheck(iTime) : getNewRow(iTime);
				for (DataSource ds : raw.getDataSources()) {
					newRow.setColumn(ds, Double.NaN);
				}
			}
		}
	}

	private ArchiveRow getNewRowNoCheck(long lTime) {
		int rs;
		if (rowsSize >= rowCapacity) {
			rs = (rowsStart - 1 + rowCapacity) % rowCapacity;
			rowsStart = rs;
		} else {
			rs = rowsSize++;
		}
		rows[rs].rowIndex = rs;
		rows[rs].time = lTime;
		return rows[rs];
	}

	private ArchiveRow getNewRow(long lTime) {
		if (lTime == ArchiveRow.INVALID_TIME)
			return null;

		if (!(lTime - getLastUpdate() == raw.getStep() * this.pdpPerRow)) {
			logger.warn("kraken rrd: assertion failed in pushNewRow: lastRow: {}, newRow: {}, diff: {}, expected: {}",
					new Object[] { getLastUpdate(), lTime, lTime - getLastUpdate(), raw.getStep() * this.pdpPerRow });
			return null;
		}

		return getNewRowNoCheck(lTime);
	}

	// cdp and pdp step: 10
	// pdp per row: 3
	// time : 0 10 20 30 40 50 60
	// pdp index: -1 0 1 2 3 4 5
	// cdp index: -1 0 0 0 1 1 1
	protected long getCdpIndex(long time) {
		return (getPdpIndex(time)) / pdpPerRow;
	}

	public long getPdpIndex(long time) {
		return (time - 1) / raw.getStep();
	}

	public long getLastPdpOfCdp(long cdp) {
		return (cdp + 1) * pdpPerRow - 1;
	}

	public int getRowCapacity() {
		return rowCapacity;
	}

	public boolean isCdpExpired(long currentTime) {
		// return getCdpIndex(currentTime) - getCdpIndex(rrd.get().getLastUpdateLong()) >= 1;
		ArchiveRow lastRow = rows[rowsIndex(0)];
		long archiveLastUpdate = lastRow.getDate().getTime() / 1000L;
		long cdpSeconds = raw.getStep() * pdpPerRow;
		long previousCdpExpiringTime = archiveLastUpdate / cdpSeconds * cdpSeconds + cdpSeconds;
		return currentTime > previousCdpExpiringTime;
	}

	public void setPdpPerRow(int pdpPerRow) {
		this.pdpPerRow = pdpPerRow;
	}

	public long getPdpPerRow() {
		return pdpPerRow;
	}

	protected long evalNextCheckpoint(long lastUpdate) {
		long step = raw.getStep();
		return (long) ((lastUpdate / (step * getPdpPerRow()) + 1) * step);
	}

	public void update(long time, long lastUpdate, DataSource dataSource, double checkpointValue) {
		assert (dataSource != null);
		ConsolidatedDataPoint cdp = dataSource.getCdp(this);
		cdp.update(time, lastUpdate, checkpointValue);
	}

	public List<FetchRow> fetchRows(long normalizedStartTime, long normalizedEndTime) {
		ArrayList<FetchRow> ret = new ArrayList<FetchRow>();
		long step = raw.getStep() * pdpPerRow;

		long startTime = (normalizedStartTime / step + 1) * step;
		// commenting "+ 1" makes slightly different result from linux rrdtools' one..
		// but we think it makes sense.
		long endTime = (normalizedEndTime / step /* + 1 */) * step;
		long lastUpdate = getLastUpdate();
		for (long t = startTime; t <= endTime; t += step) {
			ArchiveRow row = getRow(t, lastUpdate);
			if (row == null)
				ret.add(new EmptyRow(t, raw));
			else
				ret.add(row);
		}
		return ret;
	}

	private ArchiveRow getRow(long t, long lastUpdate) {
		long step = raw.getStep() * pdpPerRow;

		if (t % step != 0)
			return null;

		long bias = (lastUpdate % step == 0) ? 0 : -1;
		long k = getCdpIndex(lastUpdate) - getCdpIndex(t) + bias;

		if (0 <= k && k < rowsSize) {
			ArchiveRow row = rows[rowsIndex((int) k)];
			assert (row.getTimeInSec() == t);
			return row;
		}
		return null;
	}

	private int rowsIndex(int k) {
		return (rowsStart + rowCapacity + k) % rowCapacity;
	}

	public void setRrd(RrdRaw rrd) {
		this.raw = rrd;
	}

	public RrdRaw getRrd() {
		return raw;
	}

	public int length() {
		try {
			int len = func.toString().getBytes("utf-8").length + 2;
			len += 16;
			len += 8 + rowsSize * 4;
			return len;
		} catch (UnsupportedEncodingException e) {
			return 0;
		}
	}

	public void writeToPersLayer(PersistentLayer persLayer) throws IOException {
		persLayer.writeUTF(func.toString());
		persLayer.writeInt(pdpPerRow);
		persLayer.writeDouble(xff);
		persLayer.writeInt(rowCapacity);

		persLayer.writeInt(rowsSize);
		persLayer.writeInt(rowsStart);
		// from most recent to last row
		for (ArchiveRow row : rows)
			persLayer.writeInt(row.rowIndex);
	}

	public void dump(PrintWriter writer) {
		writer.printf("== archive func: %s, pdpPerRow: %d, xff: %f, capacity: %d ==\n", func.toString(), pdpPerRow, xff,
				this.rowCapacity);

		writer.printf("\n=== rows ===\n");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (long i = 0; i < rowsSize; ++i) {
			ArchiveRow row = rows[rowsIndex((int) i)];
			ArrayList<String> columnStrs = new ArrayList<String>();
			for (Double value : row.getColumns()) {
				columnStrs.add(String.format("%f", value));
			}
			writer.printf("%s: %s\n", df.format(row.getDate()), StringUtils.join(columnStrs.toArray()));
		}

	}

	@Override
	public boolean equals(Object obj) {
		Archive rhs = (Archive) obj;
		if (this.func != rhs.func)
			return false;
		if (this.pdpPerRow != rhs.pdpPerRow)
			return false;
		if (this.rowCapacity != rhs.rowCapacity)
			return false;
		if (this.xff != rhs.xff)
			return false;
		if (this.rows.length != rhs.rows.length)
			return false;
		for (int i = 0; i < this.rows.length; ++i) {
			if (!this.rows[rowsIndex(i)].equals(rhs.rows[rhs.rowsIndex(i)]))
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "Archive [func=" + func + ", pdpPerRow=" + pdpPerRow + ", rowCapacity=" + rowCapacity + "]";
	}

	private class ArchiveRow implements FetchRow {
		private static final long INVALID_TIME = Long.MIN_VALUE;
		private int rowIndex = -1;
		private long time = Long.MIN_VALUE;

		public ArchiveRow(int rowIndex, long time) {
			this.rowIndex = rowIndex;
			this.time = time;
		}

		@Override
		public double getColumn(int columnIndex) {
			return getColumn(raw.getDataSources().get(columnIndex));
		}

		@Override
		public double getColumn(String dataSourceName) {
			for (DataSource ds : raw.getDataSources()) {
				if (ds.getName().equals(dataSourceName))
					return getColumn(ds);
			}
			return Double.NaN;
		}

		private double getColumn(DataSource ds) {
			return ds.getData(Archive.this, rowIndex);
		}

		@Override
		public double[] getColumns() {
			int size = raw.getDataSources().size();
			double[] ret = new double[size];
			for (int i = 0; i < size; i++)
				ret[i] = getColumn(i);
			return ret;
		}

		@Override
		public Map<DataSourceConfig, Double> getColumnsMap() {
			Map<DataSourceConfig, Double> ret = new HashMap<DataSourceConfig, Double>();
			int i = 0;
			for (DataSource ds : raw.getDataSources())
				ret.put(RrdUtil.dataSourceToConfig(ds), getColumn(i++));
			return ret;
		}

		public void setColumn(DataSource ds, double value) {
			ds.setData(Archive.this, rowIndex, value);
		}

		@Override
		public Date getDate() {
			return new Date(time * 1000L);
		}

		public long getTimeInSec() {
			return time;
		}

		@Override
		public boolean equals(Object obj) {
			ArchiveRow rhs = (ArchiveRow) obj;
			if (this.time != rhs.time)
				return false;
			return true;
		}

		public String toString() {
			return "Time:" + Long.toString(time) + ", Columns:" + getColumns().toString();
		}
	}

}
