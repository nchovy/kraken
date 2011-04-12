package org.krakenapps.safebrowsing.interfaces;

public interface SafeBrowsingConfiguration {

	public void setAPIKey(String apikey);

	public String getAPIKey();

	public void setDataStorePath(String path);

	public String getDataStorePath();

}
