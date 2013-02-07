/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
