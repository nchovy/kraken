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

public interface UploadToken {
	/**
	 * It should be universally unique identifier.
	 * 
	 * @return the unique token
	 */
	String getToken();

	/**
	 * The id of logically separated upload space.
	 * 
	 * @return the space id
	 */
	String getSpaceId();

	/**
	 * Download file name. Upload file name may not same with this file name.
	 * 
	 * @return the file name
	 */
	String getFileName();

	/**
	 * Upload file size. When upload session is terminated, upload service
	 * checks if received size and notified size is same. If not, uploaded file
	 * will be discarded.
	 * 
	 * @return
	 */
	long getFileSize();
}
