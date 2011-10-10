package org.krakenapps.confdb;

public interface ConfigListener {
	void onPreEvent(ConfigCollection col, Config c, ActionType type);

	void onPostEvent(ConfigCollection col, Config c, ActionType type);
}
