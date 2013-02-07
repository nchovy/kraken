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
package org.krakenapps.logdb.cmd;

import java.util.Arrays;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.SyntaxProvider;

@Component(name = "logdb-cmd")
public class CmdExtender {
	@Requires
	private SyntaxProvider syntaxProvider;

	private CmdParser parser;

	@Validate
	public void start() {
		parser = new CmdParser();
		syntaxProvider.addParsers(Arrays.asList(parser));
	}

	@Invalidate
	public void stop() {
		if (syntaxProvider != null)
			syntaxProvider.removeParsers(Arrays.asList(parser));
	}
}
