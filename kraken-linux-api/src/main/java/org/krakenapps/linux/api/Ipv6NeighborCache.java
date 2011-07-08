package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ipv6NeighborCache {
	private Ipv6NeighborCache() {
	}

	public static List<Ipv6NeighborEntry> getEntries() {
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
		} catch (IOException e) {
		}
		return entries;
	}
}