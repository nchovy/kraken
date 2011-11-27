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
package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;

@Component(name = "dom-host-api")
@Provides
public class HostApiImpl extends DefaultEntityEventProvider<Host> implements HostApi {
	private static final Class<Host> host = Host.class;
	private static final String HOST_NOT_FOUND = "host-not-found";
	private static final String HOST_ALREADY_EXIST = "host-already-exist";

	private static final Class<HostType> type = HostType.class;
	private static final String TYPE_NOT_FOUND = "host-type-not-found";
	private static final String TYPE_ALREADY_EXIST = "host-type-already-exist";
	private static DefaultEntityEventProvider<HostType> typeEventProvider = new DefaultEntityEventProvider<HostType>();

	private static final Class<HostExtension> ext = HostExtension.class;
	private static final String EXT_NOT_FOUND = "host-extension-not-found";
	private static final String EXT_ALREADY_EXIST = "host-extension-already-exist";
	private static DefaultEntityEventProvider<HostExtension> extEventProvider = new DefaultEntityEventProvider<HostExtension>();

	@Requires
	private ConfigManager cfg;

	@Requires
	private AreaApi areaApi;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private Predicate getExtPred(String className) {
		return Predicates.field("className", className);
	}

	@Override
	public Collection<Host> getHosts(String domain) {
		return cfg.all(domain, host);
	}

	@Override
	public Collection<Host> getHosts(String domain, String areaGuid, boolean includeChildren) {
		Collection<Host> hosts = new ArrayList<Host>();
		hosts.addAll(cfg.all(domain, host, Predicates.field("area/guid", areaGuid)));
		if (includeChildren) {
			Area area = areaApi.getArea(domain, areaGuid);
			for (Area child : area.getChildren())
				hosts.addAll(getHosts(domain, child.getGuid(), includeChildren));
		}
		return hosts;
	}

	@Override
	public Host findHost(String domain, String guid) {
		return cfg.find(domain, host, getPred(guid));
	}

	@Override
	public Host getHost(String domain, String guid) {
		return cfg.get(domain, host, getPred(guid), HOST_NOT_FOUND);
	}

	@Override
	public void createHost(String domain, Host host) {
		host.setExtensions(host.getHostType().getExtensions());
		cfg.add(domain, HostApiImpl.host, getPred(host.getGuid()), host, HOST_ALREADY_EXIST, this);
	}

	@Override
	public void updateHost(String domain, Host host) {
		host.setUpdated(new Date());
		cfg.update(domain, HostApiImpl.host, getPred(host.getGuid()), host, HOST_NOT_FOUND, this);
	}

	@Override
	public void removeHost(String domain, String guid) {
		cfg.remove(domain, HostApiImpl.host, getPred(guid), HOST_NOT_FOUND, this);
	}

	@Override
	public Collection<HostType> getHostTypes(String domain) {
		return cfg.all(domain, type);
	}

	@Override
	public HostType findHostType(String domain, String guid) {
		return cfg.find(domain, type, getPred(guid));
	}

	@Override
	public HostType getHostType(String domain, String guid) {
		return cfg.get(domain, type, getPred(guid), TYPE_NOT_FOUND);
	}

	@Override
	public void createHostType(String domain, HostType hostType) {
		cfg.add(domain, type, getPred(hostType.getGuid()), hostType, TYPE_ALREADY_EXIST, typeEventProvider);
	}

	@Override
	public void updateHostType(String domain, HostType hostType) {
		cfg.update(domain, type, getPred(hostType.getGuid()), hostType, TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public void removeHostType(String domain, String guid) {
		cfg.remove(domain, type, getPred(guid), TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public Collection<HostExtension> getHostExtensions(String domain) {
		List<HostExtension> extensions = (List<HostExtension>) cfg.all(domain, ext);
		Collections.sort(extensions, new Comparator<HostExtension>() {
			@Override
			public int compare(HostExtension o1, HostExtension o2) {
				return o1.getOrd() - o2.getOrd();
			}
		});
		return extensions;
	}

	@Override
	public HostExtension findHostExtension(String domain, String guid) {
		return cfg.find(domain, ext, getPred(guid));
	}

	@Override
	public HostExtension getHostExtension(String domain, String guid) {
		return cfg.get(domain, ext, getPred(guid), EXT_NOT_FOUND);
	}

	@Override
	public void createHostExtension(String domain, HostExtension extension) {
		cfg.add(domain, ext, getExtPred(extension.getClassName()), extension, EXT_ALREADY_EXIST, extEventProvider);
	}

	@Override
	public void updateHostExtension(String domain, HostExtension extension) {
		cfg.update(domain, ext, getExtPred(extension.getClassName()), extension, EXT_NOT_FOUND, extEventProvider);
	}

	@Override
	public void removeHostExtension(String domain, String className) {
		cfg.remove(domain, ext, getExtPred(className), EXT_NOT_FOUND, extEventProvider);
	}
}
