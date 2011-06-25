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
