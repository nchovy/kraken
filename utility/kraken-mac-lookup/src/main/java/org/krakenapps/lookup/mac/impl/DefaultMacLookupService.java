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
package org.krakenapps.lookup.mac.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.lookup.mac.MacLookupService;
import org.krakenapps.lookup.mac.Vendor;

@Component(name = "mac-lookup-service")
@Provides
public class DefaultMacLookupService implements MacLookupService {
	private Map<String, Vendor> ouiMap;

	public DefaultMacLookupService() {
		ouiMap = new HashMap<String, Vendor>();

		InputStream in = this.getClass().getResourceAsStream("/mac-oui.txt");
		InputStreamReader isr = null;
		if (in == null) {
			isr = new InputStreamReader(ClassLoader.getSystemResourceAsStream("mac-oui.txt"));
		} else {
			isr = new InputStreamReader(in);
		}
		BufferedReader br = new BufferedReader(isr);

		while (true) {
			String line;
			try {
				line = br.readLine();
				if (line == null)
					break;

				Vendor vendor = buildVendor(line);
				if (vendor != null)
					ouiMap.put(vendor.getOui(), vendor);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	@Override
	public Vendor find(String oui) {
		if (oui == null)
			return null;

		return ouiMap.get(oui.toUpperCase());
	}

	@Override
	public Vendor findByMac(String mac) {
		mac = mac.trim().replace("-", "").replace(":", "");
		return find(mac.substring(0, 6));
	}

	private Vendor buildVendor(String line) {
		Vendor vendor = new Vendor();
		String[] tokens = line.split("\\|");
		if (tokens.length != 4)
			return null;

		vendor.setOui(tokens[0]);
		vendor.setName(tokens[1]);
		vendor.setAddress(tokens[2]);
		vendor.setCountry(tokens[3]);
		return vendor;
	}
}
