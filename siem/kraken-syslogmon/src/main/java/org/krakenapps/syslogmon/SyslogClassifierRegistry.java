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
