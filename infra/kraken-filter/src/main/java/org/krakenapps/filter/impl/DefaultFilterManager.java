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
import java.util.HashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.ComponentDescription;
import org.krakenapps.filter.ComponentDescriptionParser;
import org.krakenapps.filter.Filter;
import org.krakenapps.filter.FilterEventListener;
import org.krakenapps.filter.FilterHandler;
import org.krakenapps.filter.FilterManager;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.exception.AlreadyBoundException;
import org.krakenapps.filter.exception.DuplicatedFilterNameException;
import org.krakenapps.filter.exception.FilterNotBoundException;
import org.krakenapps.filter.exception.FilterNotFoundException;
import org.krakenapps.filter.exception.FilterFactoryNotFoundException;
import org.krakenapps.filter.exception.MessageSpecMismatchException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides default implementation for the {@link FilterManager}
 * interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultFilterManager implements FilterManager {
	final Logger logger = LoggerFactory.getLogger(DefaultFilterManager.class.getName());

	private static final String KRAKEN_FILTER_INTERFACE = "org.krakenapps.filter.Filter";
	private BundleContext bc;

	private Map<String, Filter> filterMap;
	private Map<String, ActiveFilterRunner> runnerMap;
	private Map<String, List<Filter>> forwardBindMap;
	private Map<String, List<Filter>> reverseBindMap;
	private Map<String, FilterHandler> handlerMap;
	private Map<String, ComponentInstance> componentMap;
	private List<FilterEventListener> listeners;
	private List<String> latestFilterFactories;
	private FilterConfig filterConfig;
	private FilterFactoryTracker factoryTracker;

	public DefaultFilterManager(BundleContext bc) {
		filterMap = new HashMap<String, Filter>();
		runnerMap = new HashMap<String, ActiveFilterRunner>();
		forwardBindMap = new HashMap<String, List<Filter>>();
		reverseBindMap = new HashMap<String, List<Filter>>();
		handlerMap = new HashMap<String, FilterHandler>();
		componentMap = new HashMap<String, ComponentInstance>();
		listeners = new ArrayList<FilterEventListener>();

		this.bc = bc;

		filterConfig = new FilterConfig(getSystemPreferences());
		factoryTracker = new FilterFactoryTracker(bc, this, filterConfig);
		factoryTracker.open();
	}

	@Override
	public void loadFilter(int filterTypeIndex, String filterId) throws FilterFactoryNotFoundException,
			DuplicatedFilterNameException {
		if (latestFilterFactories == null) {
			throw new FilterFactoryNotFoundException(null);
		}

		String filterFactoryName = latestFilterFactories.get(filterTypeIndex - 1);
		if (filterFactoryName == null) {
			throw new FilterFactoryNotFoundException(filterFactoryName);
		}

		loadFilter(filterFactoryName, filterId);
	}

	@Override
	public void loadFilter(String factoryName, String filterId) throws FilterFactoryNotFoundException,
			DuplicatedFilterNameException {
		ServiceReference factoryReference = findFilterFactoryReference(factoryName);
		if (factoryReference == null)
			throw new FilterFactoryNotFoundException(factoryName);

		ComponentDescription description = getComponentDescription(factoryReference);

		Factory factory = (Factory) bc.getService(factoryReference);
		Properties configuration = newServiceConfiguration(filterId, description);
		ComponentInstance instance = null;
		try {
			instance = factory.createComponentInstance(configuration);
		} catch (UnacceptableConfiguration e) {
			logger.warn("load filter error: ", e);
			throw new DuplicatedFilterNameException(filterId);
		} catch (MissingHandlerException e) {
			logger.warn("load filter error: ", e);
			return;
		} catch (ConfigurationException e) {
			// not reachable. filter handler do not throw this exception
			// actually.
			logger.warn("load filter error: ", e);
			return;
		}
		bc.ungetService(factoryReference);

		if (instance == null) {
			logger.warn("[{} -> {}] component initialization failed.", factoryName, filterId);
			return;
		}

		synchronized (this) {
			componentMap.put(filterId, instance);

			for (FilterEventListener listener : listeners) {
				listener.onFilterLoaded(filterId);
			}
		}

		instance.start();
	}

	@Override
	public void unloadFilter(String filterId) throws FilterNotFoundException {
		synchronized (this) {
			ComponentInstance instance = componentMap.remove(filterId);
			if (instance == null) {
				logger.warn("filter not found: " + filterId);
				throw new FilterNotFoundException(filterId);
			}

			for (FilterEventListener listener : listeners) {
				listener.onFilterUnloading(filterId);
			}

			instance.stop();
			instance.dispose();
		}
	}

	@Override
	public void runFilter(String filterId, long period) throws FilterNotFoundException, IllegalThreadStateException {
		Filter filter = getFilterInstance(filterId);

		if (filter instanceof ActiveFilter) {
			ActiveFilter activeFilter = (ActiveFilter) filter;
			if (activeFilter.isRunning())
				throw new IllegalThreadStateException(filterId + " thread is already running.");

			ActiveFilterRunner runner = new ActiveFilterRunner(activeFilter, period);
			synchronized (this) {
				runnerMap.put(filterId, runner);
			}
			runner.start();
		} else {
			throw new FilterNotFoundException(filterId);
		}
	}

	@Override
	public void stopFilter(String filterId) throws FilterNotFoundException {
		ActiveFilterRunner runner = runnerMap.get(filterId);
		if (runner == null)
			throw new FilterNotFoundException(filterId);

		runner.stop();
	}

	@Override
	public void registerFilter(FilterHandler filterHandler, String filterId, Filter filter) {
		logger.info("registering filter: " + filterId);

		synchronized (this) {
			filterMap.put(filterId, filter);
			forwardBindMap.put(filterId, new ArrayList<Filter>());
			reverseBindMap.put(filterId, new ArrayList<Filter>());
			handlerMap.put(filterId, filterHandler);
		}
	}

	@Override
	public void unregisterFilter(String filterId) {
		logger.info("unregistering filter: " + filterId);

		/*
		 * example) pid1 -> pid 2 -> pid3 if you want to unload pid 2, you
		 * should remove 2 forward link and 2 reverse link. forward link in
		 * bindMap: 1->2, 2->3 reverse link in reverseBindMap: 2->1, 3->2
		 */
		synchronized (this) {
			// remove filter
			Filter filter = filterMap.remove(filterId);

			// stop thread
			if (filter instanceof ActiveFilter) {
				ActiveFilter activeFilter = (ActiveFilter) filter;
				if (activeFilter.isRunning()) {
					ActiveFilterRunner runner = runnerMap.remove(filterId);
					if (runner != null) {
						logger.debug("[{}] thread stopping.", filterId);
						runner.stop();
						runner.waitToFinish();
						logger.debug("[{}] thread stopped.", filterId);
					}
				}
			}

			// remove 2->3
			filterMap.remove(filterId);
			List<Filter> filters2to3 = forwardBindMap.remove(filterId);

			// remove 3->2
			if (filters2to3 != null) {
				for (Filter filter3 : filters2to3) {
					// reverse lookup and remove pid
					String filter3Pid = (String) filter3.getProperty("instance.name");
					List<Filter> filters3to2 = reverseBindMap.get(filter3Pid);
					filters3to2.remove(filter);
				}
			}

			// remove 2->1
			List<Filter> filters2to1 = reverseBindMap.get(filterId);

			// remove 1->2
			if (filters2to1 != null) {
				for (Filter filter1 : filters2to1) {
					// forward lookup and remove pid
					String filter1Pid = (String) filter1.getProperty("instance.name");
					List<Filter> filters1to2 = forwardBindMap.get(filter1Pid);
					filters1to2.remove(filter);

					// notify state changed
					FilterHandler handler1 = handlerMap.get(filter1Pid);
					handler1.stateChanged(convertToArray(filters1to2));
				}
			}

			// remove event handler
			handlerMap.remove(filterId);

			// remove from db
			filterConfig.removeFilterInstance(filterId);
		}
	}

	private Properties newServiceConfiguration(String filterId, ComponentDescription description) {
		Properties configuration = new Properties();
		configuration.put("instance.name", filterId);
		configuration.put("filter.manager", this);
		configuration.put("filter.config", filterConfig);
		return configuration;
	}

	private ServiceReference findFilterFactoryReference(String factoryName) {
		try {
			ServiceReference[] refs = bc.getServiceReferences(Factory.class.getName(), "(component.class="
					+ factoryName + ")");

			if (refs == null || refs.length == 0)
				return null;

			return refs[0];
		} catch (InvalidSyntaxException e) {
			// ignore it.
		}
		return null;
	}

	@Override
	public List<String> getFilterFactoryNames() {
		try {
			// get all iPOJO factories and filter classes that implements kraken
			// filter interface.
			List<String> filterTypes = new ArrayList<String>();
			ServiceReference[] refs = bc.getServiceReferences(Factory.class.getName(), null);
			for (ServiceReference ref : refs) {
				ComponentDescription componentDescription = getComponentDescription(ref);
				List<String> specifications = componentDescription.getSpecifications();
				if (isFilterFactory(specifications)) {
					filterTypes.add(componentDescription.getImplementationClass());
				}
			}

			latestFilterFactories = filterTypes;
			return filterTypes;
		} catch (InvalidSyntaxException e) {
			// not reachable. ignore.
		}
		return null;
	}

	private ComponentDescription getComponentDescription(ServiceReference ref) {
		if (ref == null)
			return null;

		Object property = ref.getProperty("component.description");
		if (property == null)
			return null;

		String instanceName = (String) ref.getProperty("instance.name");
		String description = property.toString();
		return ComponentDescriptionParser.parse(instanceName, description);
	}

	private boolean isFilterFactory(List<String> specifications) {
		for (String specification : specifications) {
			if (specification.equals(KRAKEN_FILTER_INTERFACE))
				return true;
		}
		return false;
	}

	@Override
	public List<ComponentDescription> getFilterInstanceDescriptions() {
		List<ComponentDescription> descriptions = new ArrayList<ComponentDescription>();
		try {
			ServiceReference[] refs = bc.getServiceReferences(Filter.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {

					ComponentDescription description = new ComponentDescription();
					description.setInstanceName((String) ref.getProperty("instance.name"));
					description.setFactoryName((String) ref.getProperty("factory.name"));
					descriptions.add(description);
				}
			}
		} catch (InvalidSyntaxException e) {
			// not reachable. ignore.
		}

		return descriptions;
	}

	@Override
	public void bindFilter(String fromFilterId, String toFilterId) throws FilterNotFoundException,
			AlreadyBoundException, MessageSpecMismatchException {
		synchronized (this) {
			// get filter instances.
			Filter fromFilter = getFilterInstance(fromFilterId);
			Filter toFilter = getFilterInstance(toFilterId);

			// match message specification
			MessageSpec sendMessageSpec = fromFilter.getOutputMessageSpec();
			MessageSpec[] receiveMessageSpecs = toFilter.getInputMessageSpecs();
			if (matchMessageSpec(sendMessageSpec, receiveMessageSpecs) == false)
				throw new MessageSpecMismatchException(fromFilterId, toFilterId, sendMessageSpec, receiveMessageSpecs);

			// check if same filter already exists.
			List<Filter> bindedFilters = forwardBindMap.get(fromFilterId);
			for (Filter bindedFilter : bindedFilters) {
				String instanceName = (String) bindedFilter.getProperty("instance.name");
				if (instanceName.equals(toFilterId)) {
					throw new AlreadyBoundException(fromFilterId, toFilterId);
				}
			}

			// double check if same filter already exists.
			List<Filter> reverseBindedFilters = reverseBindMap.get(toFilterId);
			for (Filter reverseBindedFilter : reverseBindedFilters) {
				String instanceName = (String) reverseBindedFilter.getProperty("instance.name");
				if (instanceName.equals(fromFilterId)) {
					throw new AlreadyBoundException(fromFilterId, toFilterId);
				}
			}

			// bind and reverse bind
			bindedFilters.add(toFilter);
			reverseBindedFilters.add(fromFilter);

			// save bind status
			filterConfig.bindFilter(fromFilterId, toFilterId);

			// notify changed state.
			FilterHandler handler = handlerMap.get(fromFilterId);
			handler.stateChanged(convertToArray(bindedFilters));

			// notify event handlers
			for (FilterEventListener listener : listeners) {
				listener.onFilterBound(fromFilterId, toFilterId);
			}
		}
	}

	private boolean matchMessageSpec(MessageSpec outputMessageSpec, MessageSpec[] inputMessageSpecs) {
		for (MessageSpec inputSpec : inputMessageSpecs)
			if (isSatisfySpec(outputMessageSpec, inputSpec))
				return true;

		return false;
	}

	private boolean isSatisfySpec(MessageSpec sample, MessageSpec spec) {
		// Version range of send specification has only one version. (lower ==
		// upper)
		return sample.getName().equals(spec.getName()) && sample.getLatestVersion().isInRange(sample.getVersionRange());
	}

	@Override
	public void unbindFilter(String fromFilterId, String toFilterId) throws FilterNotFoundException,
			FilterNotBoundException {
		synchronized (this) {
			// get filter instances.
			Filter fromFilter = getFilterInstance(fromFilterId); // for
			// validation
			Filter toFilter = getFilterInstance(toFilterId);

			// notify event handlers
			for (FilterEventListener listener : listeners) {
				listener.onFilterUnbinding(fromFilterId, toFilterId);
			}

			// unbind it.
			List<Filter> bindedFilters = forwardBindMap.get(fromFilterId);
			boolean isRemoved = bindedFilters.remove(toFilter);

			List<Filter> reverseBindedFilters = reverseBindMap.get(toFilterId);
			reverseBindedFilters.remove(fromFilter);

			// save bind status
			filterConfig.unbindFilter(fromFilterId, toFilterId);

			// notify changed state.
			FilterHandler handler = handlerMap.get(fromFilterId);
			handler.stateChanged(convertToArray(bindedFilters));

			// raise exception if filter not found.
			if (isRemoved == false)
				throw new FilterNotBoundException(fromFilterId, toFilterId);
		}
	}

	@Override
	public Filter[] getInputFilters(String filterId) {
		synchronized (this) {
			List<Filter> bindedFilters = reverseBindMap.get(filterId);
			if (bindedFilters == null)
				return new Filter[0];

			return convertToArray(bindedFilters);
		}
	}

	@Override
	public Filter[] getOutputFilters(String filterId) {
		synchronized (this) {
			List<Filter> bindedFilters = forwardBindMap.get(filterId);
			if (bindedFilters == null)
				return new Filter[0];

			return convertToArray(bindedFilters);
		}
	}

	private Filter[] convertToArray(List<Filter> filterList) {
		Filter[] filters = new Filter[filterList.size()];
		int i = 0;
		for (Filter filter : filterList) {
			filters[i++] = filter;
		}
		return filters;
	}

	/*
	 * Get property value of specified filter instance.
	 */
	@Override
	public Object getProperty(String filterId, String key) throws FilterNotFoundException {
		synchronized (this) {
			Filter filter = getFilterInstance(filterId);
			return filter.getProperty(key);
		}
	}

	/*
	 * Get all property keys of specified filter instance.
	 */
	@Override
	public Set<String> getPropertyKeys(String filterId) throws FilterNotFoundException {
		Filter filter = getFilterInstance(filterId);
		return filter.getPropertyKeys();
	}

	/*
	 * Set property value for specific filter instance.
	 */
	@Override
	public void setProperty(String filterId, String key, String value) throws FilterNotFoundException {
		synchronized (this) {
			Filter filter = getFilterInstance(filterId);
			if (filter != null) {
				filterConfig.setFilterProperty(filterId, key, value);
				filter.setProperty(key, value);
			} else {
				throw new FilterNotFoundException(filterId);
			}

			// notify event handlers
			for (FilterEventListener listener : listeners) {
				listener.onFilterSet(filterId, key, value);
			}
		}
	}

	/*
	 * Unset property for specific filter instance.
	 */
	@Override
	public void unsetProperty(String filterId, String key) throws FilterNotFoundException {
		synchronized (this) {
			Filter filter = getFilterInstance(filterId);
			if (filter != null) {
				filterConfig.unsetFilterProperty(filterId, key);
				filter.unsetProperty(key);
			} else {
				throw new FilterNotFoundException(filterId);
			}

			// notify event handlers
			for (FilterEventListener listener : listeners) {
				listener.onFilterUnset(filterId, key);
			}
		}
	}

	/*
	 * Return filter instance or throws exception.
	 */
	private Filter getFilterInstance(String filterId) throws FilterNotFoundException {
		Filter filter = filterMap.get(filterId);
		if (filter == null)
			throw new FilterNotFoundException(filterId);
		return filter;
	}

	/*
	 * Return filter instance using id.
	 */
	@Override
	public Filter getFilter(String filterId) {
		synchronized (this) {
			return filterMap.get(filterId);
		}
	}

	@Override
	public Filter findFilter(MessageSpec inputSpec, MessageSpec outputSpec) {
		synchronized (this) {
			for (String filterId : filterMap.keySet()) {
				Filter filter = filterMap.get(filterId);
				if (isMatchedFilter(filter, inputSpec, outputSpec))
					return filter;
			}
		}
		return null;
	}

	@Override
	public Collection<Filter> findFilters(MessageSpec inputSpec, MessageSpec outputSpec) {
		Collection<Filter> filters = new ArrayList<Filter>();
		synchronized (this) {
			for (String filterId : filterMap.keySet()) {
				Filter filter = filterMap.get(filterId);
				if (isMatchedFilter(filter, inputSpec, outputSpec))
					filters.add(filter);
			}
		}

		return filters;
	}

	private boolean isMatchedFilter(Filter filter, MessageSpec inputSpec, MessageSpec outputSpec) {
		if (filter == null)
			return false;

		boolean matched = false;

		if (inputSpec != null)
			for (MessageSpec actualSpec : filter.getInputMessageSpecs())
				if (inputSpec.isSubsetOf(actualSpec))
					matched = true;

		if (inputSpec != null && matched == false)
			return false;

		if (outputSpec != null)
			if (!outputSpec.isSupersetOf(filter.getOutputMessageSpec()))
				return false;

		return true;
	}

	@Override
	public void subscribeFilterEvent(FilterEventListener listener) {
		synchronized (this) {
			listeners.add(listener);
		}
	}

	@Override
	public void unsubscribeFilterEvent(FilterEventListener listener) {
		synchronized (this) {
			listeners.remove(listener);
		}
	}

	private Preferences getSystemPreferences() {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		return prefsService.getSystemPreferences();
	}
}
