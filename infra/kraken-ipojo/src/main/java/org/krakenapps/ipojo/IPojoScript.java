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
package org.krakenapps.ipojo;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.architecture.Architecture;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ipojo.impl.ArchInspector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Kraken script for ipojo
 * 
 * @author xeraph
 * 
 */
public class IPojoScript implements Script {

	private ScriptContext context;
	private BundleContext bc;
	private ArchInspector inspector;
	private ComponentFactoryMonitor monitor;

	public IPojoScript(BundleContext bc, ComponentFactoryMonitor monitor) {
		this.bc = bc;
		this.monitor = monitor;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
		this.inspector = new ArchInspector(bc, context.getOutputStream());
	}

	@ScriptUsage(description = "creates an iPOJO component instance", arguments = {
			@ScriptArgument(name = "factory name", type = "string", description = "the name of iPOJO factory"),
			@ScriptArgument(name = "instance name", type = "string", description = "the name of iPOJO component instance") })
	public void create(String[] args) {
		String factoryName = args[0];
		String instanceName = args[1];

		try {
			ServiceReference[] refs = bc.getServiceReferences(Factory.class.getName(), "(factory.name="
					+ factoryName + ")");
			if (refs == null || refs.length == 0) {
				context.println("factory " + factoryName + " not found");
				return;
			}

			Factory factory = (Factory) bc.getService(refs[0]);
			Dictionary<String, Object> configMap = new Hashtable<String, Object>();
			configMap.put("instance.name", instanceName);

			for (int i = 2; i < args.length; i++) {
				int keyPos = args[i].indexOf("=");
				if (keyPos < 0) {
					context.println("check config syntax (should be key=value format)");
					return;
				}

				String key = args[i].substring(0, keyPos);
				String value = args[i].substring(keyPos + 1);
				configMap.put(key, value);
			}

			factory.createComponentInstance(configMap);
			context.printf("%s instance created\n", instanceName);
		} catch (InvalidSyntaxException e) {
			// ignore
		} catch (UnacceptableConfiguration e) {
			context.println("unacceptable configuration: " + e.getMessage());
			e.printStackTrace();
		} catch (MissingHandlerException e) {
			context.println("missing handler: " + e.getMessage());
		} catch (ConfigurationException e) {
			context.println("configuration error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@ScriptUsage(description = "deletes an iPOJO component instance", arguments = { @ScriptArgument(name = "name", type = "string", description = "the name of the instance to delete") })
	public void delete(String[] args) {
		System.out.println("delete call");
		try {
			String name = args[0];
			ServiceReference[] refs = bc.getServiceReferences(Architecture.class.getName(),
					"(architecture.instance=" + name + ")");
			for (int i = 0; i < refs.length; i++) {
				Architecture arch = (Architecture) bc.getService(refs[i]);
				context.println("deleting " + arch.getInstanceDescription().getComponentDescription().getName());
				IPojoFactory iPojoFactory = arch.getInstanceDescription().getComponentDescription().getFactory();
				iPojoFactory.deleted(name);
			}

			context.println("component " + args[0] + " deleted");
		} catch (Exception e) {
			context.println("delete failed");
		}
	}

	@ScriptUsage(description = "Prints iPOJO instance list")
	public void instances(String[] args) {
		boolean invalidOnly = false;
		String queryTerm = null;

		for (String arg : args) {
			if (arg.equals("-invalid"))
				invalidOnly = true;
			else
				queryTerm = arg;
		}

		inspector.printInstances(queryTerm, invalidOnly);
	}

	@ScriptUsage(description = "Prints iPOJO instance description", arguments = { @ScriptArgument(name = "name", type = "string", description = "instance name") })
	public void instance(String[] args) {
		inspector.printInstance(args[0]);
	}

	@ScriptUsage(description = "Prints iPOJO factories")
	public void factories(String[] args) {
		String queryTerm = null;
		if (args.length > 0)
			queryTerm = args[0];

		inspector.printFactories(queryTerm);
	}

	@ScriptUsage(description = "Prints iPOJO factory description", arguments = { @ScriptArgument(name = "name", type = "string", description = "factory name") })
	public void factory(String[] args) {
		inspector.printFactory(args[0]);
	}

	@ScriptUsage(description = "Prints the list of available handlers")
	public void handlers(String[] args) {
		inspector.printHandlers();
	}

	public void stats(String[] args) {
		inspector.printStats();
	}

	public void trackers(String[] args) {
		context.println("Component Trackers");
		context.println("-------------------");
		for (ComponentFactoryTracker tracker : monitor.getTrackers()) {
			context.println(tracker.toString());

			for (String factoryName : tracker.getRequiredFactoryNames())
				context.println(" * " + factoryName);
		}
	}
}