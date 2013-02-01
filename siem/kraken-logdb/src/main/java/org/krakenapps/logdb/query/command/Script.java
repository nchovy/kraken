/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryScript;
import org.krakenapps.logdb.LogQueryScriptInput;
import org.krakenapps.logdb.LogQueryScriptOutput;
import org.osgi.framework.BundleContext;

public class Script extends LogQueryCommand {
	private BundleContext bc;
	private LogQueryScript script;
	private String[] args;
	private DefaultScriptInput input;
	private DefaultScriptOutput output;

	public Script(BundleContext bc, LogQueryScript script, String[] args) {
		this.bc = bc;
		this.script = script;
		this.args = args;
		this.input = new DefaultScriptInput();
		this.output = new DefaultScriptOutput();
	}

	@Override
	public void push(LogMap m) {
		input.data = m.map();
		for (int i = 0; i < args.length; i++) {
			String k = "args" + (i + 1);
			input.data.put(k, args[i]);
		}
		script.handle(input, output);
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;
		script.eof(output);
		super.eof();
	}

	private void out(LogMap data) {
		write(data);
	}

	private class DefaultScriptInput implements LogQueryScriptInput {
		private Map<String, Object> data;

		@Override
		public BundleContext getBundleContext() {
			return bc;
		}

		@Override
		public Map<String, Object> getData() {
			return data;
		}
	}

	private class DefaultScriptOutput implements LogQueryScriptOutput {
		@Override
		public void write(Map<String, Object> data) {
			out(new LogMap(data));
		}
	}
}
