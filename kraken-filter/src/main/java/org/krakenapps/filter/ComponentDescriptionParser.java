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
 * Parses iPOJO component description string.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class ComponentDescriptionParser {
	public static ComponentDescription parse(String instanceName, String description) {
		ComponentDescription componentDescription = new ComponentDescription();
		componentDescription.setInstanceName(instanceName);
		componentDescription.setFactoryName(getValue(description, "factory name"));
		componentDescription.setState(getValue(description, "state"));
		componentDescription.setBundleId(Long.parseLong(getValue(description, "bundle")));
		componentDescription.setImplementationClass(getValue(description, "implementation-class"));
		componentDescription.setSpecifications(getValues(description, "provides specification"));

		String missingHandlers = getValue(description, "missinghandlers list");
		String requiredHandlers = getValue(description, "requiredhandlers list");

		componentDescription.setMissingHandlers(parseHandlers(missingHandlers));
		componentDescription.setRequiredHanlders(parseHandlers(requiredHandlers));
		return componentDescription;
	}

	private static List<String> parseHandlers(String handlers) {
		String data = handlers.replace("[", "").replace("]", "");
		String[] tokens = data.split(",");

		List<String> handlerList = new ArrayList<String>();
		for (String token : tokens) {
			if (token.length() > 0)
				handlerList.add(token.trim());
		}
		return handlerList;
	}

	private static String getValue(String description, String key) {
		List<String> values = getValues(description, key);
		if (values.size() == 0)
			return null;

		return values.get(0);
	}

	private static List<String> getValues(String description, String key) {
		List<String> values = new ArrayList<String>();
		int lastIndex = 0;
		while (true) {
			int keyLength = key.length() + 2;
			int beginIndex = description.indexOf(key + "=\"", lastIndex);
			if (beginIndex < 0)
				break;

			int endIndex = description.indexOf("\"", beginIndex + keyLength);
			if (endIndex < 0)
				break;

			String value = description.substring(beginIndex + keyLength, endIndex);
			values.add(value);

			lastIndex = endIndex + 1;
		}

		return values;
	}
}
