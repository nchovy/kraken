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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and saves filter states.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterConfig {
	final Logger logger = LoggerFactory.getLogger(FilterConfig.class.getName());
	private Preferences prefs;
	private AtomicInteger maxId = new AtomicInteger();

	/**
	 * Creates a filter config instance and prepare tables.
	 */
	public FilterConfig(Preferences prefs) {
		this.prefs = prefs;
		fetchMaxId();
	}

	private void fetchMaxId() {
		// cache max
		try {
			Preferences instances = getFilterPrefs().node("instances");
			for (String name : instances.childrenNames()) {
				Preferences n = instances.node(name);
				int id = n.getInt("id", -1);
				if (id > maxId.get())
					maxId.set(id);
			}
		} catch (BackingStoreException e) {
			logger.warn("kraken filter: max counting failed", e);
		}
	}

	/**
	 * Returns a List of loaded filter instance informations for specific filter
	 * class name.
	 * 
	 * @param className
	 *            the filter class name
	 * @return a List of loaded filter instance informations
	 */
	public List<FilterInstance> loadFilterInstances(String className) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			List<FilterInstance> instances = new ArrayList<FilterInstance>();

			for (String filterId : prefs.childrenNames()) {
				Preferences n = prefs.node(filterId);

				String clazzName = n.get("class_name", null);
				if (className == null || !className.equals(clazzName))
					continue;

				FilterInstance f = new FilterInstance();
				f.setId(n.getInt("id", -1));
				f.setName(filterId);
				f.setClassName(clazzName);
				f.setFilterType(n.getInt("filter_type", -1));
				instances.add(f);
			}

			return instances;
		} catch (BackingStoreException e) {
			logger.warn("kraken filter: load filter instances failed", e);
		}
		return null;
	}

	/**
	 * Returns a List of all loaded filter instance informations.
	 */
	public List<FilterInstance> loadFilterInstances() {
		return loadFilterInstances(null);
	}

	/**
	 * Save filter instance information to data store.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param filterClassName
	 *            the filter class name
	 * @param filterType
	 *            active filter or not
	 * @return true if saved successfully
	 */
	public boolean addFilterInstance(String filterId, String filterClassName, int filterType) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			// duplicate check by name
			if (prefs.nodeExists(filterId))
				return false;

			logger.info("adding filter: " + filterId + " " + filterClassName);

			Preferences newNode = prefs.node(filterId);
			newNode.putInt("id", maxId.incrementAndGet());
			newNode.put("class_name", filterClassName);
			newNode.putInt("filter_type", filterType);

			sync(newNode);
			return true;
		} catch (BackingStoreException e) {
			logger.warn("kraken filter: add filter instance failed", e);
		}
		return false;
	}

	/**
	 * Remove the filter instance information from data store.
	 * 
	 * @param filterId
	 *            the filter id
	 */
	public void removeFilterInstance(String filterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			if (!prefs.nodeExists(filterId))
				return;

			prefs.node(filterId).removeNode();
			sync(prefs);
		} catch (BackingStoreException e) {
			logger.error("kraken filter: remove filter instance error", e);
		}
	}

	/**
	 * Returns the properties of the filter.
	 * 
	 * @param filterId
	 *            the filter id
	 * @return the properties of the filter. null if error occurred.
	 */
	public Properties getFilterProperties(String filterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		Properties props = new Properties();

		try {
			if (!prefs.nodeExists(filterId))
				return null;

			Preferences instance = prefs.node(filterId);
			Preferences properties = instance.node("properties");

			for (String name : properties.childrenNames()) {
				String value = properties.get(name, null);
				props.put(name, value);
			}

			return props;
		} catch (BackingStoreException e) {
			logger.error("kraken filter: get filter properties error failed", e);
		}
		return null;
	}

	/**
	 * Save the property of the filter instance to data store.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param name
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 */
	public void setFilterProperty(String filterId, String name, String value) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			if (!prefs.nodeExists(filterId))
				return;

			Preferences instance = prefs.node(filterId);
			Preferences props = instance.node("properties");
			props.put(name, value);
			sync(instance);
		} catch (BackingStoreException e) {
			logger.error("set filter property error: ", e);
		}
	}

	/**
	 * Remove all properties of the filter from data store.
	 * 
	 * @param filterId
	 *            the filter id
	 */
	public void resetFilterProperties(String filterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			if (!prefs.nodeExists(filterId))
				return;

			Preferences instance = prefs.node(filterId);
			instance.node("properties").removeNode();
			sync(instance);
		} catch (BackingStoreException e) {
			logger.warn("kraken filter: reset filter props failed", e);
		}
	}

	/**
	 * Remove the property of the filter instance from data store.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param name
	 *            the name of the filter
	 */
	public void unsetFilterProperty(String filterId, String name) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			if (!prefs.nodeExists(filterId))
				return;

			Preferences instance = prefs.node(filterId);
			Preferences props = instance.node("properties");
			props.remove(name);

			sync(props);
		} catch (BackingStoreException e) {
			logger.error("kraken filter: unset filter property failed", e);
		}
	}

	/**
	 * Save the bind state to data store.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 */
	public void bindFilter(String fromFilterId, String toFilterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			if (!prefs.nodeExists(fromFilterId) || !prefs.nodeExists(toFilterId))
				return;

			Preferences from = prefs.node(fromFilterId).node("output_filters");
			Preferences to = prefs.node(toFilterId).node("input_filters");

			from.putBoolean(toFilterId, true);
			to.putBoolean(fromFilterId, true);

			sync(prefs);
		} catch (Exception e) {
			logger.error("kraken filter: bind filter failed", e);
		}
	}

	/**
	 * Remove the bind state from data store.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 */
	public void unbindFilter(String fromFilterId, String toFilterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		try {
			// remove output filter binding
			if (prefs.nodeExists(fromFilterId)) {
				Preferences from = prefs.node(fromFilterId).node("output_filters");
				from.remove(toFilterId);
				sync(from);
			}

			if (prefs.nodeExists(toFilterId)) {
				Preferences to = prefs.node(toFilterId).node("input_filters");
				to.remove(fromFilterId);
				sync(to);
			}

		} catch (BackingStoreException e) {
			logger.warn("kraken filter: unbind filter failed", e);
		}
	}

	/**
	 * Returns a List of bound source filters.
	 * 
	 * @param filterId
	 *            the filter id
	 * @return a List of filter id of bound source filters
	 */
	public List<String> getInputFilters(String filterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		List<String> filters = new ArrayList<String>();
		try {
			if (!prefs.nodeExists(filterId))
				return null;

			Preferences inputFilters = prefs.node(filterId).node("input_filters");
			for (String name : inputFilters.keys())
				filters.add(name);

			return filters;
		} catch (BackingStoreException e) {
			logger.error("kraken filter: get input filters error", e);
		}

		return filters;
	}

	/**
	 * Returns a List of destination filters.
	 * 
	 * @param filterId
	 *            the filter id
	 * @return a List of filter id of bound destination filters
	 */
	public List<String> getOutputFilters(String filterId) {
		Preferences prefs = getFilterPrefs().node("instances");
		List<String> filters = new ArrayList<String>();
		try {
			if (!prefs.nodeExists(filterId))
				return null;

			Preferences outputFilters = prefs.node(filterId).node("output_filters");
			for (String name : outputFilters.keys()) {
				filters.add(name);
			}
		} catch (BackingStoreException e) {
			logger.error("kraken filter: get output filters error", e);

		}
		return filters;
	}

	private void sync(Preferences newNode) throws BackingStoreException {
		newNode.flush();
		newNode.sync();
	}

	private Preferences getFilterPrefs() {
		return prefs.node("/kraken-filter");
	}
}
