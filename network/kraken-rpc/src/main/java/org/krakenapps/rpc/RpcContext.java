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
