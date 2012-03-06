package org.krakenapps.msgbus;

import java.util.Locale;
import java.util.Map;

public interface Localizable extends Marshalable {
	Map<String, Object> marshal(Locale locale);
}
