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
	private String[] args;
	private DefaultScriptInput input;
	private DefaultScriptOutput output;

	public Script(BundleContext bc, LogScript script, String[] args) {
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

	private class DefaultScriptInput implements LogScriptInput {
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

	private class DefaultScriptOutput implements LogScriptOutput {
		@Override
		public void write(Map<String, Object> data) {
			out(new LogMap(data));
		}
	}
}
