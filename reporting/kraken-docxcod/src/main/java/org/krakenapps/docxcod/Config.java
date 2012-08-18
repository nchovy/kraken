package org.krakenapps.docxcod;

import java.io.File;

public class Config {
	public static Config defaultConfig; 
	static {
		defaultConfig = new Config();
		defaultConfig.workingDir = getTempDir();
	}
	
	public File workingDir; 
	
	private static File getTempDir() {
		return new File(System.getProperty("java.io.tmpdir"), "KrakenDocxcod");
	}
}
