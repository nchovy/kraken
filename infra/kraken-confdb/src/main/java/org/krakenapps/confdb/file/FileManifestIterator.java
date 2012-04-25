package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.ManifestIterator;

public class FileManifestIterator implements ManifestIterator {
	private RevLogReader manifestReader = null;
	private RevLogReader changeLogReader = null;
	private File dbDir;
	private TreeSet<Integer> manifestIds;
	private Iterator<Integer> it;

	public FileManifestIterator(RevLogReader manifestReader, RevLogReader changeLogReader, File dbDir,
			TreeSet<Integer> manifestIds) {
		this.manifestReader = manifestReader;
		this.changeLogReader = changeLogReader;
		this.dbDir = dbDir;
		this.manifestIds = manifestIds;
		it = this.manifestIds.iterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Manifest next() {
		try {
			int manifestId = it.next();
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
