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
package org.krakenapps.dom.api;

import java.util.Collection;

import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;

public interface HostApi extends EntityEventProvider<Host> {
	Collection<Host> getHosts(String domain);

	Collection<Host> getHosts(String domain, String areaGuid, boolean includeChildren);

	Host findHost(String domain, String guid);

	Host getHost(String domain, String guid);

	void createHost(String domain, Host host);

	void updateHost(String domain, Host host);

	void removeHost(String domain, String guid);

	Collection<HostType> getHostTypes(String domain);

	HostType findHostType(String domain, String guid);

	HostType getHostType(String domain, String guid);

	void createHostType(String domain, HostType hostType);

	void updateHostType(String domain, HostType hostType);

	void removeHostType(String domain, String guid);

	Collection<HostExtension> getHostExtensions(String domain);

	HostExtension findHostExtension(String domain, String guid);

	HostExtension getHostExtension(String domain, String guid);

	void createHostExtension(String domain, HostExtension extension);

	void updateHostExtension(String domain, HostExtension extension);

	void removeHostExtension(String domain, String className);
}
