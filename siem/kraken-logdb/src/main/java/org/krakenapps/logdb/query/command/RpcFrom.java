package org.krakenapps.logdb.query.command;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.api.Primitive;
import org.krakenapps.logdb.LogQueryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcFrom extends LogQueryCommand implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(RpcTo.class.getName());

	/**
	 * mapreduce query guid
	 */
	private String guid;

	private Thread t;
	private volatile boolean end;
	private AtomicInteger counter;

	private LinkedBlockingQueue<LogMap> queue;

	public RpcFrom(String guid) {
		this.guid = guid;
		this.queue = new LinkedBlockingQueue<LogMap>();
		this.counter = new AtomicInteger();
	}

	@Override
	public void run() {
		while (!end) {
			try {
				LogMap data = queue.poll(100, TimeUnit.MILLISECONDS);
				if (data != null) {
					write(data);
					counter.incrementAndGet();
				}
			} catch (InterruptedException e) {
			}
		}

		// process all remainings
		while (true) {
			LogMap data = queue.poll();
			if (data == null)
				break;

			write(data);
			counter.incrementAndGet();
		}

		logger.info("kraken logdb: rpcfrom pass total [{}], remain [{}]", counter.get(), queue.size());
		super.eof();
		logger.info("kraken logdb: closed rpc reducer stream for query guid [{}]", guid);
	}

	@Override
	public void push(LogMap m) {
		if (end) {
			logger.info("kraken logdb: loss (will be fixed) - {}", Primitive.stringify(m));
			return;
		}

		if (t == null) {
			t = new Thread(this, "RPC Reducer [" + guid + "]");
			t.start();
		}

		queue.add(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void start() {
		status = Status.Running;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;
		end = true;
		logger.info("kraken logdb: eof for query guid [{}]", guid);
	}

	@Override
	public String toString() {
		return "RPC Reducer [" + guid + "] - " + queue.size();
	}

}
