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

import javax.persistence.EntityManager;

import org.krakenapps.jpa.handler.TransactionOption;

/**
 * Provides entity manager service for declarative transaction support.
 * 
 * @author xeraph
 * 
 */
public interface ThreadLocalEntityManagerService {
	/**
	 * Set an entity manager factory of current thread context
	 * 
	 * @param factoryName
	 *            the alias for entity manager factory
	 */
	void setEntityManagerFactory(String factoryName, TransactionOption transactionMode);

	/**
	 * Get the entity manager for current thread context
	 * 
	 * @return an opened entity manager
	 */
	EntityManager getEntityManager();

	/**
	 * Begin transaction in current thread context
	 */
	void beginTransaction();

	/**
	 * Begin transaction in current thread context, but throw exception if
	 * transaction is already active.
	 * 
	 * @param failOnActiveTransaction
	 *            true if you want to stop when transaction is already active
	 */
	void beginTransaction(boolean failOnActiveTransaction);

	/**
	 * Commit transaction in current thread context
	 */
	void commitTransaction();

	/**
	 * Rollback transaction in current thread context
	 */
	void rollbackTransaction();

	/**
	 * Set rollback only in current thread context
	 */
	void setRollbackOnlyTransaction();

	/**
	 * Close entity manager in current thread context
	 */
	void closeEntityManager();
}
