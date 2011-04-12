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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.api.BundleRepository;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.MavenArtifact;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver {
	private final Logger logger = LoggerFactory.getLogger(MavenResolver.class.getName());

	private File localRepository;
	private List<BundleRepository> repositories;
	private ProgressMonitor monitor;
	private KeyStoreManager keyStoreManager;

	public MavenResolver(File localRepository, List<BundleRepository> repositories, ProgressMonitor monitor,
			KeyStoreManager keyStoreManager) {
		this.localRepository = localRepository;
		this.repositories = repositories;
		this.monitor = monitor;
		this.keyStoreManager = keyStoreManager;
	}

	public File resolve(MavenArtifact artifact) throws MavenResolveException {
		File localJar = download(artifact);

		if (localJar == null)
			throw new MavenResolveException();

		return localJar;
	}

	private String getRelativePath(MavenArtifact artifact) {
		return String.format("%s/%s/%s/%s-%s", artifact.getGroupId().replace(".", "/"), artifact.getArtifactId(),
				artifact.getVersion(), artifact.getArtifactId(), artifact.getVersion());
	}

	private File getLocalPom(BundleRepository repo, MavenArtifact artifact) {
		String relativePath = getRelativePath(artifact);
		if (repo.getUrl().getProtocol().equals("file")) {
			try {
				return new File(new File(repo.getUrl().toURI()), relativePath + ".pom");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new File(localRepository, relativePath + ".pom");
	}

	private File getLocalJar(BundleRepository repo, MavenArtifact artifact) {
		String relativePath = getRelativePath(artifact);
		if (repo.getUrl().getProtocol().equals("file")) {
			try {
				return new File(new File(repo.getUrl().toURI()), relativePath + ".jar");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new File(localRepository, relativePath + ".jar");
	}

	private File download(MavenArtifact artifact) {
		Collections.sort(repositories, new Comparator<BundleRepository>() {
			@Override
			public int compare(BundleRepository o1, BundleRepository o2) {
				if (o1 == null && o2 == null) {
					return 1;
				} else if (o1 == null) {
					return -1;
				} else if (o2 == null) {
					return 1;
				} else {
					return o2.getPriority() - o1.getPriority();
				}
			}
		});
		for (BundleRepository repo : repositories) {
			File downloadedJar = tryDownload(repo, artifact);
			if (downloadedJar != null)
				return downloadedJar;
		}

		return null;
	}

	private File tryDownload(BundleRepository repo, MavenArtifact artifact) {
		File localPom = getLocalPom(repo, artifact);
		File localJar = getLocalJar(repo, artifact);

		localPom.getParentFile().mkdirs();

		FileOutputStream pomStream = null;
		FileOutputStream jarStream = null;
		try {
			URL pomUrl = getPomUrl(repo.getUrl(), artifact);
			URL jarUrl = getJarUrl(repo.getUrl(), artifact);

			monitor.writeln("  -> trying to download from " + repo);

			// download pom
			if (localPom.exists() == false) {
				try {
					byte[] binary = download(repo, pomUrl);
					pomStream = new FileOutputStream(localPom, false);
					pomStream.write(binary);
				} catch (Exception e) {
					logger.info("maven resolver: failed to get {} {}", pomUrl, e.getMessage());
					return null;
				}
			}

			// download jar
			if (localJar.exists() == false) {
				try {
					byte[] binary = download(repo, jarUrl);
					jarStream = new FileOutputStream(localJar, false);
					jarStream.write(binary);
				} catch (Exception e) {
					logger.info("maven resolver: failed to get {} {}", jarUrl, e.getMessage());
					return null;
				}
			}

			return localJar;
		} catch (MalformedURLException e) {
			logger.info("maven resolver: malformed url", e);
			return null;
		} finally {
			if (pomStream != null) {
				try {
					pomStream.close();
				} catch (IOException e) {
					// ignore
				}
			}

			if (jarStream != null) {
				try {
					jarStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private byte[] download(BundleRepository repository, URL url) throws IOException, KeyStoreException,
			KeyManagementException, UnrecoverableKeyException {
		if (repository.isHttps() && keyStoreManager != null) { // https
			try {
				String trustStoreAlias = repository.getTrustStoreAlias();
				String keyStoreAlias = repository.getKeyStoreAlias();

				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustStoreAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyStoreAlias, "SunX509");

				return HttpWagon.download(url, tmf, kmf);
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		} else if (repository.isAuthRequired()) // http auth
			return HttpWagon.download(url, true, repository.getAccount(), repository.getPassword());
		else if (url.getProtocol().equals("file")) {
			try {
				File file = new File(url.toURI());
				long length = file.length();
				FileInputStream stream = new FileInputStream(file);
				byte[] b = new byte[(int) length];
				stream.read(b);
				return b;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return new byte[0];
			}
		} else
			// plain http
			return HttpWagon.download(url);
	}

	private URL getPomUrl(URL repository, MavenArtifact artifact) throws MalformedURLException {
		String relativePath = getRelativePath(artifact);
		return new URL(normalize(repository) + relativePath + ".pom");
	}

	private URL getJarUrl(URL repository, MavenArtifact artifact) throws MalformedURLException {
		String relativePath = getRelativePath(artifact);
		return new URL(normalize(repository) + relativePath + ".jar");
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
}
