package org.krakenapps.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RrdRawImpl {
	private ArrayList<DataSource> dataSources;
	private ArrayList<Archive> archives;

	private long step;
	private long lastUpdate;

	public RrdRawImpl() {
	}

	public RrdRawImpl(RrdConfig config) {
		init(config);
	}

	public void init(RrdConfig config) {
		if (this.dataSources != null && this.dataSources.size() != 0)
			throw new RrdInitFailedException("instance already configured.");

		this.step = config.getStep();
		this.lastUpdate = config.getStartTimeLong();

		if (config.dataSources.size() == 0)
			throw new IllegalArgumentException("no data sources in config");

		dataSources = new ArrayList<DataSource>();
		int columnIndex = 0;
		for (DataSourceConfig dsConfig : config.dataSources) {
			dataSources.add(DataSource.createInstance(this, dsConfig, lastUpdate, columnIndex++));
		}

		if (config.archives.size() == 0)
			throw new IllegalArgumentException("no archives in config");

		archives = new ArrayList<Archive>();
		for (ArchiveConfig rraConfig : config.archives) {
			archives.add(new Archive(this, lastUpdate, dataSources, rraConfig));
		}
	}

	public void update(Date time, Double[] values) {
		if (values.length != dataSources.size())
			throw new IndexOutOfBoundsException("the size of values is not match with the number of data sources");
		if (time.getTime() / 1000 < this.lastUpdate)
			throw new IllegalArgumentException(String.format("time %1$d is not after lastUpdate %2$d",
					time.getTime() / 1000, this.lastUpdate));
		
		int i = 0;
		//		System.err.println("rrd.update: " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).format(time));
		long lTime = time.getTime() / 1000;
		processExpiredCdp(lTime);

		for (DataSource ds : dataSources) {
			ds.update(lTime, lastUpdate, values[i++]);
		}

		for (Archive ar : archives) {
			ar.onRrdUpdated(lTime);
		}
		this.lastUpdate = lTime;
	}

	public void processExpiredCdp(long lTime) {
		for (Archive ar : archives) {
			ar.processExpiredCdp(lTime);
		}
	}

	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		long lStart = start.getTime() / 1000;
		long lEnd = end.getTime() / 1000;
		Archive archive = selectArchive(f, resolution);
		assert (archive != null);
		return new FetchResultImpl(this, archive, lStart, lEnd);
	}

	private Archive selectArchive(ConsolidateFunc f, long resolution) {
		long step = this.getStep();

		Archive ret = null;
		long r = 0;

		// find longest step in archives of which step is smaller than required
		// resolution.
		// TODO: this implementation is different from original 'rrdtool fetch'.
		for (Archive ar : archives) {
			long archiveResolution = step * ar.getPdpPerRow();
			if (archiveResolution <= resolution && archiveResolution > r) {
				ret = ar;
				r = archiveResolution;
			}
		}
		if (ret != null)
			return ret;
		else
			return archives.get(0);
	}

	public long getStep() {
		return step;
	}

	public void updateArchives(long time, long lastUpdate, DataSource dataSource, double checkpointValue,
			double newValue) {
		for (Archive ar : archives) {
			ar.update(time, lastUpdate, dataSource, checkpointValue);
		}
	}

	public Date getLastUpdateDate() {
		return new Date(lastUpdate * 1000);
	}

	public void dump(OutputStream stream) {
		PrintWriter writer = new PrintWriter(stream);
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
	
	public static long getArchiveSize(RrdConfig config) {
		MemoryPersistentLayer persSource = new MemoryPersistentLayer();
		RrdRawImpl impl = new RrdRawImpl(config);
		impl.writeToPersLayer(new PersLayerOutputStream(persSource));
		return persSource.getByteBuffer().position();
	}

	public void writeToPersLayer(OutputStream os) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			
			oos.writeByte(1);
			oos.writeLong(this.step);
			oos.writeLong(this.lastUpdate);

			oos.writeLong(dataSources.size());
			for (DataSource ds : dataSources) {
				ds.writeToPersLayer(oos);
			}

			oos.writeLong(archives.size());
			for (Archive ar : archives) {
				ar.writeToPersLayer(oos);
			}
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean readFromPersLayer(InputStream is) {
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			int version = ois.readByte();
			if (version != 1)
				return false;

			step = ois.readLong();
			lastUpdate = ois.readLong();

			dataSources = new ArrayList<DataSource>();
			long dataSourceSize = ois.readLong();

			if (dataSourceSize == 0)
				throw new IllegalArgumentException("no data sources in config");

			for (int i = 0; i < dataSourceSize; ++i) {
				dataSources.add(DataSource.createFromPersLayer(this, ois, i));
			}

			archives = new ArrayList<Archive>();
			long archiveSize = ois.readLong();
			if (archiveSize == 0)
				throw new IllegalArgumentException("no archives in config");
			for (int i = 0; i < archiveSize; ++i) {
				archives.add(Archive.createFromPersLayer(this, dataSources, ois));
			}
			ois.close();
		} catch (StreamCorruptedException e) {
			// empty stream
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ParameterAssertionFailedException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public List<DataSource> getDataSources() {
		return dataSources;
	}

	@Override
	public boolean equals(Object obj) {
		RrdRawImpl rhs = (RrdRawImpl) obj;

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
			if (!this.dataSources.get(i).equals(rhs.dataSources.get(i))) {
				return false;
			}
		}

		int archiveSize = this.archives.size();
		for (int i = 0; i < archiveSize; ++i) {
			if (!this.archives.get(i).equals(rhs.archives.get(i))) {
				return false;
			}
		}

		return true;
	}

	public static final double epsilon = 1e-08;

	public static boolean doubleListEqual(double[] l1, double[] l2) {
		if (l1.length != l2.length)
			return false;

		for (int i = 0; i < l1.length; ++i) {
			if (!doubleEqual(l1[i], l2[i]))
				return false;
		}
		return true;
	}

	public static boolean doubleEqual(double v1, double v2) {
		return (Double.isNaN(v1) && Double.isNaN(v2)) || Math.abs(v1 - v2) < epsilon;
	}

	public static void main(String[] args) {
		String rrdFilename = args[0];

		System.out.printf("dumping %s\n", rrdFilename);
		try {
			FilePersistentLayer persSource = new FilePersistentLayer(new File(rrdFilename));
			RrdRawImpl rrd = new RrdRawImpl();
			rrd.readFromPersLayer(new PersLayerInputStream(persSource));
			rrd.dump(System.out);
			System.out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}