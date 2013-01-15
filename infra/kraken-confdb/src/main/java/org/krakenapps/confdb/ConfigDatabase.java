/*
 * Copyright 2011 Future Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.confdb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public interface ConfigDatabase {
	/**
	 * @return the database name
	 */
	String getName();

	Manifest getManifest(Integer changeset);

	/**
	 * @return all collection names
	 */
	Set<String> getCollectionNames();

	/**
	 * return collection. return null if collection does not exists
	 */
	ConfigCollection getCollection(Class<?> cls);

	ConfigCollection getCollection(String name);

	/**
	 * return collection. if collection does not exists, it will create
	 * collection and return it.
	 * 
	 * @param name
	 *            the collection name
	 * @return the collection
	 */
	ConfigCollection ensureCollection(Class<?> cls);

	ConfigCollection ensureCollection(String name);

	ConfigCollection ensureCollection(ConfigTransaction xact, Class<?> cls);

	ConfigCollection ensureCollection(ConfigTransaction xact, String name);

	/**
	 * drop collection, but it's just logical deletion (physical data will not
	 * be removed)
	 * 
	 * @param name
	 *            the collection name
	 */
	void dropCollection(Class<?> cls);

	void dropCollection(String name);

	/**
	 * List all commit logs (ascending order)
	 * 
	 * @return
	 */
	List<CommitLog> getCommitLogs();

	List<CommitLog> getCommitLogs(long offset, long limit);

	long getCommitCount();

	/**
	 * begin config transaction. timeout exception may be thrown when default
	 * timeout period is over
	 * 
	 * @return the transaction object
	 */
	ConfigTransaction beginTransaction();

	/**
	 * begin config transaction
	 * 
	 * @param timeout
	 *            the timeout to wait other transaction complete in milliseconds
	 * @return the transaction object
	 */
	ConfigTransaction beginTransaction(int timeout);

	/**
	 * Rollback to specified changeset revision
	 * 
	 * @param changeset
	 *            the changeset revision
	 */
	void rollback(int changeset);

	void rollback(int changeset, String committer, String log);

	int count(Class<?> cls);

	int count(Class<?> cls, Predicate pred);

	int count(ConfigTransaction xact, Class<?> cls);

	ConfigIterator findAll(Class<?> cls);

	ConfigIterator find(Class<?> cls, Predicate pred);

	Config findOne(Class<?> cls, Predicate pred);

	Config add(Object doc);

	Config add(Object doc, String committer, String log);

	Config add(ConfigTransaction xact, Object doc);

	Config update(Config c, Object doc);

	Config update(Config c, Object doc, boolean checkConflict);

	Config update(Config c, Object doc, boolean checkConflict, String committer, String log);

	Config update(ConfigTransaction xact, Config c, Object doc, boolean checkConflict);

	Config remove(Config c);

	Config remove(Config c, boolean checkConflict);

	Config remove(Config c, boolean checkConflict, String committer, String log);

	Config remove(ConfigTransaction xact, Config c, boolean checkConflict);

	void importData(InputStream is);

	void exportData(OutputStream os);

	void shrink(int count);

	ConfigCache getCache();

	void addListener(ConfigDatabaseListener listener);

	void removeListener(ConfigDatabaseListener listener);
}
