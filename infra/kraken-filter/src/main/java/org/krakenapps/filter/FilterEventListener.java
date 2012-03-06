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

/**
 * The listener interface for receiving filter events.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public interface FilterEventListener {
	/**
	 * Invoked synchronously when a filter is loaded.
	 */
	void onFilterLoaded(String filterId);

	/**
	 * Invoked synchronously when a filter is unloading.
	 */
	void onFilterUnloading(String filterId);

	/**
	 * Invoked synchronously when filters are bound.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 */
	void onFilterBound(String fromFilterId, String toFilterId);

	/**
	 * Invoked synchronously when filters are unbounding.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 */
	void onFilterUnbinding(String fromFilterId, String toFilterId);

	/**
	 * Invoked synchronously when a property is set.
	 * 
	 * @param filterId
	 *            the target filter id
	 * @param name
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 */
	void onFilterSet(String filterId, String name, Object value);

	/**
	 * Invoked synchronously when a property is removed.
	 * 
	 * @param filterId
	 *            the target filter id
	 * @param name
	 *            the name of the property
	 */
	void onFilterUnset(String filterId, String name);
}
