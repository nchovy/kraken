package org.krakenapps.msgbus;

import java.util.Locale;

public interface ResourceHandler {
	String formatText(String key, Locale locale, String[] params);

	String getText(String key, Locale locale);
}
