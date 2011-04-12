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

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
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
@JpaConfig(factory = "siem")
public class ResponseActionLoader extends ServiceTracker implements ResponseActionManagerEventListener {
	private final Logger logger = LoggerFactory.getLogger(ResponseActionLoader.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

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

	@SuppressWarnings("unchecked")
	@Transactional
	private void loadActions(ResponseActionManager manager) {
		EntityManager em = entityManagerService.getEntityManager();
		List<ResponseActionInstance> instances = em.createQuery("FROM ResponseActionInstance r WHERE r.manager = ?").setParameter(
				1, manager.getName()).getResultList();

		for (ResponseActionInstance instance : instances) {
			manager.newAction(instance.getNamespace(), instance.getName(), instance.getDescription(),
					toProperties(instance.getConfigs()));
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

	@Transactional
	@Override
	public void actionCreated(ResponseActionManager manager, ResponseAction action) {
		logger.info("kraken siem: insert new action to database [{}]", action);
		EntityManager em = entityManagerService.getEntityManager();

		ResponseActionInstance instance = new ResponseActionInstance();
		instance.setManager(manager.getName());
		instance.setNamespace(action.getNamespace());
		instance.setName(action.getName());
		instance.setDescription(action.getDescription());
		em.persist(instance);

		Properties config = action.getConfig();

		for (Object key : config.keySet()) {
			Object value = config.get(key);
			if (value == null)
				continue;

			ResponseActionConfig c = new ResponseActionConfig();
			c.setInstance(instance);
			c.setName(key.toString());
			c.setValue(value.toString());

			instance.getConfigs().add(c);
			em.persist(c);
		}
	}

	@Transactional
	@Override
	public void actionRemoved(ResponseActionManager manager, ResponseAction action) {
		logger.info("kraken siem: delete action from database [{}]", action);

		EntityManager em = entityManagerService.getEntityManager();
		ResponseActionInstance instance = (ResponseActionInstance) em.createQuery(
				"FROM ResponseActionInstance r WHERE r.namespace = ? AND r.name = ?").setParameter(1,
				action.getNamespace()).setParameter(2, action.getName()).getSingleResult();

		em.remove(instance);
	}
}
