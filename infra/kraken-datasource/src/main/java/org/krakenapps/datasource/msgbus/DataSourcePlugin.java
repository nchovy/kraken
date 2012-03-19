/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.datasource.msgbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.datasource.DataConverter;
import org.krakenapps.datasource.DataConverterRegistry;
import org.krakenapps.datasource.DataSource;
import org.krakenapps.datasource.DataSourceEventListener;
import org.krakenapps.datasource.DataSourceRegistry;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "data-source-plugin")
@MsgbusPlugin
public class DataSourcePlugin {
	private Logger logger = LoggerFactory.getLogger(DataSourcePlugin.class.getName());
	
	@Requires
	private DataSourceRegistry dataSourceRegistry;
	@Requires
	private DataConverterRegistry dataConverterRegistry;
	
	private ConcurrentMap<Session, DataSession> callbacks;

	public DataSourcePlugin() {
		callbacks = new ConcurrentHashMap<Session, DataSession>();
	}

	@MsgbusMethod
	public void getChildren(Request req, Response resp) {
		String path = req.getString("path");
		path = "/watchcat/" + req.getOrgId() + path;

		Collection<Entry<String, DataSource>> children = dataSourceRegistry.getChildren(path);
		List<Object> result = new ArrayList<Object>(children.size());
		for (Entry<String, DataSource> pair : children) {
			result.add(marshalDataSource(pair.getKey(), pair.getValue()));
		}

		resp.put("keys", dataSourceRegistry.getSubKeys(path));
		resp.put("sources", result);
	}

	@MsgbusMethod
	public void getDataSources(Request req, Response resp) {
		String query = req.getString("query");
		if (!query.startsWith("/"))
			query = "/" + query;

		query = getDataPath(req.getOrgId(), query);

		Collection<Entry<String, DataSource>> dataSources = dataSourceRegistry.query(query);
		List<Object> result = new ArrayList<Object>(dataSources.size());

		for (Entry<String, DataSource> pair : dataSources) {
			result.add(marshalDataSource(pair.getKey(), pair.getValue()));
		}

		resp.put("sources", result);
	}

	@MsgbusMethod
	public void getData(Request req, Response resp) {
		String path = getDataPath(req.getOrgId(), req.getString("path"));

		DataSource dataSource = dataSourceRegistry.getDataSource(path);
		if (dataSource == null)
			throw new DataSourceNotFoundException(path);

		resp.put("path", getUserDataPath(dataSource.getPath()));
		resp.put("type", dataSource.getType());
		resp.put("data", dataSource.getData());
	}

	/**
	 * User should not know his organization id for security reasons. Remove
	 * /watchcat/org_id segments.
	 */
	private static String getUserDataPath(String path) {
		String[] tokens = path.split("/");
		if (tokens.length <= 3)
			return path;

		if (!tokens[1].equals("watchcat"))
			return path;

		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < tokens.length; i++) {
			sb.append("/");
			sb.append(tokens[i]);
		}

		return sb.toString();
	}

	private String getDataPath(int orgId, String path) {
		return "/watchcat/" + orgId + path;
	}

	@MsgbusMethod
	public void checkBindingName(Request req, Response resp) {
		DataSession session = getDataSession(req);
		String name = req.getString("name");
		resp.put("available", session.isNameAvailable(name));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void bindDataSources(Request req, Response resp) {
		String callback = req.getString("callback");
		List<Object> array = (List<Object>) req.get("bindings");
		DataSession updater = getDataSession(req);

		for (Object item : array) {
			Map<String, Object> o = (Map<String, Object>) item;
			String name = (String) o.get("name");
			String path = getDataPath(req.getOrgId(), (String) o.get("path"));
			String converterName = (String) o.get("converter");

			DataSource source = dataSourceRegistry.getDataSource(path);
			if (source == null) {
				logger.warn("watchcat datasource plugin: bind failed, data source [{}] not found", path);
				continue;
			}

			DataConverter converter = dataConverterRegistry.getDataConverter(converterName);
			updater.bind(new DataBinding(name, Integer.parseInt(req.getSource()), source, converter, callback));

			logger.trace("watchcat datasource plugin: bind datasource [{}]", path);
		}
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void unbindDataSources(Request req, Response resp) {
		List<String> array = (List<String>) req.get("bindings");
		DataSession updater = getDataSession(req);

		for (String name : array) {
			updater.unbind(name);
			logger.trace("watchcat datasource plugin: release datasource binding [{}]", name);

		}
	}

	private DataSession getDataSession(Request req) {
		Session session = req.getSession();
		DataSession listener = new DataSession(session);
		DataSession old = callbacks.putIfAbsent(session, listener);
		if (old != null)
			listener = old;

		return listener;
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void clearDataSources(Session session) {
		logger.info("watchcat datasource plugin: session closed, clear data session [{}]", session.getId());
		DataSession updater = callbacks.remove(session);
		if (updater == null)
			return;

		updater.clear();
	}

	private Map<String, Object> marshalDataSource(String name, DataSource source) {
		Map<String, Object> m = new HashMap<String, Object>();
		String[] tokens = source.getPath().split("/");
		StringBuilder sb = new StringBuilder();

		for (int i = 3; i < tokens.length; i++) {
			sb.append("/");
			sb.append(tokens[i]);
		}

		String path = sb.toString();

		m.put("name", name);
		m.put("path", path);
		m.put("type", source.getType());
		return m;
	}

	private static class DataSession implements DataSourceEventListener {
		private Logger logger = LoggerFactory.getLogger(DataSourcePlugin.class.getName());
		private Session session;
		private ConcurrentMap<String, DataBinding> bindings;
		private ConcurrentMap<DataSource, String> names;

		public DataSession(Session session) {
			this.session = session;
			this.bindings = new ConcurrentHashMap<String, DataBinding>();
			this.names = new ConcurrentHashMap<DataSource, String>();
		}

		public boolean isNameAvailable(String name) {
			return !bindings.containsKey(name);
		}

		public void bind(DataBinding binding) {
			if (binding == null)
				throw new IllegalArgumentException("binding must be not null");

			if (binding.source == null)
				throw new IllegalArgumentException("binding source must be not null");

			// put data
			if (bindings.putIfAbsent(binding.name, binding) != null)
				throw new IllegalStateException("duplicated datasource binding name");

			names.put(binding.source, binding.name);

			binding.source.addListener(this);
		}

		public void unbind(String name) {
			DataBinding binding = bindings.remove(name);
			if (binding == null)
				return;

			names.remove(binding.source);
			binding.source.removeListener(this);
		}

		public void clear() {
			for (String name : bindings.keySet()) {
				DataBinding binding = bindings.get(name);
				binding.source.removeListener(this);
			}

			this.bindings.clear();
			this.names.clear();
		}

		@Override
		public void onUpdate(DataSource source, Object oldData, Object newData) {
			String bindingName = names.get(source);
			if (bindingName == null) {
				logger.warn("watchcat datasource plugin: binding name not found for [{}]", source.getPath());
				return;
			}

			logger.trace("watchcat datasource plugin: new data from binding [{}]", bindingName);
			DataBinding binding = bindings.get(bindingName);
			if (binding == null) {
				logger.warn("watchcat datasource plugin: binding not found for [{}]", bindingName);
				return;
			}

			if (binding.converter != null)
				newData = binding.converter.convert(newData);

			Message m = new Message();
			m.setSession(session.getId());
			m.setType(Message.Type.Trap);
			m.setMethod(binding.callback);
			m.setTarget(Integer.toString(binding.processId));
			m.getParameters().put("source", getUserDataPath(source.getPath()));
			m.getParameters().put("binding", bindingName);
			m.getParameters().put("type", source.getType());
			m.getParameters().put("data", newData);

			session.send(m);
		}
	}

	private static class DataBinding {
		public String name;
		public DataSource source;
		public DataConverter converter;
		public String callback;
		public int processId;

		public DataBinding(String name, int processId, DataSource source, DataConverter converter, String callback) {
			this.processId = processId;
			this.name = name;
			this.source = source;
			this.converter = converter;
			this.callback = callback;
		}
	}
}
