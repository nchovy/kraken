package org.krakenapps.msgbus;

import java.util.Map;

public interface Marshalable {
	Map<String, Object> marshal();
}
