package org.krakenapps.dnet.jni;

import java.io.IOException;
import java.util.List;

import org.krakenapps.dnet.Address;

public class NativeArp {
	static {
		System.loadLibrary("kdnet");
	}

	private long id = -1;

	public void open() throws IOException {
		if (id == -1)
			id = nativeOpen();
		else
			throw new IOException("ARP is already opened");
	}

	public void add(NativeArpEntry entry) {
		nativeAdd(id, entry.getProtocol(), entry.getHardware());
	}

	public void delete(NativeArpEntry entry) {
		nativeDelete(id, entry.getProtocol(), entry.getHardware());
	}

	public void get(NativeArpEntry entry) {
		nativeGet(id, entry.getProtocol(), entry.getHardware());
	}

	public List<NativeArpEntry> getEntries() {
		return nativeLoop(id);
	}

	public void close() {
		nativeClose(id);
		id = -1;
	}

	private native long nativeOpen();

	private native int nativeAdd(long id, Address protocol, Address hardware);

	private native int nativeDelete(long id, Address protocol, Address hardware);

	private native int nativeGet(long id, Address protocol, Address hardware);

	private native List<NativeArpEntry> nativeLoop(long id);

	private native long nativeClose(long id);

}
