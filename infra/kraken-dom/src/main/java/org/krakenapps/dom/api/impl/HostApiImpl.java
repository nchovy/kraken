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
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventListener;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.EntityEventProvider;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.Transaction;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.dom.model.Vendor;

@Component(name = "dom-host-api")
@Provides
public class HostApiImpl extends DefaultEntityEventProvider<Host> implements HostApi {
	private static final Class<Host> host = Host.class;
	private static final String HOST_NOT_FOUND = "host-not-found";
	private static final String HOST_ALREADY_EXIST = "host-already-exist";

	private static final Class<HostType> type = HostType.class;
	private static final String TYPE_NOT_FOUND = "host-type-not-found";
	private static final String TYPE_ALREADY_EXIST = "host-type-already-exist";
	private DefaultEntityEventProvider<HostType> typeEventProvider = new DefaultEntityEventProvider<HostType>();

	private static final Class<HostExtension> ext = HostExtension.class;
	private static final String EXT_NOT_FOUND = "host-extension-not-found";
	private static final String EXT_ALREADY_EXIST = "host-extension-already-exist";
	private DefaultEntityEventProvider<HostExtension> extEventProvider = new DefaultEntityEventProvider<HostExtension>();

	private EntityEventListener<Area> areaEventListener = new DefaultEntityEventListener<Area>() {
		@Override
		public void entityRemoving(String domain, Area area, ConfigTransaction xact, Object state) {
			boolean remove = (state != null) && (state instanceof Boolean) && ((Boolean) state);

			List<Host> hosts = new ArrayList<Host>(getHosts(domain, area.getGuid(), false));
			Transaction x = Transaction.getInstance(xact);
			if (remove) {
				cfg.removes(x, domain, host, getPreds(hosts), null, HostApiImpl.this, state, null);
			} else {
				for (Host host : hosts)
					host.setArea(null);
				cfg.updates(x, host, getPreds(hosts), hosts, null, state);
			}
		}
	};
	private EntityEventListener<Vendor> vendorEventListener = new DefaultEntityEventListener<Vendor>() {
		@Override
		public void entityRemoving(String domain, Vendor obj, ConfigTransaction xact, Object state) {
			List<HostType> types = new ArrayList<HostType>(cfg.all(domain, type,
					Predicates.field("vendor/guid", obj.getGuid())));
			for (HostType type : types)
				type.setVendor(null);
			Transaction x = Transaction.getInstance(xact);
			cfg.updates(x, type, getPreds(types), types, null, state);
		}
	};
	private EntityEventListener<HostType> typeEventListener = new DefaultEntityEventListener<HostType>() {
		@Override
		public void entityRemoving(String domain, HostType obj, ConfigTransaction xact, Object state) {
			List<Host> hosts = new ArrayList<Host>(cfg.all(domain, host, Predicates.field("type/guid", obj.getGuid())));
			Transaction x = Transaction.getInstance(xact);
			cfg.removes(x, domain, host, getPreds(hosts), null, HostApiImpl.this, state, null);
		}
	};

	@Requires
	private ConfigManager cfg;

	@Requires
	private AreaApi areaApi;

	@Requires
	private ApplicationApi appApi;

	public void setConfigManager(ConfigManager cfg) {
		this.cfg = cfg;
	}

	public void setAreaApi(AreaApi areaApi) {
		this.areaApi = areaApi;
	}

	public void setApplicationApi(ApplicationApi appApi) {
		this.appApi = appApi;
	}

	@Validate
	public void validate() {
		areaApi.addEntityEventListener(areaEventListener);
		appApi.getVendorEventProvider().addEntityEventListener(vendorEventListener);
		typeEventProvider.addEntityEventListener(typeEventListener);
	}

	@Invalidate
	public void invalidate() {
		if (areaApi != null)
			areaApi.removeEntityEventListener(areaEventListener);
		if (appApi != null)
			appApi.getVendorEventProvider().removeEntityEventListener(vendorEventListener);
		typeEventProvider.removeEntityEventListener(typeEventListener);
	}

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private Predicate getExtPred(String type) {
		return Predicates.field("type", type);
	}

	private List<Predicate> getPreds(List<? extends Object> objs) {
		if (objs == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(objs.size());
		for (Object obj : objs) {
			if (obj instanceof Host)
				preds.add(getPred(((Host) obj).getGuid()));
			else if (obj instanceof HostType)
				preds.add(getPred(((HostType) obj).getGuid()));
			else if (obj instanceof HostExtension)
				preds.add(getExtPred(((HostExtension) obj).getType()));
		}
		return preds;
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
	public Collection<Host> findHosts(String domain, Collection<String> guids) {
		// TODO Auto-generated method stub
		List<String> guidList = new ArrayList<String>(guids);
		
		Predicate[] preds = new Predicate[guidList.size()];
		int i = 0;
		for (String guid : guidList)
			preds[i++] = Predicates.field("guid", guid);
		Predicate pred = Predicates.or(preds);
		
		return cfg.all(domain, host, pred);
	}

	@Override
	public Host getHost(String domain, String guid) {
		return cfg.get(domain, host, getPred(guid), HOST_NOT_FOUND);
	}

	@Override
	public void createHosts(String domain, Collection<Host> hosts) {
		List<Host> hostList = new ArrayList<Host>(hosts);
		cfg.adds(domain, host, getPreds(hostList), hostList, HOST_ALREADY_EXIST, this);
	}

	@Override
	public void createHost(String domain, Host host) {
		host.setExtensions(host.getType().getDefaultExtensions());
		cfg.add(domain, HostApiImpl.host, getPred(host.getGuid()), host, HOST_ALREADY_EXIST, this);
	}

	@Override
	public void updateHosts(String domain, Collection<Host> hosts) {
		List<String> hostGuids = new ArrayList<String>(hosts.size());
		List<Host> hostList = new ArrayList<Host>(hosts);
		for (Host host : hostList) {
			host.setUpdated(new Date());
			hostGuids.add(host.getGuid());
		}

		cfg.updateForGuids(domain, Host.class, hostGuids, hostList, HOST_NOT_FOUND, this);
	}

	@Override
	public void updateHost(String domain, Host host) {
		host.setUpdated(new Date());
		cfg.update(domain, HostApiImpl.host, getPred(host.getGuid()), host, HOST_NOT_FOUND, this);
	}

	@Override
	public void removeHosts(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, host, preds, HOST_NOT_FOUND, this);
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
	public void createHostTypes(String domain, Collection<HostType> hostTypes) {
		List<HostType> hostTypeList = new ArrayList<HostType>(hostTypes);
		cfg.adds(domain, type, getPreds(hostTypeList), hostTypeList, TYPE_ALREADY_EXIST, typeEventProvider);
	}

	@Override
	public void createHostType(String domain, HostType hostType) {
		cfg.add(domain, type, getPred(hostType.getGuid()), hostType, TYPE_ALREADY_EXIST, typeEventProvider);
	}

	@Override
	public void updateHostTypes(String domain, Collection<HostType> hostTypes) {
		List<HostType> hostTypeList = new ArrayList<HostType>(hostTypes);
		cfg.updates(domain, type, getPreds(hostTypeList), hostTypeList, TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public void updateHostType(String domain, HostType hostType) {
		cfg.update(domain, type, getPred(hostType.getGuid()), hostType, TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public void removeHostTypes(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, type, preds, TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public void removeHostType(String domain, String guid) {
		cfg.remove(domain, type, getPred(guid), TYPE_NOT_FOUND, typeEventProvider);
	}

	@Override
	public EntityEventProvider<HostType> getHostTypeEventProvider() {
		return typeEventProvider;
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
	public void createHostExtensions(String domain, Collection<HostExtension> extensions) {
		List<HostExtension> extensionList = new ArrayList<HostExtension>(extensions);
		cfg.adds(domain, ext, getPreds(extensionList), extensionList, EXT_ALREADY_EXIST, extEventProvider);
	}

	@Override
	public void createHostExtension(String domain, HostExtension extension) {
		cfg.add(domain, ext, getExtPred(extension.getType()), extension, EXT_ALREADY_EXIST, extEventProvider);
	}

	@Override
	public void updateHostExtensions(String domain, Collection<HostExtension> extensions) {
		List<HostExtension> extensionList = new ArrayList<HostExtension>(extensions);
		cfg.updates(domain, ext, getPreds(extensionList), extensionList, EXT_NOT_FOUND, extEventProvider);
	}

	@Override
	public void updateHostExtension(String domain, HostExtension extension) {
		cfg.update(domain, ext, getExtPred(extension.getType()), extension, EXT_NOT_FOUND, extEventProvider);
	}

	@Override
	public void removeHostExtensions(String domain, Collection<String> types) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String type : types)
			preds.add(getExtPred(type));
		cfg.removes(domain, ext, preds, EXT_NOT_FOUND, extEventProvider);
	}

	@Override
	public void removeHostExtension(String domain, String type) {
		cfg.remove(domain, ext, getExtPred(type), EXT_NOT_FOUND, extEventProvider);
	}

	@Override
	public EntityEventProvider<HostExtension> getHostExtensionEventProvider() {
		return extEventProvider;
	}
}
