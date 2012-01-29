/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.console;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.main.Kraken;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRunner implements Runnable {
	private Logger logger = LoggerFactory.getLogger(ScriptRunner.class.getName());
	private ScriptContext context;
	private String methodName;
	private String[] args;
	private boolean isPromptEnabled = true;

	public ScriptRunner(ScriptContext context, String line) {
		String[] tokens = ScriptArgumentParser.tokenize(line.trim());
		String[] commandTokens = tokens[0].split("\\.");
		String alias = null;
		if (commandTokens.length != 2) {
			alias = "core";
			this.methodName = commandTokens[0];
		} else {
			alias = commandTokens[0];
			this.methodName = commandTokens[1];
		}

		this.context = context;
		this.args = getArguments(tokens);

		ServiceReference[] refs = null;
		try {
			refs = Kraken.getContext().getServiceReferences(ScriptFactory.class.getName(), "(alias=" + alias + ")");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		if (refs == null || refs.length == 0)
			throw new NullPointerException("script not found.");

		ScriptFactory scriptFactory = (ScriptFactory) Kraken.getContext().getService(refs[0]);
		if (scriptFactory == null) {
			throw new NullPointerException("script not found.");
		}

		Script script = scriptFactory.createScript();
		context.setCurrentScript(script);
		Kraken.getContext().ungetService(refs[0]);
	}

	public void setPrompt(boolean enabled) {
		this.isPromptEnabled = enabled;
	}

	private String[] getArguments(String[] tokens) {
		String[] arguments = new String[tokens.length - 1];
		for (int i = 1; i < tokens.length; ++i) {
			arguments[i - 1] = tokens[i];
		}
		return arguments;
	}

	@Override
	public void run() {
		context.turnEchoOn();
		context.getInputStream().flush();

		Script script = context.getCurrentScript();
		script.setScriptContext(context);
		invokeScript(script);

		if (isPromptEnabled)
			context.printPrompt();

		context.setCurrentScript(null);
	}

	private void invokeScript(Script script) {
		Method method;
		ScriptUsage usage = null;
		try {
			method = script.getClass().getDeclaredMethod(methodName, new Class[] { String[].class });

			usage = method.getAnnotation(ScriptUsage.class);
			verifyScriptArguments(usage, args);

			method.invoke(script, (Object) args);
		} catch (IllegalArgumentException e) {
			if (usage == null) {
				context.println("IllegalArgumentException, but no usage found. Please ask script author to add usage information.");
				return;
			}

			if (usage.description() != null) {
				context.println("Description");
				context.println("");
				context.println("\t" + usage.description());
				context.println("");
			}

			if (usage.arguments() == null || usage.arguments().length == 0)
				return;

			context.println("Arguments\n");

			int i = 1;
			for (ScriptArgument argument : usage.arguments()) {
				String optional = argument.optional() ? " (optional)" : " (required)";
				context.println("\t" + i++ + ". " + argument.name() + ": " + argument.description() + optional);
			}
		} catch (SecurityException e) {
			context.println(e.toString());
			logger.warn("script runner: ", e);
		} catch (NoSuchMethodException e) {
			context.println("syntax error.");
			logger.warn("script runner: {}.{} not found", script.getClass().getName(), methodName);
		} catch (IllegalAccessException e) {
			context.println("syntax error.");
			logger.warn("script runner: {}.{} forbidden", script.getClass().getName(), methodName);
		} catch (InvocationTargetException e) {
			context.println(e.getTargetException().toString());
			logger.warn("script runner: ", e);
		}
	}

	private void verifyScriptArguments(ScriptUsage usage, String[] args) {
		if (usage == null || usage.arguments() == null || usage.arguments().length == 0)
			return;

		if (countRequiredArguments(usage.arguments()) > args.length)
			throw new IllegalArgumentException("arguments length does not match.");

		// TODO: type match
	}

	private int countRequiredArguments(ScriptArgument[] args) {
		int count = 0;
		for (ScriptArgument arg : args) {
			if (arg.optional() == false)
				count++;
		}
		return count;
	}
}
