package org.krakenapps.api;

import java.io.File;

public class Environment {
	public static void setKrakenSystemProperties(String krakenDir) {
		if (System.getProperty("kraken.dir") == null) {
			System.setProperty("kraken.dir", new File(krakenDir).getAbsolutePath());
		}

		String krakenDirProp = System.getProperty("kraken.dir");
		if (System.getProperty("kraken.data.dir") == null)
			System.setProperty("kraken.data.dir", new File(krakenDirProp, "data").getAbsolutePath());
		if (System.getProperty("kraken.log.dir") == null)
			System.setProperty("kraken.log.dir", new File(krakenDirProp, "log").getAbsolutePath());
		if (System.getProperty("kraken.cache.dir") == null)
			System.setProperty("kraken.cache.dir", new File(krakenDirProp, "cache").getAbsolutePath());
		if (System.getProperty("kraken.download.dir") == null)
			System.setProperty("kraken.download.dir", new File(krakenDirProp, "download").getAbsolutePath());
	}

}
