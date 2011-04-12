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

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlConverter {
	public static Document convert(Chart chart) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();

			appendChart(doc, chart);

			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void appendChart(Document doc, Chart chart) {
		Element chartElement = doc.createElement("chart");

		for (String key : chart.getAttributeKeys()) {
			chartElement.setAttribute(key, chart.getAttribute(key));
		}

		appendCategories(doc, chartElement, chart);
		appendDataSets(doc, chartElement, chart);
		appendSets(doc, chartElement, chart);
		appendStyles(doc, chartElement, chart);

		doc.appendChild(chartElement);
	}

	private static void appendCategories(Document doc, Element chartElement, Chart chart) {
		if (chart.getCategories().size() == 0)
			return;
			
		Element categoriesElement = doc.createElement("categories");

		for (Category category : chart.getCategories()) {
			appendCategory(doc, categoriesElement, category);
		}

		chartElement.appendChild(categoriesElement);
	}

	private static void appendCategory(Document doc, Element categoriesElement, Category category) {
		Element categoryElement = doc.createElement("category");
		categoryElement.setAttribute("label", category.getName());
		categoriesElement.appendChild(categoryElement);
	}

	private static void appendDataSets(Document doc, Element chartElement, Chart chart) {
		for (DataSet dataSet : chart.getDataSets()) {
			appendDataSet(doc, chartElement, dataSet);
		}
	}

	private static void appendDataSet(Document doc, Element chartElement, DataSet dataSet) {
		Element dataSetElement = doc.createElement("dataset");

		for (String key : dataSet.getAttributeKeys()) {
			dataSetElement.setAttribute(key, dataSet.getAttribute(key));
		}

		for (Set set : dataSet.getSetList()) {
			appendSet(doc, dataSetElement, set);
		}

		chartElement.appendChild(dataSetElement);
	}
	
	private static void appendSets(Document doc, Element chartElement, Chart chart) {
		if (chart.getSets().size() == 0)
			return;
		
		for (Set set : chart.getSets()) {
			appendSet(doc, chartElement, set);
		}
	}

	private static void appendSet(Document doc, Element dataSetElement, Set set) {
		Element setElement = doc.createElement("set");

		for (String key : set.getAttributeKeys()) {
			try {
				setElement.setAttribute(key, set.getAttribute(key));
			} catch (Exception e) {
				System.out.println("KEY = " + key);
				e.printStackTrace();
			}
		}

		dataSetElement.appendChild(setElement);
	}

	private static void appendStyles(Document doc, Element chartElement, Chart chart) {
		Element stylesElement = doc.createElement("styles");
		Element definitionElement = doc.createElement("definition");
		Element applicationElement = doc.createElement("application");

		if (chart.getStyleApplications().size() == 0 && chart.getStyleDefinitions().size() == 0)
			return;

		for (StyleDefinition definition : chart.getStyleDefinitions()) {
			appendStyleDefinitions(doc, definitionElement, definition);
		}

		for (StyleApplication application : chart.getStyleApplications()) {
			appendStyleApplications(doc, applicationElement, application);
		}

		stylesElement.appendChild(definitionElement);
		stylesElement.appendChild(applicationElement);
		chartElement.appendChild(stylesElement);
	}

	private static void appendStyleDefinitions(Document doc, Element definitionElement,
			StyleDefinition definition) {
		Element styleElement = doc.createElement("style");

		for (String key : definition.getAttributeKeys()) {
			styleElement.setAttribute(key, definition.getAttribute(key));
		}

		definitionElement.appendChild(styleElement);
	}

	private static void appendStyleApplications(Document doc, Element applicationElement,
			StyleApplication application) {
		Element applyElement = doc.createElement("apply");
		applyElement.setAttribute("toObject", application.getTarget());
		applyElement.setAttribute("styles", join(application.getStyles(), ","));
		applicationElement.appendChild(applyElement);
	}

	private static String join(List<String> list, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iter = list.iterator();
		if (iter.hasNext()) {
			buffer.append(iter.next());
			while (iter.hasNext()) {
				buffer.append(delimiter);
				buffer.append(iter.next());
			}
		}
		return buffer.toString();
	}
}
