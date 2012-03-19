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
package org.krakenapps.geoip.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.geoip.GeoIpLocation;
import org.krakenapps.geoip.GeoIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoIpScript implements Script {
	private Logger logger = LoggerFactory.getLogger(GeoIpScript.class.getName());
	private GeoIpService geoip;
	private ScriptContext context;

	public GeoIpScript(GeoIpService geoip) {
		this.geoip = geoip;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "query ip location", arguments = { @ScriptArgument(name = "ip", type = "string", description = "ipv4 address") })
	public void locate(String[] args) {
		try {
			InetAddress address = InetAddress.getByName(args[0]);
			GeoIpLocation location = geoip.locate(address);
			if (location == null) {
				context.println("location not found");
				return;
			}

			context.println(location.toString());
		} catch (UnknownHostException e) {
			context.println("invalid ip address format");
			logger.warn("locate error", e);
		}
	}

	public void install(String[] args) {
		ZipFile zip = null;
		File base = new File(System.getProperty("kraken.data.dir"), "kraken-geoip/");
		base.mkdirs();

		File file = new File(base, "geoip_city.zip");
		context.println("downloading geoip city data");
		download(file);
		try {
			zip = new ZipFile(file);
			Enumeration<? extends ZipEntry> it = zip.entries();
			while (it.hasMoreElements()) {
				ZipEntry entry = it.nextElement();
				InputStream is = zip.getInputStream(entry);
				try {
					context.println("unzipping " + entry.getName());
					writeFile(new File(base, entry.getName()), is);
				} finally {
					if (is != null)
						is.close();
				}
			}
		} catch (ZipException e) {
			context.println("unzip failed: " + e.getMessage());
			logger.warn("geoip unzip failed:", e);
		} catch (IOException e) {
			context.println("unzip failed: " + e.getMessage());
			logger.warn("geoip install failed:", e);
		} finally {
			if (zip != null)
				try {
					zip.close();
				} catch (IOException e) {
				}

			file.delete();
		}

		context.println("install completed");
	}

	private void writeFile(File file, InputStream is) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			byte[] b = new byte[16384];

			while (true) {
				int readBytes = is.read(b);
				if (readBytes <= 0)
					break;

				os.write(b, 0, readBytes);
			}
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	private void download(File file) {
		InputStream is = null;

		try {
			URL url = new URL("http://krakenapps.org/mvn/kraken/kraken-geoip/geoip_city.zip");
			URLConnection conn = url.openConnection();

			is = conn.getInputStream();
			writeFile(file, is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	@ScriptUsage(description = "compile ip blocks to binary format", arguments = { @ScriptArgument(name = "file path", type = "string", description = "blocks.csv file path of geolite city") })
	public void compileIpBlocks(String[] args) {
		try {
			File file = new File(args[0]);
			context.println("compile takes a lot of time (about 10min). please be patient.");
			geoip.compileIpBlocks(file);
			context.println("compile completed");
		} catch (FileNotFoundException e) {
			context.println("file not found: " + e.getMessage());
		} catch (IOException e) {
			context.println("io error: " + e.getMessage());
		}
	}
}
