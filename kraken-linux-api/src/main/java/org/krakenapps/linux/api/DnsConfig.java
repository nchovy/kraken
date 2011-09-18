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
 */
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class DnsConfig {
	private List<InetAddress> nameserver;
	private String domain;
	private List<String> search;
	private List<Sortlist> sortlist;
	private List<String> others;

	private DnsConfig() {
		this.nameserver = new ArrayList<InetAddress>();
		this.domain = null;
		this.search = new ArrayList<String>();
		this.sortlist = new ArrayList<Sortlist>();
		this.others = new ArrayList<String>();
	}

	public static DnsConfig getConfig() throws IOException {
		DnsConfig resolv = new DnsConfig();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/resolv.conf")));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("[\t| ]+");
				if (tokens.length < 2) {
					resolv.others.add(line);
					continue;
				}

				if (tokens[0].equals("nameserver"))
					resolv.nameserver.add(InetAddress.getByName(tokens[1]));
				else if (tokens[0].equals("domain"))
					resolv.domain = tokens[1];
				else if (tokens[0].equals("search"))
					resolv.search.add(tokens[1]);
				else if (tokens[0].equals("sortlist")) {
					String[] addresses = tokens[1].split("/");
					Sortlist list = new Sortlist(InetAddress.getByName(addresses[0]));
					if (addresses.length > 1)
						list.setNetmask(InetAddress.getByName(addresses[1]));
					resolv.sortlist.add(list);
				} else
					resolv.others.add(line);
			}
		} finally {
			if (reader != null)
				reader.close();
		}

		return resolv;
	}

	public List<InetAddress> getNameserver() {
		return nameserver;
	}

	public void addNameserver(InetAddress nameserver) {
		addNameserver(this.nameserver.size(), nameserver);
	}

	public void addNameserver(int index, InetAddress nameserver) {
		if (this.nameserver.size() == 3)
			throw new IndexOutOfBoundsException("Nameserver count limit is up to 3");

		this.nameserver.add(index, nameserver);
	}

	public void removeNameserver(InetAddress nameserver) {
		if (this.nameserver.contains(nameserver))
			this.nameserver.remove(nameserver);
	}

	public void removeAllNameserver() {
		this.nameserver.clear();
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public List<String> getSearch() {
		return search;
	}

	public void addSearch(String search) {
		addSearch(this.search.size(), search);
	}

	public void addSearch(int index, String search) {
		if (search.length() > 256)
			throw new IllegalArgumentException("Search string length limit is up to 256");
		if (this.search.size() == 6)
			throw new IndexOutOfBoundsException("Search count limit is up to 6");

		this.search.add(index, search);
	}

	public void removeSearch(String search) {
		if (this.search.contains(search))
			this.search.remove(search);
	}

	public void removeAllSearch() {
		this.search.clear();
	}

	public List<Sortlist> getSortlist() {
		return sortlist;
	}

	public void addSortlist(Sortlist sortlist) {
		addSortlist(this.sortlist.size(), sortlist);
	}

	public void addSortlist(int index, Sortlist sortlist) {
		this.sortlist.add(index, sortlist);
	}

	public void removeSortlist(Sortlist sortlist) {
		if (this.sortlist.contains(sortlist))
			this.sortlist.remove(sortlist);
	}

	public void removeAllSortlist() {
		this.sortlist.clear();
	}

	public void save() {
		PrintStream pStream = null;
		try {
			pStream = new PrintStream("/etc/resolv.conf");

			for (InetAddress addr : nameserver)
				pStream.println("nameserver " + addr.getHostAddress());
			if (domain != null)
				pStream.println("domain " + domain);
			for (String str : search)
				pStream.println("search " + str);
			for (Sortlist list : sortlist)
				pStream.println(list);
			for (String str : others)
				pStream.println(str);
		} catch (FileNotFoundException e) {
		} finally {
			if (pStream != null)
				pStream.close();
		}
	}

	public static class Sortlist {
		private InetAddress address;
		private InetAddress netmask;

		public Sortlist(InetAddress address) {
			this(address, null);
		}

		public Sortlist(InetAddress address, InetAddress netmask) {
			this.address = address;
			this.netmask = netmask;
		}

		public InetAddress getAddress() {
			return address;
		}

		public void setAddress(InetAddress address) {
			this.address = address;
		}

		public InetAddress getNetmask() {
			return netmask;
		}

		public void setNetmask(InetAddress netmask) {
			this.netmask = netmask;
		}

		@Override
		public String toString() {
			if (address == null)
				return "";
			if (netmask == null)
				return address.getHostAddress();
			return address.getHostAddress() + "/" + netmask.getHostAddress();
		}
	}
}
