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
package org.krakenapps.filter;

import java.util.List;
import java.util.Set;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.filter.exception.DuplicatedFilterNameException;
import org.krakenapps.filter.exception.FilterNotFoundException;
import org.krakenapps.filter.exception.FilterFactoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides filter management command scripts.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterScript implements Script {
	final Logger logger = LoggerFactory.getLogger(FilterScript.class.getName());

	private ScriptContext context;
	private FilterManager manager;

	public FilterScript(FilterManager manager) {
		this.manager = manager;
	}

	@ScriptUsage(description = "list all filter factory names")
	public void list(String[] args) {
		List<String> filterTypes = manager.getFilterFactoryNames();
		context.println("Kraken Filter List");
		context.println("==================");

		int i = 1;
		for (String filterType : filterTypes) {
			context.printf("[%3d] %s\n", i++, filterType);
		}
	}

	@ScriptUsage(description = "bind filter", arguments = {
			@ScriptArgument(name = "from", type = "string", description = "the filter instance name of message producer"),
			@ScriptArgument(name = "to", type = "string", description = "the filter instance name of message consumer") })
	public void bind(String[] args) {
		try {
			String fromFilterPid = args[0];
			String toFilterPid = args[1];
			manager.bindFilter(fromFilterPid, toFilterPid);
			context.println(fromFilterPid + " -> " + toFilterPid + " binded.");
		} catch (Exception e) {
			context.println("Error: " + e.toString());
			logger.warn("bind error:", e);
		}
	}

	@ScriptUsage(description = "unbind filter", arguments = {
			@ScriptArgument(name = "from", type = "string", description = "the filter instance name of message producer"),
			@ScriptArgument(name = "to", type = "string", description = "the filter instance name of message consumer") })
	public void unbind(String[] args) {
		try {
			String fromFilterPid = args[0];
			String toFilterPid = args[1];
			manager.unbindFilter(fromFilterPid, toFilterPid);
			context.println(fromFilterPid + " -> " + toFilterPid + " unbinded.");
		} catch (Exception e) {
			context.println("Error: " + e.toString());
			logger.warn("unbind error:", e);
		}
	}

	@ScriptUsage(description = "load a filter", arguments = {
			@ScriptArgument(name = "filter factory", type = "string", description = "filter factory name or index"),
			@ScriptArgument(name = "filter instance name", type = "string", description = "filter instance name") })
	public void load(String[] args) {
		String filterClassName = args[0];
		String pid = args[1];

		try {
			if (isNumber(filterClassName)) {
				manager.loadFilter(Integer.parseInt(filterClassName), pid);
			} else {
				manager.loadFilter(filterClassName, pid);
			}
			context.println("[" + pid + "] filter loaded.");
		} catch (DuplicatedFilterNameException e) {
			context.printf("Use other instance name: [%s] already used.\n", pid);
		} catch (FilterFactoryNotFoundException e) {
			context.println("filter type not found.");
		} catch (Exception e) {
			logger.warn("load error:", e);
		}
	}

	private boolean isNumber(String filterClassName) {
		try {
			Integer.parseInt(filterClassName);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@ScriptUsage(description = "unload a filter", arguments = { @ScriptArgument(name = "filter instance name", type = "string", description = "filter instance name") })
	public void unload(String[] args) {
		try {
			String pid = args[0];
			manager.unloadFilter(pid);
		} catch (Exception e) {
			context.println("Error: " + e.toString());
			logger.warn("unload error:", e);
		}
	}

	@ScriptUsage(description = "start active filter thread", arguments = {
			@ScriptArgument(name = "filter instance name", type = "string", description = "active filter instance name"),
			@ScriptArgument(name = "interval", type = "string", description = "thread sleep interval in millisecond unit", optional = true) })
	public void run(String[] args) {
		try {
			if (args.length == 1) {
				String pid = args[0];
				manager.runFilter(pid, 1000);
				context.printf("[%s] filter's thread started. run every 1 second.\n", pid);
			} else if (args.length == 2) {
				String pid = args[0];
				long period = Long.parseLong(args[1]);
				manager.runFilter(pid, period);
				context.printf("[%s] filter's thread started. run every %d millisecond.\n", pid, period);
			}
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("run error:", e);
		}
	}

	@ScriptUsage(description = "stop active filter thread", arguments = { @ScriptArgument(name = "filter instance name", type = "string", description = "active filter instance name") })
	public void stop(String[] args) {
		try {
			manager.stopFilter(args[0]);
		} catch (FilterNotFoundException e) {
			context.println("filter not found.");
		}
	}

	@ScriptUsage(description = "view status of the filter", arguments = { @ScriptArgument(name = "filter instance name", type = "string", description = "the filter instance name for detail view", optional = true) })
	public void status(String[] args) {
		if (args.length == 0) {
			List<ComponentDescription> descriptions = manager.getFilterInstanceDescriptions();
			context.println("Current Filter Instances");
			context.println("========================");
			for (ComponentDescription description : descriptions) {
				context.println(description.getInstanceName() + " -> " + description.getFactoryName());
			}
		} else if (args.length == 1) {
			String pid = args[0];

			try {
				Set<String> keys = manager.getPropertyKeys(pid);
				context.println("Current Filter Properties");
				context.println("=========================");

				for (String key : keys) {
					context.printf("Key: [%s], Value: [%s]\n", key, manager.getProperty(pid, key).toString());
				}
			} catch (FilterNotFoundException e) {
				context.printf("filter [%s] not found.\n", pid);
				logger.warn("filter not found:", e);
				return;
			}

			context.println("");
			context.println("Current Input Filters");
			context.println("=====================");
			for (Filter filter : manager.getInputFilters(pid)) {
				context.println(" * " + (String) filter.getProperty("instance.name"));
			}

			context.println("");
			context.println("Current Output Filters");
			context.println("======================");
			for (Filter filter : manager.getOutputFilters(pid)) {
				context.println(" * " + (String) filter.getProperty("instance.name"));
			}
		}
	}

	@ScriptUsage(description = "set filter's property", arguments = {
			@ScriptArgument(name = "filter instance name", type = "string", description = "the filter instance name"),
			@ScriptArgument(name = "property name", type = "string", description = "the property name"),
			@ScriptArgument(name = "property value", type = "string", description = "the property value") })
	public void set(String[] args) {
		String pid = args[0];
		String key = args[1];
		String value = args[2];
		try {
			manager.setProperty(pid, key, value);
			context.printf("Set filter [%s] key [%s] value[%s]\n", pid, key, value);
		} catch (FilterNotFoundException e) {
			context.printf("filter [%s] not found.\n", pid);
			logger.warn("set - filter not found:", e);
		}
	}

	@ScriptUsage(description = "unset filter's property", arguments = {
			@ScriptArgument(name = "filter instance name", type = "string", description = "the filter instance name"),
			@ScriptArgument(name = "property name", type = "string", description = "the property name") })
	public void unset(String[] args) {
		String pid = args[0];
		String key = args[1];
		try {
			manager.unsetProperty(pid, key);
			context.printf("Unset filter [%s] key [%s]\n", pid, key);
		} catch (FilterNotFoundException e) {
			context.printf("filter [%s] not found.\n", pid);
			logger.warn("unset - filter not found:", e);
		}
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}
}
