package org.krakenapps.confdb;

import org.krakenapps.api.PrimitiveParseCallback;

public abstract class ConfigParser {
	public Object parse(Object obj) {
		return parse(obj, null);
	}

	abstract public Object parse(Object obj, PrimitiveParseCallback callback);
}
