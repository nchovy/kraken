package org.krakenapps.msgbus.handler;

import java.lang.annotation.Annotation;
import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.PackageMetadataProvider;

public class MsgbusPackageHandler extends PrimitiveHandler implements PackageMetadataProvider {
	private MessageBus msgbus;
	private String packageKey;
	private ConcurrentMap<String, String> nameLocalizations;

	@SuppressWarnings("unchecked")
	@Override
	public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
		Object o = getInstanceManager().getPojoObject();
		MsgbusPackage pkg = o.getClass().getAnnotation(MsgbusPackage.class);
		packageKey = pkg.value();
		nameLocalizations = new ConcurrentHashMap<String, String>();

		for (Annotation annotation : o.getClass().getAnnotations()) {
			if (annotation instanceof MsgbusPackageName) {
				MsgbusPackageName name = (MsgbusPackageName) annotation;
				nameLocalizations.put(name.locale(), name.name());
			}
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void stateChanged(int state) {
		if (state == InstanceManager.VALID) {
			validate();
		} else if (state == InstanceManager.INVALID) {
			invalidate();
		}
	}

	private void validate() {
		msgbus.register(this);
	}

	private void invalidate() {
		msgbus.unregister(this);
	}

	@Override
	public String getKey() {
		return packageKey;
	}

	@Override
	public String getName() {
		return getName("en");
	}

	@Override
	public String getName(String locale) {
		String name = nameLocalizations.get(locale);
		if (name != null)
			return name;

		return nameLocalizations.get("en");
	}

}
