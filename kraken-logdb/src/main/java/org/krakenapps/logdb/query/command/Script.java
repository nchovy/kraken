package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptInput;
import org.krakenapps.logdb.LogScriptOutput;
import org.osgi.framework.BundleContext;

public class Script extends LogQueryCommand {

	private BundleContext bc;
	private LogScript script;
	private DefaultScriptInput input;
	private DefaultScriptOutput output;

	public Script(BundleContext bc, LogScript script) {
		this.script = script;
		this.input = new DefaultScriptInput();
		this.output = new DefaultScriptOutput();
	}

	@Override
	public void push(LogMap m) {
		input.data = m;
		script.handle(input, output);
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	private void out(LogMap data) {
		write(data);
	}

	private class DefaultScriptInput implements LogScriptInput {
		private LogMap data;

		@Override
		public BundleContext getBundleContext() {
			return bc;
		}

		@Override
		public Map<String, Object> getData() {
			return data.map();
		}
	}

	private class DefaultScriptOutput implements LogScriptOutput {
		@Override
		public void write(LogMap data) {
			out(data);
		}
	}
}
