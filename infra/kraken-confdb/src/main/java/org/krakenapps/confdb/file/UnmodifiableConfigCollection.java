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
package org.krakenapps.confdb.file;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicate;

public class UnmodifiableConfigCollection implements ConfigCollection {
	private static final String CANNOT_MODIFY_MSG = "you cannot update collection in flashback mode";
	private ConfigCollection col;

	public UnmodifiableConfigCollection(ConfigCollection col) {
		this.col = col;
	}

	@Override
	public String getName() {
		return col.getName();
	}

	@Override
	public int count() {
		return col.count();
	}

	@Override
	public int count(Predicate pred) {
		return col.count(pred);
	}

	@Override
	public int count(ConfigTransaction xact) {
		return col.count(xact);
	}

	@Override
	public ConfigIterator findAll() {
		return col.findAll();
	}

	@Override
	public ConfigIterator find(Predicate pred) {
		return col.find(pred);
	}

	@Override
	public Config findOne(Predicate pred) {
		return col.findOne(pred);
	}

	@Override
	public Config add(Object doc) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config add(Object doc, String committer, String log) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config add(ConfigTransaction xact, Object doc) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config update(Config c) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config update(Config c, boolean ignoreConflict) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config update(Config c, boolean ignoreConflict, String committer, String log) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config update(ConfigTransaction xact, Config c, boolean ignoreConflict) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config remove(Config c) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config remove(Config c, boolean ignoreConflict) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config remove(Config c, boolean ignoreConflict, String committer, String log) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}

	@Override
	public Config remove(ConfigTransaction xact, Config c, boolean ignoreConflict) {
		throw new IllegalStateException(CANNOT_MODIFY_MSG);
	}
}
