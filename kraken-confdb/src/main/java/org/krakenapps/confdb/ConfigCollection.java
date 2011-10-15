package org.krakenapps.confdb;

public interface ConfigCollection {
	String getName();

	ConfigIterator findAll();

	ConfigIterator find(Predicate pred);

	Config findOne(Predicate pred);

	Config add(Object doc);

	Config update(Config c);

	Config update(Config c, boolean ignoreConflict);

	Config remove(Config c);

	Config remove(Config c, boolean ignoreConflict);

	void addHook(CommitOp op, ConfigListener listener);

	void removeHook(CommitOp op, ConfigListener listener);
}
