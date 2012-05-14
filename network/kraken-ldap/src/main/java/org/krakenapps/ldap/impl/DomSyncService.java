package org.krakenapps.ldap.impl;

import java.lang.reflect.Field;
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
import org.krakenapps.api.FieldOption;
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
import org.krakenapps.dom.api.UserExtensionProvider;
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

	private UserExtensionProvider extProvider = new UserExtensionProvider() {
		@Override
		public String getExtensionName() {
			return "ldap";
		}
	};

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

	private void exportDom(Collection<DomainOrganizationalUnit> orgUnits, Collection<DomainUserAccount> users,
			LdapProfile profile) {
		Map<String, OrganizationUnit> domOrgUnits = exportOrgUnits(profile.getTargetDomain(), orgUnits);
		exportUsers(profile.getTargetDomain(), profile.getBaseDn(), users, domOrgUnits);
	}

	@SuppressWarnings("unchecked")
	private Map<String, OrganizationUnit> exportOrgUnits(String domain, Collection<DomainOrganizationalUnit> orgUnits) {
		Map<String, OrganizationUnit> result = new HashMap<String, OrganizationUnit>();

		// names-object map
		Map<List<String>, DomainOrganizationalUnit> orgUnitsMap = new HashMap<List<String>, DomainOrganizationalUnit>();
		Pattern p = Pattern.compile("OU=(.*?),");
		for (DomainOrganizationalUnit orgUnit : orgUnits) {
			// only active directory has org unit distinguished name
			String dn = orgUnit.getDistinguishedName();
			if (dn == null)
				continue;

			Matcher m = p.matcher(dn);
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
			domOrgUnitExt.put(extProvider.getExtensionName(), ext);

			Object before = domOrgUnit.getExt().get(extProvider.getExtensionName());
			Object after = domOrgUnitExt.get(extProvider.getExtensionName());
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

	private OrganizationUnit createOrgUnits(String domain, List<OrganizationUnit> create,
			Map<String, OrganizationUnit> createdRoot, List<String> names) {
		if (names.isEmpty())
			return null;

		Map<String, Object> ext = new HashMap<String, Object>();
		ext.put(extProvider.getExtensionName(), new HashMap<String, Object>());

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

	private static class DomUserConstraints {
		static public int lenLoginName;
		static public int lenName;
		static public int lenTitle;
		static public int lenEmail;
		static public int lenPhone;
		static {
			try {
				lenLoginName = getLength("loginName");
				lenName = getLength("name");
				lenTitle = getLength("title");
				lenEmail = getLength("email");
				lenPhone = getLength("phone");
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		private static int getLength(String string) throws SecurityException, NoSuchFieldException {
			Field declaredField = User.class.getDeclaredField("loginName");
			FieldOption annotation = declaredField.getAnnotation(FieldOption.class);
			if (annotation != null)
				return annotation.length();
			else
				return 0;
		}
	}

	@SuppressWarnings("unchecked")
	private void exportUsers(String domain, String baseDn, Collection<DomainUserAccount> users,
			Map<String, OrganizationUnit> orgUnits) {
		// remove removed ldap users
		Set<String> loginNames = new HashSet<String>();
		for (DomainUserAccount user : users)
			loginNames.add(user.getAccountName());
		for (User user : userApi.getUsers(domain)) {
			Map<String, Object> ext = user.getExt();
			if (ext == null || !ext.containsKey(extProvider.getExtensionName()))
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
		List<Object[]> failed = new ArrayList<Object[]>();
		for (DomainUserAccount user : users) {
			User domUser = userApi.findUser(domain, user.getAccountName());
			boolean exist = (domUser != null);

			Map<String, Object> ext = (Map<String, Object>) PrimitiveConverter.serialize(user);
			ext.remove("account_name");
			ext.remove("logon_count");
			ext.remove("last_logon");

			boolean basicInfoUpdated = false;
			try {
				if (domUser == null) {
					domUser = new User();
					basicInfoUpdated = updateDomUserFromDomainUser(orgUnits, user, domUser);
				} else if (isSameDN(domUser, user)) {
					basicInfoUpdated = updateDomUserFromDomainUser(orgUnits, user, domUser);
				}
			} catch (Exception e) {
				logger.trace("update failed", e);
				failed.add(new Object[] { user, e });
				continue;
			}

			Map<String, Object> domUserExt = domUser.getExt();
			if (domUserExt == null)
				domUserExt = new HashMap<String, Object>();
			domUserExt.put(extProvider.getExtensionName(), ext);
			if (user.isDomainAdmin() && !domUserExt.containsKey(adminApi.getExtensionName()))
				domUserExt.put(adminApi.getExtensionName(), defaultAdmin);

			boolean equals = domUserExt.equals(domUser.getExt());

			domUser.setExt(domUserExt);

			if (!exist)
				create.add(domUser);
			else {
				if (!equals || basicInfoUpdated)
					update.add(domUser);
			}
		}

		if (!failed.isEmpty()) {
			int reportId = new Object().hashCode();
			logger.warn(
					"kraken dom: Importing some accounts failed and ignored while syncing in DomSyncService. failure report identifier: {}",
					reportId);
			for (Object[] f : failed) {
				DomainUserAccount acc = (DomainUserAccount) f[0];
				Exception e = (Exception) f[1];
				logger.warn("kraken dom: {}: {}", reportId, String.format("%s: %s", acc.getDistinguishedName(), e.toString()));
			}
		}

		userApi.createUsers(domain, create);
		userApi.updateUsers(domain, update, false);
	}

	/**
	 * @return true if user data updated
	 */
	private boolean updateDomUserFromDomainUser(Map<String, OrganizationUnit> orgUnits, DomainUserAccount user,
			User domUser) {
		boolean updated = false;

		if (domUser.getLoginName() == null || !domUser.getLoginName().equals(user.getAccountName())) {
			domUser.setLoginName(user.getAccountName());
			updated = true;
		}
		if (domUser.getOrgUnit() == null || !domUser.getOrgUnit().equals(orgUnits.get(user.getOrganizationUnitName()))) {
			domUser.setOrgUnit(orgUnits.get(user.getOrganizationUnitName()));
			updated = true;
		}
		if (domUser.getName() == null || !domUser.getName().equals(user.getDisplayName())) {
			domUser.setName(user.getDisplayName());
			updated = true;
		}
		if (domUser.getTitle() == null || !domUser.getTitle().equals(user.getTitle())) {
			domUser.setName(user.getDisplayName());
			updated = true;
		}
		if (domUser.getEmail() == null || !domUser.getEmail().equals(user.getMail())) {
			domUser.setEmail(user.getMail());
			updated = true;
		}
		if (domUser.getPhone() == null || !domUser.getPhone().equals(user.getMobile())) {
			domUser.setPhone(user.getMobile());
			updated = true;
		}

		if (updated) {
			checkConstraints(domUser);
			return true;
		} else {
			return false;
		}
	}

	private void checkConstraints(User domUser) {
		if (domUser.getLoginName() != null && DomUserConstraints.lenLoginName < domUser.getLoginName().length())
			throw new IllegalArgumentException(String.format("getLoginName longer than %d: %s", DomUserConstraints.lenLoginName, domUser.getLoginName()));
		if (domUser.getName() != null && DomUserConstraints.lenName < domUser.getName().length())
			throw new IllegalArgumentException(String.format("getName longer than %d: %s", DomUserConstraints.lenName, domUser.getName()));
		if (domUser.getTitle() != null && DomUserConstraints.lenTitle < domUser.getTitle().length())
			throw new IllegalArgumentException(String.format("getTitle longer than %d: %s", DomUserConstraints.lenTitle, domUser.getTitle()));
		if (domUser.getEmail() != null && DomUserConstraints.lenEmail < domUser.getEmail().length())
			throw new IllegalArgumentException(String.format("getEmail longer than %d: %s", DomUserConstraints.lenEmail, domUser.getEmail()));
		if (domUser.getPhone() != null && DomUserConstraints.lenPhone < domUser.getPhone().length())
			throw new IllegalArgumentException(String.format("getPhone longer than %d: %s", DomUserConstraints.lenPhone, domUser.getPhone()));
	}

	private boolean isSameDN(User domUser, DomainUserAccount user) {
		if (domUser == null || user == null
				|| user.getDistinguishedName() == null
				|| domUser.getExt() == null)
			return false;
		@SuppressWarnings("unchecked")
		Map<String, String> ldapExt = (Map<String, String>) domUser.getExt().get("ldap");
		if (ldapExt == null)
			return false;
		String existDn = ldapExt.get("distinguished_name");
		if (existDn == null)
			return false;
		return user.getDistinguishedName().equals(existDn);
	}

}