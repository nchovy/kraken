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
package org.krakenapps.fusioncharts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chart {
	private Map<String, String> attributes;
	private List<Category> categories;
	private List<DataSet> dataSets;
	private List<Set> sets;
	private List<StyleDefinition> styleDefinitions;
	private List<StyleApplication> styleApplications;
	
	public Chart() {
		attributes = new HashMap<String, String>();
		categories = new ArrayList<Category>();
		dataSets = new ArrayList<DataSet>();
		sets = new ArrayList<Set>();
		styleDefinitions = new ArrayList<StyleDefinition>();
		styleApplications = new ArrayList<StyleApplication>();
	}
	
	public Chart setAttribute(String key, String value) {
		attributes.put(key, value);
		return this;
	}
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}
	
	public java.util.Set<String> getAttributeKeys() {
		return attributes.keySet();
	}
	
	public Chart appendCategory(Category category) {
		categories.add(category);
		return this;
	}
	
	public List<Category> getCategories() {
		return categories;
	}
	
	public Chart appendDataSet(DataSet dataSet) {
		dataSets.add(dataSet);
		return this;
	}
	
	public List<DataSet> getDataSets() {
		return dataSets;
	}
	
	public Chart appendSet(Set set) {
		sets.add(set);
		return this;
	}
	
	public List<Set> getSets() {
		return sets;
	}
	
	public Chart appendStyleDefinition(StyleDefinition definition) {
		styleDefinitions.add(definition);
		return this;
	}
	
	public List<StyleDefinition> getStyleDefinitions() {
		return styleDefinitions;
	}
	
	public Chart appendStyleApplication(StyleApplication application) {
		styleApplications.add(application);
		return this;
	}
	
	public List<StyleApplication> getStyleApplications() {
		return styleApplications;
	}
}
