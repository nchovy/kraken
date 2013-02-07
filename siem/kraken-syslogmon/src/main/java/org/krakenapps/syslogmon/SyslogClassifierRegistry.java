/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.syslogmon;

import java.util.Collection;

/**
 * Syslog classifier registry manages syslog classifiers.
 * 
 * @author xeraph@nchovy.com
 * 
 */
public interface SyslogClassifierRegistry {
	/**
	 * @return all registered classifier names.
	 */
	Collection<String> getClassifierNames();

	/**
	 * @param name
	 *            the classifier name to search
	 * @return the matched classifier instance. return null if not found
	 */
	SyslogClassifier getClassifier(String name);

	/**
	 * Register classifier instance with name.
	 * 
	 * @throws when
	 *             duplicated name already exists
	 * @param name
	 *            the classifier name
	 * @param classifier
	 *            the classifier instance
	 */
	void register(String name, SyslogClassifier classifier);

	/**
	 * Unregister classifier instance
	 * 
	 * @param name
	 *            the classifier name
	 */
	void unregister(String name);
}
