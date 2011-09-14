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

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.LoginCallback;
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.api.OtpApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.exception.AdminLockedException;
import org.krakenapps.dom.exception.CannotRemoveRequestingAdminException;
import org.krakenapps.dom.exception.InvalidOtpPasswordException;
import org.krakenapps.dom.exception.InvalidPasswordException;
import org.krakenapps.dom.exception.LoginFailedException;
import org.krakenapps.dom.exception.MaxSessionException;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.AdminTrustHost;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.dom.model.Role;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.msgbus.Session;

@Component(name = "dom-admin-api")
@Provides
@JpaConfig(factory = "dom")
public class AdminApiImpl extends AbstractApi<Admin> implements AdminApi, UserExtensionProvider {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private OrganizationParameterApi orgParamApi;

	@Requires(optional = true, nullable = false)
	private OtpApi otpApi;

	private Set<LoginCallback> callbacks = new HashSet<LoginCallback>();
	private PriorityQueue<LoggedInAdmin> loggedIn = new PriorityQueue<LoggedInAdmin>(11, new LoggedInAdminComparator());

	@Override
	public String getName() {
		return "admin";
	}

	@Override
	public Admin login(Session session, String nick, String hash, boolean force) throws LoginFailedException {
		Admin admin = getAdmin(nick, session);
		String password = null;

		if (otpApi != null && admin.isUseOtp())
			password = Sha1.hash(otpApi.getOtpValue(admin.getOtpSeed()));
		else
			password = admin.getUser().getPassword();

		if (hash.equals(Sha1.hash(password + session.getString("nonce")))) {
			OrganizationParameter param = orgParamApi.getOrganizationParameter(admin.getUser().getOrganization().getId(),
					"max_sessions");
			if (param != null) {
				try {
					int maxSessions = Integer.parseInt(param.getValue());
					if (maxSessions > 0) {
						if (force) {
							while (loggedIn.size() >= maxSessions) {
								if (loggedIn.peek().level > admin.getRole().getLevel())
									throw new MaxSessionException();
								loggedIn.poll().session.close();
							}
						} else if (loggedIn.size() >= maxSessions) {
							LoggedInAdmin peek = loggedIn.peek();
							throw new MaxSessionException(peek.loginName, peek.session);
						}
					}
				} catch (NumberFormatException e) {
				}
			}

			updateLoginFailures(admin, true);
			for (LoginCallback callback : callbacks)
				callback.onLoginSuccess(admin, session);
			loggedIn.add(new LoggedInAdmin(admin.getRole().getLevel(), new Date(), session, admin.getUser().getLoginName()));
			return admin;
		} else {
			updateLoginFailures(admin, false);
			for (LoginCallback callback : callbacks)
				callback.onLoginFailed(admin, session);
			if (admin.isUseOtp())
				throw new InvalidOtpPasswordException();
			else
				throw new InvalidPasswordException();
		}
	}

	private class LoggedInAdmin {
		private int level;
		private Date loginTime;
		private Session session;
		private String loginName;

		private LoggedInAdmin(Session session) {
			this.session = session;
		}

		private LoggedInAdmin(int level, Date loginTime, Session session, String loginName) {
			this.level = level;
			this.loginTime = loginTime;
			this.session = session;
			this.loginName = loginName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((session == null) ? 0 : session.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LoggedInAdmin other = (LoggedInAdmin) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (session == null) {
				if (other.session != null)
					return false;
			} else if (!session.equals(other.session))
				return false;
			return true;
		}

		private AdminApiImpl getOuterType() {
			return AdminApiImpl.this;
		}
	}

	private class LoggedInAdminComparator implements Comparator<LoggedInAdmin> {
		@Override
		public int compare(LoggedInAdmin o1, LoggedInAdmin o2) {
			if (o1.level != o2.level)
				return o1.level - o2.level;
			else
				return o1.loginTime.compareTo(o2.loginTime);
		}
	}

	@Override
	public void logout(Session session) {
		if (session.getOrgId() != null) {
			Admin admin = getAdmin(session.getOrgId(), session.getAdminId());
			loggedIn.remove(new LoggedInAdmin(session));
			for (LoginCallback callback : callbacks)
				callback.onLogout(admin, session);
		}
	}

	@Override
	public void registerLoginCallback(LoginCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void unregisterLoginCallback(LoginCallback callback) {
		callbacks.remove(callback);
	}

	@Transactional
	private void updateLoginFailures(Admin admin, boolean success) {
		EntityManager em = entityManagerService.getEntityManager();
		admin = em.find(Admin.class, admin.getId());

		if (success) {
			admin.setLoginFailures(0);
			admin.setEnabled(true);
		} else {
			admin.setLastLoginFailedDateTime(new Date());
			admin.setLoginFailures(admin.getLoginFailures() + 1);
			if (admin.isUseLoginLock() && admin.getLoginFailures() >= admin.getLoginLockCount())
				admin.setEnabled(false);
		}

		em.merge(admin);
	}

	@Transactional
	private Admin getAdmin(String nick, Session session) {
		try {
			Admin admin = getAdminByLoginName(nick);
			if (!admin.isEnabled()) {
				Date failed = admin.getLastLoginFailedDateTime();
				Calendar c = Calendar.getInstance();
				c.add(Calendar.SECOND, -10);
				if (failed != null && failed.after(c.getTime())) {
					for (LoginCallback callback : callbacks)
						callback.onLoginLocked(admin, session);
					throw new AdminLockedException();
				} else
					updateLoginFailures(admin, true);
			}
			admin.setLastLoginDateTime(new Date());

			EntityManager em = entityManagerService.getEntityManager();
			em.merge(admin);
			return admin;
		} catch (NoResultException e) {
			throw new AdminNotFoundException(nick);
		}
	}

	@Override
	public void createAdmin(int organizationId, Integer requestAdminId, Admin admin) {
		createAdminInternal(organizationId, requestAdminId, admin);
		fireEntityAdded(admin);
	}

	@Transactional
	private void createAdminInternal(int organizationId, Integer requestAdminId, Admin admin) {
		EntityManager em = entityManagerService.getEntityManager();
		Organization organization = em.find(Organization.class, organizationId);
		if (organization == null)
			throw new OrganizationNotFoundException(organizationId);

		if (requestAdminId != null) {
			Admin requestAdmin = em.find(Admin.class, requestAdminId);
			if (requestAdmin.getRole().getLevel() < admin.getRole().getLevel())
				throw new SecurityException("create admin");
		}

		if (admin.getLang() == null)
			admin.setLang("en");
		admin.setCreateDateTime(new Date());
		if (admin.isUseOtp())
			admin.setOtpSeed(createOtpSeed());

		em.persist(admin);

		// add acl
		for (AdminTrustHost host : admin.getTrustHosts()) {
			host.setAdmin(admin);
			em.persist(host);
		}
	}

	@Transactional
	@Override
	public Admin getAdmin(int organizationId, int adminId) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = em.find(Admin.class, adminId);
		if (admin == null || admin.getUser().getOrganization().getId() != organizationId)
			return null;

		// enforce lazy loading
		admin.getRole().getPermissions().size();

		return admin;
	}

	@Transactional
	@Override
	public Admin getAdminByLoginName(String loginName) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = (Admin) em.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.loginName = ?")
				.setParameter(1, loginName).getSingleResult();
		return admin;
	}

	@Transactional
	@Override
	public Admin getAdminByUser(int organizationId, int userId) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			Admin admin = (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ? AND u.id = ?")
					.setParameter(1, organizationId).setParameter(2, userId).getSingleResult();

			admin.getTrustHosts().size();

			return admin;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Admin> getAdmins(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ?")
				.setParameter(1, organizationId).getResultList();
	}

	@Override
	public boolean matchPassword(int organizationId, int adminId, String password) {
		Admin admin = getAdmin(organizationId, adminId);
		if (admin == null)
			throw new AdminNotFoundException(adminId);

		String hashedPassword = hashPassword(admin.getUser().getSalt(), password);
		return admin.getUser().getPassword().equals(hashedPassword);
	}

	@Override
	public String hashPassword(String salt, String text) {
		return Sha1.hashPassword(salt, text);
	}

	@Override
	public String hash(String text) {
		return Sha1.hash(text);
	}

	@Override
	public void removeAdmin(int organizationId, Integer requestAdminId, int adminId) {
		Admin admin = removeAdminInternal(organizationId, requestAdminId, adminId);
		fireEntityRemoved(admin);
	}

	@Transactional
	private Admin removeAdminInternal(int organizationId, Integer requestAdminId, int adminId) {
		EntityManager em = entityManagerService.getEntityManager();

		Admin admin = (Admin) em.find(Admin.class, adminId);
		if (admin.getUser().getOrganization().getId() != organizationId)
			throw new AdminNotFoundException(adminId);

		if (requestAdminId != null) {
			Admin requestAdmin = em.find(Admin.class, requestAdminId);
			if (requestAdmin == null)
				throw new AdminNotFoundException(requestAdminId);

			if (requestAdmin.getRole().getLevel() < admin.getRole().getLevel())
				throw new SecurityException("remove admin");

			if (requestAdminId == adminId) {
				throw new CannotRemoveRequestingAdminException(requestAdminId);
			}
		}

		em.remove(admin);

		return admin;
	}

	@Override
	public void updateAdmin(int organizationId, Integer requestAdminId, Admin targetAdmin) {
		Admin admin = updateAdminInternal(organizationId, requestAdminId, targetAdmin);
		fireEntityUpdated(admin);
	}

	@Transactional
	private Admin updateAdminInternal(int organizationId, Integer requestAdminId, Admin targetAdmin) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			if (requestAdminId != null) {
				Admin requestAdmin = em.find(Admin.class, requestAdminId);
				if (requestAdmin == null)
					throw new AdminNotFoundException(requestAdminId);

				if (requestAdmin.getRole().getLevel() < targetAdmin.getRole().getLevel())
					throw new SecurityException("update admin");
			}

			Admin admin = (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ? AND a.id = ?")
					.setParameter(1, organizationId).setParameter(2, targetAdmin.getId()).getSingleResult();

			Role role = em.find(Role.class, targetAdmin.getRole().getId());

			admin.setRole(role);
			admin.setLastLoginDateTime(targetAdmin.getLastLoginDateTime());
			admin.setProgramProfile(targetAdmin.getProgramProfile());
			admin.setUseIdleTimeout(targetAdmin.isUseIdleTimeout());
			admin.setUseLoginLock(targetAdmin.isUseLoginLock());
			admin.setIdleTimeout(targetAdmin.getIdleTimeout());
			admin.setLoginLockCount(targetAdmin.getLoginLockCount());
			admin.setUseOtp(targetAdmin.isUseOtp());
			if (admin.isUseOtp()) {
				if (targetAdmin.getOtpSeed() != null)
					admin.setOtpSeed(targetAdmin.getOtpSeed());
				else if (admin.getOtpSeed() == null)
					admin.setOtpSeed(createOtpSeed());
			} else
				admin.setOtpSeed(null);

			admin.setUseAcl(targetAdmin.isUseAcl());

			if (!admin.isEnabled() && targetAdmin.isEnabled())
				admin.setLoginFailures(0);

			admin.setEnabled(targetAdmin.isEnabled());

			// reset acl
			for (AdminTrustHost h : admin.getTrustHosts())
				em.remove(h);

			for (AdminTrustHost h : targetAdmin.getTrustHosts()) {
				h.setAdmin(admin);
				em.persist(h);
			}

			em.merge(admin);

			return admin;
		} catch (NoResultException e) {
			throw new AdminNotFoundException(targetAdmin.getId());
		}
	}

	private static final char[] chars = new char[62];
	static {
		int i = 0;
		char c = 'a';
		for (; i < 26; i++)
			chars[i] = c++;
		c = 'A';
		for (; i < 52; i++)
			chars[i] = c++;
		c = '0';
		for (; i < 62; i++)
			chars[i] = c++;
	}

	private String createOtpSeed() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++)
			sb.append(chars[random.nextInt(62)]);
		return sb.toString();
	}
}
