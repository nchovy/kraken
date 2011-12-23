package org.krakenapps.msgbus;

import java.util.Locale;
import java.util.Map;

public interface ResourceHandler {
	@Deprecated
	String formatText(String key, Locale locale, String[] params);

	String formatText(String key, Locale locale, Map<String, Object> properties);

	String getText(String key, Locale locale);
}
