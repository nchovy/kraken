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
package org.krakenapps.filter;

import java.util.Dictionary;
import java.util.Properties;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.krakenapps.filter.exception.AlreadyBoundException;
import org.krakenapps.filter.exception.FilterNotFoundException;
import org.krakenapps.filter.exception.MessageSpecMismatchException;
import org.krakenapps.filter.impl.DefaultFilterChain;
import org.krakenapps.filter.impl.FilterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler class provides filter extension of iPOJO component instance.
 * FilterHanler injects a {@link FilterChain} instance and loads all properties.
 * Dynamic filter binding feature is implemented based on iPOJO code injection.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterHandler extends PrimitiveHandler {
	/**
	 * the slf4j logger
	 */
	final Logger logger = LoggerFactory.getLogger(FilterHandler.class.getName());

	/**
	 * the filter manager instance
	 */
	private FilterManager filterManager;

	/**
	 * the filter config instance
	 */
	private FilterConfig filterConfig;

	/**
	 * the injected filter chain instance
	 */
	private volatile FilterChain filterChain;

	/**
	 * the filter id
	 */
	private String filterId;

	/**
	 * Invoked when a filter component instance is created. Inject filter chain
	 * field here.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Element metadata, Dictionary config) throws ConfigurationException {

		filterManager = (FilterManager) config.get("filter.manager");
		filterConfig = (FilterConfig) config.get("filter.config");
		filterId = (String) config.get("instance.name");

		// inspects all fields and inject filter chain field.
		PojoMetadata pojoMetadata = getPojoMetadata();
		for (FieldMetadata fieldMetadata : pojoMetadata.getFields()) {
			if (fieldMetadata.getFieldType().equals("org.krakenapps.filter.FilterChain")) {
				getInstanceManager().register(fieldMetadata, this);
			}
		}

		// set instance.name property.
		Filter filter = getFilter();
		filter.setProperty("instance.name", filterId);

		// creates a filter chain instance with empty filter bindings.
		filterChain = new DefaultFilterChain(filter, new Filter[0]);

		logger.trace("filter [{}] configuration succeeded.", filterId);
	}

	/**
	 * Invoked when a filter is validated. Register the filter instance and
	 * restore previous states. (properties and bind states)
	 */
	@Override
	public void start() {
		logger.debug("Kraken filter handler is started.");

		Filter filter = getFilter();
		filterManager.registerFilter(this, filterId, filter);

		loadProperties(filter);
		bindAutomatically(filterId);
	}

	private void loadProperties(Filter filter) {
		int filterType = 0;
		if (filter instanceof ActiveFilter) {
			filterType = 1;
		}

		// write filter instance information to data store.
		filterConfig.addFilterInstance(filterId, filter.getClass().getName(), filterType);

		// load properties from data store if exists.
		Properties props = filterConfig.getFilterProperties(filterId);
		for (Object key : props.keySet()) {
			filter.setProperty((String) key, (String) props.get(key));
		}
	}

	/**
	 * Invoked when a filter is invalidated.
	 */
	@Override
	public void stop() {
		logger.debug("Kraken filter handler is stopped.");

		filterManager.unregisterFilter(filterId);
	}

	private void bindAutomatically(String pid) {
		for (String target : filterConfig.getOutputFilters(pid)) {
			if (filterManager.getFilter(target) != null)
				try {
					filterManager.bindFilter(pid, target);
					logger.trace(pid + " -> " + target + " binded.");
				} catch (FilterNotFoundException e) {
					logger.warn("filter not found:", e);
				} catch (AlreadyBoundException e) {
					logger.warn("filter is already bound:", e);
				} catch (MessageSpecMismatchException e) {
					logger.warn("message spec mismatch:", e);
				}
		}

		for (String from : filterConfig.getInputFilters(pid)) {
			if (filterManager.getFilter(from) != null)
				try {
					filterManager.bindFilter(from, pid);
					logger.info(from + " -> " + pid + " binded.");
				} catch (FilterNotFoundException e) {
					logger.warn("filter not found:", e);
				} catch (AlreadyBoundException e) {
					logger.warn("filter is already bound:", e);
				} catch (MessageSpecMismatchException e) {
					logger.warn("message spec mismatch:", e);
				}
		}

	}

	/**
	 * Invoked when the registered filter chain field is accessed.
	 */
	@Override
	public Object onGet(Object pojo, String fieldName, Object value) {
		return filterChain;
	}

	private Filter getFilter() {
		return (Filter) getInstanceManager().getPojoObject();
	}

	/**
	 * Invoked when a filter's binding states are changed cause of bind command
	 * or unbind command.
	 * 
	 * @param boundOutputFilters
	 *            the bound output filters
	 */
	public void stateChanged(Filter[] boundOutputFilters) {
		filterChain = new DefaultFilterChain(getFilter(), boundOutputFilters);
	}
}
