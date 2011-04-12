package org.krakenapps.rrd;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public interface Rrd {
	String LoggerPrefix = "Rrd-";

	boolean load(InputStream istream);

	void save();
	void save(OutputStream ostream);

	void update(Date time, Double[] values);

	FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution);

	void dump(OutputStream stream);

}
