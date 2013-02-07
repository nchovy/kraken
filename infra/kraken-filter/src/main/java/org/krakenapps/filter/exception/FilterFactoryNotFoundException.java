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
package org.krakenapps.filter.exception;

/**
 * Unchecked exception thrown when the filter factory is not found.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterFactoryNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String filterFactoryName;

	/**
	 * Creates an exception with filter factory name
	 * 
	 * @param filterFactoryName
	 *            filter class name in general, but can have alias name.
	 */
	public FilterFactoryNotFoundException(String filterFactoryName) {
		this.filterFactoryName = filterFactoryName;
	}

	/**
	 * Returns the filter factory name.
	 */
	public String getFilterFactoryName() {
		return filterFactoryName;
	}

}
