package org.krakenapps.syslogmon.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.syslogmon.SyslogClassifierRegistry;

public class SyslogmonScript implements Script {
	private ScriptContext context;
	private SyslogClassifierRegistry classifierRegistry;

	public SyslogmonScript(SyslogClassifierRegistry classfierRegistry) {
		this.classifierRegistry = classfierRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void classifiers(String[] args) {
		context.println("Syslog Classifiers");
		context.println("--------------------");
		for (String name : classifierRegistry.getClassifierNames()) {
			context.println(name + ": " + classifierRegistry.getClassifier(name));
		}
	}

}
