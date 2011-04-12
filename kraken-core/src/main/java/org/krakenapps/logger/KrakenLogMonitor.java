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

import java.text.SimpleDateFormat;

import org.apache.log4j.Priority;
import org.krakenapps.api.ScriptContext;
import org.slf4j.impl.KrakenLog;
import org.slf4j.impl.KrakenLoggerFactory;

public class KrakenLogMonitor implements Runnable {
	private ScriptContext context;
	private KrakenLoggerFactory loggerFactory;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public KrakenLogMonitor(ScriptContext context, KrakenLoggerFactory loggerFactory) {
		this.context = context;
		this.loggerFactory = loggerFactory;
	}

	@Override
	public void run() {
		context.println("======= Tailing ========");
		int monitorId = loggerFactory.createMonitor();
		while (true) {
			KrakenLog log;
			try {
				log = loggerFactory.getLog(monitorId);
				context.print("[");
				context.print(dateFormat.format(log.getDate()));
				context.print(" ");
				context.print(toLevelString(log.getLevel()));
				context.print("] ");
				context.println(log.getMessage());
				Throwable t = log.getThrowable();
				if (t != null) {
					context.println(t.toString());
					for (StackTraceElement el : t.getStackTrace()) {
						String line = "\tat " + el.getClassName() + "." + el.getMethodName();
						if (el.getFileName() != null && el.getLineNumber() > 0)
							line += "(" + el.getFileName() + ":" + el.getLineNumber() + ")";
						else if (el.getFileName() != null)
							line += "(" + el.getFileName() + ")";
						
						context.println(line);	
					}
				}
			} catch (IllegalStateException e) {
				break;
			} catch (InterruptedException e) {
				break;
			}
		}

		loggerFactory.destroyMonitor(monitorId);
	}

	private String toLevelString(int level) {
		switch (level) {
		case Priority.DEBUG_INT:
			return "DEBUG";
		case Priority.INFO_INT:
			return "INFO ";
		case Priority.WARN_INT:
			return "WARN ";
		case Priority.ERROR_INT:
			return "ERROR";
		case Priority.FATAL_INT:
			return "FATAL";
		default:
			return "";
		}
	}
}
