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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.filemon.FileMonitorEventListener;
import org.krakenapps.filemon.FileMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMonitorScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(FileMonitorScript.class.getName());

	private FileMonitorService monitor;
	private ScriptContext context;

	public FileMonitorScript(FileMonitorService monitor) {
		this.monitor = monitor;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void timestamp(String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = monitor.getLastTimestamp();

		context.println("Last Baseline Timestamp");
		context.println("-------------------------");
		if (date != null)
			context.println(dateFormat.format(date));
		else
			context.println("Baseline not built yet");
	}

	public void count(String[] args) {
		Integer count = monitor.getLastFileCount();
		if (count != null)
			context.println("Last Baseline Files: " + count);
		else
			context.println("Baseline not built yet");
	}

	@ScriptUsage(description = "calculate md5sum of a file", arguments = { @ScriptArgument(name = "file path", type = "string", description = "file path") })
	public void md5sum(String[] args) {
		try {
			File f = new File(args[0]);
			String hash = monitor.getMd5(f);
			context.println(hash);
		} catch (IOException e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot md5sum", e);
		}
	}

	@ScriptUsage(description = "calculate sha1sum of a file", arguments = { @ScriptArgument(name = "file path", type = "string", description = "file path") })
	public void sha1sum(String[] args) {
		try {
			File f = new File(args[0]);
			String hash = monitor.getSha1(f);
			context.println(hash);
		} catch (IOException e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot sha1sum", e);
		}
	}

	public void inclusions(String[] args) {
		context.println("Inclusion Paths");
		context.println("-------------------");
		for (File f : monitor.getInclusionPaths())
			context.println(f.getAbsolutePath());
	}

	public void exclusions(String[] args) {
		context.println("Exclusion Patterns");
		context.println("-------------------");
		for (Pattern p : monitor.getExclusionPatterns())
			context.println(p.toString());
	}

	@ScriptUsage(description = "add inclusion path", arguments = { @ScriptArgument(name = "path", type = "string", description = "path to add") })
	public void addPath(String[] args) {
		try {
			String path = args[0];
			File f = new File(path);
			if (!f.exists()) {
				context.println("path does not exist: " + path);
				return;
			}

			monitor.addInclusionPath(f);
			context.println("added");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot add path", e);
		}
	}

	@ScriptUsage(description = "remove inclusion path", arguments = { @ScriptArgument(name = "path", type = "string", description = "path to remove") })
	public void removePath(String[] args) {
		try {
			File f = new File(args[0]);
			// NOTE: path can be removed
			monitor.removeInclusionPath(f);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot remove path", e);
		}
	}

	@ScriptUsage(description = "add exclusion pattern", arguments = { @ScriptArgument(type = "string", name = "pattern", description = "file name pattern to exclude") })
	public void addExclusionPattern(String[] args) {
		try {
			String regex = args[0];
			monitor.addExclusionPattern(regex);
			context.println("added");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot add exclusion pattern", e);
		}
	}

	@ScriptUsage(description = "remove exclusion pattern", arguments = { @ScriptArgument(type = "string", name = "pattern", description = "file name pattern to exclude") })
	public void removeExclusionPattern(String[] args) {
		try {
			String regex = args[0];
			monitor.removeExclusionPattern(regex);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken filemon: cannot remove exlusion pattern", e);
		}
	}

	public void build(String[] args) {
		try {
			monitor.createBaseline();
			context.println("baseline created");
		} catch (IOException e) {
			context.println(e.getMessage());
			logger.error("kraken baseline: baseline build failed", e);
		}
	}

	public void check(String[] args) {
		boolean verbose = false;

		for (String arg : args)
			if (arg.equals("-v"))
				verbose = true;

		FileEventPrinter p = new FileEventPrinter(verbose);
		try {
			monitor.addEventListener(p);
			monitor.check();
			context.println("completed");
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			monitor.removeEventListener(p);
		}
	}

	private class FileEventPrinter implements FileMonitorEventListener {
		private boolean verbose = false;

		public FileEventPrinter(boolean verbose) {
			this.verbose = verbose;
		}

		@Override
		public void onCheck(File f) {
			if (verbose)
				context.println(f.getAbsolutePath());
		}

		@Override
		public void onModified(FileChange f) {
			context.println("[M] " + f);
		}

		@Override
		public void onDeleted(File f) {
			context.println("[D] " + f.getAbsolutePath());
		}

		@Override
		public void onCreated(File f) {
			context.println("[C] " + f.getAbsolutePath());
		}

		@Override
		public void onModified(File f) {
			context.println("[M] " + f.getAbsolutePath());
		}
	}
}