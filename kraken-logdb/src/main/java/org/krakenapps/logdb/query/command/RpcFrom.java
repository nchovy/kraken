package org.krakenapps.logdb.query.command;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

	private LinkedBlockingQueue<Map<String, Object>> queue;

	public RpcFrom(String guid) {
		this.guid = guid;
		this.queue = new LinkedBlockingQueue<Map<String, Object>>();
	}

	@Override
	public void run() {
		while (!end) {
			try {
				Map<String, Object> data = queue.poll(100, TimeUnit.MILLISECONDS);
				if (data != null)
					write(data);
			} catch (InterruptedException e) {
			}
		}

		super.eof();
		logger.info("kraken logdb: closed rpc reducer stream for query guid [{}]", guid);
	}

	@Override
	public void push(Map<String, Object> m) {
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
		end = true;
		logger.info("kraken logdb: eof for query guid [{}]", guid);
	}

	@Override
	public String toString() {
		return "RPC Reducer [" + guid + "] - " + queue.size();
	}

}
