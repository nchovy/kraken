package org.krakenapps.logstorage;

public interface LookupHandler {
	Object lookup(String srcField, String dstField, Object value);
}
