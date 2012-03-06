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
package org.krakenapps.ipojo.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.krakenapps.api.ScriptOutputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Inspects and prints iPOJO architecture
 * 
 * @author xeraph
 * 
 */
public class ArchInspector {
	private BundleContext bundleContext;
	private ScriptOutputStream out;

	public ArchInspector(BundleContext bundleContext, ScriptOutputStream out) {
		this.bundleContext = bundleContext;
		this.out = out;
	}
	
	public void printInstances(String queryTerm) {
		printInstances(queryTerm, false);
	}

	/**
	 * Prints iPOJO instance list
	 */
	public void printInstances(String queryTerm, boolean invalidOnly) {
		ServiceReference[] refs;
		try {
			refs = bundleContext.getServiceReferences(Architecture.class.getName(), null);
			out.println("iPOJO Instances");
			out.println("-------------------");
			for (int i = 0; i < refs.length; i++) {
				Architecture arch = (Architecture) bundleContext.getService(refs[i]);
				InstanceDescription instance = arch.getInstanceDescription();

				if (queryTerm != null && !instance.getName().contains(queryTerm))
					continue;
				
				if (invalidOnly && instance.getState() != ComponentInstance.INVALID)
					continue;

				if (instance.getState() == ComponentInstance.VALID)
					out.printf("Instance [%s] -> valid\n", instance.getName());
				if (instance.getState() == ComponentInstance.INVALID)
					out.printf("Instance [%s] -> invalid\n", instance.getName());
				if (instance.getState() == ComponentInstance.STOPPED)
					out.printf("Instance [%s] -> stopped\n", instance.getName());
			}
		} catch (InvalidSyntaxException e) {

		}
	}

	/**
	 * Prints iPOJO factories
	 */
	public void printFactories(String queryTerm) {
		ServiceReference[] refs;
		try {
			out.println("iPOJO Factories");
			out.println("-------------------");
			refs = bundleContext.getServiceReferences(Factory.class.getName(), null);

			for (int i = 0; i < refs.length; i++) {
				Factory factory = (Factory) bundleContext.getService(refs[i]);
				if (queryTerm != null && !factory.getName().contains(queryTerm))
					continue;

				if (factory.getMissingHandlers().size() == 0) {
					out.println("Factory " + factory.getName() + " (VALID)");
				} else {
					out.println("Factory " + factory.getName() + " (INVALID : " + factory.getMissingHandlers() + ")");
				}
			}
		} catch (InvalidSyntaxException e) {

		}
	}

	/**
	 * Prints iPOJO factory description
	 * 
	 * @param name
	 *            factory name
	 */
	public void printFactory(String name) {
		boolean found = false;
		ServiceReference[] refs;
		try {
			out.println("iPOJO Factory");
			out.println("-------------------");
			refs = bundleContext.getServiceReferences(Factory.class.getName(), null);

			for (int i = 0; i < refs.length; i++) {
				Factory factory = (Factory) bundleContext.getService(refs[i]);
				if (factory.getName().equalsIgnoreCase(name)) {
					if (found) {
						out.println("");
					}
					out.println(factory.getDescription().toString().replace("\n", "\r\n"));
					found = true;
				}
			}

			if (!found) {
				out.println("Factory " + name + " not found");
			}
		} catch (InvalidSyntaxException e) {

		}

	}

	/**
	 * Prints iPOJO instance description
	 * 
	 * @param name
	 *            instance name
	 */
	public void printInstance(String name) {
		ServiceReference[] refs;
		try {
			refs = bundleContext.getServiceReferences(Architecture.class.getName(), null);
			out.println("iPOJO Instance");
			out.println("-------------------");
			for (int i = 0; i < refs.length; i++) {
				Architecture arch = (Architecture) bundleContext.getService(refs[i]);
				InstanceDescription instance = arch.getInstanceDescription();
				if (instance.getName().equalsIgnoreCase(name)) {
					out.println(instance.getDescription().toString().replace("\n", "\r\n"));
					return;
				}
			}

			out.println("Instance " + name + " not found");
		} catch (InvalidSyntaxException e) {

		}
	}

	/**
	 * Prints the list of available handlers
	 */
	public void printHandlers() {
		ServiceReference[] refs;
		try {
			out.println("iPOJO Handlers");
			out.println("-------------------");
			refs = bundleContext.getServiceReferences(HandlerFactory.class.getName(), null);

			for (int i = 0; i < refs.length; i++) {
				HandlerFactory handler = (HandlerFactory) bundleContext.getService(refs[i]);
				String name = handler.getHandlerName();
				if ("composite".equals(handler.getType()))
					name = name + " [composite]";

				if (handler.getMissingHandlers().size() == 0) {
					out.println("Handler " + name + " (VALID)");
				} else {
					out.println("Handler " + name + " (INVALID : " + handler.getMissingHandlers() + ")");
				}
			}
		} catch (InvalidSyntaxException e) {

		}
	}

	/**
	 * Prints the statistics
	 */
	public void printStats() {
		try {
			Field field = IPojoFactory.class.getDeclaredField("INSTANCE_NAME");
			field.setAccessible(true);
			List<?> names = (List<?>) field.get(null);
			out.println("Number of living instances: " + names.size());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
