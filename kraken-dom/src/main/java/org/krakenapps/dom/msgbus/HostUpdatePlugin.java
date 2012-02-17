package org.krakenapps.dom.msgbus;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MsgbusPlugin
@Component(name = "dom-host-update-plugin")
public class HostUpdatePlugin implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(HostUpdatePlugin.class.getName());

	@Requires
	private HostApi hostApi;

	// for fast queue counting
	private AtomicInteger counter = new AtomicInteger();

	private ConcurrentLinkedQueue<Request> queue = new ConcurrentLinkedQueue<Request>();

	private Thread t;
	private volatile boolean doStop;

	@Validate
	public void start() {
		doStop = false;
		t = new Thread();
		t.start();
	}

	@Invalidate
	public void stop() {
		doStop = true;
		t.interrupt();
	}

	@MsgbusMethod
	public void update(Request req, Response resp) {
		// data cleansing

		// queueing
		counter.incrementAndGet();
		queue.add(req);
	}

	/**
	 * batch update using
	 */
	@Override
	public void run() {
		try {
			logger.info("kraken dom: starting host updater thread");
			while (!doStop) {
				runOnce();
			}
		} finally {
			logger.info("kraken dom: host updater thread stopped");
		}
	}

	private void runOnce() {
		// find diff

		// update

		// descrease counter
	}

}
