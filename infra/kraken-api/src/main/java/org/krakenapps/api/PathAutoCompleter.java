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
import java.io.IOException;
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
		String parent = null;
		if (p > 0)
			parent = prefix.substring(0, p);
		else if (p == 0)
			parent = "/";

		List<ScriptAutoCompletion> paths = new ArrayList<ScriptAutoCompletion>();
		String filePrefix = p >= 0 ? prefix.substring(p + 1) : prefix;
		File parentFile = canonicalize(dir, parent);
		if (parentFile == null)
			return paths;
		
		boolean absolute = parent != null && (parent.startsWith("/") || isWindowsDriveRoot(parentFile.getAbsolutePath()));

		File[] files = null;

		// for windows drive root enumeration
		if (parent != null && parent.equals("/") && File.separatorChar == '\\') {
			files = File.listRoots();
		} else {
			files = parentFile.listFiles();
		}
		
		if (files == null)
			return paths;

		for (File f : files) {
			String name = f.getName();
			if (name.startsWith(filePrefix)) {
				if (filterOption == FilterOption.DirectoryOnly && !f.isDirectory())
					continue;

				if (filterOption == FilterOption.FileOnly && !f.isFile())
					continue;

				if (absolute) {
					String path = f.getAbsolutePath();
					try {
						path = f.getCanonicalPath();
					} catch (IOException e) {
					}

					if (!path.startsWith("/"))
						path = "/" + path;
					path = path.replaceAll("\\\\", "/");
					if (f.isDirectory() && !path.endsWith("/"))
						path = path + "/";

					if (path.toLowerCase().startsWith(prefix.toLowerCase()))
						paths.add(new ScriptAutoCompletion(path, name));
				} else {
					if (f.isDirectory())
						name = name + "/";

					if (p > 0) {
						String completion = parent + "/" + name;
						if (completion.toLowerCase().startsWith(prefix.toLowerCase()))
							paths.add(new ScriptAutoCompletion(completion, name));
					} else {
						if (name.toLowerCase().startsWith(prefix.toLowerCase()))
							paths.add(new ScriptAutoCompletion(name));
					}
				}
			}
		}

		return paths;
	}

	private File canonicalize(File dir, String path) {
		try {
			if (path == null || path.isEmpty()) {
				return dir.getCanonicalFile();
			} else if (path.startsWith("/") || isWindowsDriveRoot(path)) {
				File f = new File(path.endsWith("/") ? path : path + "/");
				return f.getCanonicalFile();
			} else {
				return new File(dir, path).getCanonicalFile();
			}
		} catch (IOException e) {
			return null;
		}
	}

	private boolean isWindowsDriveRoot(String path) {
		try {
			File f = new File(path);
			for (File root : File.listRoots()) {
				String s1 = root.getCanonicalPath();
				String s2 = f.getCanonicalPath();
				if (s1.equals(s2))
					return true;
			}
		} catch (IOException e) {
		}
		return false;
	}

}
