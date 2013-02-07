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
package org.krakenapps.cron.impl;

import java.text.ParseException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.cron.CronService;
import org.krakenapps.cron.Schedule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides cron management command scripts.
 * 
 * @author periphery
 * @since 1.0.0
 */
public class CronScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(CronScript.class.getName());
	private ScriptContext context;
	private BundleContext bundleContext;
	private CronService manager;

	public CronScript(BundleContext bundleContext, CronService manager) {
		this.bundleContext = bundleContext;
		this.manager = manager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "list all cron schedules")
	public void list(String[] args) {
		context.println("Cron Schedules");
		context.println("---------------");

		Map<Integer, Schedule> schedules = manager.getSchedules();
		for (Integer key : schedules.keySet()) {
			Schedule schedule = schedules.get(key);
			context.println(String.format("[%3d] %s", key, schedule));
		}
	}

	@ScriptUsage(description = "view current state of cron scheduling queue")
	public void queue(String[] args) {
		context.println("Cron Job Queue");
		context.println("---------------");

		for (String st : manager.getJobList()) {
			context.println(st);
		}
	}

	@ScriptUsage(description = "register new cron schedule", arguments = {
			@ScriptArgument(name = "min", type = "string", description = "(0 - 59)"),
			@ScriptArgument(name = "hour", type = "string", description = "(0 - 23)"),
			@ScriptArgument(name = "day_of_month", type = "string", description = "(1 - 31)"),
			@ScriptArgument(name = "month", type = "string", description = "(1 - 12)"),
			@ScriptArgument(name = "day_of_week", type = "string", description = "(0 - 6) (Sunday=0)"),
			@ScriptArgument(name = "task", type = "string", description = "Runnable instance name") })
	public void register(String[] args) {
		try {
			Schedule.Builder builder = new Schedule.Builder(args[5]);
			builder.set(CronField.Type.MINUTE, args[0]);
			builder.set(CronField.Type.HOUR, args[1]);
			builder.set(CronField.Type.DAY_OF_MONTH, args[2]);
			builder.set(CronField.Type.MONTH, args[3]);
			builder.set(CronField.Type.DAY_OF_WEEK, args[4]);
			int id = manager.registerSchedule(builder.build());

			context.printf("new schedule [%s] registered.\n", id);
		} catch (ParseException e) {
			context.println("register error: " + e.toString());
			logger.warn("cron script: register error", e);
		}
	}

	@ScriptUsage(description = "unregister cron schedules", arguments = { @ScriptArgument(name = "id", type = "string", description = "cron schedule id") })
	public void unregister(String[] args) {
		for (String arg : args) {
			try {
				manager.unregisterSchedule(Integer.parseInt(arg));
				context.println("schedule " + arg + " unregistered.");
			} catch (NumberFormatException e) {
				context.println("id should be number.");
			} catch (NoSuchElementException e) {
				context.println("cron script: no schedule of given id " + Integer.parseInt(arg));
			}
		}
	}

	@ScriptUsage(description = "print cron schedule syntax")
	public void usage(String[] args) {
		context.println("SYNTAX:");
		context.println("* * * * * Runnable instance.name\n\r" + "- - - - -\n\r" + "| | | | |\n\r"
				+ "| | | | +----- day of week (0 - 6) (Sunday=0)\n\r" + "| | | +------- month (1 - 12)\n\r"
				+ "| | +--------- day of month (1 - 31)\n\r" + "| +----------- hour (0 - 23)\n\r" + "+------------- min (0 - 59)\n\r");
		context.println("EXAMPLES: \n\r" + "Run once a year                       |   0 0 1 1 * \n\r"
				+ "Run once a week                       |   0 0 * * 0 \n\r" + "Run every five minute                 | */5 * * * * \n\r"
				+ "Run once a day from monday to friday  |   0 0 * * 1-5 \n\r"
				+ "Run once a day on saturday and sunday |   0 0 * * 0,6 \n\r");

	}

	@ScriptUsage(description = "run", arguments = { @ScriptArgument(name = "instance name", type = "string", description = "instance.name of service instance") })
	public void run(String[] args) {
		String instanceName = args[0];

		ServiceReference[] refs;
		try {
			refs = bundleContext.getServiceReferences(Runnable.class.getName(), "(instance.name=" + instanceName + ")");
			if (refs == null || refs.length == 0) {
				context.println("instance not found: " + instanceName);
				return;
			}

			if (refs.length > 1)
				context.println("warn: duplicated instance.name found.");

			for (int i = 0; i < refs.length; i++) {
				Runnable runnable = (Runnable) bundleContext.getService(refs[i]);
				try {
					runnable.run();
					context.println("run completed.");
				} catch (Exception e) {
					context.println("error: " + e.toString());
					logger.warn("cron script: run error", e);
				}
			}
		} catch (InvalidSyntaxException e) {
			logger.warn("cron script: run error", e);
			context.println("instance.name syntax error");
		}
	}

	@ScriptUsage(description = "list all active Runnables")
	public void runnables(String[] args) throws InvalidSyntaxException {
		context.println("Active Runnables");
		context.println("------------------");
		ServiceReference[] refs;
		try {
			refs = bundleContext.getServiceReferences(Runnable.class.getName(), null);
			if (refs == null || refs.length == 0) {
				context.println("empty list");
				return;
			}
			for (ServiceReference ref : refs) {
				context.println(ref.getProperty("instance.name").toString());
			}
		} catch (InvalidSyntaxException e) {
			logger.warn("cron script: runnables error", e);
		}
	}
}