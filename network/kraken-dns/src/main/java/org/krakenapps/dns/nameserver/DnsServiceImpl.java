package org.krakenapps.dns.nameserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
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
import org.krakenapps.dns.DnsEventListener;
import org.krakenapps.dns.DnsFlags;
import org.krakenapps.dns.DnsMessage;
import org.krakenapps.dns.DnsMessageCodec;
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
	private DatagramSocket socket;
	private Thread t;
	private ThreadPoolExecutor executor;
	private Runner runner;
	private LinkedBlockingQueue<Runnable> queue;
	private AtomicLong recvCount;
	private AtomicLong dropCount;
	private volatile boolean doStop;

	public DnsServiceImpl() {
		cache = new Cache();
		listeners = new CopyOnWriteArraySet<DnsEventListener>();
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
						listener.onReceive(query);
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
								listener.onDrop(query, null);
							} catch (Throwable t) {
								logger.warn("kraken dns: dns listener should not throw any exception", t);
							}
						}

						return;
					}

					ByteBuffer rbuf = DnsMessageCodec.encode(reply);

					DatagramPacket r = new DatagramPacket(rbuf.array(), rbuf.limit(), packet.getAddress(), packet.getPort());
					socket.send(r);

					// fire response callbacks
					for (DnsEventListener listener : listeners) {
						try {
							listener.onSend(query, reply);
						} catch (Throwable t) {
							logger.warn("kraken dns: dns listener should not throw any exception", t);
						}
					}
				} else {
					String remote = packet.getAddress().getHostAddress();
					String dump = dumpPacket();
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
				String dump = dumpPacket();
				String remote = packet.getAddress().getHostAddress();
				logger.error("kraken dns: cannot handle query from [" + remote + "], raw packet => [" + dump + "]", t);
			}
		}

		private DnsMessage resolve() throws UnknownHostException {
			DnsResourceRecord qr = query.getQuestions().get(0);
			String domain = qr.getName();
			DnsCacheKey key = new DnsCacheKey(domain, DnsResourceRecord.Type.A, DnsResourceRecord.Clazz.IN);
			DnsCacheEntry entry = cache.lookup(key);
			if (entry == null)
				return null;

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

		private String dumpPacket() {
			StringBuilder sb = new StringBuilder();
			byte[] buf = packet.getData();
			int offset = packet.getOffset();
			int length = packet.getLength();

			sb.append("byte[] b = new byte[] { ");
			for (int i = 0; i < length; i++) {
				if (i != 0)
					sb.append(", ");
				sb.append(String.format("(byte) 0x%02x", buf[i + offset]));
			}
			sb.append("};");

			return sb.toString();
		}
	}
}
