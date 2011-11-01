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
import java.util.List;

import org.krakenapps.dom.model.FileSpace;
import org.krakenapps.dom.model.UploadedFile;

public interface FileUploadApi {
	/**
	 * Return the base upload directory.
	 * 
	 * @return the directory file
	 */
	File getBaseDirectory();

	/**
	 * Set upload directory. Directory will be created automatically if not
	 * exists.
	 * 
	 * @param dir
	 *            the upload path.
	 */
	void setBaseDirectory(File dir);

	/**
	 * Get all file spaces in the organization
	 * 
	 * @param orgId
	 *            the organization id
	 * @return
	 */
	List<FileSpace> getFileSpaces(int orgId);

	/**
	 * Create new file space
	 * 
	 * @param orgId
	 *            the organization id
	 * @param userId
	 *            the owner id
	 * @param spaceName
	 *            the space name
	 * @return the created instance id
	 */
	int createFileSpace(int orgId, int userId, String spaceName);

	/**
	 * Remove file space
	 * 
	 * @param userId
	 *            the user id
	 * @param spaceId
	 *            the space id
	 */
	void removeFileSpace(int userId, int spaceId);

	/**
	 * Get all files in space.
	 * 
	 * @param spaceName
	 * @return the uploaded files
	 */
	Collection<UploadedFile> getFiles(int orgId, int spaceId);

	/**
	 * Set upload token. It will be used only once, and discarded after upload
	 * is initiated.
	 * 
	 * @param token
	 *            the upload description
	 * @param callback
	 *            may be null; invoked when upload is completed (even if failed)
	 * @return the resource id for new upload file
	 */
	int setUploadToken(UploadToken token, UploadCallback callback);

	/**
	 * Set token for access-controlled download
	 * 
	 * @param token
	 *            the download token
	 */
	void setDownloadToken(String token, int userId);

	/**
	 * Remove download token.
	 * 
	 * @param token
	 *            the download token
	 */
	void removeDownloadToken(String token);

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
	 * Write file segment. Partial saving is crucial for heap space management.
	 * Entire file will be merged if all segments are received.
	 * 
	 * @param token
	 *            the upload token
	 * @param begin
	 *            begin byte offset
	 * @param end
	 *            end byte offset
	 * @param is
	 *            the input stream
	 * @throws IOException
	 *             when temp file io or merging io failed
	 */
	void writePartialFile(String token, long begin, long end, InputStream is) throws IOException;

	/**
	 * Get file metadata using download token, space id and resource id.
	 * 
	 * @param resourceId
	 *            the resource id that you obtained before using setUploadToken
	 * @param token
	 *            the download token
	 * @param spaceId
	 *            the space id as you notified through upload token
	 * 
	 * @return the uploaded file metadata
	 * @throws IOException
	 *             when file open failed
	 */
	UploadedFile getFileMetadata(int resourceId, String token) throws IOException;

	/**
	 * Get file metadata for internal use
	 * 
	 * @param resourceId
	 * @param token
	 * @return the file metadata
	 */
	UploadedFile getFileMetadata(int resourceId);

	/**
	 * Delete uploaded file from space.
	 * 
	 * @param spaceId
	 *            the space id
	 * @param resourceId
	 *            the resource id
	 * 
	 */
	void deleteFile(int userId, int resourceId);

	/**
	 * Delete uploaded file for internal use
	 * 
	 * @param resourceId
	 *            the resource id
	 */
	void deleteFile(int resourceId);
}
