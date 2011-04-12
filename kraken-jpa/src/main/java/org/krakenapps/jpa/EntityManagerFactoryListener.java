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

import javax.persistence.EntityManagerFactory;

/**
 * Provides entity manager factory life cycle events
 * 
 * @author xeraph
 * 
 */
public interface EntityManagerFactoryListener {
	/**
	 * Callback for entity manager factory addition
	 * 
	 * @param factoryName
	 *            the name of entity manager factory
	 * @param factory
	 *            the newly added entity manager factory
	 */
	void factoryAdded(String factoryName, EntityManagerFactory factory);

	/**
	 * Callback for entity manger factory removal
	 * 
	 * @param factoryName
	 *            the name of entity manager factory
	 * @param factory
	 *            the removed entity manager factory
	 */
	void factoryRemoved(String factoryName, EntityManagerFactory factory);
}
