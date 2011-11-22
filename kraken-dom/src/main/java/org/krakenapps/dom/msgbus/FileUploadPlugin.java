package org.krakenapps.dom.msgbus;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.UploadToken;
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
	private FileUploadApi fileUploadApi;

	// session to download token mapping
	private ConcurrentMap<Integer, String> tokens = new ConcurrentHashMap<Integer, String>();

	@Invalidate
	public void invalidate() {
		for (String token : tokens.values())
			fileUploadApi.removeDownloadToken(token);
		tokens.clear();
	}

	@MsgbusMethod
	public void getFileSpaces(Request req, Response resp) {
		Collection<FileSpace> spaces = fileUploadApi.getFileSpaces(req.getOrgDomain());
		resp.put("spaces", PrimitiveConverter.serialize(spaces));
	}

	@MsgbusMethod
	public void getFileSpace(Request req, Response resp) {
		String guid = req.getString("guid");
		FileSpace space = fileUploadApi.getFileSpace(req.getOrgDomain(), guid);
		resp.put("space", PrimitiveConverter.serialize(space));
	}

	@MsgbusMethod
	public void createFileSpace(Request req, Response resp) {
		FileSpace space = PrimitiveConverter.parse(FileSpace.class, req.getParams());
		fileUploadApi.createFileSpace(req.getOrgDomain(), space);
		resp.put("guid", space.getGuid());
	}

	@MsgbusMethod
	public void updateFileSpace(Request req, Response resp) {
		FileSpace space = PrimitiveConverter.parse(FileSpace.class, req.getParams());
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
		logger.info(template, new Object[] { req.getSession().getId(), token, spaceGuid, fileName, fileSize });

		resp.put("token", token);
	}

	@MsgbusMethod
	public void issueDownloadToken(Request req, Response resp) {
		String token = fileUploadApi.setDownloadToken(req.getSession());
		tokens.putIfAbsent(req.getSession().getId(), token);
		resp.put("token", token);
	}

	@MsgbusMethod
	public void deleteFile(Request req, Response resp) {
		String guid = req.getString("guid");
		fileUploadApi.deleteFile(req.getOrgDomain(), req.getAdminLoginName(), guid);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void removeDownloadTokens(Session session) {
		int sessionId = session.getId();
		logger.trace("kraken dom: clearing download token for session {}", sessionId);
		String token = tokens.remove(sessionId);
		if (token != null)
			fileUploadApi.removeDownloadToken(token);
	}
}
