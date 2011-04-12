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
package org.krakenapps.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface FileUploadService {
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
	 * Get all files in space.
	 * 
	 * @param spaceId
	 * @return the uploaded files
	 */
	Collection<UploadedFile> getFiles(String spaceId);

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
	 * @param spaces
	 *            the space id list of allowed spaces.
	 */
	void setDownloadToken(String token, Collection<String> spaces);

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
	 * Get file metadata using download token, space id and resource id.
	 * 
	 * @param token
	 *            the download token
	 * @param spaceId
	 *            the space id as you notified through upload token
	 * @param resourceId
	 *            the resource id that you obtained before using setUploadToken
	 * @return the uploaded file metadata
	 * @throws IOException
	 *             when file open failed
	 */
	UploadedFile getFile(String token, String spaceId, int resourceId) throws IOException;

	/**
	 * Delete uploaded file from space.
	 * 
	 * @param spaceId
	 *            the space id
	 * @param resourceId
	 *            the resource id
	 * 
	 */
	void deleteFile(String spaceId, int resourceId);
}
