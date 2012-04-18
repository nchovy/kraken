package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.ManifestIterator;

public class FileManifestIterator implements ManifestIterator {
	private RevLogReader manifestReader = null;
	private RevLogReader changeLogReader = null;
	private File dbDir;
	private TreeSet<Integer> manifestIds;

	public FileManifestIterator(RevLogReader manifestReader, RevLogReader changLogReader, File dbDir, TreeSet<Integer> manifestIds) throws IOException {
		this.manifestReader = manifestReader;
		this.changeLogReader = changLogReader;
		this.dbDir = dbDir;
		this.manifestIds = manifestIds;
	}

	@Override
	public boolean hasNext() {
		if (manifestIds.size() == 0)
			return false;
		else
			return true;
	}

	@Override
	public Manifest next() {
		try {
			int manifestId = manifestIds.pollFirst();			
			RevLog revlog = manifestReader.findDoc(manifestId);
			byte[] doc = manifestReader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
			FileManifest manifest = FileManifest.deserialize(doc);

			if (manifest.getVersion() == 1)
				FileManifest.upgradeManifest(manifest, dbDir);

			manifest.setId(manifestId);
			return manifest;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void remove() {
	}

	@Override
	public void close() {
		if (manifestReader != null)
			manifestReader.close();
		if (changeLogReader != null)
			changeLogReader.close();
	}
}
