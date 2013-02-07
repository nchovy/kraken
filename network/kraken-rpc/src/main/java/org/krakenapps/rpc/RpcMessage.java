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

import java.util.HashMap;
import java.util.Map;

public class RpcMessage {
	private RpcSession session;
	private Map<String, Object> header;
	private Map<String, Object> body;

	private RpcMessage() {
		header = new HashMap<String, Object>();
		body = new HashMap<String, Object>();
	}

	private RpcMessage(int id, int sessionId, String type) {
		this();
		putHeader("id", id);
		putHeader("session", sessionId);
		putHeader("type", type);
	}
	
	public static RpcMessage newCall(int id, int session, String method, Object[] params) {
		RpcMessage call = new RpcMessage(id, session, "rpc-call");
		call.put("method", method);
		call.put("params", params);
		return call;
	}

	public static RpcMessage newPost(int id, int session, String method, Object[] params) {
		RpcMessage post = new RpcMessage(id, session, "rpc-post");
		post.put("method", method);
		post.put("params", params);
		return post;
	}

	public static RpcMessage newResponse(int id, int session, int reqId, String method, Object value) {
		RpcMessage resp = new RpcMessage(id, session, "rpc-ret");
		resp.putHeader("ret-for", reqId);
		resp.put("method", method);
		resp.put("ret", value);
		return resp;
	}

	public static RpcMessage newException(int id, int session, int reqId, String cause) {
		RpcMessage error = new RpcMessage(id, session, "rpc-error");
		error.putHeader("ret-for", reqId);
		error.put("cause", cause);
		return error;
	}

	@SuppressWarnings("unchecked")
	public RpcMessage(Object[] data) {
		header = (Map<String, Object>) data[0];
		body = (Map<String, Object>) data[1];
	}

	public RpcSession getSession() {
		return session;
	}

	public void setSession(RpcSession session) {
		this.session = session;
	}

	public boolean containsHeader(String key) {
		return header.containsKey(key);
	}

	public boolean containsField(String key) {
		return body.containsKey(key);
	}

	public Object getHeader(String key) {
		return header.get(key);
	}

	public Object get(String key) {
		return body.get(key);
	}

	public String getString(String key) {
		return (String) body.get(key);
	}

	public Boolean getBoolean(String key) {
		return (Boolean) body.get(key);
	}

	public Integer getInt(String key) {
		return (Integer) body.get(key);
	}

	public Long getLong(String key) {
		return (Long) body.get(key);
	}

	public void putHeader(String key, Object value) {
		header.put(key, value);
	}

	public void put(String key, Object value) {
		body.put(key, value);
	}

	public Object marshal() {
		return new Object[] { header, body };
	}
}
