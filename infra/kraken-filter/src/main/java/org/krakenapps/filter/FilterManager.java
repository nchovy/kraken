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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.krakenapps.filter.exception.AlreadyBoundException;
import org.krakenapps.filter.exception.DuplicatedFilterNameException;
import org.krakenapps.filter.exception.FilterNotBoundException;
import org.krakenapps.filter.exception.FilterNotFoundException;
import org.krakenapps.filter.exception.FilterFactoryNotFoundException;
import org.krakenapps.filter.exception.MessageSpecMismatchException;

/**
 * The interface for filter management.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public interface FilterManager {
	/**
	 * Returns a list of filter factory type name.
	 */
	List<String> getFilterFactoryNames();

	/**
	 * Returns a list of all filter instance descriptions.
	 */
	List<ComponentDescription> getFilterInstanceDescriptions();

	/**
	 * Loads a filter using filter factory index.
	 * 
	 * @param filterFactoryIndex
	 *            the filter factory index which is listed before.
	 * @param filterId
	 *            the filter id
	 * @throws FilterFactoryNotFoundException
	 *             if filter factory is not found
	 * @throws DuplicatedFilterNameException
	 *             if filter id is duplicated
	 */
	void loadFilter(int filterFactoryIndex, String filterId) throws FilterFactoryNotFoundException,
			DuplicatedFilterNameException;

	/**
	 * Loads a filter using the filter factory name.
	 * 
	 * @param filterFactoryName
	 *            the filter factory name
	 * @param filterId
	 *            the filter id
	 * @throws FilterFactoryNotFoundException
	 *             if filter factory is not found
	 * @throws DuplicatedFilterNameException
	 *             if filter id is duplicated
	 */
	void loadFilter(String filterFactoryName, String filterId) throws FilterFactoryNotFoundException,
			DuplicatedFilterNameException;

	/**
	 * Unloads the filter.
	 * 
	 * @param filterId
	 *            the filter id
	 * @throws FilterNotFoundException
	 *             if filter is not found
	 */
	void unloadFilter(String filterId) throws FilterNotFoundException;

	/**
	 * Starts an {@link ActiveFilter} thread with specified milliseconds
	 * interval.
	 * 
	 * @param filterId
	 *            the {@link ActiveFilter} id
	 * @param period
	 *            the sleep interval in milliseconds
	 * @throws FilterNotFoundException
	 *             if active filter is not found
	 * @throws IllegalThreadStateException
	 *             if active filter is already running
	 */
	void runFilter(String filterId, long period) throws FilterNotFoundException, IllegalThreadStateException;

	/**
	 * Stops the active filter.
	 * 
	 * @param filterId
	 *            the {@link ActiveFilter} id
	 * @throws FilterNotFoundException
	 *             if active filter is not found
	 */
	void stopFilter(String filterId) throws FilterNotFoundException;

	/**
	 * Binds two filters.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 * @throws FilterNotFoundException
	 *             if source or destination filter is not found
	 * @throws AlreadyBoundException
	 *             if filters are already bound
	 * @throws MessageSpecMismatchException
	 *             if source filter's message specification and destinatino
	 *             filter's message specification is not matched.
	 */
	void bindFilter(String fromFilterId, String toFilterId) throws FilterNotFoundException, AlreadyBoundException,
			MessageSpecMismatchException;

	/**
	 * Unbind filters.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 * @throws FilterNotFoundException
	 *             if source or destination filter is not found
	 * @throws FilterNotBoundException
	 *             if filters are not bound
	 */
	void unbindFilter(String fromFilterId, String toFilterId) throws FilterNotFoundException, FilterNotBoundException;

	/**
	 * Returns the filter
	 * 
	 * @param filterId
	 *            the filter id
	 * @return the filter. returns null if not exists.
	 */
	Filter getFilter(String filterId);

	/**
	 * Returns matched filters that satisfy provided message specification. If
	 * both input and output specification are provided, search result will be
	 * AND'ed.
	 * 
	 * @param inputSpec
	 *            input message specification, check if inputSpec is subset of
	 *            filter's input message specifications. ignored if null.
	 * @param outputSpec
	 *            output message specification, check if outputSpec if superset
	 *            of filter's output message specification. ignored if null.
	 * 
	 * @return matched filters
	 */
	Collection<Filter> findFilters(MessageSpec inputSpec, MessageSpec outputSpec);

	/**
	 * Same with findFilters(), but returns only one of result.
	 * 
	 * @param inputSpec
	 *            input message specification, can be null.
	 * @param outputSpec
	 *            output message specification, can be null.
	 * @return
	 */
	Filter findFilter(MessageSpec inputSpec, MessageSpec outputSpec);

	/**
	 * Returns all bound input filters.
	 * 
	 * @param filterId
	 *            the filter id
	 * @return all bound input filters. empty array if input filter does not
	 *         exists
	 */
	Filter[] getInputFilters(String filterId);

	/**
	 * Returns all bound output filters.
	 * 
	 * @param filterId
	 *            the filter id
	 * @return all bound output filters. empty array if output filter does not
	 *         exists
	 */
	Filter[] getOutputFilters(String filterId);

	/**
	 * Register the filter with filter id and handler. Can be used as
	 * sub-routine of loadFilter. Invoked from {@link FilterHandler}.
	 * 
	 * @param filterHandler
	 *            the filter handler
	 * @param filterId
	 *            the filter id
	 * @param filter
	 *            the filter
	 */
	void registerFilter(FilterHandler filterHandler, String filterId, Filter filter);

	/**
	 * Unregister the filter. Can be used as sub-routine of unloadFilter.
	 * Invoked from {@link FilterHandler}.
	 * 
	 * @param filterId
	 *            the filter id
	 */
	void unregisterFilter(String filterId);

	/**
	 * Returns a Set of property keys
	 * 
	 * @param filterId
	 *            the filter id
	 * @return a Set of property keys
	 * @throws FilterNotFoundException
	 *             if filter is not found
	 */
	Set<String> getPropertyKeys(String filterId) throws FilterNotFoundException;

	/**
	 * Returns the value of the property.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param key
	 *            the key of property
	 * @return the property value
	 * @throws FilterNotFoundException
	 *             if filter is not found
	 */
	Object getProperty(String filterId, String key) throws FilterNotFoundException;

	/**
	 * Sets the property.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param key
	 *            the key of property
	 * @param value
	 *            the value of property
	 * @throws FilterNotFoundException
	 *             if filter is not found
	 */
	void setProperty(String filterId, String key, String value) throws FilterNotFoundException;

	/**
	 * Removes the property.
	 * 
	 * @param filterId
	 *            the filter id
	 * @param key
	 *            the key of property
	 * @throws FilterNotFoundException
	 *             if filter is not found
	 */
	void unsetProperty(String filterId, String key) throws FilterNotFoundException;

	/**
	 * Subscribes filter events.
	 * 
	 * @param listener
	 *            the filter event listener
	 */
	void subscribeFilterEvent(FilterEventListener listener);

	/**
	 * Unsubscribes filter events.
	 * 
	 * @param listener
	 *            the filter event listener
	 */
	void unsubscribeFilterEvent(FilterEventListener listener);
}
