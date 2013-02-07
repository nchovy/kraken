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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonRegistry<K, V extends Managed> {
	private ConcurrentHashMap<K, V> instances = new ConcurrentHashMap<K, V>();
	private ManagedInstanceFactory<V, K> factory;

	public SingletonRegistry(ManagedInstanceFactory<V, K> factory) {
		this.factory = factory;
	}

	public V get(K key) {
		return get(key, this.factory);
	}

	public V get(K key, ManagedInstanceFactory<V, K> fctry) {
		// check table existence
		V online = instances.get(key);
		if (online != null && online.isOpen())
			return online;

		try {
			V old = instances.get(key);
			if (old != null) {
				synchronized (old) {
					if (old.isClosing()) { // closing
						while (!old.isClosed()) {
							try {
								old.wait(1000);
							} catch (InterruptedException e) {
							}
						}
						while (instances.get(key) == old) {
							Thread.yield();
						}

						online = getNewSingleInstance(key, fctry);
					} else if (old.isClosed()) {
						while (instances.get(key) == old) {
							Thread.yield();
						}
						online = getNewSingleInstance(key, fctry);
					} else {
						online = old;
					}
				}
			} else {
				online = getNewSingleInstance(key, fctry);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return online;
	}

	private V getNewSingleInstance(K key, ManagedInstanceFactory<V, K> fctry) throws IOException {
		V online;
		V newOne = fctry.newInstance(key);
		V consensus = instances.putIfAbsent(key, newOne);
		if (consensus == null)
			online = newOne;
		else {
			online = consensus;
			if (consensus != newOne)
				newOne.close();
		}
		return online;
	}

	public void closeAll() {
		for (K key : instances.keySet()) {
			V v = instances.get(key);
			try {
				synchronized (v) {
					v.onClose();
					notifyAll();
				}
			} catch (IOException e) {
			} catch (Exception e) {
			}
		}
	}

	public void dispose(Object key) {
		instances.remove(key);
	}
}
