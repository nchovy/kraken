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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.UploadCallback;
import org.krakenapps.dom.api.UploadToken;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.msgbus.Session;

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
	private static DefaultEntityEventProvider<UploadedFile> fileEventProvider = new DefaultEntityEventProvider<UploadedFile>();

	private ConcurrentMap<String, UploadItem> uploadTokens = new ConcurrentHashMap<String, FileUploadApiImpl.UploadItem>();
	// private ConcurrentMap<String, String> downloadTokens = new
	// ConcurrentHashMap<String, String>(); // token-loginName
	private ConcurrentMap<Integer, DownloadToken> downloadTokens = new ConcurrentHashMap<Integer, DownloadToken>(); // session-token

	@Requires
	private ConfigManager cfg;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private UserApi userApi;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	@Override
	public File getBaseDirectory(String domain) {
		String dir = orgApi.getOrganizationParameter(domain, FSP_BASE_DIR_KEY, String.class);
		if (dir == null)
			dir = new File(System.getProperty("kraken.data.dir"), "kraken-http/upload/" + domain).getAbsolutePath();
		return new File(dir);
	}

	@Override
	public void setBaseDirectory(String domain, File dir) {
		orgApi.setOrganizationParameter(domain, FSP_BASE_DIR_KEY, dir.getAbsoluteFile());
	}

	@Override
	public Collection<FileSpace> getFileSpaces(String domain) {
		return cfg.ensureCollection(domain, fsp).findAll().getDocuments(fsp);
	}

	@Override
	public FileSpace findFileSpace(String domain, String guid) {
		return cfg.find(domain, fsp, getPred(guid));
	}

	@Override
	public FileSpace getFileSpace(String domain, String guid) {
		return cfg.get(domain, fsp, getPred(guid), FSP_NOT_FOUND);
	}

	@Override
	public void createFileSpace(String domain, FileSpace space) {
		cfg.add(domain, fsp, getPred(space.getGuid()), space, FSP_ALREADY_EXIST, this);
	}

	@Override
	public void updateFileSpace(String domain, String loginName, FileSpace space) {
		checkPermissionLevel(domain, loginName, space.getGuid(), "update-file-space-permission-denied");
		cfg.update(domain, fsp, getPred(space.getGuid()), space, FSP_NOT_FOUND, this);
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

		OutputStream os = null;
		try {
			String orgDomain = item.token.getOrgDomain();
			File temp = File.createTempFile("tmp-", null, getBaseDirectory(orgDomain));
			if (temp.exists())
				temp.delete();
			os = new FileOutputStream(temp);

			byte[] buf = new byte[8096];
			long totalSize = 0;
			while (true) {
				int l = is.read(buf);
				if (l < 0)
					break;
				totalSize += l;
				os.write(buf, 0, l);
			}

			String guid = null;
			if (totalSize == item.token.getFileSize()) {
				File newFile = new File(getBaseDirectory(orgDomain), "");

				UploadedFile uploaded = new UploadedFile();
				guid = uploaded.getGuid();
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
		} finally {
			if (os != null)
				os.close();
		}
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
		if (downloadTokens.containsKey(session.getId()))
			return downloadTokens.get(session.getId()).guid;

		userApi.getUser(session.getOrgDomain(), session.getAdminLoginName());
		DownloadToken token = new DownloadToken(session.getOrgDomain(), session.getAdminLoginName());
		downloadTokens.putIfAbsent(session.getId(), token);
		return token.guid;
	}

	@Override
	public UploadedFile getFileMetadata(String domain, String tokenGuid) {
		DownloadToken token = downloadTokens.get(tokenGuid);
		if (token == null)
			throw new DOMException("download-token-not-found");

		UploadedFile uploaded = cfg.get(domain, file, getPred(tokenGuid), FILE_NOT_FOUND);
		if (!token.loginName.equals(uploaded.getOwner().getLoginName())) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("guid", uploaded.getGuid());
			params.put("owner", uploaded.getOwner().getLoginName());
			throw new DOMException("read-access-denied", params);
		}
		return uploaded;
	}

	@Override
	public void removeDownloadToken(String token) {
		downloadTokens.remove(token);
	}

	private class DownloadToken {
		private String guid = UUID.randomUUID().toString();
		private String orgDomain;
		private String loginName;

		public DownloadToken(String orgDomain, String loginName) {
			this.orgDomain = orgDomain;
			this.loginName = loginName;
		}
	}

	@Override
	public void deleteFile(String domain, String guid) {
		UploadedFile uploaded = cfg.get(domain, UploadedFile.class, getPred(guid), FILE_NOT_FOUND);
		new File(uploaded.getPath()).delete();
		cfg.remove(domain, file, getPred(guid), FILE_NOT_FOUND, fileEventProvider);
	}

	@Override
	public void deleteFile(String domain, String loginName, String guid) {
		UploadedFile file = cfg.get(domain, UploadedFile.class, getPred(guid), FILE_NOT_FOUND);
		if (!loginName.equals(file.getOwner().getLoginName())) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("guid", file.getGuid());
			throw new DOMException("file-delete-permission-denied", params);
		}
		deleteFile(domain, guid);
	}
}
