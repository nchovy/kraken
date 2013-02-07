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
