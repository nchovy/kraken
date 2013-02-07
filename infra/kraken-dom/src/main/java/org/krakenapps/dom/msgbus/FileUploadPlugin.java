/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.dom.msgbus;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.api.PrimitiveConverter.SerializeOption;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.UploadToken;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-file-upload-plugin")
@MsgbusPlugin
public class FileUploadPlugin {
	private final Logger logger = LoggerFactory.getLogger(FileUploadPlugin.class);

	@Requires
	private ConfigManager conf;

	@Requires
	private FileUploadApi fileUploadApi;

	@Requires
	private UserApi userApi;

	@MsgbusMethod
	public void getFileSpaces(Request req, Response resp) {
		Collection<FileSpace> spaces = fileUploadApi.getFileSpaces(req.getOrgDomain());
		resp.put("spaces", PrimitiveConverter.serialize(spaces));
	}

	@MsgbusMethod
	public void getFileSpace(Request req, Response resp) {
		String guid = req.getString("guid");
		FileSpace space = fileUploadApi.getFileSpace(req.getOrgDomain(), guid);
		resp.put("space", PrimitiveConverter.serialize(space, SerializeOption.INCLUDE_SKIP_FIELD));
	}

	@MsgbusMethod
	public void createFileSpace(Request req, Response resp) {
		FileSpace space = (FileSpace) PrimitiveConverter.overwrite(new FileSpace(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		space.setOwner(userApi.getUser(req.getOrgDomain(), req.getAdminLoginName()));
		fileUploadApi.createFileSpace(req.getOrgDomain(), space);
		resp.put("guid", space.getGuid());
	}

	@MsgbusMethod
	public void updateFileSpace(Request req, Response resp) {
		FileSpace before = fileUploadApi.getFileSpace(req.getOrgDomain(), req.getString("guid"));
		FileSpace space = (FileSpace) PrimitiveConverter.overwrite(before, req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		fileUploadApi.updateFileSpace(req.getOrgDomain(), req.getAdminLoginName(), space);
	}

	@MsgbusMethod
	public void removeFileSpace(Request req, Response resp) {
		String guid = req.getString("guid");
		fileUploadApi.removeFileSpace(req.getOrgDomain(), req.getAdminLoginName(), guid);
	}

	@MsgbusMethod
	public void setUploadToken(Request req, Response resp) {
		String spaceGuid = req.getString("space_guid");
		String fileName = req.getString("file_name");
		long fileSize = req.getInteger("file_size");
		UploadToken uploadToken = new UploadToken(req.getSession(), spaceGuid, fileName, fileSize);
		String token = fileUploadApi.setUploadToken(uploadToken, null);

		final String template = "kraken dom: set upload info, session [{}], token [{}], space [{}], filename [{}], size [{}]";
		logger.info(template, new Object[] { req.getSession().getGuid(), token, spaceGuid, fileName, fileSize });

		resp.put("token", token);
		resp.put("file_guid", uploadToken.getFileGuid());
		resp.put("file_name", fileName);
	}

	@MsgbusMethod
	public void issueDownloadToken(Request req, Response resp) {
		String token = fileUploadApi.setDownloadToken(req.getSession());
		resp.put("token", token);
	}

	@MsgbusMethod
	public void deleteFile(Request req, Response resp) {
		String guid = req.getString("guid");
		fileUploadApi.deleteFile(req.getOrgDomain(), req.getAdminLoginName(), guid);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void deleteFiles(Request req, Response resp) {
		Collection<String> guids = (Collection<String>) req.get("guids");
		fileUploadApi.deleteFiles(req.getOrgDomain(), req.getAdminLoginName(), guids);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void removeDownloadTokens(Session session) {
		if (session == null)
			return;

		logger.trace("kraken dom: clearing download token for session {}", session.getGuid());
		if (fileUploadApi != null)
			fileUploadApi.removeDownloadToken(session);
	}
}
