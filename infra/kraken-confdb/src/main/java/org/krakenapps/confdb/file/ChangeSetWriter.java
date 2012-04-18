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
import java.util.List;

import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;

class ChangeSetWriter {
	private ChangeSetWriter() {
	}

	public static void log(File changeLogFile, File changeDatFile, List<ConfigChange> changeSet, int manifestId,
			String committer, String log) throws IOException {
		log(changeLogFile, changeDatFile, changeSet, manifestId, committer, log, new Date());
	}

	public static void log(File changeLogFile, File changeDatFile, List<ConfigChange> changeSet, int manifestId,
			String committer, String log, Date created) throws IOException {
		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(changeLogFile, changeDatFile);
			log(writer, changeSet, manifestId, committer, log, created);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static void log(RevLogWriter writer, List<ConfigChange> changeSet, int manifestId, String committer,
			String log, Date created) throws IOException {
		ChangeLog change = new ChangeLog();
		change.setManifestId(manifestId);
		change.setCommitter(committer);
		change.setMessage(log);
		change.setChangeSet(changeSet);
		change.setCreated(created);

		RevLog cl = new RevLog();
		cl.setRev(1);
		cl.setOperation(CommitOp.CreateDoc);
		cl.setDoc(change.serialize());
		writer.write(cl);
	}
}
