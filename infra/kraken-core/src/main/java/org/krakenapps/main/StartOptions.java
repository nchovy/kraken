/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.main;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class StartOptions {
	CommandLine cmdline;

	public StartOptions() {
		this.cmdline = null;
	}

	public StartOptions(String[] args) {
		Options options = new Options();
		setOptions(options);

		try {
			cmdline = new BasicParser().parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			cmdline = null;
		}
	}

	private void setOptions(Options options) {
		options.addOption("d", false,
				"developer mode: start with updating bundles installed from local repository");
	}

	public boolean isDeveloperMode() {
		return cmdline != null && cmdline.hasOption("d");
	}
}
