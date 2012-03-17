package org.krakenapps.logdb.jython.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.Primitive;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptInput;
import org.krakenapps.logdb.LogScriptOutput;
import org.krakenapps.logdb.jython.JythonLogScriptRegistry;
import org.osgi.framework.BundleContext;

public class JythonLogScript implements Script {

	private JythonLogScriptRegistry scriptRegistry;
	private BundleContext bc;
	private ScriptContext context;

	public JythonLogScript(BundleContext bc, JythonLogScriptRegistry scriptRegistry) {
		this.bc = bc;
		this.scriptRegistry = scriptRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void scripts(String[] args) {
		context.println("Log Scripts");
		context.println("-------------");
		for (String name : scriptRegistry.getScriptNames()) {
			context.println(name + ": " + scriptRegistry.getLogScript(name));
		}
	}

	@ScriptUsage(description = "print script", arguments = { @ScriptArgument(name = "script name", type = "string", description = "script name") })
	public void script(String[] args) {
		String s = scriptRegistry.getScript(args[0]);
		if (s == null) {
			context.println("script not found");
			return;
		}

		context.println(s);
	}

	@ScriptUsage(description = "print script", arguments = {
			@ScriptArgument(name = "script name", type = "string", description = "script name"),
			@ScriptArgument(name = "line", type = "string", description = "test data") })
	public void testrun(String[] args) {
		LogScript s = scriptRegistry.getLogScript(args[0]);
		if (s == null) {
			context.println("script not found");
			return;
		}

		s.handle(new ConsoleInput(args[1]), new ConsoleOutput());
	}

	private class ConsoleInput implements LogScriptInput {
		private Map<String, Object> data;

		public ConsoleInput(String line) {
			data = new HashMap<String, Object>();
			data.put("line", line);
		}

		@Override
		public BundleContext getBundleContext() {
			return bc;
		}

		@Override
		public Map<String, Object> getData() {
			return data;
		}
	}

	private class ConsoleOutput implements LogScriptOutput {
		@Override
		public void write(LogMap data) {
			context.println(Primitive.stringify(data));
		}
	}

	@ScriptUsage(description = "import script file", arguments = {
			@ScriptArgument(name = "script name", type = "string", description = "script name"),
			@ScriptArgument(name = "file path", type = "string", description = "absolute or relative file path") })
	public void load(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");
		File f = canonicalize(dir, args[1]);
		try {
			String s = readAllLines(f);
			scriptRegistry.addScript(args[0], s);
			context.println("loaded " + countLines(s) + " lines");
		} catch (FileNotFoundException e) {
			context.println("file not found: " + f.getAbsolutePath());
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}

	private static int countLines(String s) {
		int last = 0;
		int count = 0;
		while (true) {
			last = s.indexOf('\n', last);
			if (last < 0)
				break;

			last = last + 1;
			count++;
		}
		return count;
	}

	private String readAllLines(File f) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileInputStream is = new FileInputStream(f);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				context.println(line);
				sb.append(line);
				sb.append("\n");
			}

			return sb.toString();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}

			if (is != null)
				is.close();
		}
	}

	private File canonicalize(File dir, String path) {
		if (path.startsWith("/"))
			return new File(path);
		else
			return new File(dir, path);
	}
}
