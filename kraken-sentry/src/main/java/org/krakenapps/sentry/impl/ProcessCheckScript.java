/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.sentry.process.ProcessCheck;
import org.krakenapps.sentry.process.ProcessCheckEventListener;
import org.krakenapps.sentry.process.ProcessCheckOption;
import org.krakenapps.sentry.process.ProcessChecker;
import org.krakenapps.sentry.process.ProcessMonitor;

public class ProcessCheckScript implements Script {
	private ScriptContext context;
	private ProcessMonitor processMonitor;
	private ProcessChecker checker;

	public ProcessCheckScript(ProcessMonitor processMonitor, ProcessChecker checker) {
		this.processMonitor = processMonitor;
		this.checker = checker;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		context.println("Process Checklist");
		context.println("---------------------");
		for (ProcessCheck check : processMonitor.getProcessChecklist()) {
			context.println(check.toString());
		}
	}

	@ScriptUsage(description = "set process check", arguments = {
			@ScriptArgument(name = "process name", type = "string", description = "process name"),
			@ScriptArgument(name = "check option", type = "string", description = "allow or deny") })
	public void set(String[] args) {
		String processName = args[0];
		String option = args[1];
		ProcessCheckOption o;

		if (option.equalsIgnoreCase("allow"))
			o = ProcessCheckOption.Allow;
		else if (option.equalsIgnoreCase("deny"))
			o = ProcessCheckOption.Deny;
		else {
			context.println("invalid check option");
			return;
		}

		processMonitor.addProcess(processName, o);
		context.println("ok");
	}

	@ScriptUsage(description = "remove process check", arguments = { @ScriptArgument(name = "process name", type = "string", description = "process name") })
	public void remove(String[] args) {
		String processName = args[0];

		processMonitor.removeProcess(processName);
		context.println("ok");
	}

	public void check(String[] args) {
		if (checker == null) {
			context.println("process check is not supported");
			return;
		}

		ProcessCheckEventListener callback = new ProcessCheckEventListener() {
			@Override
			public void onCheck(String processName, ProcessCheckOption option, boolean isRunning) {
				context.printf("process=%s, option=%s, running=%s\n", processName, option.toString().toLowerCase(),
						isRunning);
			}
		};

		processMonitor.addListener(callback);
		try {
			checker.run();
			context.println("check completed");
		} finally {
			processMonitor.removeListener(callback);
		}
	}
}
