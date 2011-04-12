package org.krakenapps.dnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.dnet.jni.NativeArp;
import org.krakenapps.dnet.jni.NativeArpEntry;

public class Arp {
	private NativeArp arp;

	public Arp() {
		arp = new NativeArp();
	}

	public void open() throws IOException {
		arp.open();
	}

	public void add(ArpEntry entry) {
		arp.add(entry.getNativeEntry());
	}

	public void delete(ArpEntry entry) {
		arp.delete(entry.getNativeEntry());
	}

	public void get(ArpEntry entry) {
		arp.get(entry.getNativeEntry());
	}

	public static List<ArpEntry> getEntries() {
		NativeArp arp = new NativeArp();

		try {
			arp.open();
		} catch (IOException e) {
		}

		List<NativeArpEntry> nativeEntries = arp.getEntries();
		List<ArpEntry> entries = new ArrayList<ArpEntry>(nativeEntries.size());

		for (NativeArpEntry entry : nativeEntries)
			entries.add(entry.getJavaEntry());

		arp.close();

		return entries;
	}

	public void close() {
		arp.close();
	}
}
