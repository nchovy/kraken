package org.krakenapps.webconsole.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
		ctx.addServlet("msgbus", this, "/msgbus/*");

		t = new Thread(this, "Msgbus Push");
		t.start();
	}

	@Invalidate
	public void stop() {
		HttpContext ctx = httpd.ensureContext("webconsole");
		ctx.removeServlet("msgbus");

		doStop = true;
		t.interrupt();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Session session = ensureSession(req, resp, true);

		if (req.getPathInfo().equals("/trap")) {
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getId());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getId(), aCtx);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("kraken webconsole: msgbus post [{}]", req.getPathInfo());

		if (req.getPathInfo().equals("/request")) {
			Session session = ensureSession(req, resp, false);
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
			msgbus.execute(session, msg);
		} else if (req.getPathInfo().equals("/trap")) {
			Session session = ensureSession(req, resp, true);
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getId());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getId(), aCtx);
		}
	}

	private Session ensureSession(HttpServletRequest req, HttpServletResponse resp, boolean async) {
		HttpSession httpSession = req.getSession();
		logger.trace("kraken webconsole: using http session [{}, {}]", httpSession.getId(),
				httpSession.getAttribute("msgbus_session"));

		Session session = (Session) httpSession.getAttribute("msgbus_session");
		if (session == null) {
			if (async)
				session = new HttpMsgbusAsyncSession(req, resp);
			else
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
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.info("kraken webconsole: msgbus push thread interrupted");
				}
			}
		} finally {
			logger.info("kraken webconsole: msgbus push thread stopped");
		}
	}

	private void runOnce() throws InterruptedException {
		for (Integer sessionId : contexts.keySet()) {
			Queue<String> frames = pendingQueues.get(sessionId);
			if (frames == null || frames.size() == 0)
				continue;

			flushTraps(sessionId, frames);
		}
	}

	private void flushTraps(int sessionId, Queue<String> frames) {
		AsyncContext ctx = contexts.get(sessionId);
		if (ctx == null)
			return;

		try {
			synchronized (ctx) {
				ctx.getResponse().getOutputStream().write("[".getBytes());
				int i = 0;
				while (true) {
					String frame = frames.poll();
					if (frame == null)
						break;

					if (i != 0)
						ctx.getResponse().getOutputStream().write(",".getBytes());

					logger.trace("kraken webconsole: trying to send pending frame [{}]", frame);
					ctx.getResponse().getOutputStream().write(frame.getBytes("utf-8"));
					i++;
				}
				ctx.getResponse().getOutputStream().write("]".getBytes());
			}
		} catch (IOException e) {
			logger.error("kraken webconsole: cannot send pending msg", e);
		} finally {
			ctx.complete();
			contexts.remove(sessionId);
		}
	}

	private class HttpMsgbusSession extends AbstractSession {
		private int id;
		private HttpServletRequest req;
		private HttpServletResponse resp;

		public HttpMsgbusSession(HttpServletRequest req, HttpServletResponse resp) {
			this.id = new Random().nextInt(Integer.MAX_VALUE);
			this.req = req;
			this.resp = resp;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public void send(Message msg) {
			Session session = ensureSession(req, resp, false);
			String payload = KrakenMessageEncoder.encode(session, msg);
			try {
				logger.trace("kraken webconsole: trying to send response [{}]", payload);
				resp.getOutputStream().write(payload.getBytes("utf-8"));
			} catch (IOException e) {
				logger.error("kraken webconsole: cannot send response [{}]", payload);
			}
		}

		@Override
		public String toString() {
			return "msgbus session=" + id + ", remote=" + req.getRemoteAddr() + ":" + req.getRemotePort()
					+ formatSessionData(req.getSession());
		}
	}

	private class HttpMsgbusAsyncSession extends AbstractSession {
		private int id;
		private HttpServletRequest req;

		public HttpMsgbusAsyncSession(HttpServletRequest req, HttpServletResponse resp) {
			this.id = new Random().nextInt(Integer.MAX_VALUE);
			this.req = req;
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

			if (contexts.containsKey(msg.getSession())) {
				logger.debug("kraken webconsole: sending trap immediately [session={}, payload={}]", msg.getSession(), payload);
				flushTraps(msg.getSession(), frames);
			} else
				logger.debug("kraken webconsole: queueing trap [session={}, payload={}]", msg.getSession(), payload);
		}

		@Override
		public String toString() {
			return "msgbus session=" + id + ", remote=" + req.getRemoteAddr() + ":" + req.getRemotePort()
					+ formatSessionData(req.getSession());
		}
	}

	private static String formatSessionData(HttpSession session) {
		String sessiondata = "";
		if (session != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date since = new Date(session.getCreationTime());
			Date lastAccess = new Date(session.getLastAccessedTime());
			sessiondata = String.format(", jsession=%s, since=%s, lastaccess=%s", session.getId(), dateFormat.format(since),
					dateFormat.format(lastAccess));
		}
		return sessiondata;
	}

	@Override
	public String toString() {
		return "async contexts=" + contexts;
	}
}
