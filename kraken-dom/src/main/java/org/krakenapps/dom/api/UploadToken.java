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

public class UploadToken {
	private String token;
	private int userId;
	private int spaceId;
	private String fileName;
	private long fileSize;
	private Object userData;

	public UploadToken(String token, int userId, int spaceId, String fileName, long fileSize) {
		this.token = token;
		this.userId = userId;
		this.spaceId = spaceId;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	/**
	 * It should be universally unique identifier.
	 * 
	 * @return the unique token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * The id of user id
	 * 
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * The id of logically separated upload space.
	 * 
	 * @return the space id
	 */
	public int getSpaceId() {
		return spaceId;
	}

	/**
	 * Download file name. Upload file name may not same with this file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Upload file size. When upload session is terminated, upload service
	 * checks if received size and notified size is same. If not, uploaded file
	 * will be discarded.
	 * 
	 * @return
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @return user data
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * Set user data
	 * 
	 * @param userData
	 *            the user data
	 */
	public void setUserData(Object userData) {
		this.userData = userData;
	}
}
