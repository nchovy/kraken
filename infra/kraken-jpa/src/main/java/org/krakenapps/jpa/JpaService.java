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
package org.krakenapps.jpa;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.osgi.framework.BundleException;

/**
 * Provides management functionality for JPA entity manager
 * 
 * @author xeraph
 * 
 */
public interface JpaService {
	/**
	 * Create new entity manager factory with specified configurations. Entity
	 * manager factory's life cycle is not managed by JPA service.
	 * 
	 * @param props
	 *            the jpa configurations
	 * @param entityClasses
	 *            the related JPA entity class names
	 * @return the newly created entity manager factory
	 */
	EntityManagerFactory createEntityManagerFactory(Properties props, List<Class<?>> entityClasses);

	/**
	 * Returns names of current registered JPA entity manager factories
	 */
	Set<String> getProfileNames();

	/**
	 * Returns JPA configurations for specified entity manager factory
	 * 
	 * @param factoryName
	 *            the alias for entity manager factory
	 * @return the configurations for the entity manager factory or null
	 */
	JpaProfile getProfile(String factoryName);

	/**
	 * Create and register new entity manager factory
	 * 
	 * @param factoryName
	 *            the alias for new entity manager factory
	 * @param props
	 *            the JPA configurations
	 * @param bundleId
	 *            the id of bundle that contains kraken-jpa configurations and
	 *            entity classes
	 * @throws BundleException
	 *             if bundle not found
	 */
	void registerEntityManagerFactory(String factoryName, Properties props, long bundleId) throws BundleException;

	/**
	 * Close and unregister the entity manager factory
	 * 
	 * @param factoryName
	 *            the alias for new entity manager factory
	 */
	void unregisterEntityManagerFactory(String factoryName);

	/**
	 * Check if jpa service has specified entity manager factory
	 * 
	 * @param factoryName
	 *            the name of entity manager factory
	 * @return true if exists
	 */
	boolean hasEntityManagerFactory(String factoryName);

	/**
	 * Get an entity manager factory from JPA service
	 * 
	 * @param factoryName
	 *            the alias for the entity manager factory
	 * @return the entity manager factory or null
	 */
	EntityManagerFactory getEntityManagerFactory(String factoryName);

	/**
	 * Create a new entity manager using registered entity manager factory
	 * 
	 * @param factoryName
	 *            the name of registered entity manager factory
	 * @return an entity manager or null if factory not found
	 */
	EntityManager createEntityManager(String factoryName);

	/**
	 * Create a new entity manager with the specified Map of properties using
	 * registered entity manager factory
	 * 
	 * @param factoryName
	 *            the alias for the entity manager factory
	 * @param map
	 *            the properties
	 * @return an entity manager or null if factory not found
	 */
	EntityManager createEntityManager(String factoryName, Map<Object, Object> map);

	/**
	 * Add a listener for receiving entity manager factory event
	 * 
	 * @param listener
	 *            the listener instance
	 */
	void addEntityManagerFactoryListener(EntityManagerFactoryListener listener);

	/**
	 * Remove the listener for receiving entity manager factory event
	 * 
	 * @param listener
	 *            the listener instance
	 */
	void removeEntityManagerFactoryListener(EntityManagerFactoryListener listener);
}
