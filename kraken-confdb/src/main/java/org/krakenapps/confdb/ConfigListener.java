package org.krakenapps.confdb;

public interface ConfigListener {
	void onPreEvent(ConfigCollection col, Config c, CommitOp op);

	void onPostEvent(ConfigCollection col, Config c, CommitOp op);
}
