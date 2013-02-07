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
package org.krakenapps.rrd;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.krakenapps.rrd.impl.DataSource;
import org.krakenapps.rrd.impl.RrdRaw;
import org.krakenapps.rrd.impl.RrdUtil;
import org.krakenapps.rrd.io.FilePersistentLayer;
import org.krakenapps.rrd.io.MemoryPersistentLayer;
import org.krakenapps.rrd.io.PersistentLayer;

public class RrdTest {
	// TODO: RRD memory persistent layer memory reallocation test.

	@Test
	public void CompactMemoryRrdTest() throws InterruptedException, IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException {
		try {
			SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			df.setLenient(true);
			RrdConfig config = new RrdConfig(df.parse("Fri Jan 29 22:08:32 KST 2010"), 10);
			config.addDataSource("asdf", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
			config.addArchive(ConsolidateFunc.SUM, 0.5, 6, 10);
			CompactRrd cmr = new CompactRrd(new MemoryPersistentLayer(), config);

			while (getCompactMemoryRrdRaw(cmr) != null) {
				Runtime.getRuntime().gc();
				Thread.sleep(100);
			}
			updateRrd(df, cmr);

			ConcurrentHashMap<String, CompactRrd> rrds = new ConcurrentHashMap<String, CompactRrd>();
			rrds.put("asdf", cmr);

			Runtime.getRuntime().gc();
			try {
				rrds.get("asdf").save(new FilePersistentLayer(new File("CompactMemoryRrdTest.bin")));
			} catch (IOException e) {
				e.printStackTrace();
			}

			cmr = null;

			// wait for gc-ed.
			while (getCompactMemoryRrdRaw(rrds.get("asdf")) != null) {
				Runtime.getRuntime().gc();
				Thread.sleep(100);
			}

			try {
				rrds.get("asdf").save(new FilePersistentLayer(new File("CompactMemoryRrdTest.bin")));
			} catch (IOException e) {
				e.printStackTrace();
			}

			FetchResult fetch = rrds.get("asdf").fetch(ConsolidateFunc.SUM, df.parse("Fri Jan 29 22:10:00 KST 2010"),
					df.parse("Fri Jan 29 22:16:00 KST 2010"), 60);

			for (FetchRow row : fetch.getRows()) {
				assertTrue(row.getColumn(0) == 1.0);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private RrdRaw getCompactMemoryRrdRaw(CompactRrd cmr) throws NoSuchFieldException, IllegalAccessException {
		Field pImplField = cmr.getClass().getDeclaredField("rawref");
		pImplField.setAccessible(true);
		RrdRaw rawImpl = ((WeakReference<RrdRaw>) pImplField.get(cmr)).get();
		return rawImpl;
	}

	@Test
	public void persistentLayerEqualityTest() {
		try {
			String testfileName = "equalityTest.bin";
			FilePersistentLayer fileLayer = new FilePersistentLayer(new File(testfileName));
			MemoryPersistentLayer memoryLayer = new MemoryPersistentLayer();

			SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			df.setLenient(true);
			RrdConfig config;

			config = new RrdConfig(df.parse("Fri Jan 29 22:08:32 KST 2010"), 10);
			config.addDataSource("asdf", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
			config.addArchive(ConsolidateFunc.SUM, 0.5, 6, 10);
			Rrd fileRrd = new DefaultRrd(fileLayer, config);

			updateRrd(df, fileRrd);

			fileRrd.save();
			fileRrd.save(memoryLayer);

			fileRrd = new DefaultRrd(new FilePersistentLayer(new File(testfileName)));

			FetchResult fetch = fileRrd.fetch(ConsolidateFunc.SUM, df.parse("Fri Jan 29 22:10:00 KST 2010"),
					df.parse("Fri Jan 29 22:16:00 KST 2010"), 60);

			for (FetchRow row : fetch.getRows()) {
				assertTrue(row.getColumns()[0] == 1.0);
			}

			ByteBuffer memoryData = ByteBuffer.allocate(8192), fileData = ByteBuffer.allocate(8192);
			int memorySize = memoryLayer.read(memoryData.array(), 0, 8192);
			memoryData.limit(memorySize);
			memoryData.flip();
			FileInputStream fis = new FileInputStream(new File(testfileName));
			int fileSize = fis.read(fileData.array());
			fileData.limit(fileSize);
			fileData.flip();
			assertTrue(memoryData.equals(fileData));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private void updateRrd(SimpleDateFormat df, Rrd rrd) throws ParseException {
		rrd.update(df.parse("Fri Jan 29 22:09:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:10:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:11:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:12:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:13:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:14:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:15:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
		rrd.update(df.parse("Fri Jan 29 22:16:04 KST 2010"), (Double[]) Arrays.asList(1.0).toArray());
	}

	@Test
	public void instantiationTest() {
		try {
			SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			df.setLenient(true);
			RrdConfig config;

			config = new RrdConfig(df.parse("Fri Jan 29 22:08:32 KST 2010"), 10);
			config.addDataSource("asdf", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
			config.addArchive(ConsolidateFunc.SUM, 0.5, 6, 10);
			FilePersistentLayer persLayer = new FilePersistentLayer(new File("instantiationTest.bin"));
			Rrd rrd = new DefaultRrd(persLayer, config);
			updateRrd(df, rrd);

			rrd = new DefaultRrd(new FilePersistentLayer(new File("instantiationTest.bin")));

			FetchResult fetch = rrd.fetch(ConsolidateFunc.SUM, df.parse("Fri Jan 29 22:10:00 KST 2010"),
					df.parse("Fri Jan 29 22:16:00 KST 2010"), 60);

			for (FetchRow row : fetch.getRows()) {
				assertTrue(row.getColumns()[0] == 1.0);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testXff() {
		SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		df.setLenient(true);

		try {
			// long start = df.parse("Fri Jan 29 22:08:04 KST 2010").getTime() / 1000;
			RrdConfig config = new RrdConfig(df.parse("Fri Jan 29 22:08:32 KST 2010"), 10);
			config.addDataSource("asdf", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
			config.addArchive(ConsolidateFunc.SUM, 0.5, 6, 10);
			// RrdRawImpl rrd = new RrdRawImpl(new MemoryPersistentLayer());
			DefaultRrd rrd = new DefaultRrd(new MemoryPersistentLayer(), config);
			updateRrd(df, rrd);

			FetchResult fetch = rrd.fetch(ConsolidateFunc.SUM, df.parse("Fri Jan 29 22:10:00 KST 2010"),
					df.parse("Fri Jan 29 22:16:00 KST 2010"), 60);

			for (FetchRow row : fetch.getRows()) {
				assertTrue(row.getColumns()[0] == 1.0);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testRrdSum() throws InterruptedException {
		RrdConfig config = new RrdConfig(new Date(2010, 1, 22, 0, 59, 59), 10);
		config.addDataSource("test", DataSourceType.ABSOLUTE2, 10, Double.NaN, Double.NaN);
		config.addArchive(ConsolidateFunc.SUM, 0.5, 3, 12);

		RrdRaw rrd = new RrdRaw(config);
		Date start = new Date(2010, 1, 22, 1, 0, 0);
		rrd.update(start, new Double[] { 1d });
		rrd.update(new Date(2010, 1, 22, 1, 0, 10), (Double[]) Arrays.asList(2d).toArray());
		rrd.update(new Date(2010, 1, 22, 1, 0, 20), (Double[]) Arrays.asList(3d).toArray());
		Date end = new Date(2010, 1, 22, 1, 0, 30);
		rrd.update(end, new Double[] { 3d });

		FetchResult fetch = rrd.fetch(ConsolidateFunc.SUM, start, end, 30);
		FetchRow r = fetch.getRows().get(0);
		assertTrue(8.0d == (double) r.getColumns()[0]);
	}

	@Test
	public void testRrdPersistentLayer() {
		Calendar cl = Calendar.getInstance();
		cl.set(2009, Calendar.NOVEMBER, 14, 0, 0, 0);

		long startTime = cl.getTime().getTime() / 1000;
		long step = 10;

		RrdConfig config = new RrdConfig(makeDate(startTime - 1), step);
		config.addDataSource("test", DataSourceType.GAUGE, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.COUNTER, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.DERIVE, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.ABSOLUTE, step * 2, Double.NaN, Double.NaN);
		// per a minute, during 10 minutes
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.MAX, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.MIN, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.LAST, 0.5, 6, 10);
		// per a 10 minutes, during 3 hours
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.MAX, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.MIN, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.LAST, 0.5, 60, 18);

		FilePersistentLayer pl;
		try {
			pl = new FilePersistentLayer(new File("rrd_perslayer_test.bin"));

			long t = startTime;
			ArrayList<DoubleSample> samples = new ArrayList<DoubleSample>();
			samples.add(new SinSample(t));
			samples.add(new SinCounterSample(t));
			samples.add(new SinSample(t));
			samples.add(new SinSample(t));

			RrdRaw rrd1 = new RrdRaw(config);
			for (t = startTime; t < startTime + 32 * 6 * step; t += step) {
				Double row[] = new Double[samples.size()];
				int colIndex = 0;
				for (DoubleSample sample : samples) {
					row[colIndex++] = sample.getSample(t);
				}
				rrd1.update(new Date(t * 1000), (Double[]) row);
			}
			rrd1.write(pl);
			pl.close();

			RrdRaw rrd2 = new RrdRaw(pl);

			assertTrue(rrd1.equals(rrd2));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPersistentLayer() {
		PersistentLayer persLayer = new MemoryPersistentLayer();
		testPersistentLayer(persLayer);
		try {
			persLayer = new FilePersistentLayer(new File("test.bin"));
			testPersistentLayer(persLayer);
			persLayer.close();
			persLayer = new FilePersistentLayer(new File("test.bin"));
			testPersistentLayerReading(persLayer);
			persLayer.close();

			File file = new File("test.bin");
			assertTrue(file.exists());
			assertTrue(file.delete());
		} catch (IOException e) {
			e.printStackTrace();
			assert (false);
		}
	}

	private void testPersistentLayerReading(PersistentLayer persLayer) throws IOException {
		assertTrue(persLayer.readBoolean() == true);
		assertTrue(persLayer.readBoolean() == false);
		assertTrue(persLayer.readByte() == (byte) 0x7);
		assertTrue(persLayer.readDouble() == 0.573);
		assertTrue(persLayer.readLong() == 0x573765);
		assertTrue(persLayer.readUTF().equals("stania"));
		assertTrue(persLayer.readLong() == 0x765573);
		assertTrue(persLayer.readLong() == 0x75595251);
		assertTrue(persLayer.readEnum(ConsolidateFunc.class) == ConsolidateFunc.AVERAGE);
	}

	private void testPersistentLayer(PersistentLayer persLayer) {
		try {
			persLayer.writeBoolean(true);
			persLayer.writeBoolean(false);
			persLayer.writeByte(0x7);
			persLayer.writeDouble(0.573);
			persLayer.writeLong(0x573765);
			persLayer.writeUTF("stania");
			persLayer.writeLong(0x765573);
			persLayer.writeLong(0x75595251);
			persLayer.writeEnum(ConsolidateFunc.AVERAGE);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void rrdBasicTest() {
		Calendar cl = Calendar.getInstance();
		cl.set(2009, Calendar.NOVEMBER, 14, 0, 0, 0);

		long startTime = cl.getTime().getTime() / 1000;

		RrdConfig config = new RrdConfig(new Date(startTime * 1000), 10);
		config.addDataSource("test", DataSourceType.GAUGE, 20, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.COUNTER, 20, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.DERIVE, 20, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.ABSOLUTE, 20, Double.NaN, Double.NaN);
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 1, 15);
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 3, 5);
		// config.addRoundRobinArchive(ConsolidateFunc.AVERAGE, 0.5, 60, 5);
		// config.addRoundRobinArchive(ConsolidateFunc.MAX, 0.5, 12, 25);
		// config.addRoundRobinArchive(ConsolidateFunc.MIN, 0.5, 12, 25);

		{
			RrdRaw rrd = new RrdRaw(config);
			long t = startTime;

			rrd.update(new Date(t * 1000 + 3000), new Double[] { 2.0, 2.0, 2.0, 2.0 });
			rrd.update(new Date(t * 1000 + 6000), new Double[] { 3.0, 3.0, 3.0, 3.0 });
			rrd.update(new Date(t * 1000 + 9000), new Double[] { 4.0, 4.0, 4.0, 4.0 });

			// datasource test
			ArrayList<DataSource> ds = getDataSource(rrd);
			// assertTrue("dataSource time test", row.getDate().equals(new
			// Date(t + 10)));
			assertTrue("dataSource Test1: GAUGE", RrdUtil.doubleEqual(ds.get(0).getCurrentValue(), 2.7000000000e+01));
			assertTrue("dataSource Test1: COUNTER", RrdUtil.doubleEqual(ds.get(1).getCurrentValue(), 2.0000000000e+00));
			assertTrue("dataSource Test1: DERIVE", RrdUtil.doubleEqual(ds.get(2).getCurrentValue(), 2.0000000000e+00));
			assertTrue("dataSource Test1: ABSOLUTE", RrdUtil.doubleEqual(ds.get(3).getCurrentValue(), 9.0000000000e+00));

			rrd.update(new Date(t * 1000 + 12000), new Double[] { 5.0, 5.0, 5.0, 5.0 });
			assertTrue("dataSource Test2: GAUGE", RrdUtil.doubleEqual(ds.get(0).getCurrentValue(), 1.0000000000e+01));
			assertTrue("dataSource Test2: COUNTER", RrdUtil.doubleEqual(ds.get(1).getCurrentValue(), 6.6666666667e-01));
			assertTrue("dataSource Test2: DERIVE", RrdUtil.doubleEqual(ds.get(2).getCurrentValue(), 6.6666666667e-01));
			assertTrue("dataSource Test2: ABSOLUTE", RrdUtil.doubleEqual(ds.get(3).getCurrentValue(), 3.3333333333e+00));

			FetchResult result = rrd.fetch(ConsolidateFunc.AVERAGE, makeDate(t), makeDate(t + 20), 10);
			FetchRow row = result.getRows().get(0);

			assertTrue("dataSource fetch Test: GAUGE", RrdUtil.doubleEqual(row.getColumns()[0], 3.2000000000e+00));
			assertTrue("dataSource fetch Test: COUNTER", RrdUtil.doubleEqual(row.getColumns()[1], 3.3333333333e-01));
			assertTrue("dataSource fetch Test: DERIVE", RrdUtil.doubleEqual(row.getColumns()[2], 3.3333333333e-01));
			assertTrue("dataSource fetch Test: ABSOLUTE", RrdUtil.doubleEqual(row.getColumns()[3], 1.0666666667e+00));
		}

		// TODO implement tests
		// heartbeat test
		// cf test
		// xff / unknown datapoint test
	}

	private Date makeDate(long time) {
		return new Date(time * 1000);
	}

	@Test
	public void rrdBasicTest2() {
		Calendar cl = Calendar.getInstance();
		cl.set(2009, Calendar.NOVEMBER, 14, 0, 0, 0);

		long startTime = cl.getTime().getTime() / 1000;
		long step = 10;

		RrdConfig config = new RrdConfig(makeDate(startTime - 1), step);
		config.addDataSource("test", DataSourceType.GAUGE, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.COUNTER, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.DERIVE, step * 2, Double.NaN, Double.NaN);
		config.addDataSource("test", DataSourceType.ABSOLUTE, step * 2, Double.NaN, Double.NaN);
		// per a minute, during 10 minutes
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.MAX, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.MIN, 0.5, 6, 10);
		config.addArchive(ConsolidateFunc.LAST, 0.5, 6, 10);
		// per a 10 minutes, during 3 hours
		config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.MAX, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.MIN, 0.5, 60, 18);
		config.addArchive(ConsolidateFunc.LAST, 0.5, 60, 18);

		{
			RrdRaw rrd = new RrdRaw(config);
			long t = startTime;

			ArrayList<DoubleSample> samples = new ArrayList<DoubleSample>();
			samples.add(new SinSample(t));
			samples.add(new SinCounterSample(t));
			samples.add(new SinSample(t));
			samples.add(new SinSample(t));

			for (t = startTime; t < startTime + 32 * 6 * step; t += step) {
				Double row[] = new Double[samples.size()];
				int colIndex = 0;
				for (DoubleSample sample : samples) {
					row[colIndex++] = (sample.getSample(t));
				}
				rrd.update(new Date(t * 1000), row);
			}

			ArrayList<SampleRow> resultData = openResultData("");
			assertTrue("load sample result data", resultData != null);

			long fetchStartTime = startTime + 32 * 6 * step - 10 * 6 * step;
			long fetchEndTime = startTime + 32 * 6 * step;

			FetchResult result = rrd.fetch(ConsolidateFunc.AVERAGE, makeDate(fetchStartTime), makeDate(fetchEndTime), 10);

			for (int i = 0; i < resultData.size(); ++i) {
				FetchRow row = result.getRows().get(i);
				assertTrue(row.getTimeInSec() == resultData.get(i).time);
				assertTrue(RrdUtil.doubleListEqual(row.getColumns(), resultData.get(i).columns));
			}
		}
	}

	class SampleRow {
		public long time;
		public double[] columns;

		public SampleRow(long time, double[] columns) {
			this.time = time;
			this.columns = columns;
		}

		@Override
		public String toString() {
			return "Time:" + Long.toString(time) + ", Columns:" + columns.toString();
		}
	}

	private ArrayList<SampleRow> openResultData(String string) {
		ArrayList<SampleRow> ret = new ArrayList<SampleRow>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("src/test/resources/AverageSample.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		String line = null;
		try {
			line = reader.readLine();
			while (line != null) {
				int colonPos = line.indexOf(':');
				if (colonPos != -1) {
					long t = Long.parseLong(line.substring(0, colonPos));

					String[] tokens = line.substring(colonPos + 1).split(" ");
					ArrayList<Double> rowList = new ArrayList<Double>();
					for (String strNumber : tokens) {
						try {
							if (strNumber.equals("nan"))
								rowList.add(Double.NaN);
							else
								rowList.add(Double.parseDouble(strNumber));
						} catch (NumberFormatException e) {
						}
					}

					double[] row = new double[rowList.size()];
					for (int i = 0; i < row.length; ++i)
						row[i] = rowList.get(i);
					ret.add(new SampleRow(t, row));
				}
				line = reader.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public class DoubleSample {
		private long startTime;

		public DoubleSample(long startTime) {
			this.startTime = startTime;
		}

		public void setStartTime(long t) {
			startTime = t;
		}

		public long getStartTime() {
			return startTime;
		}

		public double getSample(long t) {
			return t;
		}
	}

	public class SinSample extends DoubleSample {
		public SinSample(long startTime) {
			super(startTime);
		}

		@Override
		public double getSample(long t) {
			return (long) (Math.sin((t - getStartTime()) / 30.0) * 50 + 50);
		}
	}

	public class SinCounterSample extends SinSample {
		long lastSample;

		public SinCounterSample(long startTime) {
			super(startTime);
			this.lastSample = 0;
		}

		@Override
		public double getSample(long t) {
			double ret = this.lastSample + (long) super.getSample(t);
			this.lastSample = (long) ret;
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<DataSource> getDataSource(RrdRaw rrd) {
		ArrayList<DataSource> dataSources = null;
		try {
			Field f = rrd.getClass().getDeclaredField("dataSources");
			f.setAccessible(true);
			dataSources = (ArrayList<DataSource>) f.get(rrd);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return dataSources;
	}

	@Test
	public void dynamicDataSources() {
		long startTime = System.currentTimeMillis() - 10000;
		RrdConfig config = new RrdConfig(new Date(startTime), 1);
		config.addDataSource("ds1", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
		config.addArchive(ConsolidateFunc.SUM, 0.5, 1, 10);

		Rrd rrd = new DefaultRrd(new MemoryPersistentLayer(), config);
		Map<String, Double> data = new HashMap<String, Double>();
		data.put("ds1", 1.0);
		data.put("ds2", 1.0);
		rrd.update(new Date(startTime + 1000), data);
		rrd.update(new Date(startTime + 2000), data);
		rrd.update(new Date(startTime + 3000), data);

		rrd.addDataSource("ds2", DataSourceType.ABSOLUTE2, 20, Double.NaN, Double.NaN);
		rrd.update(new Date(startTime + 4000), data);
		rrd.update(new Date(startTime + 5000), data);
		rrd.update(new Date(startTime + 6000), data);

		// rrd.removeDataSource("ds1");
		rrd.update(new Date(startTime + 7000), data);
		rrd.update(new Date(startTime + 8000), data);
		rrd.update(new Date(startTime + 9000), data);
		rrd.update(new Date(startTime + 10000), data);

		FetchResult fetch = rrd.fetch(ConsolidateFunc.SUM, new Date(startTime), new Date(startTime + 10000), 1);
		for (FetchRow row : fetch.getRows()) {
			System.out.println(row.getDate() + " " + row.getColumnsMap());
		}
	}
}
