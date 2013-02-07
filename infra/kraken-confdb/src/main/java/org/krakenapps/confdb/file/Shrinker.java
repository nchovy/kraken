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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.ManifestIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shrinker {
	private final Logger logger = LoggerFactory.getLogger(Shrinker.class.getName());
	private FileConfigDatabase db;
	private File dbDir;

	public Shrinker(FileConfigDatabase db) {
		this.db = db;
		this.dbDir = db.getDbDirectory();
	}

	public void shrink(int count) throws IOException {
		if (count < 1) {
			throw new IllegalArgumentException("count should be positive");
		}
		db.lock();

		try {
			logger.debug("kraken confdb: start shrink for {} logs", count);
			List<CommitLog> logs = getSortedCommitLogs(count);

			TreeSet<Integer> manifestIds = new TreeSet<Integer>();
			for (CommitLog c : logs) {
				ChangeLog changeLog = (ChangeLog) c;
				manifestIds.add(changeLog.getManifestId());
			}

			TreeSet<ConfigEntry> configEntries = new TreeSet<ConfigEntry>();
			List<Manifest> manifests = new ArrayList<Manifest>();
			loadManifests(manifestIds, configEntries, manifests);

			// mapping old ConfigEntry to new ConfigEntry
			Map<ConfigEntry, ConfigEntry> docIdMap = writeConfigEntries(configEntries);

			// create new Manifest. Insert ConfigEntries and CollectionEntries
			List<Manifest> newManifests = createNewManifests(manifests, docIdMap);

			writeNewChangeLog(logs, writeNewManifestLog(newManifests));

			renameFiles(configEntries);
			removeOldFiles();
		} catch (IOException e) {
			revertFileNames();
			logger.error("kraken confdb: shrink fail", e);
			throw e;
		} finally {
			db.unlock();
		}
		logger.debug("kraken confdb: shrink complete");
	}

	private void removeOldFiles() {
		for (File f : new File(dbDir.getAbsolutePath()).listFiles()) {
			if (f.getName().startsWith("old_") && (f.getName().endsWith(".log") || f.getName().endsWith(".dat")))
				f.delete();
		}
	}

	private void loadManifests(TreeSet<Integer> manifestIds, TreeSet<ConfigEntry> configEntries, List<Manifest> manifests)
			throws IOException {
		ManifestIterator it = null;
		try {
			it = db.getManifestIterator(manifestIds);
			while (it.hasNext()) {
				Manifest manifest = it.next();

				for (String name : manifest.getCollectionNames()) {
					for (ConfigEntry c : manifest.getConfigEntries(name)) {
						configEntries.add(c);
					}
				}
				manifests.add(manifest);
			}
		} finally {
			if (it != null)
				it.close();
		}
	}

	private List<Manifest> createNewManifests(List<Manifest> manifests, Map<ConfigEntry, ConfigEntry> docIdMap)
			throws IOException {
		List<Manifest> newManifests = new ArrayList<Manifest>();
		for (Manifest old : manifests) {
			FileManifest newManifest = new FileManifest();
			newManifest.setId(old.getId());
			newManifest.setVersion(old.getVersion());

			for (String name : old.getCollectionNames()) {
				newManifest.add(old.getCollectionEntry(name));
			}

			for (String name : old.getCollectionNames()) {
				for (ConfigEntry e : old.getConfigEntries(name)) {
					newManifest.add(docIdMap.get(e));
				}
			}
			newManifests.add(newManifest);
		}
		return newManifests;
	}

	private List<CommitLog> getSortedCommitLogs(int count) {
		List<CommitLog> logs;
		logs = db.getCommitLogs(0, count);

		Comparator<CommitLog> comparator = new Comparator<CommitLog>() {
			@Override
			public int compare(CommitLog o1, CommitLog o2) {
				Long rev1 = o1.getRev();
				return rev1.compareTo(o2.getRev());
			}
		};
		Collections.sort(logs, comparator);
		return logs;
	}

	private void renameFiles(TreeSet<ConfigEntry> configEntries) throws IOException {
		TreeSet<Integer> collectionIds = new TreeSet<Integer>();
		for (ConfigEntry c : configEntries) {
			collectionIds.add(c.getColId());
		}

		for (Integer i : collectionIds)
			renameTo("col" + i);

		renameTo("manifest");
		renameTo("changeset");

	}

	private void renameTo(String fileName) throws IOException {
		String datName = fileName + ".dat";
		String logName = fileName + ".log";
		File oldDat = new File(dbDir, datName);
		File oldLog = new File(dbDir, logName);
		File newDat = new File(dbDir, "new_" + datName);
		File newLog = new File(dbDir, "new_" + logName);

		if (new File(dbDir, "old_" + datName).exists())
			new File(dbDir, "old_" + datName).delete();
		if (new File(dbDir, "old_" + logName).exists())
			new File(dbDir, "old_" + logName).delete();

		if (!(oldDat.renameTo(new File(dbDir, "old_" + datName)) && oldLog.renameTo(new File(dbDir, "old_" + logName))
				&& newDat.renameTo(new File(dbDir, datName)) && newLog.renameTo(new File(dbDir, logName)))) {
			throw new IOException("file rename fail");
		}
	}

	private void revertFileNames() {
		for (File f : new File(dbDir.getAbsolutePath()).listFiles()) {
			if (f.getName().startsWith("new_") && (f.getName().endsWith(".log") || f.getName().endsWith(".dat")))
				f.delete();
		}
		for (File f : new File(dbDir.getAbsolutePath()).listFiles()) {
			if (f.getName().startsWith("old_") && (f.getName().endsWith(".log") || f.getName().endsWith(".dat"))) {
				File oldFile = new File(f.getAbsoluteFile(), f.getName().replaceFirst("old_", ""));
				if (oldFile.exists())
					oldFile.delete();
				f.renameTo(oldFile);
			}
		}
	}

	private void writeNewChangeLog(List<CommitLog> logs, Map<Integer, Integer> manifestIds) throws IOException {
		File changeLogFile = new File(dbDir, "new_changeset.log");
		File changeDatFile = new File(dbDir, "new_changeset.dat");
		RevLogWriter changeLogWriter = null;

		try {
			changeLogWriter = new RevLogWriter(changeLogFile, changeDatFile);
			for (CommitLog c : logs) {
				ChangeLog changeLog = (ChangeLog) c;
				ChangeSetWriter.log(changeLogWriter, changeLog.getChangeSet(), manifestIds.get(changeLog.getManifestId()),
						changeLog.getCommitter(), changeLog.getMessage(), changeLog.getCreated());
			}
		} finally {
			if (changeLogWriter != null)
				changeLogWriter.close();
		}
	}

	private Map<Integer, Integer> writeNewManifestLog(List<Manifest> newManifests) throws IOException {
		File manifestLogFile = new File(dbDir, "new_manifest.log");
		File manifestDatFile = new File(dbDir, "new_manifest.dat");
		RevLogWriter manifestWriter = null;

		Map<Integer, Integer> manifestIds = new HashMap<Integer, Integer>();
		try {
			manifestWriter = new RevLogWriter(manifestLogFile, manifestDatFile);
			for (Manifest manifest : newManifests)
				manifestIds.put(manifest.getId(), FileManifest.writeManifest(manifest, manifestWriter).getId());

		} finally {
			if (manifestWriter != null)
				manifestWriter.close();
		}

		return manifestIds;
	}

	private Map<ConfigEntry, ConfigEntry> writeConfigEntries(TreeSet<ConfigEntry> configEntries) throws IOException {
		int lastDocId = -1;
		int newDocId = 0;
		int collectionId = 0;
		RevLogWriter writer = null;
		RevLogReader reader = null;
		Map<ConfigEntry, ConfigEntry> changeIndex = new HashMap<ConfigEntry, ConfigEntry>();

		try {
			for (ConfigEntry c : configEntries) {
				if (collectionId != c.getColId()) {
					if (reader != null)
						reader.close();
					if (writer != null)
						writer.close();
					writer = new RevLogWriter(new File(dbDir, "new_col" + c.getColId() + ".log"), new File(dbDir, "new_col"
							+ c.getColId() + ".dat"));
					reader = new RevLogReader(new File(dbDir, "col" + c.getColId() + ".log"), new File(dbDir, "col"
							+ c.getColId() + ".dat"));
					collectionId = c.getColId();
				}

				int newIndex;
				RevLog log = reader.read(c.getIndex());
				log.setDoc(reader.readDoc(log.getDocOffset(), log.getDocLength()));
				if (log.getOperation() != CommitOp.CreateDoc && log.getDocId() != lastDocId) {
					RevLog fakeLog = log;
					fakeLog.setOperation(CommitOp.CreateDoc);
					newDocId = writer.write(fakeLog);
					newIndex = writer.count() - 1;
				}

				log.setDocId(newDocId);

				newDocId = writer.write(log);
				newIndex = writer.count() - 1;
				lastDocId = c.getDocId();
				changeIndex.put(c, new ConfigEntry(collectionId, newDocId, c.getRev(), newIndex));
			}
		} finally {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
		}

		return changeIndex;
	}
}
