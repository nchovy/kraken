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
package org.krakenapps.filter.impl;

import java.util.List;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.FactoryStateListener;
import org.krakenapps.filter.FilterManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks loading and unloading of iPOJO component factory. Tracker detects
 * factory loading and restores all filter instances of the factory.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterFactoryTracker extends ServiceTracker implements FactoryStateListener {
	/**
	 * the filter manager
	 */
	private FilterManager filterManager;

	/**
	 * the filter config
	 */
	private FilterConfig filterConfig;

	/**
	 * slf4j logger
	 */
	final Logger logger = LoggerFactory.getLogger(FilterFactoryTracker.class);

	/**
	 * Create a filter factory tracker.
	 * 
	 * @param context
	 *            the OSGi bundle context
	 * @param filterManager
	 *            the filter manager
	 * @param filterConfig
	 *            the filter config
	 */
	public FilterFactoryTracker(BundleContext context, FilterManager filterManager,
			FilterConfig filterConfig) {
		super(context, Factory.class.getName(), null);
		this.filterManager = filterManager;
		this.filterConfig = filterConfig;

		logger.trace("filter factory tracker started.");
	}

	/**
	 * Invoked when new iPOJO component factory service is loaded.
	 */
	@Override
	public Object addingService(ServiceReference reference) {
		Factory factory = (Factory) super.addingService(reference);
		factory.addFactoryStateListener(this);

		logger.trace("adding factory: " + factory.getName());
		return factory;
	}

	/**
	 * Invoked when the iPOJO component factory service is removed.
	 */
	@Override
	public void removedService(ServiceReference reference, Object service) {
		Factory factory = (Factory) service;
		logger.trace("factory removed: " + factory.getName());

		factory.removeFactoryStateListener(this);
		super.removedService(reference, service);
	}

	/**
	 * Invoked when iPOJO factory's state is changed.
	 */
	@Override
	public void stateChanged(Factory factory, int newState) {
		// adding service first, then validated.
		// removed service first, then invalidated.
		if (newState == Factory.VALID) {
			loadFilterInstancesAutomatically(factory.getName());
			logger.trace(factory.getName() + " validated.");
		} else {
			logger.trace(factory.getName() + " invalidated.");
		}
	}

	/**
	 * Loads all filter instance states from config data store and triggers
	 * filter loading.
	 * 
	 * @param className
	 */
	private void loadFilterInstancesAutomatically(String className) {
		// className can be any class. (including filter)
		List<FilterInstance> instances = filterConfig.loadFilterInstances(className);
		if (instances == null)
			return;

		for (FilterInstance instance : instances) {
			try {
				logger.trace("Loading filter instance: {} -> {}", className, instance.getName());
				filterManager.loadFilter(className, instance.getName());
			} catch (Exception e) {
				logger.warn("load from tracker: ", e);
			}
		}

	}
}
