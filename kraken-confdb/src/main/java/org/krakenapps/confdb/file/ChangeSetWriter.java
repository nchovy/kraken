package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;

class ChangeSetWriter {
	private ChangeSetWriter() {
	}

	public static void log(File changeLogFile, File changeDatFile, List<ConfigChange> changeSet,
			int manifestId, String committer, String log) throws IOException {
		ChangeLog change = new ChangeLog();
		change.setManifestId(manifestId);
		change.setCommitter(committer);
		change.setMessage(log);
		change.setChangeSet(changeSet);

		RevLog cl = new RevLog();
		cl.setRev(1);
		cl.setOperation(CommitOp.CreateDoc);
		cl.setDoc(change.serialize());

		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(changeLogFile, changeDatFile);
			writer.write(cl);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

}
