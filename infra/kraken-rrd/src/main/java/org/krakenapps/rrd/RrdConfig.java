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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RrdConfig {
	private int version;
	private long stepSeconds;
	private long startTime;

	private List<DataSourceConfig> dataSources;
	private List<ArchiveConfig> archives;

	public RrdConfig(Date startTime, long stepSeconds) {
		this.version = 3;
		this.startTime = startTime.getTime();
		this.stepSeconds = stepSeconds;
		this.dataSources = new ArrayList<DataSourceConfig>();
		this.archives = new ArrayList<ArchiveConfig>();
	}

	public int getVersion() {
		return version;
	}

	public long getStep() {
		return stepSeconds;
	}

	public long getStartTime() {
		return startTime;
	}

	public List<DataSourceConfig> getDataSources() {
		return dataSources;
	}

	public void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max) {
		dataSources.add(new DataSourceConfig(name, type, heartbeat, min, max));
	}

	public List<ArchiveConfig> getArchives() {
		return archives;
	}

	public void addArchive(ConsolidateFunc func, double xff, int steps, int size) {
		archives.add(new ArchiveConfig(func, xff, steps, size));
	}
}