package org.krakenapps.ldap.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.LdapOrganizationalUnitApi;
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.LdapOrganizationalUnit;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.User;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.ldap.LdapSyncService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Component(name = "ldap-sync-service")
@Provides
public class DomSyncService implements LdapSyncService, EntityEventListener<OrganizationUnit> {
	private BundleContext bc;

	@Requires
	private LdapService ldap;

	@Requires
	private UserApi userApi;

	@Requires
	private LdapOrganizationalUnitApi ldapOrgUnitApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	public DomSyncService(BundleContext bc) {
		this.bc = bc;
	}

	@Validate
	public void start() {
		orgUnitApi.addEntityEventListener(this);
	}

	@Invalidate
	public void stop() {
		if (orgUnitApi != null)
			orgUnitApi.removeEntityEventListener(this);
	}

	@Override
	public void entityAdded(OrganizationUnit obj) {
	}

	@Override
	public void entityUpdated(OrganizationUnit obj) {
	}

	@Override
	public void entityRemoving(OrganizationUnit obj) {
	}

	@Override
	public void entityRemoved(OrganizationUnit obj) {
		
	}

	@Override
	public void sync(LdapProfile profile) {
		try {
			Map<String, LdapOrganizationalUnit> ldapOrgUnits = syncLdapOrgUnit(profile);
			syncUser(bc, profile, ldapOrgUnits);
		} catch (NullPointerException e) {
			throw new IllegalStateException("check database character set");
		}
	}

	private Map<String, LdapOrganizationalUnit> syncLdapOrgUnit(LdapProfile profile) {
		Map<String, LdapOrganizationalUnit> before = new HashMap<String, LdapOrganizationalUnit>();

		for (LdapOrganizationalUnit ldapOrgUnit : ldapOrgUnitApi.getLdapOrganizationalUnits())
			before.put(ldapOrgUnit.getDistinguishedName(), ldapOrgUnit);

		Collection<DomainOrganizationalUnit> domainOrgUnits = ldap.getOrganizationUnits(profile);
		for (DomainOrganizationalUnit domainOrgUnit : domainOrgUnits) {
			String dn = domainOrgUnit.getDistinguishedName();

			if (before.containsKey(dn))
				before.remove(dn);
			else {
				LdapOrganizationalUnit ldapOrgUnit = new LdapOrganizationalUnit();
				ldapOrgUnit.setDistinguishedName(domainOrgUnit.getDistinguishedName());
				ldapOrgUnit.setProfile(profile.getDc());
				ldapOrgUnitApi.createLdapOrganizationalUnit(ldapOrgUnit);
			}
		}

		for (LdapOrganizationalUnit dn : before.values())
			ldapOrgUnitApi.removeLdapOrganizationalUnit(dn.getId());

		ldapOrgUnitApi.sync();

		Map<String, LdapOrganizationalUnit> after = new HashMap<String, LdapOrganizationalUnit>();
		for (LdapOrganizationalUnit ldapOrgUnit : ldapOrgUnitApi.getLdapOrganizationalUnits())
			after.put(ldapOrgUnit.getOrganizationUnit().getName(), ldapOrgUnit);

		return after;
	}

	private void syncUser(BundleContext bc, LdapProfile profile, Map<String, LdapOrganizationalUnit> ldapOrgUnits) {
		Collection<User> users = userApi.getUsers(profile.getDc());
		Map<String, User> before = new HashMap<String, User>();
		for (User user : users)
			before.put(user.getLoginName(), user);

		Collection<DomainUserAccount> domainUsers = ldap.getDomainUserAccounts(profile);
		for (DomainUserAccount domainUser : domainUsers) {
			User user = null;
			boolean isUpdate = false;

			if (before.containsKey(domainUser.getAccountName())) {
				user = before.get(domainUser.getAccountName());
				before.remove(domainUser.getAccountName());
				isUpdate = true;
			} else {
				user = new User();
				user.setLoginName(domainUser.getAccountName());
				user.setDomainController(profile.getDc());
				user.setCreateDateTime(new Date());
			}

			user.setName(domainUser.getDisplayName());
			user.setOrganizationUnit(ldapOrgUnits.get(domainUser.getOrganizationUnitName()).getOrganizationUnit());
			user.setOrganization(user.getOrganizationUnit().getOrganization());
			user.setTitle(domainUser.getTitle());
			user.setDepartment(domainUser.getDepartment());
			user.setEmail(domainUser.getMail());
			user.setPhone(domainUser.getMobile());
			user.setUpdateDateTime(new Date());
			user.setExternal(true);

			if (isUpdate)
				userApi.updateUser(user);
			else
				userApi.createUser(user);

			if (domainUser.isDomainAdmin())
				syncAdmin(bc, user, domainUser);
		}

		for (User user : before.values())
			userApi.removeUser(user.getId());
	}

	private void syncAdmin(BundleContext bc, User user, DomainUserAccount domainUser) {
		ServiceReference adminApiRef = bc.getServiceReference(AdminApi.class.getName());
		ServiceReference roleApiRef = bc.getServiceReference(RoleApi.class.getName());
		AdminApi adminApi = (AdminApi) bc.getService(adminApiRef);
		RoleApi roleApi = (RoleApi) bc.getService(roleApiRef);

		Admin admin = null;
		boolean isUpdate = false;

		if (user.getAdmin() == null) {
			admin = new Admin();
			admin.setRole(roleApi.getRole("admin"));
			admin.setUser(user);
		} else {
			admin = user.getAdmin();
			isUpdate = true;
		}
		admin.setLastLoginDateTime(domainUser.getLastLogon());
		admin.setEnabled(true);

		ProgramProfile programProfile = getProgramProfile(bc, admin);
		if (programProfile == null)
			return;

		admin.setProgramProfile(programProfile);

		if (isUpdate)
			adminApi.updateAdmin(admin.getUser().getOrganization().getId(), null, admin);
		else
			adminApi.createAdmin(admin.getUser().getOrganization().getId(), null, admin);
	}

	private ProgramProfile getProgramProfile(BundleContext bc, Admin admin) {
		ServiceReference orgParameterApiRef = bc.getServiceReference(OrganizationParameterApi.class.getName());
		OrganizationParameterApi orgParameterApi = (OrganizationParameterApi) bc.getService(orgParameterApiRef);
		ServiceReference programApiRef = bc.getServiceReference(ProgramApi.class.getName());
		ProgramApi programApi = (ProgramApi) bc.getService(programApiRef);

		OrganizationParameter op = orgParameterApi.getOrganizationParameter(admin.getUser().getOrganization().getId(),
				"default_program_profile_id");
		ProgramProfile profile = programApi.getProgramProfile(admin.getUser().getOrganization().getId(),
				Integer.parseInt(op.getValue()));

		return profile;
	}

}
