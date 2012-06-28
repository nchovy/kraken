package org.krakenapps.webconsole.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
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
import org.krakenapps.msgbus.Message.Type;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.krakenapps.webconsole.impl.KrakenMessageDecoder;
import org.krakenapps.webconsole.impl.KrakenMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-msgbus-servlet")
public class MsgbusServlet extends HttpServlet implements Runnable {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(MsgbusServlet.class);

	/**
	 * msgbus session id to waiting async context mappings
	 */
	private ConcurrentMap<String, AsyncContext> contexts;

	/**
	 * msgbus session id to pending messages mappings
	 */
	private ConcurrentMap<String, Queue<String>> pendingQueues;

	/**
	 * pending msgbus requests
	 */
	private ConcurrentMap<String, HttpServletResponse> pendingRequests;

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
		contexts = new ConcurrentHashMap<String, AsyncContext>();
		pendingQueues = new ConcurrentHashMap<String, Queue<String>>();
		pendingRequests = new ConcurrentHashMap<String, HttpServletResponse>();
	}

	public void setMessageBus(MessageBus msgbus) {
		this.msgbus = msgbus;
	}

	public void setHttpService(HttpService httpd) {
		this.httpd = httpd;
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
		if (httpd != null) {
			HttpContext ctx = httpd.ensureContext("webconsole");
			ctx.removeServlet("msgbus");
		}
		doStop = true;
		t.interrupt();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Session session = ensureSession(req);

		if (req.getPathInfo().equals("/trap")) {
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getGuid());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getGuid(), aCtx);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sessionId = req.getSession().getId();
		logger.debug("kraken webconsole: msgbus post from http session [{}], data [{}]", sessionId,
				formatSessionData(req.getSession()));

		if (req.getPathInfo().equals("/request")) {
			Session session = ensureSession(req);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] b = new byte[4096];

			while (true) {
				int readBytes = req.getInputStream().read(b);
				if (readBytes < 0)
					break;

				os.write(b, 0, readBytes);
			}

			String text = os.toString("utf-8");
			if (text != null && text.trim().isEmpty()) {
				logger.debug("kraken webconsole: empty text from [{}], request [{}]", req.getRemoteAddr(), req.getRequestURI());
				resp.sendError(500);
				return;
			}

			Message msg = KrakenMessageDecoder.decode(session, text);
			pendingRequests.putIfAbsent(msg.getGuid(), resp);

			msgbus.execute(session, msg);
		} else if (req.getPathInfo().equals("/trap")) {
			Session session = ensureSession(req);
			logger.trace("kraken webconsole: waiting msgbus response/trap [session={}]", session.getGuid());
			AsyncContext aCtx = req.startAsync();
			contexts.put(session.getGuid(), aCtx);
		}
	}

	private Session ensureSession(HttpServletRequest req) throws UnknownHostException {
		String sessionId = req.getSession().getId();
		logger.trace("kraken webconsole: using http session [{}]", sessionId);

		Session session = msgbus.getSession(sessionId);
		if (session == null) {
			session = new HttpMsgbusSession(sessionId, InetAddress.getByName(req.getRemoteAddr()), InetAddress.getByName(req
					.getLocalAddr()));
			msgbus.openSession(session);
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
		for (String sessionId : contexts.keySet()) {
			Queue<String> frames = pendingQueues.get(sessionId);
			if (frames == null || frames.size() == 0)
				continue;

			flushTraps(sessionId, frames);
		}
	}

	private void flushTraps(String sessionId, Queue<String> frames) {
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
		private String sessionId;
		private InetAddress remoteAddr;
		private InetAddress localAddr;

		public HttpMsgbusSession(String sessionId, InetAddress remoteAddr, InetAddress localAddr) {
			this.sessionId = sessionId;
			this.remoteAddr = remoteAddr;
			this.localAddr = localAddr;
		}

		@Override
		public String getGuid() {
			return sessionId;
		}

		@Override
		public InetAddress getLocalAddress() {
			return localAddr;
		}

		@Override
		public InetAddress getRemoteAddress() {
			return remoteAddr;
		}

		@Override
		public void send(Message msg) {
			if (msg.getType() == Type.Trap) {

				pendingQueues.putIfAbsent(msg.getSession(), new ConcurrentLinkedQueue<String>());
				Queue<String> frames = pendingQueues.get(msg.getSession());
				String payload = KrakenMessageEncoder.encode(this, msg);
				frames.add(payload);

				if (contexts.containsKey(msg.getSession())) {
					logger.debug("kraken webconsole: sending trap immediately [session={}, payload={}]", msg.getSession(),
							payload);
					flushTraps(msg.getSession(), frames);
				} else
					logger.debug("kraken webconsole: queueing trap [session={}, payload={}]", msg.getSession(), payload);
			} else if (msg.getType() == Type.Response) {
				HttpServletResponse resp = pendingRequests.remove(msg.getRequestId());
				if (resp != null) {
					Session session = msgbus.getSession(sessionId);
					if (session != null) {
						String payload = KrakenMessageEncoder.encode(session, msg);
						try {
							logger.trace("kraken webconsole: trying to send response [{}]", payload);
							resp.getOutputStream().write(payload.getBytes("utf-8"));
						} catch (IOException e) {
							logger.error("kraken webconsole: cannot send response [{}]", payload);
						} finally {
							try {
								resp.getOutputStream().close();
							} catch (IOException e) {
								logger.error("kraken webconsole: cannot close http response stream", e);
							}
						}
					} else {
						logger.error("kraken webconsole: msgbus session [{}] not found", sessionId);
					}
				} else {
					logger.error("kraken webconsole: http response lost for msgbus req [{}]", msg.getRequestId());
				}

			}
		}

		@Override
		public String toString() {
			return "msgbus session=" + sessionId + ", remote=" + getRemoteAddress();
		}
	}

	private static String formatSessionData(HttpSession session) {
		String sessiondata = "";
		if (session != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date since = new Date(session.getCreationTime());
			Date lastAccess = new Date(session.getLastAccessedTime());
			sessiondata = String.format("jsession=%s, since=%s, lastaccess=%s", session.getId(), dateFormat.format(since),
					dateFormat.format(lastAccess));
		}
		return sessiondata;
	}

	@Override
	public String toString() {
		return "async contexts=" + contexts;
	}

}
