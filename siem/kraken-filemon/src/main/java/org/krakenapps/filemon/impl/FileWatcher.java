/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.filemon.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.krakenapps.filemon.FileMonitorEventListener;

public class FileWatcher {
	private Set<FileMonitorEventListener> callbacks;

	public FileWatcher() {
		callbacks = new HashSet<FileMonitorEventListener>();
	}

	public void register(FileMonitorEventListener callback) {
		callbacks.add(callback);
	}

	public void unregister(FileMonitorEventListener callback) {
		callbacks.remove(callback);
	}

	public void watch(String directoryPath) throws Exception {
		Path path = Paths.get(directoryPath);
		WatchService watcher = path.getFileSystem().newWatchService();
		path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

		WatchKey watchKey = watcher.take();

		List<WatchEvent<?>> events = watchKey.pollEvents();
		for (WatchEvent<?> event : events) {
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onCreated(new File(event.context().toString()));
				}
			}
			if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onDeleted(new File(event.context().toString()));
				}
			}

			if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				for (FileMonitorEventListener callback : callbacks) {
					callback.onModified(new File(event.context().toString()));
				}
			}
		}
	}
}