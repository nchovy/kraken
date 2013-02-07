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
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.rrd.DataSourceConfig;
import org.krakenapps.rrd.DataSourceType;
import org.krakenapps.rrd.io.PersistentLayer;

public abstract class DataSource {
	protected RrdRaw raw;

	private String name;
	private DataSourceType type;
	private long minimalHeartbeat;
	private double min;
	private double max;

	protected double last = Double.NaN;
	protected double value = Double.NaN;
	protected long unknownSec = 0;

	private Map<Archive, ConsolidatedDataPoint> cdps;
	private Map<Archive, long[]> data;

	public static DataSource createInstance(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		switch (dsConfig.getType()) {
		case COUNTER:
			return new DataSourceCounter(rrd, dsConfig, lastUpdate);
		case DERIVE:
			return new DataSourceDerive(rrd, dsConfig, lastUpdate);
		case ABSOLUTE:
			return new DataSourceAbsolute(rrd, dsConfig, lastUpdate);
		case ABSOLUTE2:
			return new DataSourceSum(rrd, dsConfig, lastUpdate);
		default:
			return new DataSourceGauge(rrd, dsConfig, lastUpdate);
		}
	}

	public static DataSource createFromPersLayer(RrdRaw raw, PersistentLayer persLayer) throws IOException {
		DataSourceType type = DataSourceType.valueOf(persLayer.readUTF());
		String name = persLayer.readUTF();
		long minimalHeartbeat = persLayer.readLong();
		double min = persLayer.readDouble();
		double max = persLayer.readDouble();
		double last = persLayer.readDouble();
		double value = persLayer.readDouble();
		long unknownSec = persLayer.readLong();

		DataSourceConfig config = new DataSourceConfig(name, type, minimalHeartbeat, min, max);
		DataSource newDataSource = DataSource.createInstance(raw, config, raw.getLastUpdateDate().getTime() / 1000);
		newDataSource.value = value;
		newDataSource.unknownSec = unknownSec;
		newDataSource.last = last;
		newDataSource.cdps = new HashMap<Archive, ConsolidatedDataPoint>(raw.getArchives().size());
		for (Archive archive : raw.getArchives())
			newDataSource.cdps.put(archive, new ConsolidatedDataPoint(archive, persLayer));

		newDataSource.data = new HashMap<Archive, long[]>(raw.getArchives().size());
		for (Archive archive : raw.getArchives()) {
			long[] data = new long[archive.getRowCapacity()];
			for (int i = 0; i < data.length; i++)
				data[i] = persLayer.readLong();
			newDataSource.data.put(archive, data);
		}

		return newDataSource;
	}

	public DataSource(RrdRaw raw, DataSourceConfig dsConfig, long lastUpdate) {
		this.raw = raw;
		this.name = dsConfig.getName();
		this.type = dsConfig.getType();
		this.minimalHeartbeat = dsConfig.getMinimalHeartbeat();
		this.min = dsConfig.getMin();
		this.max = dsConfig.getMax();
		this.cdps = new HashMap<Archive, ConsolidatedDataPoint>(raw.getArchives().size());
		for (Archive archive : raw.getArchives())
			cdps.put(archive, new ConsolidatedDataPoint(archive));
		this.data = new HashMap<Archive, long[]>(raw.getArchives().size());
		for (Archive archive : raw.getArchives())
			data.put(archive, new long[archive.getRowCapacity()]);
	}

	public String getName() {
		return name;
	}

	public DataSourceType getType() {
		return type;
	}

	public long getMinimalHeartbeat() {
		return minimalHeartbeat;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getLastValue() {
		return last;
	}

	public double getCurrentValue() {
		return value;
	}

	public ConsolidatedDataPoint getCdp(Archive archive) {
		return cdps.get(archive);
	}

	public double getData(Archive archive, int index) {
		return Double.longBitsToDouble(data.get(archive)[index]);
	}

	public void setData(Archive archive, int index, double value) {
		data.get(archive)[index] = Double.doubleToRawLongBits(value);
	}

	protected abstract double getValueDelta(long time, long lastUpdate, double newValue);

	protected abstract void onNextCheckpointPassed(long time, long lastUpdate, long nextCheckpoint, double newValue);

	protected double linearInterpolation(double target, double x1, double x2, double v1, double v2) {
		return v1 + (v2 - v1) / (x2 - x1) * (target - x1);
	}

	public void update(long currentTime, long lastUpdate, double newValue) {
		long step = raw.getStep();
		long expectedLastCheckpoint = ((lastUpdate / step) + 1) * step;
		long previousCheckpoint = ((currentTime / step)) * step;

		if (Double.isNaN(this.last) && Double.isNaN(this.value))
			this.unknownSec = currentTime - previousCheckpoint;

		if (currentTime != lastUpdate) {
			if (expectedLastCheckpoint <= currentTime) {
				onNextCheckpointPassed(currentTime, lastUpdate, previousCheckpoint, newValue);
				this.unknownSec = 0;
			} else {
				double valueDelta = getValueDelta(currentTime, lastUpdate, newValue);
				this.value = Double.isNaN(this.value) ? valueDelta : this.value + valueDelta;
			}
		} else {
			if (currentTime % step == 0)
				onNextCheckpointPassed(currentTime, lastUpdate, previousCheckpoint, newValue);
			else
				updateLast(newValue);
		}

		this.last = newValue;
	}

	public int length() {
		try {
			int len = type.toString().getBytes("utf-8").length + 2;
			len += name.getBytes("utf-8").length + 2;
			len += 48;

			len += cdps.size() * 40;
			len += raw.getRowCapacity();
			return len;
		} catch (UnsupportedEncodingException e) {
			return 0;
		}
	}

	public void writeToPersLayer(PersistentLayer persLayer) throws IOException {
		persLayer.writeUTF(type.toString());
		persLayer.writeUTF(name);

		persLayer.writeLong(minimalHeartbeat);
		persLayer.writeDouble(min);
		persLayer.writeDouble(max);
		persLayer.writeDouble(last);
		persLayer.writeDouble(value);
		persLayer.writeLong(unknownSec);

		for (Archive key : raw.getArchives())
			cdps.get(key).writeToPersLayer(persLayer);

		for (Archive archive : raw.getArchives()) {
			for (long d : data.get(archive))
				persLayer.writeLong(d);
		}
	}

	public void dump(PrintWriter writer) {
		writer.printf("== %s ==\ntype: %s\n", name, type.toString());
		writer.printf("minimalHeartbaet: %d\n", minimalHeartbeat);
		writer.printf("min: %f, max: %f\n", min, max);
		writer.printf("last: %f, value: %f, unknownSec: %s\n", last, value, unknownSec);

		writer.printf("=== cdp ===\n");
		int cdpIndex = 0;
		for (Archive key : raw.getArchives()) {
			writer.printf("%d: \n", cdpIndex++);
			cdps.get(key).dump(writer);
		}
	}

	public abstract void updateLast(Double value);

	@Override
	public boolean equals(Object obj) {
		DataSource rhs = (DataSource) obj;

		if (!this.name.equals(rhs.name))
			return false;
		if (this.type != rhs.type)
			return false;
		if (this.minimalHeartbeat != rhs.minimalHeartbeat)
			return false;
		if (!RrdUtil.doubleEqual(this.min, rhs.min) || !RrdUtil.doubleEqual(this.max, rhs.max))
			return false;
		if (!RrdUtil.doubleEqual(this.last, rhs.last))
			return false;
		if (!RrdUtil.doubleEqual(this.value, rhs.value))
			return false;
		if (this.unknownSec != rhs.unknownSec)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getName();
	}
}

class DataSourceGauge extends DataSource {
	public DataSourceGauge(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		super(rrd, dsConfig, lastUpdate);
	}

	@Override
	protected double getValueDelta(long time, long lastUpdate, double newValue) {
		return newValue * (time - lastUpdate);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long nextCheckpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		double nextValue = Double.isNaN(this.value) ? valueDelta : this.value + valueDelta;

		double interpolated = linearInterpolation(nextCheckpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / raw.getStep();

		raw.updateArchives(time, lastUpdate, this, checkpointValue, newValue);

		this.value = Double.isNaN(interpolated) ? 0 : (nextValue - interpolated);
	}

	@Override
	public void updateLast(Double value) {
	}
}

class DataSourceCounter extends DataSource {
	public DataSourceCounter(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		super(rrd, dsConfig, lastUpdate);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / (raw.getStep() - unknownSec);
		raw.updateArchives(time, lastUpdate, this, checkpointValue, newValue);

		this.value = nextValue - (Double.isNaN(interpolated) ? 0 : interpolated);
	}

	@Override
	protected double getValueDelta(long time, long lastUpdate, double newValue) {
		return newValue - this.last;
	}

	@Override
	public void updateLast(Double value) {
	}
}

class DataSourceDerive extends DataSource {
	public DataSourceDerive(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		super(rrd, dsConfig, lastUpdate);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = Double.isNaN(this.value) ? valueDelta : this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / (raw.getStep() - unknownSec);
		raw.updateArchives(time, lastUpdate, this, checkpointValue, newValue);

		this.value = nextValue - (Double.isNaN(interpolated) ? 0 : interpolated);
	}

	@Override
	protected double getValueDelta(long time, long lastUpdate, double newValue) {
		return newValue - this.last;
	}

	@Override
	public void updateLast(Double value) {
	}
}

class DataSourceAbsolute extends DataSource {
	public DataSourceAbsolute(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		super(rrd, dsConfig, lastUpdate);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / raw.getStep();
		raw.updateArchives(time, lastUpdate, this, checkpointValue, newValue);

		this.value = nextValue - (Double.isNaN(interpolated) ? 0 : interpolated);
	}

	@Override
	protected double getValueDelta(long time, long lastUpdate, double newValue) {
		return newValue;
	}

	@Override
	public void updateLast(Double value) {
	}
}

class DataSourceSum extends DataSource {
	public DataSourceSum(RrdRaw rrd, DataSourceConfig dsConfig, long lastUpdate) {
		super(rrd, dsConfig, lastUpdate);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		if (Double.isNaN(newValue))
			newValue = 0;

		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		raw.updateArchives(time, lastUpdate, this, (time == checkpoint) ? (value + valueDelta) : value, newValue);

		this.value = time == checkpoint ? 0 : valueDelta;
	}

	@Override
	protected double getValueDelta(long time, long lastUpdate, double newValue) {
		return newValue;
	}

	@Override
	public void updateLast(Double value) {
		this.value += value;
	}
}