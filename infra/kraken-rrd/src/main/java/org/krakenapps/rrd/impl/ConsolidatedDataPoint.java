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

import org.krakenapps.rrd.ConsolidateFunc;
import org.krakenapps.rrd.io.PersistentLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsolidatedDataPoint {
	private Logger logger = LoggerFactory.getLogger(ConsolidatedDataPoint.class);

	private Archive archive;
	private double primaryValue = Double.NaN;
	private double secondaryValue = Double.NaN;
	private double value = Double.NaN;
	private long unknownDatapoints = 0;

	private double lastCheckpointValue = Double.NaN;

	public ConsolidatedDataPoint(Archive archive) {
		this.archive = archive;
	}

	public ConsolidatedDataPoint(Archive archive, PersistentLayer persLayer) throws IOException {
		this(archive);
		primaryValue = persLayer.readDouble();
		secondaryValue = persLayer.readDouble();
		value = persLayer.readDouble();
		unknownDatapoints = persLayer.readLong();
		lastCheckpointValue = persLayer.readDouble();
	}

	public void update(long currentTime, long lastUpdate, double checkpointValue) {
		long step = archive.getRrd().getStep();

		if (archive.isCdpExpired(currentTime - step))
			logger.debug("Cdp Expired unexpectedly: {}, {}", new Object[] { currentTime - step, archive.getLastUpdate() });

		if (Double.isNaN(value))
			value = checkpointValue;
		else if (archive.getCf() == ConsolidateFunc.AVERAGE || archive.getCf() == ConsolidateFunc.SUM)
			value = value + (Double.isNaN(checkpointValue) ? 0 : checkpointValue);
		else if (archive.getCf() == ConsolidateFunc.MAX)
			value = Math.max(value, (Double.isNaN(checkpointValue) ? Double.MIN_VALUE : checkpointValue));
		else if (archive.getCf() == ConsolidateFunc.MIN)
			value = Math.min(value, (Double.isNaN(checkpointValue) ? Double.MAX_VALUE : checkpointValue));
		else if (archive.getCf() == ConsolidateFunc.LAST)
			value = checkpointValue;

		// currentTime must be multiplier of step.
		// reason of -1: currentTime - lastUpdate is
		// from 1 to 10 : unknownDatapoints + 0
		// from 11 to 20 : unknownDatapoints + 1
		long cdpSeconds = step * archive.getPdpPerRow();
		long unknownSeconds = Math.min((currentTime - lastUpdate - 1), currentTime % cdpSeconds);
		this.unknownDatapoints += (unknownSeconds / step) + (Double.isNaN(checkpointValue) ? 1 : 0);

		// System.err.printf("cdp updated: value: %s (unknownDatapoints: %d)\n",
		// Double.toString(this.value), this.unknownDatapoints);

		this.lastCheckpointValue = checkpointValue;
	}

	public Double getCdpValue(long t) {
		// long lastUpdate = rrd.get().getLastUpdateLong();
		// long lastPdpIndex = archive.get().getLastPdpOfCdp(getCdpIndex(lastUpdate));
		// long numUnknown = this.unknownDatapoints + lastPdpIndex - getPdpIndex(lastUpdate);

		long lastPdpIndex = archive.getLastPdpOfCdp(archive.getCdpIndex(t));
		@SuppressWarnings("unused")
		long numUnknown = this.unknownDatapoints + lastPdpIndex - archive.getPdpIndex(t);

		/*
		 * xff The xfiles factor defines what part of a consolidation interval may be made up from *UNKNOWN* data while the
		 * consolidated value is still regarded as known. It is given as the ratio of allowed *UNKNOWN* PDPs to the number of PDPs
		 * in the interval. Thus, it ranges from 0 to 1 (exclusive).
		 */
		/*
		 * if ((double) numUnknown / (double) pdpPerRow > archive.get().getXff()) { System.err.println("warning: xff operated");
		 * return Double.NaN; }
		 */
		// XXX: because I can't find exact formula to calc xff(exactly numUnknown),
		// I removed concerning xff in evaluation cdp value temporarily.

		if (archive.getCf() == ConsolidateFunc.AVERAGE)
			return this.value / (archive.getPdpPerRow() /*- numUnknown*/);
		return this.value;
	}

	public void prepareNewCdp(double primaryValue) {
		this.primaryValue = primaryValue;
		this.secondaryValue = this.lastCheckpointValue;
		this.lastCheckpointValue = Double.NaN;
		this.value = Double.NaN;
		this.unknownDatapoints = 0;
	}

	public void writeToPersLayer(PersistentLayer persLayer) throws IOException {
		persLayer.writeDouble(primaryValue);
		persLayer.writeDouble(secondaryValue);
		persLayer.writeDouble(value);
		persLayer.writeLong(unknownDatapoints);
		persLayer.writeDouble(lastCheckpointValue);
	}

	public void dump(PrintWriter writer) {
		writer.printf("primary: %f, secondary: %f, value: %f\n", primaryValue, secondaryValue, value);
		writer.printf("unknown datapoints: %d, lastCheckpointValue: %f", unknownDatapoints, lastCheckpointValue);
	}

	@Override
	public boolean equals(Object obj) {
		ConsolidatedDataPoint rhs = (ConsolidatedDataPoint) obj;
		if (!RrdUtil.doubleEqual(this.primaryValue, rhs.primaryValue))
			return false;
		if (!RrdUtil.doubleEqual(this.secondaryValue, rhs.secondaryValue))
			return false;
		if (!RrdUtil.doubleEqual(this.value, rhs.value))
			return false;
		if (this.unknownDatapoints != rhs.unknownDatapoints)
			return false;
		if (this.lastCheckpointValue != rhs.lastCheckpointValue)
			return false;

		return true;
	}
}