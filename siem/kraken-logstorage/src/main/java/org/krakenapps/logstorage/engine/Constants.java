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
package org.krakenapps.logstorage.engine;

public enum Constants {
	LogStorageDirectory("log_storage_dir"),
	LogCheckInterval("log_check_interval"),
	LogMaxIdleTime("log_max_idle_time"), 
	LogFlushInterval("log_flush_interval"), 
	LogBlockSize("log_block_size"),
	MinFreeDiskSpaceType("min_free_disk_space_type", "string"),
	MinFreeDiskSpaceValue("min_free_disk_space_value"),
	DiskLackAction("disk_lack_action", "string");

	Constants(String name) {
		this(name, "int");
	}

	Constants(String name, String type) {
		this.name = name;
		this.type = type;
	}

	private String name;
	private String type;

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public static Constants parse(String name) {
		for (Constants c : Constants.values())
			if (c.getName().equals(name))
				return c;

		return null;
	}
}
