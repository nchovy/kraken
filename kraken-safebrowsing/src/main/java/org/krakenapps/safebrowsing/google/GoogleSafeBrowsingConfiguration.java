package org.krakenapps.safebrowsing.google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.krakenapps.safebrowsing.interfaces.SafeBrowsingConfiguration;


public class GoogleSafeBrowsingConfiguration implements SafeBrowsingConfiguration {

	private String apiKey;
	private String dataPath;
	private Properties propertiesFile;

	public GoogleSafeBrowsingConfiguration(String path) {
		
		final String property = "googleSafeBrowsing.property";

		apiKey = "";
		dataPath = path;

		try {
			InputStream input;
			File f = new File(this.dataPath+property);
			if(f.exists() && f.canRead() && f.canWrite()) {
				input = new FileInputStream(this.dataPath+property);
			}
			else {
				input = GoogleSafeBrowsingConfiguration.class.getResourceAsStream(property);
			}
			this.propertiesFile.load(input);
		}
		catch (FileNotFoundException e) {	e.printStackTrace();	}
		catch (IOException e) 			{	e.printStackTrace();	}
	}

	@Override
	public void setAPIKey(String key) {
		this.apiKey = key;
	}

	@Override
	public String getAPIKey() {
		return this.apiKey;
	}

	@Override
	public void setDataStorePath(String directory) {
	}

	@Override
	public String getDataStorePath() {
		return null;
	}

}
