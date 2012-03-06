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
package org.krakenapps.tftp.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.tftp.TftpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "tftp-script-factory")
@Provides
public class TftpScriptFactory implements ScriptFactory {
	private final Logger logger = LoggerFactory.getLogger(TftpScriptFactory.class.getName());

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "tftp")
	private String alias;
	public static TftpServer server;

	@Override
	public Script createScript() {
		return new TftpScript();
	}

	@SuppressWarnings("unused")
	@Invalidate
	private void shutdown() {
		try {
			if (server != null)
				server.stop();
		} catch (Exception e) {
			logger.error("kraken tftp: cannot shutdown server gracefully", e);
		}
	}
}
