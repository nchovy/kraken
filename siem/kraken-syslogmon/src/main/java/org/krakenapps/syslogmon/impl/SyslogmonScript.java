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
