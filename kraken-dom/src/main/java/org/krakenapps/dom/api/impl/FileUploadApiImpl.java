/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.dom.api.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.UploadCallback;
import org.krakenapps.dom.api.UploadToken;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.dom.model.User;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-file-upload-api")
@Provides
@JpaConfig(factory = "dom")
public class FileUploadApiImpl implements FileUploadApi {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(FileUploadApiImpl.class.getName());
	private static final String BASE_PATH = "base_path";

	@Requires
	private UserApi userApi;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	// upload config node
	private Preferences prefs;
	private File baseDir;
	private File tempDir;

	private AtomicInteger counter;
	private ConcurrentMap<String, UploadItem> items;
	private ConcurrentMap<String, Integer> downloadTokens;

	public FileUploadApiImpl(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		if (ref == null)
			throw new IllegalStateException("osgi preferences service is required");

		PreferencesService prefsvc = (PreferencesService) bc.getService(ref);
		prefs = prefsvc.getSystemPreferences().node("upload");
	}

	@Validate
	public void start() {
		baseDir = new File(prefs.get(BASE_PATH, "data/kraken-dom/upload/"));
		tempDir = new File(prefs.get(BASE_PATH, "data/kraken-dom/upload/temp"));
		baseDir.mkdirs();
		tempDir.mkdirs();

		items = new ConcurrentHashMap<String, UploadItem>();
		downloadTokens = new ConcurrentHashMap<String, Integer>();
		counter = new AtomicInteger(getMaxCounter());
	}

	@Transactional
	private int getMaxCounter() {
		EntityManager em = entityManagerService.getEntityManager();
		Integer max = (Integer) em.createQuery("SELECT MAX(f.id) FROM UploadedFile f").getSingleResult();
		if (max == null)
			return 0;
		return max;
	}

	public void stop() {
		// nothing to do here
	}

	@Override
	public File getBaseDirectory() {
		return baseDir;
	}

	@Override
	public void setBaseDirectory(File dir) {
		if (dir == null)
			throw new IllegalArgumentException("upload dir must be not null");

		dir.mkdirs();

		prefs.put(BASE_PATH, dir.getAbsolutePath());

		baseDir = dir;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<FileSpace> getFileSpaces(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM FileSpace s WHERE s.organization.id = ?").setParameter(1, orgId).getResultList();
	}

	@Transactional
	@Override
	public int createFileSpace(int orgId, int userId, String spaceName) {
		EntityManager em = entityManagerService.getEntityManager();

		// basic check (you should add multicolumn unique index to file space.
		long count = (Long) em
				.createQuery("SELECT COUNT(*) FROM FileSpace s WHERE s.organization.id = ? AND s.name = ?")
				.setParameter(1, orgId).setParameter(2, spaceName).getSingleResult();

		if (count > 0)
			throw new IllegalStateException("duplicated-name");

		User owner = userApi.getUser(userId);
		if (owner == null)
			throw new IllegalStateException("user-not-found");

		// create
		FileSpace space = new FileSpace();
		Organization org = em.find(Organization.class, orgId);
		space.setOrganization(org);
		space.setOwner(owner);
		space.setName(spaceName);
		space.setCreateDateTime(new Date());
		em.persist(space);

		return space.getId();
	}

	@Transactional
	@Override
	public void removeFileSpace(int userId, int spaceId) {
		EntityManager em = entityManagerService.getEntityManager();
		FileSpace space = em.find(FileSpace.class, spaceId);
		if (space.getOwner().getId() != userId)
			throw new SecurityException();

		em.remove(space);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<UploadedFile> getFiles(int orgId, int spaceId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM UploadedFile f WHERE f.fileSpace.id = ?").setParameter(1, spaceId).getResultList();
	}

	@Override
	public int setUploadToken(UploadToken token, UploadCallback callback) {
		if (token == null)
			throw new IllegalArgumentException("upload token must be not null");

		// get new resource id
		int nextId = counter.incrementAndGet();

		// set upload item on waiting table
		UploadItem old = items.putIfAbsent(token.getToken(), new UploadItem(token, nextId, callback));
		if (old != null)
			throw new IllegalStateException("duplicated http upload token: " + token.getToken());

		return nextId;
	}

	@Override
	public void removeDownloadToken(String token) {
		if (token != null)
			downloadTokens.remove(token);
	}

	@Override
	public void setDownloadToken(String token, int userId) {
		Integer old = downloadTokens.putIfAbsent(token, userId);
		if (old != null)
			throw new IllegalStateException("duplicated http download token: " + token);
	}

	@Override
	public void writeFile(String token, InputStream is) throws IOException {
		FileOutputStream os = null;
		long totalReadBytes = 0;

		// remove token from waiting table
		UploadItem item = items.remove(token);
		if (item == null)
			throw new IOException("upload token not found: " + token);

		logger.trace("kraken dom: new upload post for token {}, resouce id {}", token, item.resourceId);

		try {
			File temp = File.createTempFile("krakenhttp-", null, tempDir);
			os = new FileOutputStream(temp);
			byte[] buffer = new byte[8096];

			while (true) {
				int readBytes = is.read(buffer);
				if (readBytes <= 0)
					break;

				totalReadBytes += readBytes;
				os.write(buffer, 0, readBytes);
			}

			os.close();
			os = null;

			// check file size
			if (totalReadBytes == item.token.getFileSize()) {
				saveFile(temp, item);
			} else {
				// delete
				temp.delete();

				// failure callback
				try {
					item.callback.onUploadFile(item.token, null);
					logger.info("kraken dom: upload failure {} {}", item.token.getSpaceId(), item.token.getFileName());
				} catch (Exception e) {
					logger.warn("kraken dom: upload callback should not throw any exception", e);
				}
			}
		} catch (IOException e) {
			throw e;
		}
	}

	@Transactional
	private void saveFile(File temp, UploadItem item) throws IOException {
		EntityManager em = entityManagerService.getEntityManager();

		// callback
		UploadToken token = item.token;

		// build path
		File spaceDir = new File(baseDir, Integer.toString(token.getSpaceId()));
		spaceDir.mkdirs();

		// move file
		File dest = new File(spaceDir, Integer.toString(item.resourceId));
		logger.trace("kraken dom: move {} to {}", temp.getAbsolutePath(), dest.getAbsolutePath());
		if (!temp.renameTo(dest)) {
			throw new IOException(String.format("kraken dom: move [%s] to [%s] failed", temp.getAbsolutePath(),
					dest.getAbsolutePath()));
		}

		// save properties
		FileSpace space = em.find(FileSpace.class, token.getSpaceId());
		if (space == null) {
			String msg = String.format("kraken dom: file space [{}] not found for uploaded file [{}]",
					token.getSpaceId(), token.getFileName());
			throw new IllegalStateException(msg);
		}

		User owner = em.find(User.class, token.getUserId());
		if (owner == null) {
			String msg = String.format("kraken dom: user [{}] not found for uploaded file [{}]", token.getUserId(),
					token.getFileName());
			throw new IllegalStateException(msg);
		}

		UploadedFile file = new UploadedFile();
		file.setFileSpace(space);
		file.setOwner(owner);
		file.setFileName(token.getFileName());
		file.setFileSize(token.getFileSize());
		file.setPath(dest.getAbsolutePath());
		file.setCreateDateTime(new Date());
		em.persist(file);

		// fire callbacks
		if (item.callback != null)
			item.callback.onUploadFile(token, item.resourceId);

		logger.trace("kraken dom: user file uploaded, space {}, name {}", token.getSpaceId(), token.getFileName());
	}

	@Transactional
	@Override
	public UploadedFile getFile(String token, int resourceId) throws IOException {
		Integer userId = downloadTokens.get(token);
		if (userId == null)
			throw new IllegalStateException("download token not found: " + token);

		User user = userApi.getUser(userId);
		if (user == null)
			throw new IllegalStateException("user not found: " + userId);

		EntityManager em = entityManagerService.getEntityManager();
		UploadedFile f = em.find(UploadedFile.class, resourceId);
		if (f == null)
			throw new IllegalStateException("resource not found: " + resourceId);

		if (f.getOwner().getOrganization().getId() != userId)
			throw new SecurityException("access denied for token [" + token + "], user [" + userId + "], file ["
					+ resourceId + "]");

		return f;
	}

	@Transactional
	@Override
	public void deleteFile(int userId, int resourceId) {
		EntityManager em = entityManagerService.getEntityManager();
		UploadedFile file = em.find(UploadedFile.class, resourceId);
		if (file == null)
			return;

		if (file.getOwner().getId() != userId)
			throw new SecurityException("cannot delete other's file: " + resourceId);

		em.remove(file);

		// remove physical file
		int spaceId = file.getFileSpace().getId();
		File spaceDir = new File(baseDir, Integer.toString(spaceId));
		spaceDir.mkdirs();

		File f = new File(spaceDir, Integer.toString(resourceId));
		if (!f.delete())
			throw new IllegalStateException(String.format(
					"failed to delete uploaded file: space %d, resource %d, path %s", spaceId, resourceId,
					f.getAbsolutePath()));
	}

	private static class UploadItem {
		public UploadToken token;
		public int resourceId;
		public UploadCallback callback;

		public UploadItem(UploadToken token, int resourceId, UploadCallback callback) {
			this.token = token;
			this.resourceId = resourceId;
			this.callback = callback;
		}
	}
}
