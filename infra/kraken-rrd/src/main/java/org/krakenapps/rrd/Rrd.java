/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rrd;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.rrd.io.PersistentLayer;

public interface Rrd {
	List<DataSourceConfig> getDataSources();

	List<ArchiveConfig> getArchives();

	void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max);

	void removeDataSource(String name);

	void update(Date time, Map<String, Double> values);

	void update(Date time, Double[] values);

	int length();

	void save();

	void save(PersistentLayer persLayer);

	FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution);

	void dump(OutputStream os);
}
