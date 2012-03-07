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
package org.krakenapps.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.api.BundleRepository;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.Version;

public class MavenMetadata {
	private BundleRepository repo;
	private KeyStoreManager keyStoreManager;
	private String groupId;
	private String artifactId;
	private Version release;
	private Version latest;
	private List<Version> versions;
	private Date lastUpdated;

	public MavenMetadata(BundleRepository repo, KeyStoreManager keyStoreManager, String groupId, String artifactId)
			throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, IOException {
		this.repo = repo;
		this.keyStoreManager = keyStoreManager;
		this.groupId = groupId;
		this.artifactId = artifactId;
		refresh();
	}

	public void refresh() throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, IOException {
		if (this.repo.getUrl().getProtocol().equals("file"))
			refresh("maven-metadata-local.xml");
		else
			refresh("maven-metadata.xml");
	}

	public void refresh(String metadataFilename) throws KeyManagementException, UnrecoverableKeyException,
			KeyStoreException, IOException {
		URL url = new URL(normalize(repo.getUrl())
				+ String.format("%s/%s/%s", groupId.replace(".", "/"), artifactId, metadataFilename));
		String metadata = download(repo, url);

		String versioning = extract(metadata, "versioning");
		if (versioning != null) {
			String releaseStr = extract(versioning, "release");
			if (releaseStr != null)
				release = new Version(releaseStr);
			else
				release = null;

			String latestStr = extract(versioning, "latest");
			if (latestStr != null)
				latest = new Version(latestStr);
			else
				latest = null;

			String versionsStr = extract(versioning, "versions");
			if (versionsStr != null) {
				Matcher versionsMatcher = Pattern.compile("(?<=<version>).+?(?=</version>)", Pattern.DOTALL).matcher(
						versionsStr);
				versions = new ArrayList<Version>();
				while (versionsMatcher.find())
					versions.add(new Version(versionsMatcher.group()));
				Collections.sort(versions, new VersionComparator());
			} else
				versions = null;

			String lastUpdatedStr = extract(versioning, "lastUpdated");
			if (lastUpdatedStr != null) {
				try {
					lastUpdated = new SimpleDateFormat("yyyyMMddHHmmss").parse(lastUpdatedStr);
				} catch (ParseException e) {
				}
			} else
				lastUpdated = null;
		} else {
			release = null;
			latest = null;
			versions = null;
			lastUpdated = null;
		}
	}

	private String download(BundleRepository repository, URL url) throws IOException, KeyStoreException,
			KeyManagementException, UnrecoverableKeyException {
		if (repository.isHttps() && keyStoreManager != null) { // https
			try {
				String trustStoreAlias = repository.getTrustStoreAlias();
				String keyStoreAlias = repository.getKeyStoreAlias();

				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustStoreAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyStoreAlias, "SunX509");

				return new String(HttpWagon.download(url, tmf, kmf));
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		} else if (repository.isAuthRequired()) // http auth
			return new String(HttpWagon.download(url, true, repository.getAccount(), repository.getPassword()));
		else if (url.getProtocol().equals("file")) {
			try {
				File file = new File(url.toURI());
				long length = file.length();
				FileInputStream stream = new FileInputStream(file);
				byte[] b = new byte[(int) length];
				stream.read(b);
				return new String(b);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return "";
			}
		} else
			// plain http
			return new String(HttpWagon.download(url));
	}

	private URL normalize(URL url) {
		String urlStr = url.toString();
		if (urlStr.lastIndexOf('/') == urlStr.length() - 1)
			return url;

		try {
			return new URL(url + "/");
		} catch (MalformedURLException e) {
			return null; // ignore
		}
	}

	private String extract(String orig, String prefix) {
		String regex = String.format("(?<=<%s>).+?(?=</%s>)", prefix, prefix);
		Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(orig);
		if (matcher.find())
			return matcher.group();
		else
			return null;
	}

	private class VersionComparator implements Comparator<Version> {

		@Override
		public int compare(Version o1, Version o2) {
			String[] v1 = o1.toString().split("\\.");
			String[] v2 = o2.toString().split("\\.");
			for (int i = 0; i < Math.min(v1.length, v2.length); i++) {
				try {
					int i1 = Integer.parseInt(v1[i]);
					int i2 = Integer.parseInt(v2[i]);
					if (i1 != i2)
						return (i2 - i1);
				} catch (NumberFormatException e) {
					if (!v1[i].equals(v2[i]))
						return v2[i].compareTo(v1[i]);
				}
			}
			return (v2.length - v1.length);
		}

	}

	public BundleRepository getRepo() {
		return repo;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public Version getRelease() {
		return release;
	}

	public Version getLatest() {
		return latest;
	}

	public List<Version> getVersions() {
		return versions;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
}
