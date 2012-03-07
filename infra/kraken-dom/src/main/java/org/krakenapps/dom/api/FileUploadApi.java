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
package org.krakenapps.dom.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.msgbus.Session;

public interface FileUploadApi extends EntityEventProvider<FileSpace> {
	/**
	 * Return the base upload directory.
	 * 
	 * @return the directory file
	 */
	File getBaseDirectory(String domain);

	/**
	 * Set upload directory. Directory will be created automatically if not
	 * exists.
	 * 
	 * @param dir
	 *            the upload path.
	 */
	void setBaseDirectory(String domain, File dir);

	/**
	 * Get all file spaces in the organization
	 * 
	 * @param domain
	 *            the organization domain
	 * @return
	 */
	Collection<FileSpace> getFileSpaces(String domain);

	FileSpace findFileSpace(String domain, String guid);

	FileSpace getFileSpace(String domain, String guid);

	/**
	 * Create new file space
	 * 
	 * @param domain
	 *            the organization domain
	 * @param space
	 *            new file space
	 */
	void createFileSpaces(String domain, Collection<FileSpace> spaces);

	void createFileSpace(String domain, FileSpace space);

	void updateFileSpaces(String domain, String loginName, Collection<FileSpace> spaces);

	void updateFileSpace(String domain, String loginName, FileSpace space);

	/**
	 * Remove file space
	 * 
	 * @param domain
	 *            the organization domain
	 * @param loginName
	 *            the owner login name
	 * @param guid
	 *            the space guid
	 */
	void removeFileSpaces(String domain, String loginName, Collection<String> guids);

	void removeFileSpace(String domain, String loginName, String guid);

	/**
	 * Set upload token. It will be used only once, and discarded after upload
	 * is initiated.
	 * 
	 * @param token
	 *            the upload description
	 * @param callback
	 *            may be null; invoked when upload is completed (even if failed)
	 * @return the resource guid for new upload file
	 */
	String setUploadToken(UploadToken token, UploadCallback callback);

	/**
	 * Write file to preset location.
	 * 
	 * @param token
	 * @param is
	 *            the upload file stream
	 * @throws IOException
	 *             when temp file io failed
	 */
	void writeFile(String token, InputStream is) throws IOException;

	/**
	 * Set token for access-controlled download
	 * 
	 * @param session
	 *            TODO
	 * @param token
	 *            the download token
	 */
	String setDownloadToken(Session session);

	/**
	 * Get file metadata using download token, space id and resource id.
	 * 
	 * @param tokenGuid
	 *            the resource id that you obtained before using
	 *            setDownloadToken
	 * 
	 * @return the uploaded file metadata
	 * @throws IOException
	 *             when file open failed
	 */
	UploadedFile getFileMetadataWithToken(String tokenGuid, String fileGuid);

	UploadedFile getFileMetadata(String domain, String fileGuid);

	/**
	 * Remove download token.
	 * 
	 * @param token
	 *            the download token
	 */
	void removeDownloadToken(Session session);

	/**
	 * Delete uploaded file from space.
	 * 
	 * @param guid
	 *            the resource guid
	 */
	void deleteFiles(String domain, Collection<String> guids);

	void deleteFile(String domain, String guid);

	void deleteFiles(String domain, String loginName, Collection<String> guids);

	void deleteFile(String domain, String loginName, String guid);
	
	EntityEventProvider<UploadedFile> getUploadedFileEventProvider();
}
