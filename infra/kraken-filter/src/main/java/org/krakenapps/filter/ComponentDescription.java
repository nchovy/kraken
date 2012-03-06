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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents iPOJO component description.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class ComponentDescription {
	private String instanceName;
	private String factoryName;
	private String state;
	private long bundleId;
	private String implementationClass;
	private List<String> specifications = new ArrayList<String>();
	private List<String> missingHandlers = new ArrayList<String>();
	private List<String> requiredHanlders = new ArrayList<String>();

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getBundleId() {
		return bundleId;
	}

	public void setBundleId(long bundleId) {
		this.bundleId = bundleId;
	}

	public String getImplementationClass() {
		return implementationClass;
	}

	public void setImplementationClass(String implementationClass) {
		this.implementationClass = implementationClass;
	}

	public List<String> getSpecifications() {
		return specifications;
	}

	public void setSpecifications(List<String> specifications) {
		this.specifications = specifications;
	}

	public List<String> getMissingHandlers() {
		return missingHandlers;
	}

	public void setMissingHandlers(List<String> missingHandlers) {
		this.missingHandlers = missingHandlers;
	}

	public List<String> getRequiredHanlders() {
		return requiredHanlders;
	}

	public void setRequiredHanlders(List<String> requiredHanlders) {
		this.requiredHanlders = requiredHanlders;
	}
}
