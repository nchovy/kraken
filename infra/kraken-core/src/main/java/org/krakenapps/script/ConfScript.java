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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
import org.krakenapps.confdb.Manifest;

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

	@ScriptUsage(description = "print documents", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "collection name", type = "string", description = "collection name") })
	public void createcol(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}
		db.ensureCollection(args[1]);
		context.println("created");
	}

	@ScriptUsage(description = "print documents", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "collection name", type = "string", description = "collection name") })
	public void dropcol(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}
		db.dropCollection(args[1]);
		context.println("dropped");
	}

	@ScriptUsage(description = "print manifest", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "rev id", type = "integer", description = "changelog revision id", optional = true) })
	public void manifest(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		Integer revId = null;
		if (args.length >= 2)
			revId = Integer.parseInt(args[1]);

		Manifest m = db.getManifest(revId);

		if (m == null) {
			context.println("manifest not found");
			return;
		}

		context.println(db.getManifest(revId).toString());
	}

	@ScriptUsage(description = "show revision logs", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "database name"),
			@ScriptArgument(name = "offset", type = "integer", optional = true, description = "log offset"),
			@ScriptArgument(name = "limit", type = "integer", optional = true, description = "log count limit") })
	public void logs(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		long offset = 0;
		long limit = 10;

		if (args.length > 1)
			offset = Long.parseLong(args[1]);
		if (args.length > 2)
			limit = Long.parseLong(args[2]);

		List<CommitLog> logs = db.getCommitLogs(offset, limit);
		for (CommitLog log : logs)
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
		for (String name : db.getCollectionNames()) {
			ConfigCollection col = db.getCollection(name);
			context.println(col);
		}
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
				String s = "id=" + c.getId() + ", rev=" + c.getRevision() + ", doc="
						+ Primitive.stringify(c.getDocument());
				context.println(s);
			}
		} finally {
			it.close();
		}
	}

	@ScriptUsage(description = "print documents", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "collection name", type = "string", description = "collection name"),
			@ScriptArgument(name = "doc id", type = "integer", description = "document id") })
	public void delete(String[] args) {
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

		int id = Integer.parseInt(args[2]);
		ConfigIterator it = col.findAll();
		Config config = null;
		try {
			while (it.hasNext()) {
				Config c = it.next();
				if (c.getId() == id) {
					config = c;
					break;
				}
			}
		} finally {
			it.close();
		}

		if (config != null) {
			col.remove(config);
			context.println("removed");
		} else {
			context.println("document not found");
		}
	}

	@ScriptUsage(description = "print documents", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "rollback revision", type = "integer", description = "rollback revision id") })
	public void rollback(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}

		db.rollback(Integer.parseInt(args[1]));
		context.println("complete");
	}

	@ScriptUsage(description = "shrink log", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "limit", type = "integer", description = "log acount limit") })
	public void shrink(String[] args) {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not fount");
			return;
		}
		if (args[1] == null) {
			context.println("count should be input over 0");
			return;
		}

		db.shrink(Integer.parseInt(args[1]));
	}

	@ScriptUsage(description = "export db data", arguments = {
			@ScriptArgument(name = "database name", type = "string", description = "database name"),
			@ScriptArgument(name = "export revision", type = "integer", description = "export revision id"),
			@ScriptArgument(name = "file name", type = "string", description = "export file name") })
	public void export(String[] args) throws IOException {
		ConfigDatabase db = conf.getDatabase(args[0], Integer.parseInt(args[1]));
		if (db == null) {
			context.println("database not found");
			return;
		}
		if (args[2] == null) {
			context.println("input file name");
			return;
		}
		File exportFile = new File(System.getProperty("kraken.data.dir") + "/kraken-confdb/" + args[0], args[2]);
		OutputStream os = new FileOutputStream(exportFile);
		db.exportData(os);

		os.close();
	}

	// catch IOExeption, close InputStream
	@ScriptUsage(description = "import db data", arguments = {
			@ScriptArgument(name = "database name", type = "String", description = "database name"),
			@ScriptArgument(name = "file name", type = "string", description = "target file name") })
	public void importData(String[] args) throws IOException {
		ConfigDatabase db = conf.getDatabase(args[0]);
		if (db == null) {
			context.println("database not found");
			return;
		}
		if (args[1] == null) {
			context.println("input file name");
			return;
		}

		File targetFile = new File(System.getProperty("kraken.data.dir") + "/kraken-confdb/" + args[0], args[1]);

		if (!targetFile.exists()) {
			context.println("file is not exsist");
			return;
		}

		InputStream is = new FileInputStream(targetFile);
		db.importData(is);
		is.close();
	}
}
