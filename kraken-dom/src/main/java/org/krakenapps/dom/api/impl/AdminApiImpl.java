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
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.LoginCallback;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OtpApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.Session;

@Component(name = "dom-admin-api")
@Provides
public class AdminApiImpl implements AdminApi {
	private static final Class<User> cls = User.class;
	private static final String EXT_KEY = "admin";
	private static final String NOT_FOUND = "admin-not-found";
	private static final String ALREADY_EXIST = "admin-already-setted";
	private static final String LOCKED_ADMIN = "locked-admin";

	@Requires
	private ConfigManager cfg;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private UserApi userApi;

	@Requires
	private PushApi pushApi;

	@Requires(optional = true, nullable = false)
	private OtpApi otpApi;

	private Set<LoginCallback> callbacks = new HashSet<LoginCallback>();
	private PriorityQueue<LoggedInAdmin> loggedIn = new PriorityQueue<LoggedInAdmin>(11, new LoggedInAdminComparator());

	private Predicate getPred() {
		return Predicates.not(Predicates.field("ext/admin", null));
	}

	private Predicate getPred(String loginName) {
		return Predicates.and(getPred(), Predicates.field("loginName", loginName));
	}

	@Override
	public Collection<Admin> getAdmins(String domain) {
		Collection<Admin> admins = new ArrayList<Admin>();
		for (User user : cfg.all(domain, cls, getPred()))
			admins.add(parseAdmin(domain, user));
		return admins;
	}

	@Override
	public Admin findAdmin(String domain, String loginName) {
		return parseAdmin(domain, cfg.find(domain, cls, getPred(loginName)));
	}

	@Override
	public Admin getAdmin(String domain, String loginName) {
		return parseAdmin(domain, cfg.get(domain, cls, getPred(loginName), NOT_FOUND));
	}

	@Override
	public Admin getAdmin(String domain, User user) {
		Object o = user.getExt().get(EXT_KEY);
		if (o == null)
			return null;

		if (o instanceof Admin)
			return (Admin) o;
		else if (o instanceof Map)
			return parseAdmin(domain, user);

		return null;
	}

	private Admin parseAdmin(String domain, User user) {
		Admin admin = PrimitiveConverter.parse(Admin.class, user.getExt().get(EXT_KEY), cfg.getParseCallback(domain));
		admin.setUser(user);
		return admin;
	}

	@Override
	public void setAdmin(String domain, String requestAdminLoginName, String targetUserLoginName, Admin admin) {
		checkPermissionLevel(domain, requestAdminLoginName, admin, "set-admin-permission-denied");

		if (findAdmin(domain, targetUserLoginName) != null)
			throw new DOMException(ALREADY_EXIST);

		prepare(admin);
		User target = userApi.getUser(domain, targetUserLoginName);
		target.getExt().put(EXT_KEY, admin);
		userApi.updateUser(domain, target);
	}

	@Override
	public void updateAdmin(String domain, String requestAdminLoginName, String targetUserLoginName, Admin admin) {
		checkPermissionLevel(domain, requestAdminLoginName, admin, "update-admin-permission-denied");

		prepare(admin);
		User target = getAdmin(domain, targetUserLoginName).getUser();
		target.getExt().put(EXT_KEY, admin);
		userApi.updateUser(domain, target);
	}

	@Override
	public String updateOtpSeed(String domain, String requestAdminLoginName, String targetUserLoginName) {
		Admin admin = getAdmin(domain, targetUserLoginName);
		String newSeed = createOtpSeed();
		admin.setOtpSeed(newSeed);
		updateAdmin(domain, requestAdminLoginName, targetUserLoginName, admin);
		return newSeed;
	}

	private void prepare(Admin admin) {
		if (admin.getLang() == null)
			admin.setLang("en");
		if (admin.isUseOtp() && admin.getOtpSeed() == null)
			admin.setOtpSeed(createOtpSeed());
		else if (!admin.isUseOtp())
			admin.setOtpSeed(null);
	}

	private static final char[] chars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	private String createOtpSeed() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++)
			sb.append(chars[random.nextInt(chars.length)]);
		return sb.toString();
	}

	@Override
	public void unsetAdmin(String domain, String requestAdminLoginName, String targetUserLoginName) {
		if (requestAdminLoginName.equals(targetUserLoginName))
			throw new DOMException("cannot-remove-requesting-admin");

		Admin target = getAdmin(domain, targetUserLoginName);
		checkPermissionLevel(domain, requestAdminLoginName, target, "unset-admin-permission-denied");

		User user = target.getUser();
		user.getExt().remove(EXT_KEY);
		userApi.updateUser(domain, user);
	}

	private void checkPermissionLevel(String domain, String requestAdminLoginName, Admin admin, String exceptionMessage) {
		Admin request = findAdmin(domain, requestAdminLoginName);
		if (requestAdminLoginName == null || request == null)
			throw new DOMException("request-admin-not-found");
		if (request.getRole().getLevel() < admin.getRole().getLevel())
			throw new DOMException(exceptionMessage);
	}

	@Override
	public Admin login(Session session, String loginName, String hash, boolean force) {
		String domain = session.getOrgDomain();
		Admin admin = getAdmin(domain, loginName);

		try {
			checkAcl(session, admin);

			if (!admin.isEnabled()) {
				int lockTime = 180;
				Object param = orgApi.getOrganizationParameter(domain, "login_lock_time");
				if (param != null) {
					try {
						lockTime = (Integer) param;
					} catch (NumberFormatException e) {
					}
				}
				Calendar c = Calendar.getInstance();
				c.add(Calendar.SECOND, -lockTime);

				Date failed = admin.getLastLoginFailedDateTime();
				if (failed != null && failed.after(c.getTime()))
					throw new DOMException(LOCKED_ADMIN);
			}

			String password = null;
			if (otpApi != null && admin.isUseOtp())
				password = Sha1.hash(otpApi.getOtpValue(admin.getOtpSeed()));
			else
				password = admin.getUser().getPassword();

			if (!hash.equals(Sha1.hash(password + session.getString("nonce")))) {
				if (admin.isUseOtp())
					throw new DOMException("invalid-otp-password");
				else
					throw new DOMException("invalid-password");
			}

			Integer maxSession = orgApi.getOrganizationParameter(domain, "max_sessions", Integer.class);
			if (maxSession != null) {
				if (maxSession > 0) {
					if (force) {
						while (loggedIn.size() >= maxSession) {
							if (loggedIn.peek().level > admin.getRole().getLevel())
								throw new DOMException("max-session");

							LoggedInAdmin ban = loggedIn.poll();
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("type", "terminate");
							m.put("kick_by", admin.getUser().getLoginName());
							pushApi.push(ban.session, "kraken-system-event", m);

							long wait = System.currentTimeMillis() + 5000;
							while (loggedIn.contains(ban) && System.currentTimeMillis() < wait) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
							}
							ban.session.close();
						}
					} else if (loggedIn.size() >= maxSession) {
						LoggedInAdmin peek = loggedIn.peek();
						Map<String, Object> m = new HashMap<String, Object>();
						m.put("login_name", peek.loginName);
						m.put("session_id", peek.session.getId());
						m.put("ip", session.getRemoteAddress().getHostAddress());
						throw new DOMException("max-session", m);
					}
				}
			}

			updateLoginFailures(domain, admin, true);
			loggedIn.add(new LoggedInAdmin(admin.getRole().getLevel(), new Date(), session, admin.getUser().getLoginName()));

			for (LoginCallback callback : callbacks)
				callback.onLoginSuccess(admin, session);

			return admin;
		} catch (DOMException e) {
			updateLoginFailures(domain, admin, false);
			for (LoginCallback callback : callbacks) {
				if (e.getErrorCode().equals(LOCKED_ADMIN))
					callback.onLoginLocked(admin, session);
				else
					callback.onLoginFailed(admin, session, e);
			}
			throw e;
		}
	}

	private void checkAcl(Session session, Admin admin) {
		if (!admin.isUseAcl())
			return;

		String remote = session.getRemoteAddress().getHostAddress();
		for (String host : admin.getTrustHosts()) {
			if (host != null && host.equals(remote))
				return;
		}
		throw new DOMException("not-trust-host");
	}

	private void updateLoginFailures(String domain, Admin admin, boolean success) {
		if (success) {
			admin.setLastLoginDateTime(new Date());
			admin.setLoginFailures(0);
			admin.setEnabled(true);
		} else {
			admin.setLastLoginFailedDateTime(new Date());
			admin.setLoginFailures(admin.getLoginFailures() + 1);
			if (admin.isUseLoginLock() && admin.getLoginFailures() >= admin.getLoginLockCount())
				admin.setEnabled(false);
		}
		String loginName = admin.getUser().getLoginName();
		updateAdmin(domain, loginName, loginName, admin);
	}

	@Override
	public void logout(Session session) {
		if (session.getOrgDomain() != null) {
			Admin admin = getAdmin(session.getOrgDomain(), session.getAdminLoginName());
			loggedIn.remove(new LoggedInAdmin(session));
			for (LoginCallback callback : callbacks)
				callback.onLogout(admin, session);
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
	public void registerLoginCallback(LoginCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void unregisterLoginCallback(LoginCallback callback) {
		callbacks.remove(callback);
	}
}
