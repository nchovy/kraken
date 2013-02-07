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
 * This class provides default implementations for the
 * {@link FilterEventListener} interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultFilterEventListener implements FilterEventListener {

	@Override
	public void onFilterBound(String fromFilterId, String toFilterId) {
	}

	@Override
	public void onFilterLoaded(String filterId) {
	}

	@Override
	public void onFilterUnbinding(String fromFilter, String toFilterId) {
	}

	@Override
	public void onFilterUnloading(String filterId) {
	}

	@Override
	public void onFilterSet(String filterId, String name, Object value) {
	}

	@Override
	public void onFilterUnset(String filterId, String name) {
	}

}
