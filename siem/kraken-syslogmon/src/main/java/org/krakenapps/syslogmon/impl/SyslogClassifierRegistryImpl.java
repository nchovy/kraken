package org.krakenapps.syslogmon.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.syslogmon.SyslogClassifier;
import org.krakenapps.syslogmon.SyslogClassifierRegistry;

@Component(name = "syslog-classifier-registry")
@Provides
public class SyslogClassifierRegistryImpl implements SyslogClassifierRegistry {

	private ConcurrentMap<String, SyslogClassifier> classifiers;

	public SyslogClassifierRegistryImpl() {
		classifiers = new ConcurrentHashMap<String, SyslogClassifier>();
	}

	@Override
	public Collection<String> getClassifierNames() {
		return Collections.unmodifiableCollection(classifiers.keySet());
	}

	@Override
	public SyslogClassifier getClassifier(String name) {
		return classifiers.get(name);
	}

	@Override
	public void register(String name, SyslogClassifier classifier) {
		SyslogClassifier old = classifiers.putIfAbsent(name, classifier);
		if (old != null)
			throw new IllegalStateException("duplicated classifier name: " + name);
	}

	@Override
	public void unregister(String name) {
		classifiers.remove(name);
	}

}
