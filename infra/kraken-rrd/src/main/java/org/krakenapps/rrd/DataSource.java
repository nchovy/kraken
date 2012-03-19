package org.krakenapps.rrd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

public abstract class DataSource {
	@Override
	public boolean equals(Object obj) {
		DataSource rhs = (DataSource) obj;

		if (!this.name.equals(rhs.name))
			return false;
		if (this.type != rhs.type)
			return false;
		if (this.minimalHeartbeat != rhs.minimalHeartbeat)
			return false;
		if (!RrdRawImpl.doubleEqual(this.min, rhs.min) || !RrdRawImpl.doubleEqual(this.max, rhs.max))
			return false;
		if (!RrdRawImpl.doubleEqual(this.last, rhs.last))
			return false;
		if (!RrdRawImpl.doubleEqual(this.value, rhs.value))
			return false;
		if (this.unknownSec != rhs.unknownSec)
			return false;
		if (this.columnIndex != rhs.columnIndex)
			return false;

		return true;
	}

	public enum Type {
		GAUGE, COUNTER, DERIVE, ABSOLUTE, ABSOLUTE2
	}

	private String name;
	private Type type;
	private long minimalHeartbeat;
	private double min;
	private double max;

	protected double last;
	protected double value;
	protected long unknownSec;

	//	private long lastUpdate;
	protected WeakReference<RrdRawImpl> rrd;

	private int columnIndex;

	public DataSource(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		this.rrd = new WeakReference<RrdRawImpl>(rrd);
		this.name = dsConfig.getName();
		this.type = dsConfig.getType();
		this.minimalHeartbeat = dsConfig.getMinimalHeartbeat();
		this.min = dsConfig.getMin();
		this.max = dsConfig.getMax();

		this.last = Double.NaN;
		this.value = Double.NaN;
		this.unknownSec = 0;
		this.columnIndex = columnIndex;
	}

	protected abstract double getValueDelta(long time, long lastUpdate, double newValue);

	protected abstract void onNextCheckpointPassed(long time, long lastUpdate, long nextCheckpoint, double newValue);

	public void update(long currentTime, long lastUpdate, double newValue) {
		long step = rrd.get().getStep();
		long expectedLastCheckpoint = ((lastUpdate / step) + 1) * step;
		long previousCheckpoint = ((currentTime / step)) * step;

		if (Double.isNaN(this.last) && Double.isNaN(this.value)) {
			this.unknownSec = currentTime - previousCheckpoint;
		}

		if (currentTime != lastUpdate) {
			if (expectedLastCheckpoint <= currentTime) {
				onNextCheckpointPassed(currentTime, lastUpdate, previousCheckpoint, newValue);
				this.unknownSec = 0;
			} else {
				double valueDelta = getValueDelta(currentTime, lastUpdate, newValue);
				this.value = Double.isNaN(this.value) ? valueDelta : this.value + valueDelta;
			}
		} else {
			// currentTime == lastUpdate
			if (currentTime % step == 0) {
				onNextCheckpointPassed(currentTime, lastUpdate, previousCheckpoint, newValue);
			} else {
				updateLast(newValue);
			}
		}
		this.last = newValue;
	}

	protected long evalNextCheckpoint(long lastUpdate) {
		long step = rrd.get().getStep();
		return ((lastUpdate / step) + 1) * step;
	}

	public double getLastValue() {
		return this.last;
	}

	public double getCurrentValue() {
		return this.value;
	}

	public static DataSource createInstance(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		switch (dsConfig.getType()) {
		case COUNTER:
			return new DataSourceCounter(rrd, dsConfig, lastUpdate, columnIndex);
		case DERIVE:
			return new DataSourceDerive(rrd, dsConfig, lastUpdate, columnIndex);
		case ABSOLUTE:
			return new DataSourceAbsolute(rrd, dsConfig, lastUpdate, columnIndex);
		case ABSOLUTE2:
			return new DataSourceSum(rrd, dsConfig, lastUpdate, columnIndex);
		default:
			return new DataSourceGauge(rrd, dsConfig, lastUpdate, columnIndex);
		}
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	//	public long getLastUpdate() {
	//		if (Double.isNaN(this.last)) {
	//			long step = rrd.get().getStep();
	//			long startTime = rrd.get().getStartTimeLong();
	//			long recentCheckpoint = startTime / step * step;
	//			return Math.max(startTime, recentCheckpoint);
	//		} else
	//			return lastUpdate;
	//	}

	public static double linearInterpolation(double target, double x1, double x2, double v1, double v2) {
		return v1 + (v2 - v1) / (x2 - x1) * (target - x1);
	}

	public void writeToPersLayer(ObjectOutputStream oos) throws IOException {
		oos.writeUTF(type.toString());
		oos.writeUTF(name);
		oos.writeLong(minimalHeartbeat);
		oos.writeDouble(min);
		oos.writeDouble(max);
		oos.writeDouble(last);
		oos.writeDouble(value);
		oos.writeLong(unknownSec);
	}

	public static DataSource createFromPersLayer(RrdRawImpl rrdSource, ObjectInputStream ois, int i)
			throws IOException {
		Type type = Type.valueOf(ois.readUTF());
		String name = ois.readUTF();
		long minimalHeartbeat = ois.readLong();
		double min = ois.readDouble();
		double max = ois.readDouble();
		double last = ois.readDouble();
		double value = ois.readDouble();
		long unknownSec = ois.readLong();

		DataSourceConfig config = new DataSourceConfig(name, type, minimalHeartbeat, min, max);
		DataSource newDataSource = DataSource.createInstance(rrdSource, config,
				rrdSource.getLastUpdateDate().getTime() / 1000, i);
		newDataSource.value = value;
		newDataSource.unknownSec = unknownSec;
		newDataSource.last = last;

		return newDataSource;
	}

	public String getName() {
		return name;
	}

	public void dump(PrintWriter writer) {
		writer.printf("== %s ==\ntype: %s\n", name, type.toString());
		writer.printf("minimalHeartbaet: %d\n", minimalHeartbeat);
		writer.printf("min: %f, max: %f\n", min, max);
		writer.printf("last: %f, value: %f, unknownSec: %s\n", last, value, unknownSec);
	}

	public abstract void updateLast(Double value);
}

class DataSourceGauge extends DataSource {
	public DataSourceGauge(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		super(rrd, dsConfig, lastUpdate, columnIndex);
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
		double checkpointValue = interpolated / rrd.get().getStep();

		rrd.get().updateArchives(time, lastUpdate, this, checkpointValue, newValue);

		this.value = Double.isNaN(interpolated) ? 0 : nextValue - interpolated;
	}

	@Override
	public void updateLast(Double value) {
	}
}

class DataSourceCounter extends DataSource {
	public DataSourceCounter(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		super(rrd, dsConfig, lastUpdate, columnIndex);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / (rrd.get().getStep() - unknownSec);
		rrd.get().updateArchives(time, lastUpdate, this, checkpointValue, newValue);

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
	public DataSourceDerive(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		super(rrd, dsConfig, lastUpdate, columnIndex);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = Double.isNaN(this.value) ? valueDelta : this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / (rrd.get().getStep() - unknownSec);
		rrd.get().updateArchives(time, lastUpdate, this, checkpointValue, newValue);

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
	public DataSourceAbsolute(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		super(rrd, dsConfig, lastUpdate, columnIndex);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		double nextValue = this.value + valueDelta;

		double interpolated = linearInterpolation(checkpoint, lastUpdate, time, this.value, nextValue);
		double checkpointValue = interpolated / rrd.get().getStep();
		rrd.get().updateArchives(time, lastUpdate, this, checkpointValue, newValue);

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
	public DataSourceSum(RrdRawImpl rrd, DataSourceConfig dsConfig, long lastUpdate, int columnIndex) {
		super(rrd, dsConfig, lastUpdate, columnIndex);
	}

	@Override
	protected void onNextCheckpointPassed(long time, long lastUpdate, long checkpoint, double newValue) {
		if (Double.isNaN(newValue))
			newValue = 0;

		double valueDelta = getValueDelta(time, lastUpdate, newValue);

		if (Double.isNaN(this.value))
			this.value = 0;

		rrd.get().updateArchives(time, lastUpdate, this,
				time == checkpoint ? this.value + valueDelta : this.value, newValue);

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