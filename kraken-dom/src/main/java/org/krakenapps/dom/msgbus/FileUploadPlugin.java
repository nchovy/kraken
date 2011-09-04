package org.krakenapps.dom.msgbus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.api.UploadToken;
import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.msgbus.Marshaler;
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
	private final Logger logger = LoggerFactory.getLogger(FileUploadPlugin.class.getName());

	@Requires
	private FileUploadApi upload;

	// session to download token mapping
	private Map<Integer, String> tokens;

	public FileUploadPlugin() {
		tokens = new ConcurrentHashMap<Integer, String>();
	}

	@Invalidate
	public void invalidate() {
		for (String token : tokens.values()) {
			upload.removeDownloadToken(token);
		}

		tokens.clear();
	}

	@MsgbusMethod
	public void getFileSpaces(Request req, Response resp) {
		List<FileSpace> spaces = upload.getFileSpaces(req.getOrgId());
		resp.put("spaces", Marshaler.marshal(spaces));
	}

	@MsgbusMethod
	public void getFileSpace(Request req, Response resp) {
		int spaceId = req.getInteger("id");
		Collection<UploadedFile> files = upload.getFiles(req.getOrgId(), spaceId);
		resp.put("files", Marshaler.marshal(files));
	}

	@MsgbusMethod
	public void createFileSpace(Request req, Response resp) {
		String spaceName = req.getString("name");
		int id = upload.createFileSpace(req.getOrgId(), req.getAdminId(), spaceName);
		resp.put("id", id);
	}

	@MsgbusMethod
	public void removeFileSpace(Request req, Response resp) {
		int spaceId = req.getInteger("id");
		upload.removeFileSpace(req.getAdminId(), spaceId);
	}

	@MsgbusMethod
	public void setUploadToken(Request req, Response resp) {
		String token = UUID.randomUUID().toString();
		Integer spaceId = req.getInteger("space_id");
		String fileName = req.getString("file_name");
		long fileSize = req.getInteger("file_size");

		final String template = "kraken dom: set upload info, session [{}], token [{}], space [{}], filename [{}], size [{}]";
		logger.info(template, new Object[] { req.getSession().getId(), token, spaceId, fileName, fileSize });

		UploadToken uploadToken = new UploadToken(token, req.getAdminId(), spaceId, fileName, fileSize);
		int fileId = upload.setUploadToken(uploadToken, null);
		resp.put("file_id", fileId);
		resp.put("file_name", fileName);
		resp.put("token", token);
	}

	@MsgbusMethod
	public void issueDownloadToken(Request req, Response resp) {
		int sessionId = req.getSession().getId();
		String token = getDownloadToken(sessionId);
		upload.setDownloadToken(token, req.getAdminId());

		resp.put("token", token);
	}

	private String getDownloadToken(int sessionId) {
		String token = null;
		if (tokens.containsKey(sessionId)) {
			token = tokens.get(sessionId);
		} else {
			token = UUID.randomUUID().toString();
		}
		return token;
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void removeDownloadTokens(Session session) {
		int sessionId = session.getId();
		logger.info("kraken dom: clearing download token for session {}", sessionId);

		String token = tokens.remove(sessionId);
		if (token != null)
			upload.removeDownloadToken(token);
	}

	@MsgbusMethod
	public void deleteFile(Request req, Response resp) {
		int fileId = req.getInteger("file_id");

		try {
			upload.deleteFile(req.getAdminId(), fileId);
		} catch (Exception e) {
			logger.error("kraken dom: failed to delete file", e);
		}
	}
}
