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
package org.krakenapps.jpa.handler;

import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.krakenapps.jpa.EntityManagerFactoryListener;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide declarative transaction support using iPOJO component handler.
 * TransactionHandler starts transaction at method entry, and commit/rollback
 * transaction at method exit.
 * 
 * @author xeraph
 * 
 */
public class TransactionalHandler extends PrimitiveHandler implements EntityManagerFactoryListener {
	final Logger logger = LoggerFactory.getLogger(Transactional.class.getName());

	private String factoryName;
	private JpaService jpaService;
	private ThreadLocalEntityManagerService threadLocalEntityManager;
	private String pojoClassName;

	private Map<Method, TransactionOption> methodFactoryMap = new ConcurrentHashMap<Method, TransactionOption>();

	/**
	 * Find and hook all transactional methods.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
		// Get factory name
		Element[] elements = metadata.getElements("Transactional", "org.krakenapps.jpa.handler");
		if (elements != null && elements.length > 0) {
			Element transactionalElement = elements[0];
			factoryName = transactionalElement.getAttribute("name");
		}

		// try external handler annotation
		if (factoryName == null) {
			Element[] el = metadata.getElements("JpaConfig", "org.krakenapps.jpa.handler");
			if (el == null || el.length == 0)
				throw new IllegalStateException("@JpaConfig configuration not found.");

			Element entityManagerFactoryElement = el[0];
			factoryName = entityManagerFactoryElement.getAttribute("factory");
		}

		Object o = getInstanceManager().getPojoObject();
		pojoClassName = o.getClass().getName();

		logger.trace("JPA: using [" + factoryName + "] entity manager factory for [{}].", pojoClassName);

		for (Method m : o.getClass().getDeclaredMethods()) {
			Transactional t = m.getAnnotation(Transactional.class);
			if (t != null) {
				logger.trace("JPA: transactional method detected: " + m.getName());

				String[] parameterTypes = getMethodParameterTypes(m);

				MethodMetadata mm = getPojoMetadata().getMethod(m.getName(), parameterTypes);
				if (mm != null) {
					logger.trace("JPA: annotated method injected: " + getMethodSignature(m));

					methodFactoryMap.put(m, t.value());
					getInstanceManager().register(mm, this);
				}
			}
		}
	}

	private String[] getMethodParameterTypes(Method m) {
		Class<?>[] types = m.getParameterTypes();
		String[] typeNames = new String[types.length];

		for (int i = 0; i < types.length; ++i) {
			if (types[i].isArray()) {
				String typeName = types[i].getComponentType().getName();
				typeNames[i] = typeName + "[]";
			} else {
				typeNames[i] = types[i].getName();
			}
		}

		return typeNames;
	}

	private String getMethodSignature(Method m) {
		StringBuilder sr = new StringBuilder();
		String[] types = getMethodParameterTypes(m);

		if (types.length == 0)
			return "()";

		sr.append("(");
		sr.append(types[0]);

		for (int i = 1; i < types.length; i++) {
			sr.append(", ");
			sr.append(types[i]);
		}

		sr.append(")");
		return sr.toString();
	}

	/**
	 * Check if entity manager factory is available and register factory life
	 * cycle event listener
	 */
	@Override
	public void start() {
		logger.trace("JPA: transactional handler for [{}] started ", pojoClassName);

		setValidity(false);
		if (jpaService != null) {
			validateEntityManagerFactory(factoryName);
		} else {
			logger.trace("JPA: factory not found for [{}] transaction handler.", pojoClassName);
		}
	}

	private void validateEntityManagerFactory(String factoryName) {
		logger.trace("JPA: adding [{}] entity manager factory listener for {}", factoryName, pojoClassName);
		jpaService.addEntityManagerFactoryListener(this);

		if (jpaService.getEntityManagerFactory(factoryName) != null) {
			logger.trace("JPA: [{}] factory found. [{}] transaction handler validated.", factoryName, pojoClassName);
			setValidity(true);
		}
	}

	/**
	 * Unregister factory life cycle event listener.
	 */
	@Override
	public void stop() {
		logger.trace("JPA: transactional handler for [{}] stopped.", pojoClassName);

		if (jpaService != null) {
			jpaService.removeEntityManagerFactoryListener(this);
		}
	}

	/**
	 * Find the entity manager in the current thread context and begin
	 * transaction at entry of transactional method.
	 */
	@Override
	public void onEntry(Object pojo, Method method, Object[] args) {
		logger.debug("JPA: TransactionalHandler method [{}] entry", method.getName());

		TransactionOption transactionMode = methodFactoryMap.get(method);

		if (threadLocalEntityManager != null) {
			threadLocalEntityManager.setEntityManagerFactory(factoryName, transactionMode);
			threadLocalEntityManager.beginTransaction();
		}
	}

	/**
	 * Commit transaction in the current thread context at exit of transactional
	 * method. onError method will be called if commit failed.
	 */
	@Override
	public void onExit(Object pojo, Method method, Object returnedObj) {
		String methodName = method.getName();

		if (threadLocalEntityManager != null)
			threadLocalEntityManager.commitTransaction();

		logger.debug("JPA: TransactionalHandler method [{}] exit", methodName);
	}

	/**
	 * Rollback transaction in the current thread context if an exception
	 * raised.
	 */
	@Override
	public void onError(Object pojo, Method method, Throwable throwable) {
		if (threadLocalEntityManager != null)
			threadLocalEntityManager.rollbackTransaction();

		logger.error("JPA: TransactionalHandler onError: ", throwable);
	}

	/**
	 * Close the entity manager in the current thread context.
	 */
	@Override
	public void onFinally(Object pojo, Method method) {
		if (threadLocalEntityManager != null)
			threadLocalEntityManager.closeEntityManager();
		else
			logger.warn("JPA: thread local entity manager is null.");
	}

	/**
	 * Set new JPA service and register event listener to JPA service.
	 * 
	 * @param jpaService
	 *            new JPA service
	 */
	public void setJpaService(JpaService jpaService) {
		this.jpaService = jpaService;
		validateEntityManagerFactory(factoryName);
	}

	/**
	 * Set new thread local entity manager service
	 * 
	 * @param threadLocalEntityManager
	 *            the thread local entity manager service
	 */
	public void setThreadLocalEntityManager(ThreadLocalEntityManagerService threadLocalEntityManager) {
		this.threadLocalEntityManager = threadLocalEntityManager;
	}

	/**
	 * Move to valid state if associated entity manager factory is added.
	 */
	@Override
	public void factoryAdded(String factoryName, EntityManagerFactory factory) {
		if (this.factoryName.compareTo(factoryName) == 0) {
			logger.trace("JPA: {} entity manager factory added. validating [{}]", factoryName, pojoClassName);
			setValidity(true);
		}
	}

	/**
	 * Move to invalid state if associated entity manager factory is removed.
	 */
	@Override
	public void factoryRemoved(String factoryName, EntityManagerFactory factory) {
		if (this.factoryName.compareTo(factoryName) == 0) {
			logger.trace("JPA: {} entity manager factory removed. invalidating [{}]", factoryName, pojoClassName);
			setValidity(false);
		}
	}
}
