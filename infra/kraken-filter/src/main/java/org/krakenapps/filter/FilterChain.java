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
 * A container of {@link Filter}s that forwards {@link Message}s to the
 * consisting filters sequentially.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public interface FilterChain {
	/**
	 * Processes a {@link Message}.
	 * 
	 * @param message
	 *            the forwarding message
	 */
	void process(Message message);
}
