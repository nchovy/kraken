package org.krakenapps.confdb;

import java.util.Iterator;

public interface ConfigIterator extends Iterator<Config> {
	void close();
}
