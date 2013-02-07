/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.datasource.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.datasource.DataConverter;
import org.krakenapps.datasource.DataConverterRegistry;

@Component(name = "data-converter-registry")
@Provides
public class DataConverterRegistryImpl implements DataConverterRegistry {
	private ConcurrentMap<String, DataConverter> converters;

	public DataConverterRegistryImpl() {
		converters = new ConcurrentHashMap<String, DataConverter>();
	}

	@Override
	public Collection<DataConverter> getDataConverters() {
		return Collections.unmodifiableCollection(converters.values());
	}

	@Override
	public Collection<DataConverter> getDataConverters(String inputType, String outputType) {
		List<DataConverter> matches = new ArrayList<DataConverter>();

		for (DataConverter dc : converters.values()) {
			if (inputType != null && !inputType.equals(dc.getInputType()))
				continue;

			if (outputType != null && !outputType.equals(dc.getOutputType()))
				continue;
			
			matches.add(dc);
		}

		return matches;
	}

	@Override
	public DataConverter getDataConverter(String name) {
		return converters.get(name);
	}

	@Override
	public void addDataConverter(DataConverter converter) {
		if (converters.putIfAbsent(converter.getName(), converter) != null)
			throw new IllegalStateException("duplicated name already exists: " + converter.getName());
	}

	@Override
	public void removeDataConverter(String name) {
		converters.remove(name);
	}

	@Override
	public void removeDataConverter(DataConverter converter) {
		DataConverter old = converters.remove(converter.getName());
		if (old != converter)
			converters.put(old.getName(), old);
	}

}
