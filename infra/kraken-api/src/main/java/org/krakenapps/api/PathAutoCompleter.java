/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathAutoCompleter implements ScriptAutoCompletionHelper {
	enum FilterOption {
		All, DirectoryOnly, FileOnly
	}

	private FilterOption filterOption;

	public PathAutoCompleter() {
		filterOption = FilterOption.All;
	}

	public PathAutoCompleter(FilterOption filterOption) {
		this.filterOption = filterOption;
	}

	@Override
	public List<ScriptAutoCompletion> matches(ScriptSession session, String prefix) {

		File dir = (File) session.getProperty("dir");

		int p = prefix.lastIndexOf('/');
		String parent = p >= 0 ? prefix.substring(0, p) : null;
		String filePrefix = p >= 0 ? prefix.substring(p + 1) : prefix;

		File parentFile = canonicalize(dir, parent);
		boolean absolute = parent != null && parent.startsWith("/");

		List<ScriptAutoCompletion> paths = new ArrayList<ScriptAutoCompletion>();
		for (File f : parentFile.listFiles()) {
			if (f.getName().startsWith(filePrefix)) {
				if (filterOption == FilterOption.DirectoryOnly && !f.isDirectory())
					continue;

				if (filterOption == FilterOption.FileOnly && !f.isFile())
					continue;

				if (absolute) {
					String path = f.getAbsolutePath();
					if (!path.startsWith("/"))
						path = "/" + path;
					paths.add(new ScriptAutoCompletion(path.replaceAll("\\\\", "/")));
				} else {
					if (p > 0)
						paths.add(new ScriptAutoCompletion(parent + "/" + f.getName(), f.getName()));
					else
						paths.add(new ScriptAutoCompletion(f.getName()));
				}
			}
		}

		return paths;
	}

	private File canonicalize(File dir, String path) {
		if (path == null)
			return dir;
		else if (path.startsWith("/"))
			return new File(path);
		else
			return new File(dir, path);
	}
}
