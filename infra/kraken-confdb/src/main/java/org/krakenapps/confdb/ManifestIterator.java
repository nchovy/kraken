package org.krakenapps.confdb;

import java.util.Iterator;

public interface ManifestIterator extends Iterator<Manifest> {
	void close();
}
