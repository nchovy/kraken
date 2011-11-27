/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;
import org.krakenapps.confdb.CollectionName;

@CollectionName("host-type")
public class HostType {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@ReferenceKey("guid")
	private Vendor vendor;

	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(nullable = false, length = 60)
	private String version;

	private boolean isSentrySupported;

	@CollectionTypeHint(HostExtension.class)
	private List<HostExtension> defaultExtensions = new ArrayList<HostExtension>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isSentrySupported() {
		return isSentrySupported;
	}

	public void setSentrySupported(boolean isSentrySupported) {
		this.isSentrySupported = isSentrySupported;
	}

	public List<HostExtension> getDefaultExtensions() {
		return defaultExtensions;
	}

	public void setDefaultExtensions(List<HostExtension> defaultExtensions) {
		this.defaultExtensions = defaultExtensions;
	}

	@Override
	public String toString() {
		return "guid=" + guid + ", vendor=" + vendor.getName() + ", name=" + name + ", version=" + version;
	}
}
