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
package org.krakenapps.script;

import org.krakenapps.api.Primitive;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;

public class ConfScript implements Script {
	private ConfigService conf;
	private ScriptContext context;

	public ConfScript(ConfigService conf) {
		this.conf = conf;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void databases(String[] args) {
		context.println("Databases");
		context.println("-----------");
		for (String name : conf.getDatabaseNames())
			context.println(name);
	}

	@ScriptUsage(description = "create conf db", arguments = { @ScriptArgument(name = "name", type = "string", description = "database name") })
	public void createdb(String[] args) {
		conf.createDatabase(args[0]);
		context.println("created");
	}

	@ScriptUsage(description = "drop conf db", arguments = { @ScriptArgument(name = "name", type = "string", description = "database name") })
	public void dropdb(String[] args) {
		conf.dropDatabase(args[0]);
		context.println("dropped");
	}

	@ScriptUsage(description = "show revision logs", arguments = { @ScriptArgument(name = "name", type = "string", description = "database name") })
	public void logs(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		for (CommitLog log : db.getCommitLogs())
			context.println(log);
	}

	@ScriptUsage(description = "print collection names", arguments = { @ScriptArgument(name = "name", type = "string", description = "database name") })
	public void cols(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		context.println("Collections");
		context.println("-------------");
		for (String name : db.getCollectionNames())
			context.println(name);
	}

	@ScriptUsage(description = "print documents", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "collection name", type = "string", description = "collection name") })
	public void docs(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		ConfigCollection col = db.getCollection(args[1]);
		if (col == null) {
			context.println("collection not found");
			return;
		}

		context.println("Documents");
		context.println("-----------");
		ConfigIterator it = col.findAll();
		try {
			while (it.hasNext()) {
				Config c = it.next();
				String s = "id=" + c.getId() + ", rev=" + c.getRevision() + ", doc=" + Primitive.stringify(c.getDocument());
				context.println(s);
			}
		} finally {
			it.close();
		}
	}
}
