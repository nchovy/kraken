/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.rule.http.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.rule.http.HttpRequestContext;
import org.krakenapps.rule.http.HttpRequestRule;
import org.krakenapps.rule.http.HttpRuleEngine;
import org.krakenapps.rule.http.URLParser;

public class HttpRuleScript implements Script {
	private HttpRuleEngine engine;
	private ScriptContext context;

	public HttpRuleScript(HttpRuleEngine engine) {
		this.engine = engine;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void reload(String[] args) {
		engine.reload();
		context.println("reloaded rules");
	}

	@ScriptUsage(description = "inspect http uri and test attack detection", arguments = { @ScriptArgument(name = "url", type = "string", description = "path + querystring") })
	public void inspect(String[] args) {
		HttpRequestContext c = URLParser.parse("GET", args[0]);

		HttpRequestRule match = engine.match(c);
		if (match != null) {
			context.println(match.toString());
		} else {
			context.println("no attack found");
		}
	}

	@ScriptUsage(description = "list all request rules", arguments = { @ScriptArgument(name = "id", type = "string", description = "rule id", optional = true) })
	public void requestRules(String[] args) {
		context.println("HTTP Request Rules");
		context.println("----------------------");
		Collection<HttpRequestRule> requestRules = null;

		if (args.length > 0) {
			requestRules = engine.getRequestRules(args[0]);
		} else {
			requestRules = engine.getRequestRules();
		}

		if (requestRules == null) {
			context.println("No rules found");
			return;
		}

		for (HttpRequestRule r : requestRules) {
			context.println(r.toString());
		}

		context.println("---------------");
		context.println("Total " + requestRules.size() + " rules");
	}

	@ScriptUsage(description = "import rule file", arguments = { @ScriptArgument(name = "file path", type = "string", description = "rule file path") })
	public void importRuleFiles(String[] args) {
		for (String path : args) {
			File source = new File(path);

			if (!source.exists()) {
				context.println(path + " not found.");
				continue;
			}

			File destination = null;
			for (int i = 0;; i++) {
				StringBuilder newPath = new StringBuilder();
				String filename = source.getName();
				int ext = filename.contains(".") ? filename.lastIndexOf(".") : filename.length();

				newPath.append("kraken-http-rule/");
				newPath.append(filename.substring(0, ext));
				if (i > 0)
					newPath.append(" (" + i + ")");
				newPath.append(filename.substring(ext));
				destination = new File(System.getProperty("kraken.data.dir"), newPath.toString());
				if (!destination.exists())
					break;
			}

			FileChannel fcin = null;
			FileChannel fcout = null;
			try {
				destination.createNewFile();

				fcin = new FileInputStream(source).getChannel();
				fcout = new FileOutputStream(destination).getChannel();
				fcin.transferTo(0, fcin.size(), fcout);

				context.println("import successed.");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				context.println("import failed.");
			} finally {
				try {
					if (fcin != null)
						fcin.close();
				} catch (IOException e) {
				}

				try {
					if (fcout != null)
						fcout.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
