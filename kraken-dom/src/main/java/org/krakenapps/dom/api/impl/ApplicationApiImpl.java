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
import java.util.Arrays;
import java.util.Collection;
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
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventListener;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.EntityEventProvider;
import org.krakenapps.dom.api.Transaction;
import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationGroup;
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;

@Component(name = "dom-app-api")
@Provides
public class ApplicationApiImpl extends DefaultEntityEventProvider<Application> implements ApplicationApi {
	private static final Class<Vendor> ven = Vendor.class;
	private static final String VEN_NOT_FOUND = "vendor-not-found";
	private static final String VEN_ALREADY_EXIST = "vendor-already-exist";
	private DefaultEntityEventProvider<Vendor> vendorEventProvider = new DefaultEntityEventProvider<Vendor>();

	private static final Class<Application> app = Application.class;
	private static final String APP_NOT_FOUND = "application-not-found";
	private static final String APP_ALREADY_EXIST = "application-already-exist";

	private static final Class<ApplicationVersion> ver = ApplicationVersion.class;
	private static final String VER_NOT_FOUND = "application-version-not-found";
	private static final String VER_ALREADY_EXIST = "application-version-already-exist";
	private DefaultEntityEventProvider<ApplicationVersion> versionEventProvider = new DefaultEntityEventProvider<ApplicationVersion>();

	private static final Class<ApplicationGroup> grp = ApplicationGroup.class;
	private static final String GRP_NOT_FOUND = "application-group-not-found";
	private static final String GRP_ALREADY_EXIST = "application-group-already-exist";
	private DefaultEntityEventProvider<ApplicationGroup> groupEventProvider = new DefaultEntityEventProvider<ApplicationGroup>();

	private EntityEventListener<Vendor> vendorEventListener = new DefaultEntityEventListener<Vendor>() {
		@Override
		public void entityRemoving(String domain, Vendor obj, ConfigTransaction xact, Object state) {
			List<Predicate> preds = Arrays.asList(Predicates.field("vendor/guid", obj.getGuid()));
			Transaction x = Transaction.getInstance(xact);
			cfg.removes(x, domain, Application.class, preds, null, ApplicationApiImpl.this, state, null);
		}
	};
	private EntityEventListener<Application> applicationEventListener = new DefaultEntityEventListener<Application>() {
		@Override
		public void entityRemoving(String domain, Application obj, ConfigTransaction xact, Object state) {
			Transaction x = Transaction.getInstance(xact);
			cfg.removes(x, domain, ApplicationVersion.class, getPreds(obj.getVersions()), null, versionEventProvider, state, null);
		}
	};

	@Requires
	private ConfigManager cfg;

	@Validate
	public void validate() {
		vendorEventProvider.addEntityEventListener(vendorEventListener);
		this.addEntityEventListener(applicationEventListener);
	}

	@Invalidate
	public void invalidate() {
		vendorEventProvider.removeEntityEventListener(vendorEventListener);
		this.removeEntityEventListener(applicationEventListener);
	}

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private List<Predicate> getPreds(List<? extends Object> objs) {
		if (objs == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(objs.size());
		for (Object obj : objs) {
			if (obj instanceof Vendor)
				preds.add(getPred(((Vendor) obj).getGuid()));
			else if (obj instanceof Application)
				preds.add(getPred(((Application) obj).getGuid()));
			else if (obj instanceof ApplicationVersion)
				preds.add(getPred(((ApplicationVersion) obj).getGuid()));
			else if (obj instanceof ApplicationGroup)
				preds.add(getPred(((ApplicationGroup) obj).getGuid()));
		}
		return preds;
	}

	@Override
	public Collection<Vendor> getVendors(String domain) {
		return cfg.all(domain, ven);
	}

	@Override
	public Vendor findVendor(String domain, String guid) {
		return cfg.find(domain, ven, getPred(guid));
	}

	@Override
	public Vendor getVendor(String domain, String guid) {
		return cfg.get(domain, ven, getPred(guid), VEN_NOT_FOUND);
	}

	@Override
	public void createVendors(String domain, Collection<Vendor> vendors) {
		List<Vendor> vendorList = new ArrayList<Vendor>(vendors);
		cfg.adds(domain, ven, getPreds(vendorList), vendorList, VEN_ALREADY_EXIST, vendorEventProvider);
	}

	@Override
	public void createVendor(String domain, Vendor vendor) {
		cfg.add(domain, ven, getPred(vendor.getGuid()), vendor, VEN_ALREADY_EXIST, vendorEventProvider);
	}

	@Override
	public void updateVendors(String domain, Collection<Vendor> vendors) {
		List<Vendor> vendorList = new ArrayList<Vendor>(vendors);
		for (Vendor vendor : vendorList)
			vendor.setUpdated(new Date());
		cfg.updates(domain, ven, getPreds(vendorList), vendorList, VEN_NOT_FOUND, vendorEventProvider);
	}

	@Override
	public void updateVendor(String domain, Vendor vendor) {
		vendor.setUpdated(new Date());
		cfg.update(domain, ven, getPred(vendor.getGuid()), vendor, VEN_NOT_FOUND, vendorEventProvider);
	}

	@Override
	public void removeVendors(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, ven, preds, VEN_NOT_FOUND, vendorEventProvider);
	}

	@Override
	public void removeVendor(String domain, String guid) {
		cfg.remove(domain, ven, getPred(guid), VEN_NOT_FOUND, vendorEventProvider);
	}

	@Override
	public EntityEventProvider<Vendor> getVendorEventProvider() {
		return vendorEventProvider;
	}

	@Override
	public Collection<Application> getApplications(String domain) {
		return cfg.all(domain, app);
	}

	@Override
	public Application findApplication(String domain, String guid) {
		return cfg.find(domain, app, getPred(guid));
	}

	@Override
	public Application getApplication(String domain, String guid) {
		return cfg.get(domain, app, getPred(guid), APP_NOT_FOUND);
	}

	@Override
	public void createApplications(String domain, Collection<Application> applications) {
		List<Application> applicationList = new ArrayList<Application>(applications);
		cfg.adds(domain, app, getPreds(applicationList), applicationList, APP_ALREADY_EXIST, this);
	}

	@Override
	public void createApplication(String domain, Application application) {
		cfg.add(domain, app, getPred(application.getGuid()), application, APP_ALREADY_EXIST, this);
	}

	@Override
	public void updateApplications(String domain, Collection<Application> applications) {
		List<Application> applicationList = new ArrayList<Application>(applications);
		for (Application application : applicationList)
			application.setUpdated(new Date());
		cfg.updates(domain, app, getPreds(applicationList), applicationList, APP_NOT_FOUND, this);
	}

	@Override
	public void updateApplication(String domain, Application application) {
		application.setUpdated(new Date());
		cfg.update(domain, app, getPred(application.getGuid()), application, APP_NOT_FOUND, this);
	}

	@Override
	public void removeApplications(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, app, preds, APP_NOT_FOUND, this);
	}

	@Override
	public void removeApplication(String domain, String guid) {
		cfg.remove(domain, app, getPred(guid), APP_NOT_FOUND, this);
	}

	@Override
	public Collection<ApplicationVersion> getApplicationVersions(String domain) {
		return cfg.all(domain, ver);
	}

	@Override
	public ApplicationVersion findApplicationVersion(String domain, String guid) {
		return cfg.find(domain, ver, getPred(guid));
	}

	@Override
	public ApplicationVersion getApplicationVersion(String domain, String guid) {
		return cfg.get(domain, ver, getPred(guid), VER_NOT_FOUND);
	}

	@Override
	public void createApplicationVersions(String domain, Collection<ApplicationVersion> versions) {
		List<ApplicationVersion> versionList = new ArrayList<ApplicationVersion>(versions);
		cfg.adds(domain, ver, getPreds(versionList), versionList, VER_ALREADY_EXIST, versionEventProvider);

	}

	@Override
	public void createApplicationVersion(String domain, ApplicationVersion version) {
		cfg.add(domain, ver, getPred(version.getGuid()), version, VER_ALREADY_EXIST, versionEventProvider);
	}

	@Override
	public void updateApplicationVersions(String domain, Collection<ApplicationVersion> versions) {
		List<ApplicationVersion> versionList = new ArrayList<ApplicationVersion>(versions);
		for (ApplicationVersion version : versionList)
			version.setUpdated(new Date());
		cfg.updates(domain, ver, getPreds(versionList), versionList, VER_NOT_FOUND, versionEventProvider);
	}

	@Override
	public void updateApplicationVersion(String domain, ApplicationVersion version) {
		version.setUpdated(new Date());
		cfg.update(domain, ver, getPred(version.getGuid()), version, VER_NOT_FOUND, versionEventProvider);
	}

	@Override
	public void removeApplicationVersions(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, ver, preds, VER_NOT_FOUND, versionEventProvider);
	}

	@Override
	public void removeApplicationVersion(String domain, String guid) {
		cfg.remove(domain, ver, getPred(guid), VER_NOT_FOUND, versionEventProvider);
	}

	@Override
	public EntityEventProvider<ApplicationVersion> getApplicationVersionEventProvider() {
		return versionEventProvider;
	}

	@Override
	public Collection<ApplicationGroup> getApplicationGroups(String domain) {
		return cfg.all(domain, grp);
	}

	@Override
	public ApplicationGroup findApplicationGroup(String domain, String guid) {
		return cfg.find(domain, grp, getPred(guid));
	}

	@Override
	public ApplicationGroup getApplicationGroup(String domain, String guid) {
		return cfg.get(domain, grp, getPred(guid), GRP_NOT_FOUND);
	}

	@Override
	public void createApplicationGroups(String domain, Collection<ApplicationGroup> groups) {
		List<ApplicationGroup> groupList = new ArrayList<ApplicationGroup>(groups);
		cfg.adds(domain, grp, getPreds(groupList), groupList, GRP_ALREADY_EXIST, groupEventProvider);
	}

	@Override
	public void createApplicationGroup(String domain, ApplicationGroup group) {
		cfg.add(domain, grp, getPred(group.getGuid()), group, GRP_ALREADY_EXIST, groupEventProvider);
	}

	@Override
	public void updateApplicationGroups(String domain, Collection<ApplicationGroup> groups) {
		List<ApplicationGroup> groupList = new ArrayList<ApplicationGroup>(groups);
		for (ApplicationGroup group : groupList)
			group.setUpdated(new Date());
		cfg.updates(domain, grp, getPreds(groupList), groupList, VEN_NOT_FOUND, groupEventProvider);
	}

	@Override
	public void updateApplicationGroup(String domain, ApplicationGroup group) {
		group.setUpdated(new Date());
		cfg.update(domain, grp, getPred(group.getGuid()), group, GRP_NOT_FOUND, groupEventProvider);
	}

	@Override
	public void removeApplicationGroups(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, grp, preds, GRP_NOT_FOUND, groupEventProvider);
	}

	@Override
	public void removeApplicationGroup(String domain, String guid) {
		cfg.remove(domain, grp, getPred(guid), GRP_NOT_FOUND, groupEventProvider);
	}

	@Override
	public EntityEventProvider<ApplicationGroup> getApplicationGroupEventProvider() {
		return groupEventProvider;
	}
}
