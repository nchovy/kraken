/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.util.List;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.siem.ConfigManager;
import org.krakenapps.siem.model.ResponseActionConfig;
import org.krakenapps.siem.model.ResponseActionInstance;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseActionManagerEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-response-action-loader")
public class ResponseActionLoader extends ServiceTracker implements ResponseActionManagerEventListener {
	private final Logger logger = LoggerFactory.getLogger(ResponseActionLoader.class.getName());

	@Requires
	private ConfigManager configManager;

	public ResponseActionLoader(BundleContext bc) {
		super(bc, ResponseActionManager.class.getName(), null);
	}

	@Validate
	public void start() {
		super.open();
	}

	@Invalidate
	public void stop() {
		super.close();
	}

	private void loadActions(ResponseActionManager manager) {
		ConfigCollection col = getCol();
		ConfigIterator it = col.find(Predicates.field("manager", manager.getName()));
		try {
			while (it.hasNext()) {
				Config c = it.next();
				ResponseActionInstance instance = PrimitiveConverter.parse(ResponseActionInstance.class, c.getDocument());
				manager.newAction(instance.getNamespace(), instance.getName(), instance.getDescription(),
						toProperties(instance.getConfigs()));
			}
		} finally {
			if (it != null)
				it.close();
		}
	}

	private Properties toProperties(List<ResponseActionConfig> configs) {
		Properties p = new Properties();
		for (ResponseActionConfig c : configs) {
			p.put(c.getName(), c.getValue());
		}
		return p;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		ResponseActionManager manager = (ResponseActionManager) super.addingService(reference);
		loadActions(manager);
		manager.addEventListener(this);

		return manager;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		ResponseActionManager manager = (ResponseActionManager) service;
		manager.removeEventListener(this);
		super.removedService(reference, service);
	}

	@Override
	public void actionCreated(ResponseActionManager manager, ResponseAction action) {
		logger.info("kraken siem: insert new action to database [{}]", action);
		ConfigCollection col = getCol();
		Config c = findAction(col, manager, action);
		if (c != null)
			return;

		ResponseActionInstance instance = new ResponseActionInstance();
		instance.setManager(manager.getName());
		instance.setNamespace(action.getNamespace());
		instance.setName(action.getName());
		instance.setDescription(action.getDescription());

		Properties config = action.getConfig();

		for (Object key : config.keySet()) {
			Object value = config.get(key);
			if (value == null)
				continue;

			instance.getConfigs().add(new ResponseActionConfig(key.toString(), value.toString()));
		}

		col.add(PrimitiveConverter.serialize(instance));
	}

	@Override
	public void actionRemoved(ResponseActionManager manager, ResponseAction action) {
		logger.info("kraken siem: delete action from database [{}]", action);
		ConfigCollection col = getCol();
		Config c = findAction(col, manager, action);

		if (c != null)
			col.remove(c);
	}

	private Config findAction(ConfigCollection col, ResponseActionManager manager, ResponseAction action) {
		Config c = col.findOne(Predicates.and( //
				Predicates.field("manager", manager.getName()),//
				Predicates.field("namespace", action.getNamespace()),//
				Predicates.field("name", action.getName())));
		return c;
	}

	private ConfigCollection getCol() {
		ConfigDatabase db = configManager.getDatabase();
		return db.ensureCollection("response_action_instance");
	}
}
