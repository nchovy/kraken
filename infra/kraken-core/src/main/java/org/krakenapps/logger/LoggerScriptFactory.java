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
package org.krakenapps.logger;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.slf4j.impl.KrakenLoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

public class LoggerScriptFactory implements ScriptFactory {
	public LoggerScriptFactory() {
		// temporary hard coded some logger default settings
		KrakenLoggerFactory loggerFactory = (KrakenLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory();

		loggerFactory.setLogLevel("org.apache.sshd.server.session.ServerSession", "info", false);
	}
	
	@Override
	public Script createScript() {
		return new LoggerScript();
	}

}
