package org.krakenapps.util;

import java.io.Closeable;
import java.io.IOException;

public abstract class Managed implements Closeable {
	private volatile boolean closing = false;
	private volatile boolean closed = false;
	private final SingletonRegistry<?, ? extends Managed> registry;
	private final Object key;
	
	public Managed(SingletonRegistry<?, ? extends Managed> r, Object k) {
		this.registry = r;
		this.key = k;
	}
	
	public boolean isOpen() {
		return closing == false; 
	}
	
	public boolean isClosing() {
		return closing == true && closed == false;
	}
	
	public boolean isClosed() {
		return closing == true && closed == true;
	}
	
	protected abstract void onClose() throws IOException;
	protected abstract void errorOnClosing(Throwable e);
	
	@Override
	public void close() throws IOException {
		if (closing)
			return;

		try {
			synchronized (this) {
				closing = true;
				onClose();
				notifyAll();
				closed = true;
			}
			if (registry != null)
				registry.dispose(key);

		} catch (IOException e) {
			errorOnClosing(e);
		}		
	}
}
