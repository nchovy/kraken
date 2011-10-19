package org.krakenapps.confdb;

public interface ConfigCollection {
	String getName();

	ConfigIterator findAll();

	ConfigIterator find(Predicate pred);

	Config findOne(Predicate pred);

	Config add(Object doc);

	Config add(Object doc, String committer, String log);

	Config update(Config c);

	Config update(Config c, boolean ignoreConflict);

	Config update(Config c, boolean ignoreConflict, String committer, String log);

	Config remove(Config c);

	Config remove(Config c, boolean ignoreConflict);

	Config remove(Config c, boolean ignoreConflict, String committer, String log);

	void addHook(CommitOp op, ConfigListener listener);

	void removeHook(CommitOp op, ConfigListener listener);
}
