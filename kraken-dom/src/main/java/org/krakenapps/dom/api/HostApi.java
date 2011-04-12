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

import java.util.List;
import java.util.Set;

import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;

public interface HostApi extends EntityEventProvider<Host> {
	HostExtension getHostExtension(String className);

	List<Host> getAllHosts();

	List<Host> getHosts(int organizationId);

	List<HostType> getHostTypes();

	List<HostType> getSentrySupportedHostTypes();

	Host getHost(String guid);

	Host getHost(int organizationId, int hostId);

	List<Host> getHosts(int organizationId, int areaId);

	List<Host> getHostsRecursively(int organizationId, int rootAreaId);

	int createHost(int organizationId, int hostTypeId, int areaId, String name, String description);

	void updateHost(int organizationId, int hostId, String name, String description);

	void updateHostGuid(int organizationId, int hostId, String guid);

	void removeHost(int organizationId, int hostId);

	void moveHost(int organizationId, int hostId, int areaId);

	void mapHostExtensions(int organizationId, int hostId, Set<String> hostExtensionNames);

	void unmapHostExtensions(int organizationId, int hostId, Set<String> hostExtensionNames);
}
