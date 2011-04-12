/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.http.msgbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.http.FileUploadService;
import org.krakenapps.http.UploadToken;
import org.krakenapps.http.UploadedFile;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "file-upload-plugin")
@MsgbusPlugin
public class FileUploadPlugin {
	private final Logger logger = LoggerFactory.getLogger(FileUploadPlugin.class.getName());
	
	@Requires
	private FileUploadService upload;
	
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	// session to download token mapping
	private Map<Integer, String> tokens;

	public FileUploadPlugin() {
		tokens = new ConcurrentHashMap<Integer, String>();
	}

	public void invalidate() {
		for (String token : tokens.values()) {
			upload.removeDownloadToken(token);
		}

		tokens.clear();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@MsgbusMethod
	public void getFileSpaces(Request req, Response resp) {
		EntityManager em = entityManagerService.getEntityManager();
		List<FileSpace> spaces = em.createQuery("FROM FileSpace s WHERE s.organization.id = ?").setParameter(1,
				req.getOrgId()).getResultList();
		resp.put("spaces", Marshaler.marshal(spaces));
	}

	@Transactional
	@MsgbusMethod
	public void getFileSpace(Request req, Response resp) {
		int id = req.getInteger("id");
		EntityManager em = entityManagerService.getEntityManager();
		FileSpace space = em.find(FileSpace.class, id);
		if (space == null || space.getOrganization().getId() != req.getOrgId())
			throw new FileSpaceNotFoundException();

		final Collection<UploadedFile> files = upload.getFiles(getSpaceId(space.getId()));
		resp.put("files", convert(files));
	}

	private List<Object> convert(Collection<UploadedFile> files) {
		List<Object> l = new ArrayList<Object>(files.size());
		for (UploadedFile file : files) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("resource_id", file.getResourceId());
			m.put("file_name", file.getFileName());
			m.put("file_size", file.getFileSize());

			l.add(m);
		}

		return l;
	}

	@Transactional
	@MsgbusMethod
	public void createFileSpace(Request req, Response resp) {
		EntityManager em = entityManagerService.getEntityManager();
		String name = req.getString("name");

		// basic check (you should add multicolumn unique index to file space.
		long count = (Long) em.createQuery(
				"SELECT COUNT(*) FROM FileSpace s WHERE s.organization.id = ? AND s.name = ?").setParameter(1,
				req.getOrgId()).setParameter(2, name).getSingleResult();

		if (count > 0)
			throw new DuplicatedFileSpaceNameException();

		// create
		Organization org = em.find(Organization.class, req.getOrgId());
		FileSpace space = new FileSpace();
		space.setOrganization(org);
		space.setName(name);
		space.setCreateDateTime(new Date());
		em.persist(space);

		resp.put("id", space.getId());
	}

	@Transactional
	@MsgbusMethod
	public void removeFileSpace(Request req, Response resp) {
		EntityManager em = entityManagerService.getEntityManager();
		int id = req.getInteger("id");
		FileSpace space = em.find(FileSpace.class, id);
		if (space.getOrganization().getId() != req.getOrgId())
			throw new SecurityException();

		em.remove(space);
	}

	private String getSpaceId(int spaceId) {
		return "wc" + Integer.toString(spaceId);
	}

	@MsgbusMethod
	public void setUploadToken(Request req, Response resp) {
		String token = UUID.randomUUID().toString();
		int spaceId = req.getInteger("id");
		String fileName = req.getString("file_name");
		long fileSize = req.getInteger("file_size");

		final String template = "watchcat file upload plugin: set upload info, session [{}], token [{}], space [{}], filename [{}], size [{}]";
		logger.info(template, new Object[] { req.getSession().getId(), token, spaceId, fileName, fileSize });

		UploadToken uploadToken = new UploadTokenImpl(token, getSpaceId(spaceId), fileName, fileSize);
		int fileId = upload.setUploadToken(uploadToken, null);
		resp.put("file_id", fileId);
		resp.put("file_name", fileName);
		resp.put("token", token);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@MsgbusMethod
	public void issueDownloadToken(Request req, Response resp) {
		int sessionId = req.getSession().getId();
		String token = null;
		if (tokens.containsKey(sessionId)) {
			token = tokens.get(sessionId);
		} else {
			token = UUID.randomUUID().toString();
		}

		EntityManager em = entityManagerService.getEntityManager();
		List<FileSpace> spaces = em.createQuery("FROM FileSpace s WHERE s.organization.id = ?").setParameter(1,
				req.getOrgId()).getResultList();

		List<String> allowed = new ArrayList<String>();
		for (FileSpace space : spaces) {
			allowed.add(getSpaceId(space.getId()));
		}

		upload.setDownloadToken(token, allowed);

		resp.put("token", token);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void removeDownloadTokens(Session session) {
		int sessionId = session.getId();
		logger.info("watchcat file upload plugin: clearing download token for session {}", sessionId);

		String token = tokens.remove(sessionId);
		if (token != null)
			upload.removeDownloadToken(token);
	}

	@Transactional
	@MsgbusMethod
	public void deleteFile(Request req, Response resp) {
		EntityManager em = entityManagerService.getEntityManager();

		int spaceId = req.getInteger("id");
		int fileId = req.getInteger("file_id");

		FileSpace fileSpace = em.find(FileSpace.class, spaceId);
		if (fileSpace.getOrganization().getId() != req.getOrgId())
			throw new SecurityException(String.format("cannot delete other's file: space %d file %d", spaceId, fileId));

		String spaceName = "wc" + spaceId;

		try {
			upload.deleteFile(spaceName, fileId);
		} catch (Exception e) {
			logger.error("watchcat file upload plugin: failed to delete file", e);
		}
	}

	private static class UploadTokenImpl implements UploadToken {
		private String token;
		private String spaceId;
		private String fileName;
		private long fileSize;

		public UploadTokenImpl(String token, String spaceId, String fileName, long fileSize) {
			this.token = token;
			this.spaceId = spaceId;
			this.fileName = fileName;
			this.fileSize = fileSize;
		}

		@Override
		public String getFileName() {
			return fileName;
		}

		@Override
		public long getFileSize() {
			return fileSize;
		}

		@Override
		public String getSpaceId() {
			return spaceId;
		}

		@Override
		public String getToken() {
			return token;
		}

	}
}
