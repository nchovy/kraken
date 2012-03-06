package org.krakenapps.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment {
	public static String expandSystemProperties(String path) {
		Matcher m = Pattern.compile("\\$\\{(?!\\$\\{)(.*?)\\}").matcher(path);
		while (m.find()) {
			String replacement = System.getProperty(m.group(1));
			if (replacement == null)
				continue;
			path = path.replace(m.group(), replacement);
		}
		return path;
	}

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
		if (System.getProperty("kraken.cert.dir") == null)
			System.setProperty("kraken.cert.dir", new File(krakenDirProp, "cert").getAbsolutePath());
	}
}
