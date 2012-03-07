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

import org.krakenapps.filter.Filter;
import org.krakenapps.filter.FilterChain;
import org.krakenapps.filter.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides default implementation for the {@link FilterChain}
 * interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultFilterChain implements FilterChain {
	final Logger logger = LoggerFactory.getLogger(DefaultFilterChain.class.getName());

	private Filter hostFilter;
	private Filter[] outputFilters;

	public DefaultFilterChain(Filter hostFilter, Filter[] outputFilters) {
		this.hostFilter = hostFilter;
		this.outputFilters = outputFilters;
	}

	@Override
	public void process(Message message) {
		for (Filter filter : outputFilters) {
			try {
				filter.process(message);
			} catch (Exception e) {
				logger.warn(getHostFilterId() + " filter chain error: ", e);
			}
		}
	}

	private String getHostFilterId() {
		return (String) hostFilter.getProperty("instance.name");
	}
}
