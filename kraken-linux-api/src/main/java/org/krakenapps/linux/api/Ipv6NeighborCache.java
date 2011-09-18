/*
 * Copyright 2011 Future Systems
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
 */package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ipv6NeighborCache {
	private Ipv6NeighborCache() {
	}

	public static List<Ipv6NeighborEntry> getEntries() throws IOException {
		List<Ipv6NeighborEntry> entries = new ArrayList<Ipv6NeighborEntry>();
		java.lang.Process p = null;
		BufferedReader br = null;

		try {
			String line = null;

			p = Runtime.getRuntime().exec("ip -6 neigh show");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = br.readLine()) != null) {
				String[] values = line.split(" ");
				String address = values[0];
				String device = values[2];
				String mac = values[4];
				String state = values[values.length - 1];

				Ipv6NeighborEntry entry = new Ipv6NeighborEntry(address, device, mac, state);
				entries.add(entry);
			}
		} finally {
			if (br != null)
				br.close();
		}
		
		return entries;
	}
}