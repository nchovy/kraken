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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventListener;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.EntityEventProvider;
import org.krakenapps.dom.api.EntityState;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.Transaction;
import org.krakenapps.dom.api.UploadCallback;
import org.krakenapps.dom.api.UploadToken;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-file-upload-api")
@Provides
public class FileUploadApiImpl extends DefaultEntityEventProvider<FileSpace> implements FileUploadApi {
	private static final String FSP_BASE_DIR_KEY = "upload-base-dir";

	private static final Class<FileSpace> fsp = FileSpace.class;
	private static final String FSP_NOT_FOUND = "file-space-not-found";
	private static final String FSP_ALREADY_EXIST = "file-space-already-exist";

	private static final Class<UploadedFile> file = UploadedFile.class;
	private static final String FILE_NOT_FOUND = "uploaded-file-not-found";
	private static final String FILE_ALREADY_EXIST = "uploaded-file-already-exist";
	private DefaultEntityEventProvider<UploadedFile> fileEventProvider = new DefaultEntityEventProvider<UploadedFile>();

	private Logger logger = LoggerFactory.getLogger(FileUploadApiImpl.class);
	private ConcurrentMap<String, UploadItem> uploadTokens = new ConcurrentHashMap<String, FileUploadApiImpl.UploadItem>();
	private ConcurrentMap<String, String> sessionDownloadTokens = new ConcurrentHashMap<String, String>(); // session-token
	private ConcurrentMap<String, DownloadToken> downloadTokens = new ConcurrentHashMap<String, DownloadToken>(); // session-token

	private EntityEventListener<User> userEventListener = new DefaultEntityEventListener<User>() {

		@Override
		public void entitiesRemoved(String domain, Collection<EntityState> objs) {
			LoginNameFilter filter = new LoginNameFilter();
			for (EntityState o : objs) {
				User user = (User) o.entity;
				filter.add(user.getLoginName());
			}

			cfg.removes(domain, UploadedFile.class, Arrays.asList((Predicate) filter), null, null);
			fileSpaceRemoveOrUpdate(domain, filter);
		}

		@Override
		public void entityRemoved(String domain, User obj, Object state) {
			Predicate pred = Predicates.field("owner/loginName", obj.getLoginName());
			cfg.removes(domain, UploadedFile.class, Arrays.asList(pred), null, null);
			fileSpaceRemoveOrUpdate(domain, pred);
		}

		private void fileSpaceRemoveOrUpdate(String domain, Predicate pred) {
			Collection<FileSpace> fileSpaces = cfg.all(domain, FileSpace.class, pred);
			if (!fileSpaces.isEmpty()) {
				User master = null;
				for (Admin admin : adminApi.getAdmins(domain)) {
					if (admin.getRole().getName().equals("master")) {
						master = admin.getUser();
						break;
					}
				}

				for (FileSpace fileSpace : fileSpaces) {
					if (fileSpace.getFiles().isEmpty()) {
						cfg.remove(domain, FileSpace.class, Predicates.field("guid", fileSpace.getGuid()), null, null);
					} else {
						fileSpace.setOwner(master);
						cfg.update(domain, FileSpace.class, Predicates.field("guid", fileSpace.getGuid()), fileSpace, null, null);
					}
				}
			}
		}
	};

	private static class LoginNameFilter implements Predicate {

		private HashSet<String> loginNames = new HashSet<String>();

		public void add(String loginName) {
			loginNames.add(loginName);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean eval(Config c) {
			Map<String, Object> m = (Map<String, Object>) c.getDocument();
			if (m == null)
				return false;

			Map<String, Object> owner = (Map<String, Object>) m.get("owner");
			if (owner == null)
				return false;

			String loginName = (String) owner.get("login_name");
			if (loginName == null)
				return false;

			return loginNames.contains(loginName);
		}
	}

	private EntityEventListener<FileSpace> spaceEventListener = new DefaultEntityEventListener<FileSpace>() {
		@Override
		public void entityRemoving(String domain, FileSpace obj, ConfigTransaction xact, Object state) {
			Transaction x = Transaction.getInstance(xact);
			cfg.removes(x, domain, file, getPreds(obj.getFiles()), null, fileEventProvider, state, null);
		}
	};
	private EntityEventListener<UploadedFile> fileEventListener = new DefaultEntityEventListener<UploadedFile>() {
		@Override
		public void entityRemoved(String domain, UploadedFile obj, Object state) {
			File file = new File(obj.getPath());
			if (!file.delete())
				logger.error("kraken dom: file delete failed [{}]", file.getAbsolutePath());
		}
	};

	@Requires
	private ConfigManager cfg;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private UserApi userApi;

	@Requires
	private AdminApi adminApi;

	@Validate
	public void validate() {
		userApi.addEntityEventListener(userEventListener);
		this.addEntityEventListener(spaceEventListener);
		fileEventProvider.addEntityEventListener(fileEventListener);
	}

	@Invalidate
	public void invalidate() {
		if (userApi != null)
			userApi.removeEntityEventListener(userEventListener);
		this.removeEntityEventListener(spaceEventListener);
		fileEventProvider.removeEntityEventListener(fileEventListener);
	}

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private List<Predicate> getPreds(List<? extends Object> objs) {
		if (objs == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(objs.size());
		for (Object obj : objs) {
			if (obj instanceof FileSpace)
				preds.add(getPred(((FileSpace) obj).getGuid()));
			else if (obj instanceof UploadedFile)
				preds.add(getPred(((UploadedFile) obj).getGuid()));
		}
		return preds;
	}

	@Override
	public File getBaseDirectory(String domain) {
		String dir = orgApi.getOrganizationParameter(domain, FSP_BASE_DIR_KEY, String.class);
		if (dir == null)
			dir = new File(System.getProperty("kraken.data.dir"), "kraken-dom/upload/" + domain).getAbsolutePath();
		File f = new File(dir);
		f.mkdirs();
		return f;
	}

	@Override
	public void setBaseDirectory(String domain, File dir) {
		orgApi.setOrganizationParameter(domain, FSP_BASE_DIR_KEY, dir.getAbsoluteFile());
	}

	@Override
	public Collection<FileSpace> getFileSpaces(String domain) {
		return cfg.all(domain, fsp);
	}

	@Override
	public FileSpace findFileSpace(String domain, String guid) {
		FileSpace fileSpace = cfg.find(domain, fsp, getPred(guid));
		if (fileSpace != null)
			fileSpace.setFiles((List<UploadedFile>) cfg.all(domain, UploadedFile.class, Predicates.field("space/guid", guid)));
		return fileSpace;
	}

	@Override
	public FileSpace getFileSpace(String domain, String guid) {
		FileSpace fileSpace = cfg.get(domain, fsp, getPred(guid), FSP_NOT_FOUND);
		fileSpace.setFiles((List<UploadedFile>) cfg.all(domain, UploadedFile.class, Predicates.field("space/guid", guid)));
		return fileSpace;
	}

	@Override
	public void createFileSpaces(String domain, Collection<FileSpace> spaces) {
		List<FileSpace> spaceList = new ArrayList<FileSpace>(spaces);
		cfg.adds(domain, fsp, getPreds(spaceList), spaceList, FSP_ALREADY_EXIST, this);
	}

	@Override
	public void createFileSpace(String domain, FileSpace space) {
		cfg.add(domain, fsp, getPred(space.getGuid()), space, FSP_ALREADY_EXIST, this);
	}

	@Override
	public void updateFileSpaces(String domain, String loginName, Collection<FileSpace> spaces) {
		List<FileSpace> spaceList = new ArrayList<FileSpace>(spaces);
		for (FileSpace space : spaceList)
			space.setUpdated(new Date());
		cfg.updates(domain, fsp, getPreds(spaceList), spaceList, FSP_NOT_FOUND, this);
	}

	@Override
	public void updateFileSpace(String domain, String loginName, FileSpace space) {
		checkPermissionLevel(domain, loginName, space.getGuid(), "update-file-space-permission-denied");
		space.setUpdated(new Date());
		cfg.update(domain, fsp, getPred(space.getGuid()), space, FSP_NOT_FOUND, this);
	}

	@Override
	public void removeFileSpaces(String domain, String loginName, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, fsp, preds, FSP_NOT_FOUND, this);
	}

	@Override
	public void removeFileSpace(String domain, String loginName, String guid) {
		checkPermissionLevel(domain, loginName, guid, "remove-file-space-permission-denied");
		cfg.remove(domain, fsp, getPred(guid), FSP_NOT_FOUND, this);
	}

	private void checkPermissionLevel(String domain, String loginName, String guid, String exceptionMessage) {
		FileSpace space = getFileSpace(domain, guid);
		if (!space.getOwner().getLoginName().equals(loginName))
			throw new DOMException(exceptionMessage);
	}

	@Override
	public String setUploadToken(UploadToken token, UploadCallback callback) {
		userApi.getUser(token.getOrgDomain(), token.getLoginName());
		UploadItem item = new UploadItem(token, callback);
		uploadTokens.putIfAbsent(item.guid, item);
		return item.guid;
	}

	@Override
	public void writeFile(String token, InputStream is) throws IOException {
		UploadItem item = uploadTokens.remove(token);
		if (item == null)
			throw new DOMException("upload-token-not-found");

		String orgDomain = item.token.getOrgDomain();
		File temp = File.createTempFile("tmp-", null, getBaseDirectory(orgDomain));
		if (temp.exists())
			temp.delete();

		OutputStream os = null;
		long totalSize = 0;
		try {
			os = new FileOutputStream(temp);

			byte[] buf = new byte[8096];
			while (true) {
				int l = is.read(buf);
				if (l < 0)
					break;
				totalSize += l;
				os.write(buf, 0, l);
			}
		} catch (IOException e) {
			throw new DOMException("upload-failed");
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
			}
		}

		String guid = null;
		if (totalSize == item.token.getFileSize()) {
			UploadedFile uploaded = new UploadedFile();
			uploaded.setGuid(item.token.getFileGuid());
			File newFile = null;
			guid = uploaded.getGuid();
			if (item.token.getSpaceGuid() != null) {
				FileSpace space = getFileSpace(orgDomain, item.token.getSpaceGuid());
				uploaded.setSpace(space);
				File spaceDir = new File(getBaseDirectory(orgDomain), space.getGuid());
				spaceDir.mkdirs();
				newFile = new File(spaceDir, guid);
			} else
				newFile = new File(getBaseDirectory(orgDomain), guid);

			if (newFile.exists())
				newFile.delete();
			logger.trace("kraken dom: rename from [{}] to [{}]", temp.getAbsolutePath(), newFile.getAbsolutePath());
			if (!temp.renameTo(newFile))
				throw new DOMException("rename-uploaded-file-failed");

			uploaded.setOwner(userApi.findUser(orgDomain, item.token.getLoginName()));
			uploaded.setFileName(item.token.getFileName());
			uploaded.setFileSize(item.token.getFileSize());
			uploaded.setPath(newFile.getAbsolutePath());
			cfg.add(orgDomain, UploadedFile.class, getPred(guid), uploaded, FILE_ALREADY_EXIST, fileEventProvider);
		} else {
			temp.delete();
		}

		if (item.callback != null)
			item.callback.onUploadFile(item.token, guid);
	}

	private static class UploadItem {
		public UploadToken token;
		public String guid = UUID.randomUUID().toString();
		public UploadCallback callback;

		public UploadItem(UploadToken token, UploadCallback callback) {
			this.token = token;
			this.callback = callback;
		}
	}

	@Override
	public String setDownloadToken(Session session) {
		userApi.getUser(session.getOrgDomain(), session.getAdminLoginName());
		DownloadToken token = new DownloadToken(session);
		String old = sessionDownloadTokens.putIfAbsent(session.getGuid(), token.guid);
		if (old != null)
			return old;

		downloadTokens.put(token.guid, token);
		return token.guid;
	}

	@Override
	public UploadedFile getFileMetadataWithToken(String tokenGuid, String fileGuid) {
		DownloadToken token = downloadTokens.get(tokenGuid);
		if (token == null)
			throw new DOMException("download-token-not-found");

		return getFileMetadata(token.session.getOrgDomain(), fileGuid);
	}

	@Override
	public UploadedFile getFileMetadata(String domain, String fileGuid) {
		return cfg.get(domain, file, getPred(fileGuid), FILE_NOT_FOUND);
	}

	@Override
	public void removeDownloadToken(Session session) {
		String token = sessionDownloadTokens.remove(session.getGuid());
		if (token != null)
			downloadTokens.remove(token);

	}

	private class DownloadToken {
		private String guid = UUID.randomUUID().toString();
		private Session session;

		public DownloadToken(Session session) {
			this.session = session;
		}
	}

	@Override
	public void deleteFiles(String domain, Collection<String> guids) {
		List<UploadedFile> uploadeds = new ArrayList<UploadedFile>();
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids) {
			uploadeds.add(cfg.get(domain, UploadedFile.class, getPred(guid), FILE_NOT_FOUND));
			preds.add(getPred(guid));
		}
		cfg.removes(domain, file, preds, FILE_NOT_FOUND, fileEventProvider);
	}

	@Override
	public void deleteFile(String domain, String guid) {
		deleteFiles(domain, Arrays.asList(guid));
	}

	@Override
	public void deleteFiles(String domain, String loginName, Collection<String> guids) {
		List<UploadedFile> uploadeds = new ArrayList<UploadedFile>();
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids) {
			UploadedFile uploaded = cfg.get(domain, UploadedFile.class, getPred(guid), FILE_NOT_FOUND);
			if (!loginName.equals(uploaded.getOwner().getLoginName())) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("guid", uploaded.getGuid());
				throw new DOMException("file-delete-permission-denied", params);
			}
			uploadeds.add(uploaded);
			preds.add(getPred(guid));
		}
		cfg.removes(domain, file, preds, FILE_NOT_FOUND, fileEventProvider);
	}

	@Override
	public void deleteFile(String domain, String loginName, String guid) {
		deleteFiles(domain, loginName, Arrays.asList(guid));
	}

	@Override
	public EntityEventProvider<UploadedFile> getUploadedFileEventProvider() {
		return fileEventProvider;
	}
}
