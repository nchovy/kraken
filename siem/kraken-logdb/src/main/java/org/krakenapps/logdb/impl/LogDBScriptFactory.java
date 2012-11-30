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
package org.krakenapps.logdb.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.logdb.CsvLookupRegistry;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.LookupHandlerRegistry;
import org.krakenapps.logdb.mapreduce.MapReduceService;

@Component(name = "logdb-script-factory")
@Provides
public class LogDBScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "logdb")
	private String alias;

	@Requires
	private LogQueryService qs;

	@Requires
	private DataSourceRegistry dsr;

	@Requires
	private MapReduceService mapreduce;

	@Requires
	private LogScriptRegistry scriptRegistry;
	
	@Requires
	private LookupHandlerRegistry lookup;
	
	@Requires
	private CsvLookupRegistry csvLookup;

	@Override
	public Script createScript() {
		return new LogDBScript(qs, dsr, mapreduce, scriptRegistry, lookup, csvLookup);
	}

}
