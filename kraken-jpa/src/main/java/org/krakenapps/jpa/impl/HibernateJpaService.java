/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.jpa.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.hibernate.MappingException;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.cfg.annotations.CollectionBinder;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.cfg.annotations.QueryBinder;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.TableMetadata;
import org.krakenapps.api.LoggerControlService;
import org.krakenapps.jpa.JpaProfile;
import org.krakenapps.jpa.EntityManagerFactoryListener;
import org.krakenapps.jpa.JpaService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateJpaService implements JpaService {
	final Logger logger = LoggerFactory.getLogger(HibernateJpaService.class.getName());

	private LoggerControlService loggerControl;

	private BundleContext bc;
	private Map<String, EntityManagerFactory> factoryMap;
	private Map<String, JpaProfile> profileMap;
	private Set<EntityManagerFactoryListener> listeners;
	private JpaConfig jpaConfig;

	private static String ENTITIES = "/OSGI-INF/kraken-jpa/classes";
	private static String CONFIG = "/OSGI-INF/kraken-jpa/config";

	public HibernateJpaService(BundleContext bc) {
		logger.info("Hibernate JPA Service created.");
		Preferences prefs = getPreferences(bc);

		this.bc = bc;
		this.factoryMap = new ConcurrentHashMap<String, EntityManagerFactory>();
		this.profileMap = new ConcurrentHashMap<String, JpaProfile>();
		this.listeners = new HashSet<EntityManagerFactoryListener>();
		this.jpaConfig = new JpaConfig(prefs);
	}

	private Preferences getPreferences(BundleContext context) {
		ServiceReference ref = context.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) context.getService(ref);
		Preferences prefs = prefsService.getSystemPreferences();
		return prefs;
	}

	/**
	 * Restore all entity manager factories with saved configurations
	 */
	public void start() {
		disableLog(SettingsFactory.class);
		disableLog(TableMetadata.class);
		disableLog(CollectionBinder.class);
		disableLog(AnnotationBinder.class);
		disableLog(EntityBinder.class);
		disableLog(QueryBinder.class);

		loadEntityManagerFactories();
	}

	private void disableLog(Class<?> clazz) {
		loggerControl.setLogLevel(clazz.getName(), "info", false);
		loggerControl.setLogLevel(clazz.getName(), "trace", false);
		loggerControl.setLogLevel(clazz.getName(), "debug", false);
	}

	/**
	 * shutdown
	 */
	public void stop() {
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory(Properties props, List<Class<?>> entityClasses) {
		Ejb3Configuration c = new Ejb3Configuration();
		c.setProperties(props);

		for (Class<?> entityClass : entityClasses) {
			try {
				c.addAnnotatedClass(entityClass);
			} catch (MappingException e) {
				logger.error("JPA: error mapping entity class ", e);
			}
		}
		return c.buildEntityManagerFactory();
	}

	@Override
	public Set<String> getProfileNames() {
		return profileMap.keySet();
	}

	@Override
	public JpaProfile getProfile(String factoryName) {
		return profileMap.get(factoryName);
	}

	@Override
	public void registerEntityManagerFactory(String name, Properties properties, long bundleId) throws BundleException {
		Bundle bundle = bc.getBundle(bundleId);
		if (bundle == null) {
			logger.warn("JPA: bundle [{}] not found.", bundleId);
			throw new BundleException(bundleId + " bundle not found.");
		}

		URL entitiesFile = bundle.getEntry(ENTITIES);
		if (entitiesFile == null) {
			logger.warn("JPA: entities configuration not found: bundle " + bundleId);
			return;
		}

		logger.trace("JPA: adding entity classes from bundle: " + bundle.getSymbolicName());

		// load and override configuration
		URL configFile = bundle.getEntry(CONFIG);
		if (configFile == null) {
			logger.warn("JPA: config not found.");
		}

		Properties overridedProperties = new Properties();

		if (configFile != null)
			try {
				overridedProperties.load(new InputStreamReader(configFile.openStream()));
			} catch (IOException e1) {
				logger.warn("JPA: config load error: ", e1);
			}

		for (Object key : properties.keySet()) {
			overridedProperties.put(key, properties.get(key));
		}

		Ejb3Configuration c = new Ejb3Configuration();
		c.setProperties(overridedProperties);

		List<String> classNames = new ArrayList<String>();

		try {
			InputStreamReader isr = new InputStreamReader(entitiesFile.openStream());
			BufferedReader br = new BufferedReader(isr);
			String entity = null;

			while ((entity = br.readLine()) != null) {
				entity = entity.trim();
				if (entity.startsWith("#"))
					continue;

				logger.trace("JPA: adding entity class - " + entity);

				try {
					c.addAnnotatedClass(bundle.loadClass(entity));
					classNames.add(entity);
				} catch (Exception e) {
					logger.error("JPA: failed to add JPA entity: ", e);
				}
			}
		} catch (IOException e) {
			logger.error("JPA: error reading from bundle: ", e);
		}

		logger.info("JPA: generating entity manager factory for bundle " + bundleId);

		try {
			// if model is broken, buildEntityManagerFactory will throw
			// exception
			EntityManagerFactory factory = c.buildEntityManagerFactory();

			// verify connection (is valid password?)
			EntityManager em = factory.createEntityManager();
			if (em.isOpen()) {
				try {
					em.getTransaction().begin();
					em.getTransaction().rollback();
				} finally {
					em.close();
				}
			} else
				throw new IllegalStateException("JDBC connection open failed");

			// set config
			JpaProfile config = new JpaProfile();
			config.setName(name);
			config.setProperties(overridedProperties);
			config.setClassNames(classNames);
			profileMap.put(name, config);
			factoryMap.put(name, factory);

			logger.info("JPA: entity manager factory [{}] added.", name);

			// send event to listeners
			synchronized (listeners) {
				for (EntityManagerFactoryListener listener : listeners) {
					listener.factoryAdded(name, factory);
				}
			}

			// save information to db
			jpaConfig.addEntityManagerFactoryInstance(bundleId, name, overridedProperties);
		} catch (PersistenceException e) {
			logger.warn("JPA: entity manager factory not built", e);
			throw e;
		}
	}

	@Override
	public void unregisterEntityManagerFactory(String name) {
		EntityManagerFactory factory = factoryMap.remove(name);
		if (factory != null && factory.isOpen()) {
			factory.close();
		}

		profileMap.remove(name);

		// send event to listeners
		synchronized (listeners) {
			for (EntityManagerFactoryListener listener : listeners) {
				listener.factoryRemoved(name, factory);
			}
		}

		// remove from db
		jpaConfig.removeEntityManagerFactoryInstance(name);
	}

	@Override
	public boolean hasEntityManagerFactory(String factoryName) {
		return factoryMap.get(factoryName) != null;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory(String factoryName) {
		logger.debug("JPA: get entity manager factory [{}]", factoryName);
		return factoryMap.get(factoryName);
	}

	@Override
	public EntityManager createEntityManager(String factoryName) {
		EntityManagerFactory factory = factoryMap.get(factoryName);
		if (factory != null) {
			return factory.createEntityManager();
		}

		logger.error("JPA: entity manager factory [{}] not found.", factoryName);
		return null;
	}

	@Override
	public EntityManager createEntityManager(String factoryName, Map<Object, Object> map) {
		EntityManagerFactory factory = factoryMap.get(factoryName);
		if (factory != null) {
			return factory.createEntityManager(map);
		}

		logger.error("JPA: entity manager factory [{}] not found.", factoryName);
		return null;
	}

	@Override
	public void addEntityManagerFactoryListener(EntityManagerFactoryListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeEntityManagerFactoryListener(EntityManagerFactoryListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void loadEntityManagerFactories() {
		List<EntityManagerFactoryInstance> instances = jpaConfig.getEntityManagerFactoryInstances();
		for (EntityManagerFactoryInstance instance : instances) {
			String factoryName = instance.getFactoryName();
			try {
				registerEntityManagerFactory(factoryName, instance.getProperties(), instance.getBundleId());
			} catch (BundleException e) {
				// remove saved state if model bundle is removed.
				jpaConfig.removeEntityManagerFactoryInstance(factoryName);
			}
		}
	}
}
