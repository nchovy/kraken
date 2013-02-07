/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.Predicates;

public class Console {
	public static void main(String[] args) throws IOException {
		new Console().run(args);
	}

	@CollectionName("users")
	private static class User {
		private String loginName;
		private String name;
		private String password;

		public User(String loginName, String name, String password) {
			this.loginName = loginName;
			this.name = name;
			this.password = password;
		}
	}

	public void run(String[] args) throws IOException {
		File workingDir = new File(System.getProperty("user.dir"));

		FileConfigDatabase db = new FileConfigDatabase(workingDir, "testdb");
		try {

			ConfigTransaction xact = db.beginTransaction();
			for (int i = 0; i < 30000; i++) {
				User user = new User("autogen" + i, "name" + i, "test-password");
				db.add(xact, user);
			}

			xact.commit("test", "test");

			Date begin = new Date();
//			for (int i = 0; i < 1000; i++) {
//				Config found = db.findOne(User.class, Predicates.field("login_name", "autogen5555"));
//			}
			
//			System.out.println(db.count(User.class));
			System.out.println(db.findAll(User.class).getDocuments().size());

			System.out.println(new Date().getTime() - begin.getTime());
		} finally {
			db.purge();
		}

		// FileConfigDatabase db = new FileConfigDatabase(new File("."),
		// args[0]);
		// Integer rev = null;
		// if (args.length > 1)
		// rev = Integer.valueOf(args[1]);
		//
		// Manifest manifest = db.getManifest(rev);
		//
		// System.out.println("Collections");
		// for (String name : manifest.getCollectionNames()) {
		// CollectionEntry e = manifest.getCollectionEntry(name);
		// System.out.println(e);
		// }
	}
}
