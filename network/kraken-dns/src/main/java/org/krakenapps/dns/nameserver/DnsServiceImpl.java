/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns.nameserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dns.DnsCache;
import org.krakenapps.dns.DnsCacheEntry;
import org.krakenapps.dns.DnsCacheKey;
import org.krakenapps.dns.DnsDump;
import org.krakenapps.dns.DnsEventListener;
import org.krakenapps.dns.DnsFlags;
import org.krakenapps.dns.DnsMessage;
import org.krakenapps.dns.DnsMessageCodec;
import org.krakenapps.dns.DnsResolver;
import org.krakenapps.dns.DnsResolverProvider;
import org.krakenapps.dns.DnsResourceRecord;
import org.krakenapps.dns.DnsService;
import org.krakenapps.dns.DnsServiceConfig;
import org.krakenapps.dns.DnsServiceStatus;
import org.krakenapps.dns.rr.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dns-service")
@Provides
public class DnsServiceImpl implements DnsService {
	private final Logger logger = LoggerFactory.getLogger(DnsServiceImpl.class);
	private DnsCache cache;
	private CopyOnWriteArraySet<DnsEventListener> listeners;
	private ConcurrentHashMap<String, DnsResolverProvider> providers;

	private DatagramSocket socket;
	private Thread t;
	private ThreadPoolExecutor executor;
	private Runner runner;
	private LinkedBlockingQueue<Runnable> queue;
	private AtomicLong recvCount;
	private AtomicLong dropCount;
	private String defaultResolverName = "proxy";
	private volatile boolean doStop;

	public DnsServiceImpl() {
		cache = new Cache();
		listeners = new CopyOnWriteArraySet<DnsEventListener>();
		providers = new ConcurrentHashMap<String, DnsResolverProvider>();
		recvCount = new AtomicLong();
		dropCount = new AtomicLong();
		queue = new LinkedBlockingQueue<Runnable>();
		runner = new Runner();
	}

	@Validate
	public void start() {
		listeners.clear();
		cache.clear();
		logger.info("kraken dns: service started");

		int cpuCount = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(1, cpuCount, 10, TimeUnit.SECONDS, queue, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "DNS Worker");
			}
		});
	}

	@Invalidate
	public void stop() {
		close();
		executor.shutdownNow();
		logger.info("kraken dns: service stopped");
	}

	@Override
	public DnsServiceStatus getStatus() {
		DnsServiceStatus status = new DnsServiceStatus();
		status.setRunning(t != null);
		status.setReceiveCount(recvCount.get());
		status.setDropCount(dropCount.get());
		return status;
	}

	@Override
	public void reload(DnsServiceConfig config) {
		// TODO:
	}

	@Override
	public void open() throws IOException {
		if (t != null)
			throw new IOException("dns server is already listening");

		socket = new DatagramSocket(53);
		socket.setSoTimeout(1000);

		t = new Thread(runner, "DNS Listener");
		t.start();
	}

	@Override
	public void close() {
		if (t == null)
			return;

		socket.close();
		doStop = true;
		t.interrupt();
		t = null;
	}

	@Override
	public List<DnsResolverProvider> getResolverProviders() {
		return new ArrayList<DnsResolverProvider>(providers.values());
	}

	@Override
	public DnsResolver newDefaultResolver() {
		DnsResolverProvider provider = providers.get(defaultResolverName);
		if (provider == null)
			throw new IllegalStateException("dns resolver provider not found: " + defaultResolverName);

		return provider.newResolver();
	}

	@Override
	public void setDefaultResolverProvider(String name) {
		if (!providers.containsKey(name))
			throw new IllegalStateException("dns resolver provider not found: " + name);

		this.defaultResolverName = name;
	}

	@Override
	public void registerProvider(DnsResolverProvider provider) {
		if (provider == null)
			throw new IllegalArgumentException("dns resolver provider should be not null");

		DnsResolverProvider old = providers.putIfAbsent(provider.getName(), provider);
		if (old != null)
			throw new IllegalStateException("dns resolver provider name conflicts: " + provider.getName());
	}

	@Override
	public void unregisterProvider(DnsResolverProvider provider) {
		if (provider == null)
			throw new IllegalArgumentException("dns resolver provider should be not null");

		DnsResolverProvider old = providers.get(provider.getName());
		if (old == provider)
			providers.remove(provider.getName());
	}

	@Override
	public DnsCache getCache() {
		return cache;
	}

	@Override
	public void addListener(DnsEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(DnsEventListener listener) {
		listeners.remove(listener);
	}

	private class Runner implements Runnable {

		@Override
		public void run() {
			try {
				while (!doStop) {
					loop();
				}
			} finally {
				logger.error("kraken dns: closing dns server");
				doStop = false;
			}
		}

		private void loop() {
			try {
				byte[] buf = new byte[65536];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				recvCount.incrementAndGet();

				executor.submit(new Handler(packet));
			} catch (SocketTimeoutException e) {
			} catch (Throwable t) {
				logger.error("kraken dns: cannot handle query", t);
			}
		}
	}

	private class Cache implements DnsCache {
		private ConcurrentHashMap<DnsCacheKey, DnsCacheEntry> entries;

		public Cache() {
			entries = new ConcurrentHashMap<DnsCacheKey, DnsCacheEntry>();
		}

		@Override
		public DnsCacheEntry lookup(DnsCacheKey key) {
			if (key == null)
				return null;

			return entries.get(key);
		}

		@Override
		public Set<DnsCacheKey> getKeys() {
			return Collections.unmodifiableSet(entries.keySet());
		}

		@Override
		public void putEntry(DnsCacheKey key, DnsCacheEntry response) {
			entries.put(key, response);
		}

		@Override
		public void removeEntry(DnsCacheKey key) {
			entries.remove(key);
		}

		@Override
		public void clear() {
			entries.clear();
		}
	}

	private class Handler implements Runnable {
		private DatagramPacket packet;
		private DnsMessage query;

		public Handler(DatagramPacket packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			try {
				ByteBuffer bb = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
				query = DnsMessageCodec.decode(bb);

				// fire query callbacks
				for (DnsEventListener listener : listeners) {
					try {
						listener.onReceive(packet, query);
					} catch (Throwable t) {
						logger.warn("kraken dns: dns listener should not throw any exception", t);
					}
				}

				if (query.getQuestions().size() > 0) {
					DnsMessage reply = resolve();
					if (reply == null) {
						// fire drop callbacks
						dropCount.incrementAndGet();

						for (DnsEventListener listener : listeners) {
							try {
								listener.onDrop(packet, query, null);
							} catch (Throwable t) {
								logger.warn("kraken dns: dns listener should not throw any exception", t);
							}
						}

						return;
					}

					ByteBuffer rbuf = DnsMessageCodec.encode(reply);
					int limit = rbuf.limit();

					DatagramPacket r = new DatagramPacket(rbuf.array(), rbuf.limit(), packet.getAddress(), packet.getPort());
					socket.send(r);

					// fire response callbacks
					for (DnsEventListener listener : listeners) {
						try {
							listener.onSend(packet, query, r, reply);
						} catch (Throwable t) {
							logger.warn("kraken dns: dns listener should not throw any exception", t);
						}
					}

					try {
						ByteBuffer b = ByteBuffer.wrap(rbuf.array(), 0, limit);
						DnsMessageCodec.decode(b);
					} catch (Throwable t) {
						String hex = DnsDump.dumpPacket(r);
						logger.error("kraken: malformed sent - " + hex, t);
					}

				} else {
					String remote = packet.getAddress().getHostAddress();
					String dump = DnsDump.dumpPacket(packet);
					logger.error("kraken dns: empty query from [{}], raw packet => [{}]", remote, dump);
				}
			} catch (Throwable t) {
				// fire error callbacks
				for (DnsEventListener listener : listeners) {
					try {
						listener.onError(packet, t);
					} catch (Throwable t2) {
						logger.warn("kraken dns: dns listener should not throw any exception", t2);
					}
				}

				// dump raw packet for future debugging
				String dump = DnsDump.dumpPacket(packet);
				String remote = packet.getAddress().getHostAddress();
				logger.error("kraken dns: cannot handle query from [" + remote + "], raw packet => [" + dump + "]", t);
			}
		}

		private DnsMessage resolve() throws IOException {
			DnsResourceRecord qr = query.getQuestions().get(0);
			String domain = qr.getName();
			DnsCacheKey key = new DnsCacheKey(domain, DnsResourceRecord.Type.A, DnsResourceRecord.Clazz.IN);
			DnsCacheEntry entry = cache.lookup(key);
			if (entry == null) {
				DnsResolver resolver = newDefaultResolver();
				return resolver.resolve(query);
			} else {
				DnsMessage cached = entry.getResponse();

				DnsMessage reply = new DnsMessage();
				reply.setId(query.getId());
				DnsFlags flags = new DnsFlags();
				flags.setQuery(false);
				reply.setFlags(flags);
				reply.setQuestionCount(1);
				reply.setAnswerCount(cached.getAnswers().size());

				reply.addQuestion(new A(domain));

				for (DnsResourceRecord rr : cached.getAnswers()) {
					reply.addAnswer(rr);
				}

				return reply;
			}
		}
	}
}
