package org.krakenapps.rpc;

public class RpcException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(String message) {
		super(message);
	}
}
