package org.krakenapps.webconsole.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.msgbus.AbstractSession;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-msgbus-servlet")
public class MsgbusServlet extends HttpServlet implements Runnable {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(MsgbusServlet.class);

	/**
	 * msgbus session id to waiting async context mappings
	 */
	private ConcurrentMap<Integer, AsyncContext> contexts;

	/**
	 * msgbus session id to pending messages mappings
	 */
	private ConcurrentMap<Integer, Queue<String>> pendingQueues;

	@Requires
	private MessageBus msgbus;

	@Requires
	private HttpService httpd;

	/**
	 * periodic blocking checker
	 */
	private Thread t;

	private boolean doStop;

	public MsgbusServlet() {
		contexts = new ConcurrentHashMap<Integer, AsyncContext>();
		pendingQueues = new ConcurrentHashMap<Integer, Queue<String>>();
	}

	@Validate
	public void start() {
		doStop = false;
		HttpContext ctx = httpd.ensureContext("webconsole");
		ctx.addServlet("msgbus", this, "/msgbus");

		t = new Thread(this, "Msgbus Push");
		t.start();
	}

	@Invalidate
	public void stop() {
		doStop = true;
		t.interrupt();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Session session = ensureSession(req, resp);

		if (req.getPathInfo().equals("/trap")) {
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getId());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getId(), aCtx);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Session session = ensureSession(req, resp);

		if (req.getPathInfo().equals("/request")) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] b = new byte[4096];

			while (true) {
				int readBytes = req.getInputStream().read(b);
				if (readBytes < 0)
					break;

				os.write(b, 0, readBytes);
			}

			String text = os.toString("utf-8");

			Message msg = KrakenMessageDecoder.decode(session, text);
			msgbus.dispatch(session, msg);
		} else if (req.getPathInfo().equals("/trap")) {
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getId());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getId(), aCtx);
		}
	}

	private Session ensureSession(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession httpSession = req.getSession();
		logger.trace("kraken webconsole: using http session [{}, {}]", httpSession.getId(),
				httpSession.getAttribute("msgbus_session"));

		Session session = (Session) httpSession.getAttribute("msgbus_session");
		if (session == null) {
			session = new HttpMsgbusSession(req, resp);
			msgbus.openSession(session);
			httpSession.setAttribute("msgbus_session", session);
		}
		return session;
	}

	@Override
	public void run() {
		try {
			logger.info("kraken webconsole: msgbus push thread started");
			while (!doStop) {
				try {
					runOnce();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.info("kraken webconsole: msgbus push thread interrupted");
				}
			}
		} finally {
			logger.info("kraken webconsole: msgbus push thread stopped");
		}
	}

	private void runOnce() throws InterruptedException {
		// TODO: introduce new blocking queue and do pending queue
		// classification here
		for (Integer sessionId : contexts.keySet()) {
			AsyncContext ctx = contexts.get(sessionId);
			Queue<String> frames = pendingQueues.get(sessionId);
			if (frames == null)
				continue;

			String frame = frames.poll();
			if (frame == null)
				continue;

			try {
				logger.trace("kraken webconsole: trying to send pending frame [{}]", frame);
				ctx.getResponse().getOutputStream().write(frame.getBytes("utf-8"));
			} catch (IOException e) {
				logger.error("kraken webconsole: cannot send pending msg", e);
			} finally {
				ctx.complete();
			}
		}
	}

	private class HttpMsgbusSession extends AbstractSession {
		private int id;

		public HttpMsgbusSession(HttpServletRequest req, HttpServletResponse resp) {
			this.id = new Random().nextInt(Integer.MAX_VALUE);
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public void send(Message msg) {
			pendingQueues.putIfAbsent(msg.getSession(), new ConcurrentLinkedQueue<String>());
			Queue<String> frames = pendingQueues.get(msg.getSession());

			String payload = KrakenMessageEncoder.encode(this, msg);
			frames.add(payload);

			logger.debug("kraken webconsole: queue msgbus packet [session={}, payload={}]", msg.getSession(), payload);
		}
	}

	@Override
	public String toString() {
		return "async contexts=" + contexts;
	}
}
