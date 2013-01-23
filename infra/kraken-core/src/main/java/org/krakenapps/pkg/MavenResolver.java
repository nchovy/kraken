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
import java.io.InputStream;
import java.io.OutputStream;
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
import org.krakenapps.api.Version;
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
			throw new MavenResolveException(artifact);

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
				throw new RuntimeException(e);
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
				throw new RuntimeException(e);
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
			File downloadedJar = null;

			if (artifact.getVersion() == null) {
				try {
					MavenMetadata metadata = new MavenMetadata(repo, keyStoreManager, artifact.getGroupId(),
							artifact.getArtifactId());

					if (metadata.getRelease() != null) {
						artifact.setVersion(metadata.getRelease());
						downloadedJar = tryDownload(repo, artifact);
					}

					if (downloadedJar == null && metadata.getLatest() != null) {
						artifact.setVersion(metadata.getLatest());
						downloadedJar = tryDownload(repo, artifact);
					}

					if (downloadedJar == null) {
						for (Version version : metadata.getVersions()) {
							artifact.setVersion(version);
							downloadedJar = tryDownload(repo, artifact);
							if (downloadedJar != null)
								break;
						}
					}
				} catch (Exception e) {
					String metadataUrl = normalize(repo.getUrl())
							+ String.format("%s/%s/maven-metadata.xml", artifact.getGroupId().replace(".", "/"),
									artifact.getArtifactId());
					logger.info("maven resolver: failed to get {} {}", metadataUrl, e.getMessage());
				}
			} else
				downloadedJar = tryDownload(repo, artifact);

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

			if (monitor != null)
				monitor.writeln("  -> trying to download from " + repo);

			byte[] b = new byte[8096];

			// download pom
			InputStream is = null;
			if (localPom.exists() == false) {
				try {
					is = download(repo, pomUrl);
					pomStream = new FileOutputStream(localPom, false);

					while (is != null) {
						int read = is.read(b);
						if (read <= 0)
							break;

						pomStream.write(b, 0, read);
					}
				} catch (Exception e) {
					logger.info("maven resolver: failed to get {} {}", pomUrl, e.getMessage());
					return null;
				} finally {
					ensureClose(is);
				}
			}

			if (localJar.getAbsolutePath().replace("\\", "/").equals(jarUrl.getPath().substring(1)))
				return localJar;

			// download jar
			is = null;
			try {
				is = download(repo, jarUrl);
				jarStream = new FileOutputStream(localJar, false);

				while (is != null) {
					int read = is.read(b);
					if (read <= 0)
						break;

					jarStream.write(b, 0, read);
				}
			} catch (Exception e) {
				logger.info("maven resolver: failed to get {} {}", jarUrl, e.getMessage());
				return null;
			} finally {
				ensureClose(is);
			}

			return localJar;
		} catch (MalformedURLException e) {
			logger.info("maven resolver: malformed url", e);
			return null;
		} finally {
			ensureClose(pomStream);
			ensureClose(jarStream);
		}
	}

	private void ensureClose(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	private void ensureClose(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
	}

	private InputStream download(BundleRepository repository, URL url) throws IOException, KeyStoreException,
			KeyManagementException, UnrecoverableKeyException {
		if (repository.isHttps() && keyStoreManager != null) { // https
			try {
				String trustStoreAlias = repository.getTrustStoreAlias();
				String keyStoreAlias = repository.getKeyStoreAlias();

				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustStoreAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyStoreAlias, "SunX509");

				return HttpWagon.openDownloadStream(url, tmf, kmf);
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		} else if (repository.isAuthRequired()) // http auth
			return HttpWagon.openDownloadStream(url, true, repository.getAccount(), repository.getPassword());
		else if (url.getProtocol().equals("file")) {
			try {
				File file = new File(url.toURI());
				return new FileInputStream(file);
			} catch (URISyntaxException e) {
				return null;
			}
		} else
			// plain http
			return HttpWagon.openDownloadStream(url);
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
