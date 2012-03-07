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

import java.util.UUID;

import org.krakenapps.msgbus.Session;

public class UploadToken {
	private String orgDomain;
	private String loginName;
	private String spaceGuid;
	private String fileGuid;
	private String fileName;
	private long fileSize;
	private Object userData;

	public UploadToken(Session session, String spaceGuid, String fileName, long fileSize) {
		this.orgDomain = session.getOrgDomain();
		this.loginName = session.getAdminLoginName();
		this.spaceGuid = spaceGuid;
		this.fileGuid = UUID.randomUUID().toString();
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public String getOrgDomain() {
		return orgDomain;
	}

	/**
	 * The user login name
	 * 
	 * @return the user login name
	 */
	public String getLoginName() {
		return loginName;
	}

	/**
	 * The id of logically separated upload space.
	 * 
	 * @return the space guid
	 */
	public String getSpaceGuid() {
		return spaceGuid;
	}

	public String getFileGuid() {
		return fileGuid;
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
