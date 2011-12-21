package org.krakenapps.ldap.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.ldap.LdapSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "ldap-sync-service")
@Provides
public class DomSyncService implements LdapSyncService, Runnable {
	private final Logger logger = LoggerFactory.getLogger(DomSyncService.class);

	private boolean syncState;
	private Thread syncThread;

	@Requires
	private ConfigService conf;

	@Requires
	private LdapService ldap;

	@Requires
	private UserApi userApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	@Override
	public String getExtensionName() {
		return "ldap";
	}

	private ConfigDatabase getDatabase() {
		return conf.ensureDatabase("kraken-ldap");
	}

	@Validate
	public void start() {
		setPeriodicSync(true);
	}

	private void startSyncThread() {
		if (syncState) {
			syncThread = new Thread(this, "LDAP User Sync");
			syncThread.start();
		}
	}

	@Override
	public void run() {
		try {
			while (syncState) {
				try {
					syncAll();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.debug("kraken dom: sync interrupted");
				}
			}
		} catch (Exception e) {
			logger.error("kraken dom: sync error", e);
		} finally {
			logger.info("kraken dom: sync thread stopped");
		}
	}

	private void syncAll() {
		// NPE at stopping
		if (ldap == null)
			return;

		for (LdapProfile profile : ldap.getProfiles()) {
			try {
				Date d = profile.getLastSync();
				Date now = new Date();
				long span = -1;
				if (d != null)
					span = now.getTime() - d.getTime();

				if (span == -1 || span > profile.getSyncInterval()) {
					try {
						logger.debug("kraken dom: try to sync using profile [{}]", profile.getName());
						sync(profile);
					} catch (Exception e) {
						logger.error("kraken dom: periodic sync failed", e);
					}

					profile.setLastSync(now);
					ldap.updateProfile(profile);
				}
			} catch (Exception e) {
				logger.error("kraken dom: sync profile " + profile.getName() + " failed", e);
			}
		}
	}

	@Override
	public boolean getPeriodicSync() {
		return syncState;
	}

	@Override
	public void setPeriodicSync(boolean activate) {
		ConfigCollection col = getDatabase().ensureCollection("config");
		Config c = col.findOne(Predicates.field("name", "sync"));

		Map<String, Object> m = null;
		if (c == null) {
			m = new HashMap<String, Object>();
			m.put("name", "sync");
			m.put("value", activate);
			col.add(m);
		}

		if (syncState == false && activate)
			startSyncThread();

		syncState = activate;
	}

	@Override
	public void sync(LdapProfile profile) {
		importLdap(profile);
		exportDom(profile);
	}

	@Override
	public void importLdap(LdapProfile profile) {
		ConfigCollection col = getDatabase().ensureCollection(profile.getName());

		Map<String, Object> orgMap = new HashMap<String, Object>();
		orgMap.put("value", ldap.getOrganizationUnits(profile));
		updateConfig(col, "org_unit", orgMap);

		Map<String, Object> userMap = new HashMap<String, Object>();
		userMap.put("value", ldap.getDomainUserAccounts(profile));
		updateConfig(col, "user", userMap);

		Map<String, Object> meta = new HashMap<String, Object>();
		meta.put("updated", new Date());
		updateConfig(col, "metadata", meta);
	}

	private void updateConfig(ConfigCollection col, String name, Map<String, Object> values) {
		values.put("name", name);
		Object doc = PrimitiveConverter.serialize(values);

		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			col.add(doc);
		else {
			c.setDocument(doc);
			col.update(c);
		}
	}

	@Override
	public void exportDom(LdapProfile profile) {
		Map<String, OrganizationUnit> orgUnits = exportOrgUnits(profile);
		exportUsers(profile, orgUnits);
	}

	@SuppressWarnings("unchecked")
	private Map<String, OrganizationUnit> exportOrgUnits(LdapProfile profile) {
		Map<String, OrganizationUnit> result = new HashMap<String, OrganizationUnit>();

		ConfigCollection col = getDatabase().ensureCollection(profile.getName());
		String domain = profile.getTargetDomain();
		Collection<DomainOrganizationalUnit> orgUnits = loadLdapConfig(col, "org_unit", DomainOrganizationalUnit.class);

		Pattern p = Pattern.compile("OU=(.*?),");
		for (DomainOrganizationalUnit orgUnit : orgUnits) {
			Matcher m = p.matcher(orgUnit.getDistinguishedName());
			List<String> names = new ArrayList<String>();
			while (m.find())
				names.add(m.group(1));

			OrganizationUnit domOrgUnit = createOrgUnits(domain, names);
			Map<String, Object> ext = (Map<String, Object>) PrimitiveConverter.serialize(orgUnit);

			Map<String, Object> domOrgUnitExt = domOrgUnit.getExt();
			if (domOrgUnitExt == null)
				domOrgUnitExt = new HashMap<String, Object>();
			domOrgUnitExt.put(getExtensionName(), ext);
			domOrgUnit.setExt(domOrgUnitExt);
			orgUnitApi.updateOrganizationUnit(domain, domOrgUnit);
			result.put(domOrgUnit.getName(), domOrgUnit);
		}

		return result;
	}

	private OrganizationUnit createOrgUnits(String domain, List<String> names) {
		String parentGuid = null;
		OrganizationUnit orgUnit = null;

		Map<String, Object> ext = new HashMap<String, Object>();
		ext.put(getExtensionName(), new HashMap<String, Object>());

		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			orgUnit = orgUnitApi.findOrganizationUnitByName(domain, names.subList(0, i).toArray(new String[0]));
			if (orgUnit == null) {
				orgUnit = new OrganizationUnit();
				orgUnit.setName(name);
				orgUnit.setParent(parentGuid);
				orgUnit.setExt(ext);
				orgUnitApi.createOrganizationUnit(domain, orgUnit);
			}
			parentGuid = orgUnit.getGuid();
		}

		return orgUnit;
	}

	@SuppressWarnings("unchecked")
	private void exportUsers(LdapProfile profile, Map<String, OrganizationUnit> orgUnits) {
		ConfigCollection col = getDatabase().ensureCollection(profile.getName());
		String domain = profile.getTargetDomain();
		Collection<DomainUserAccount> users = loadLdapConfig(col, "user", DomainUserAccount.class);

		for (DomainUserAccount user : users) {
			User domUser = userApi.findUser(domain, user.getAccountName());

			Map<String, Object> ext = (Map<String, Object>) PrimitiveConverter.serialize(user);
			ext.remove("account_name");
			ext.remove("logon_count");
			ext.remove("last_logon");

			if (domUser == null) {
				domUser = new User();
				domUser.setLoginName(user.getAccountName());
				domUser.setOrgUnit(orgUnits.get(user.getOrganizationUnitName()));
				domUser.setName(user.getDisplayName());
				domUser.setTitle(user.getTitle());
				domUser.setEmail(user.getMail());
				domUser.setPhone(user.getMobile());
				userApi.createUser(domain, domUser);
			}

			Map<String, Object> domUserExt = domUser.getExt();
			if (domUserExt == null)
				domUserExt = new HashMap<String, Object>();
			domUserExt.put(getExtensionName(), ext);
			domUser.setExt(domUserExt);
			userApi.updateUser(domain, domUser, false);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<T> loadLdapConfig(ConfigCollection col, String name, Class<T> cls) {
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			return new ArrayList<T>();

		Object value = ((Map<String, Object>) c.getDocument()).get("value");
		return PrimitiveConverter.parseCollection(cls, Arrays.asList((Object[]) value));
	}
}