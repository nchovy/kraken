package org.krakenapps.rrd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FetchResultImpl implements FetchResult {
	private ArrayList<Row> rows;
	private WeakReference<RrdRawImpl> rrd;

	public FetchResultImpl(RrdRawImpl rrd, Archive archive, long start, long end) {
		this.rrd = new WeakReference<RrdRawImpl>(rrd);
		long step = rrd.getStep();
		long normalizedStartTime = start / step * step;
		long normalizedEndTime = end / step * step;
		rows = archive.fetchRows(normalizedStartTime, normalizedEndTime);
	}

	@Override
	public List<Row> getRows() {
		return rows;
	}

	@Override
	public RrdRawImpl getRRD() {
		return rrd.get();
	}

}
