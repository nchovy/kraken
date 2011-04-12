package org.krakenapps.rpc;

public class RpcContext {
	private static ThreadLocal<RpcMessage> message = new ThreadLocal<RpcMessage>();

	private RpcContext() {
	}

	public static void setMessage(RpcMessage value) {
		if (value == null)
			message.remove();
		else
			message.set(value);
	}

	public static RpcConnection getConnection() {
		RpcMessage msg = message.get();
		return msg.getSession().getConnection();
	}

	public static RpcSession getSession() {
		RpcMessage msg = message.get();
		return msg.getSession();
	}

	public static Object getHeader(String key) {
		RpcMessage msg = message.get();
		return msg.getHeader(key);
	}

	public static Object getParameter(String key) {
		RpcMessage msg = message.get();
		return msg.getString(key);
	}
}
