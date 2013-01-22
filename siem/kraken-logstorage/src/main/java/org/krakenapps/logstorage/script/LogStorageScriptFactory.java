/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.logstorage.IndexTokenizerRegistry;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogStorageMonitor;
import org.krakenapps.logstorage.LogTableRegistry;

@Component(name = "logstorage-script-factory")
@Provides
public class LogStorageScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "logstorage")
	private String alias;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private LogStorage storage;

	@Requires
	private ConfigService conf;

	@Requires
	private LogIndexer indexer;

	@Requires
	private LogStorageMonitor monitor;

	@Requires
	private IndexTokenizerRegistry tokenizerRegistry;

	@Override
	public Script createScript() {
		return new LogStorageScript(tableRegistry, storage, indexer, monitor, tokenizerRegistry, conf);
	}
}
