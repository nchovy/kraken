package org.krakenapps.ldap.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
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
	private static final String DEFAULT_ROLE_NAME = "admin";
	private static final String DEFAULT_PROFILE_NAME = "all";

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
	private AdminApi adminApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	@Requires
	private RoleApi roleApi;

	@Requires
	private ProgramApi programApi;

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
		Collection<DomainOrganizationalUnit> orgUnits = ldap.getOrganizationUnits(profile);
		Collection<DomainUserAccount> users = ldap.getDomainUserAccounts(profile);

		exportDom(orgUnits, users, profile);
		profile.setLastSync(new Date());
	}

	private void exportDom(Collection<DomainOrganizationalUnit> orgUnits, Collection<DomainUserAccount> users, LdapProfile profile) {
		Map<String, OrganizationUnit> domOrgUnits = exportOrgUnits(profile.getTargetDomain(), orgUnits);
		exportUsers(profile.getTargetDomain(), users, domOrgUnits);
	}

	@SuppressWarnings("unchecked")
	private Map<String, OrganizationUnit> exportOrgUnits(String domain, Collection<DomainOrganizationalUnit> orgUnits) {
		Map<String, OrganizationUnit> result = new HashMap<String, OrganizationUnit>();

		// names-object map
		Map<List<String>, DomainOrganizationalUnit> orgUnitsMap = new HashMap<List<String>, DomainOrganizationalUnit>();
		Pattern p = Pattern.compile("OU=(.*?),");
		for (DomainOrganizationalUnit orgUnit : orgUnits) {
			Matcher m = p.matcher(orgUnit.getDistinguishedName());
			List<String> names = new ArrayList<String>();
			while (m.find())
				names.add(m.group(1));
			Collections.reverse(names);
			orgUnitsMap.put(names, orgUnit);
		}

		// sync
		List<OrganizationUnit> create = new ArrayList<OrganizationUnit>();
		Map<String, OrganizationUnit> createdRoot = new HashMap<String, OrganizationUnit>();
		List<OrganizationUnit> update = new ArrayList<OrganizationUnit>();
		for (List<String> names : orgUnitsMap.keySet()) {
			DomainOrganizationalUnit orgUnit = orgUnitsMap.get(names);
			OrganizationUnit domOrgUnit = createOrgUnits(domain, create, createdRoot, names);
			Map<String, Object> ext = (Map<String, Object>) PrimitiveConverter.serialize(orgUnit);

			Map<String, Object> domOrgUnitExt = domOrgUnit.getExt();
			if (domOrgUnitExt == null)
				domOrgUnitExt = new HashMap<String, Object>();
			domOrgUnitExt.put(getExtensionName(), ext);

			Object before = domOrgUnit.getExt().get(getExtensionName());
			Object after = domOrgUnitExt.get(getExtensionName());
			boolean equals = after.equals(before);

			domOrgUnit.setExt(domOrgUnitExt);
			if (!create.contains(domOrgUnit)) {
				if (!equals)
					update.add(domOrgUnit);
			}
			result.put(domOrgUnit.getName(), domOrgUnit);
		}

		orgUnitApi.createOrganizationUnits(domain, create);
		orgUnitApi.updateOrganizationUnits(domain, update);

		return result;
	}

	private OrganizationUnit createOrgUnits(String domain, List<OrganizationUnit> create, Map<String, OrganizationUnit> createdRoot,
			List<String> names) {
		if (names.isEmpty())
			return null;

		Map<String, Object> ext = new HashMap<String, Object>();
		ext.put(getExtensionName(), new HashMap<String, Object>());

		OrganizationUnit orgUnit = orgUnitApi.findOrganizationUnitByName(domain, names.get(0));
		if (orgUnit == null) {
			orgUnit = createdRoot.get(names.get(0));
			if (orgUnit == null) {
				orgUnit = new OrganizationUnit();
				orgUnit.setName(names.get(0));
				orgUnit.setExt(ext);
				create.add(orgUnit);
				createdRoot.put(names.get(0), orgUnit);
			}
		}

		for (int i = 1; i < names.size(); i++) {
			String name = names.get(i);

			OrganizationUnit parent = orgUnit;
			orgUnit = null;
			for (OrganizationUnit child : parent.getChildren()) {
				if (name.equals(child.getName())) {
					orgUnit = child;
					break;
				}
			}

			if (orgUnit == null) {
				orgUnit = new OrganizationUnit();
				orgUnit.setName(name);
				orgUnit.setParent(parent.getGuid());
				orgUnit.setExt(ext);
				create.add(orgUnit);
				parent.getChildren().add(orgUnit);
			}
		}

		return orgUnit;
	}

	@SuppressWarnings("unchecked")
	private void exportUsers(String domain, Collection<DomainUserAccount> users, Map<String, OrganizationUnit> orgUnits) {
		// remove removed ldap users
		Set<String> loginNames = new HashSet<String>();
		for (DomainUserAccount user : users)
			loginNames.add(user.getAccountName());
		for (User user : userApi.getUsers(domain)) {
			Map<String, Object> ext = user.getExt();
			if (ext == null || !ext.containsKey(getExtensionName()))
				continue;
			if (!loginNames.contains(user.getLoginName()))
				userApi.removeUser(domain, user.getLoginName());
		}

		Admin defaultAdmin = new Admin();
		defaultAdmin.setRole(roleApi.getRole(domain, DEFAULT_ROLE_NAME));
		defaultAdmin.setProfile(programApi.getProgramProfile(domain, DEFAULT_PROFILE_NAME));
		defaultAdmin.setEnabled(true);

		// sync
		List<User> create = new ArrayList<User>();
		List<User> update = new ArrayList<User>();
		for (DomainUserAccount user : users) {
			User domUser = userApi.findUser(domain, user.getAccountName());
			boolean exist = (domUser != null);

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
			}

			Map<String, Object> domUserExt = domUser.getExt();
			if (domUserExt == null)
				domUserExt = new HashMap<String, Object>();
			domUserExt.put(getExtensionName(), ext);
			if (user.isDomainAdmin() && !domUserExt.containsKey(adminApi.getExtensionName()))
				domUserExt.put(adminApi.getExtensionName(), defaultAdmin);

			boolean equals = domUserExt.equals(domUser.getExt());

			domUser.setExt(domUserExt);

			if (!exist)
				create.add(domUser);
			else {
				if (!equals)
					update.add(domUser);
			}
		}

		userApi.createUsers(domain, create);
		userApi.updateUsers(domain, update, false);
	}
}