package org.krakenapps.rpc;


public class RpcAsyncResult {
	private boolean isCompleted;
	private RpcAsyncCallback callback;
	private Object ret;
	private RpcException exception;

	public RpcAsyncResult(RpcAsyncCallback callback) {
		this.callback = callback;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public boolean isError() {
		return exception != null;
	}

	public RpcAsyncCallback getCallback() {
		return callback;
	}

	public Object getReturn() {
		return ret;
	}

	public void setReturn(Object result) {
		this.isCompleted = true;
		this.ret = result;
	}

	public RpcException getException() {
		return exception;
	}

	public void setException(RpcException exception) {
		this.isCompleted = true;
		this.exception = exception;
	}

}
