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
 * Unchecked exception thrown when the filter is already bound to the specified
 * filter.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class AlreadyBoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String fromFilterId;
	private String toFilterId;

	/**
	 * Creates an exception with source and destination filter informations.
	 * 
	 * @param fromFilterId
	 *            the source filter id
	 * @param toFilterId
	 *            the destination filter id
	 */
	public AlreadyBoundException(String fromFilterId, String toFilterId) {
		this.fromFilterId = fromFilterId;
		this.toFilterId = toFilterId;
	}

	@Override
	public String getMessage() {
		return toFilterId + " is already bound to " + fromFilterId;
	}
}
