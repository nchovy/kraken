/*
 * Copyright 2009 NCHOVY
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
		if (System.getProperty("kraken.home.dir") == null)
			System.setProperty("kraken.home.dir", new File(krakenDirProp, "home").getAbsolutePath());
	}
}
