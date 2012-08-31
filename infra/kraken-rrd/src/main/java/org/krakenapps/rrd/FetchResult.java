package org.krakenapps.rrd;

import java.lang.ref.WeakReference;
import java.util.List;

import org.krakenapps.rrd.impl.Archive;
import org.krakenapps.rrd.impl.RrdRaw;

public class FetchResult {
	private List<FetchRow> rows;
	private WeakReference<RrdRaw> rrd;

	public FetchResult(RrdRaw rrd, Archive archive, long start, long end) {
		this.rrd = new WeakReference<RrdRaw>(rrd);
		long step = rrd.getStep();
		long normalizedStartTime = start / step * step;
		long normalizedEndTime = end / step * step;
		this.rows = archive.fetchRows(normalizedStartTime, normalizedEndTime);
	}

	public List<FetchRow> getRows() {
		return rows;
	}

	public RrdRaw getRrdRaw() {
		return rrd.get();
	}
}
