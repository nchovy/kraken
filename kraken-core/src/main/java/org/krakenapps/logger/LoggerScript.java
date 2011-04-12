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
package org.krakenapps.logger;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.slf4j.impl.KrakenLoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

public class LoggerScript implements Script {
	private ScriptContext context;
	private KrakenLoggerFactory loggerFactory;

	public LoggerScript() {
		this.loggerFactory = (KrakenLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory();
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		String pattern = "";
		if (args.length > 0)
			pattern = args[0];

		context.println("Logger List");
		context.println("----------------");

		for (String loggerName : loggerFactory.getLoggerList()) {
			if (loggerName.contains(pattern))
				context.println(loggerName);
		}
	}

	public void set(String[] args) {
		if (args.length != 3) {
			context.println("Usage: logger.set [name] [level] [on/off]");
			return;
		}

		String name = args[0];
		if (loggerFactory.hasLogger(name) == false) {
			context.println("logger not found");
			return;
		}

		String level = args[1];
		boolean isEnabled = args[2].equalsIgnoreCase("on");

		loggerFactory.setLogLevel(name, level, isEnabled);
	}

	/**
	 * Turn on trace and debug logging
	 * 
	 * @param args
	 */
	public void on(String[] args) {
		String name = args[0];
		if (loggerFactory.hasLogger(name) == false) {
			context.println("logger not found");
			return;
		}

		loggerFactory.setLogLevel(name, "trace", true);
		loggerFactory.setLogLevel(name, "debug", true);
		
		context.println("set");
	}

	/**
	 * Turn off trace and debug logging
	 * 
	 * @param args
	 */
	public void off(String[] args) {
		String name = args[0];
		if (loggerFactory.hasLogger(name) == false) {
			context.println("logger not found");
			return;
		}

		loggerFactory.setLogLevel(name, "trace", false);
		loggerFactory.setLogLevel(name, "debug", false);
		
		context.println("set");
	}

	public void tail(String[] args) throws InterruptedException {
		KrakenLogMonitor monitor = new KrakenLogMonitor(context, loggerFactory);
		Thread t = new Thread(monitor);
		t.start();

		while (true) {
			try {
				context.readLine();
			} catch (InterruptedException e) {
				break;
			}
		}

		t.interrupt();

		context.println("waiting monitor stop..");
		t.join();
		context.println("interrupted.");
	}
}
