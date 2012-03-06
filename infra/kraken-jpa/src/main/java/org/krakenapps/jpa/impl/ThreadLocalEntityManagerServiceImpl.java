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

import java.util.EmptyStackException;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.krakenapps.jpa.JpaService;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.TransactionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for thread local entity manager service interface
 * 
 * @author xeraph
 * 
 */
public class ThreadLocalEntityManagerServiceImpl implements ThreadLocalEntityManagerService {
	final Logger logger = LoggerFactory.getLogger(ThreadLocalEntityManagerServiceImpl.class.getName());

	private JpaService jpa;

	private ThreadLocal<Stack<TransactionState>> transactionStacks = new ThreadLocal<Stack<TransactionState>>();

	class TransactionState {
		public String factoryName;
		public EntityManager entityManager;
		public boolean isOwner;
	}

	@Override
	public void setEntityManagerFactory(String factoryName, TransactionOption transactionMode) {
		logger.debug("JPA: setting new entity manager factory [{}] mode [{}] ", factoryName, transactionMode);
		if (jpa == null) {
			logger.warn("JPA: JPA service not found");
			return;
		}

		EntityManagerFactory factory = jpa.getEntityManagerFactory(factoryName);
		if (factory == null) {
			logger.warn("JPA: entity manager factory [{}] not found", factoryName);
			return;
		}

		TransactionState newState = new TransactionState();
		newState.factoryName = factoryName;

		EntityManager em = findEntityManager(factoryName);
		if (em == null || (em != null && transactionMode == TransactionOption.RequiresNew)) {
			newState.entityManager = factory.createEntityManager();
			newState.isOwner = true;
			if (logger.isDebugEnabled())
				logger.debug("JPA: new entity manager created: {}", factoryName);

		} else {
			newState.entityManager = em;
			newState.isOwner = false;
			if (logger.isDebugEnabled())
				logger.debug("JPA: entity manager found in the current thread context: {}", factoryName);
		}

		Stack<TransactionState> stack = getTransactionStack();
		stack.push(newState);
	}

	private Stack<TransactionState> getTransactionStack() {
		if (transactionStacks.get() == null)
			transactionStacks.set(new Stack<TransactionState>());

		return transactionStacks.get();
	}

	private EntityManager findEntityManager(String factoryName) {
		Stack<TransactionState> stack = getTransactionStack();
		for (TransactionState state : stack) {
			if (factoryName.equals(state.factoryName)) {
				return state.entityManager;
			}
		}
		return null;
	}

	@Override
	public EntityManager getEntityManager() {
		TransactionState state = getTransactionState();
		if (state == null)
			throw new IllegalStateException(
					"Transaction is not active. Check @Transactional annotation and/or entity manager factory.");

		return state.entityManager;
	}

	public TransactionState getTransactionState() {
		try {
			return getTransactionStack().peek();
		} catch (EmptyStackException e) {
			return null;
		}
	}

	@Override
	public void beginTransaction() {
		beginTransaction(false);
	}

	@Override
	public void beginTransaction(boolean failOnActiveTransaction) {
		TransactionState state = getTransactionState();
		if (state.isOwner == false) {
			logger.debug("JPA: overlapped transaction. begin() ignored.");
			return;
		}

		logger.debug("JPA: begin transaction");

		EntityManager em = state.entityManager;
		if (isOpen(em) == false) {
			throw new IllegalStateException("JPA: EntityManager is null or already closed.");
		}

		if (em.getTransaction().isActive() == false) {
			em.getTransaction().begin();
		} else if (failOnActiveTransaction) {
			logger.warn("JPA: Transaction is already active.");
			throw new IllegalStateException("JPA: Transaction is already active.");
		}
	}

	@Override
	public void commitTransaction() {
		TransactionState state = getTransactionState();
		if (state.isOwner == false) {
			logger.debug("JPA: overlapped transaction. commit() ignored.");
			return;
		}

		logger.debug("JPA: commit transaction");

		EntityManager em = state.entityManager;
		if (isInActiveTransaction(em) == false)
			return;

		em.flush();
		em.getTransaction().commit();
	}

	@Override
	public void rollbackTransaction() {
		TransactionState state = getTransactionState();
		if (state.isOwner == false) {
			logger.debug("JPA: overlapped transaction. rollback() ignored.");
			return;
		}

		logger.debug("JPA: rollback transaction");

		EntityManager em = getEntityManager();
		if (isInActiveTransaction(em) == false)
			return;

		em.getTransaction().rollback();
	}

	@Override
	public void setRollbackOnlyTransaction() {
		TransactionState state = getTransactionState();
		if (state.isOwner) {
			logger.debug("JPA: set transaction rollback-only");
			getEntityManager().getTransaction().setRollbackOnly();
		}
	}

	@Override
	public void closeEntityManager() {
		TransactionState state = getTransactionState();

		try {
			getTransactionStack().pop();
		} catch (EmptyStackException e) {
			logger.warn("JPA: illegal closeEntityManager() call");
			return;
		}

		if (state.isOwner == false) {
			logger.debug("JPA: overlapped transaction. close() ignored.");
			return;
		}

		EntityManager em = state.entityManager;
		if (isOpen(em))
			em.close();
	}

	private boolean isOpen(EntityManager em) {
		return em != null && em.isOpen();
	}

	private boolean isInActiveTransaction(EntityManager em) {
		return isOpen(em) && em.getTransaction().isActive();
	}
}
