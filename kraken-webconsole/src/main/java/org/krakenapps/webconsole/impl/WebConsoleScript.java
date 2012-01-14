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
package org.krakenapps.webconsole.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.webconsole.Program;
import org.krakenapps.webconsole.ProgramApi;

public class WebConsoleScript implements Script {
	private ScriptContext context;
	private ProgramApi programApi;

	public WebConsoleScript(ProgramApi programApi) {
		this.programApi = programApi;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void programs(String[] args) {
		for (Program p : programApi.getPrograms()) {
			context.println(p.toString());
		}
	}
}
