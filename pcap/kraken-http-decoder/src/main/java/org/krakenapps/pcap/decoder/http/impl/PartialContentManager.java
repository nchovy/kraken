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
package org.krakenapps.pcap.decoder.http.impl;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.http.HttpDecoder;

/**
 * @author mindori
 */
public class PartialContentManager {
	private Map<String, HoleDescriptorManager> mpFileMap;

	public PartialContentManager() {
		mpFileMap = new HashMap<String, HoleDescriptorManager>();
	}

	public void handleMultipartData(HttpDecoder http, int first, int last, String totalSize, String url, byte[] data) {
		String mpid = getMPID(url, totalSize);
		HoleDescriptorManager manager;

		/* Classify exist multipart data / new multipart data */
		if (mpFileMap.containsKey(mpid))
			manager = mpFileMap.get(mpid);

		else {
			/* Create new HoleDescriptorManager */
			manager = new HoleDescriptorManager();
			mpFileMap.put(mpid, manager);
		}
		/* Create new hole */
		HoleDescriptor newHole = new HoleDescriptor(first, last, data);
		manager.addHole(http, newHole);
	}

	private String getMPID(String url, String totalSize) {
		/* MPID: Multipart data ID */
		return (url + totalSize);
	}
}
