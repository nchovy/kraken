package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	private static final Logger logger = LoggerFactory.getLogger(Util.class.getName());

	private Util() {
	}

	public static String run(String cmd) {
		java.lang.Process p = null;
		BufferedReader br = null;
		StringBuilder builder = new StringBuilder();

		logger.info("kraken-linux-api: execute " + cmd);
		try {
			String line = null;
			p = Runtime.getRuntime().exec(cmd);

			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = br.readLine()) != null)
				builder.append(line + "\n");
			br.close();

			br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = br.readLine()) != null)
				logger.error("kraken-linux-api: " + line);
		} catch (IOException e) {
			logger.error("kraken-linux-api: io error", e);
		} finally {
			if (p != null)
				p.destroy();
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return builder.toString();
	}
}
