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
package org.krakenapps.dom.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.MapTypeHint;
import org.krakenapps.api.ReferenceKey;

public class Admin {
	@FieldOption(skip = true)
	private User user;

	@FieldOption(nullable = false)
	@ReferenceKey("name")
	private Role role;

	@ReferenceKey("name")
	private ProgramProfile profile;

	@FieldOption(length = 16)
	private String lang;

	@FieldOption(nullable = false)
	private Date created = new Date();

	private boolean useLoginLock;
	private int loginLockCount;
	private int loginFailures;
	private Date lastLoginFailedDateTime;
	private boolean useIdleTimeout;
	private int idleTimeout; // in seconds
	private Date lastLoginDateTime;
	private boolean isEnabled;
	private boolean useOtp;
	private String otpSeed;
	private boolean useAcl;

	@CollectionTypeHint(String.class)
	private List<String> trustHosts = new ArrayList<String>();

	@MapTypeHint({ String.class, Object.class })
	private Map<String, Object> settings = new HashMap<String, Object>();

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public ProgramProfile getProfile() {
		return profile;
	}

	public void setProfile(ProgramProfile profile) {
		this.profile = profile;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public boolean isUseLoginLock() {
		return useLoginLock;
	}

	public void setUseLoginLock(boolean useLoginLock) {
		this.useLoginLock = useLoginLock;
	}

	public int getLoginLockCount() {
		return loginLockCount;
	}

	public void setLoginLockCount(int loginLockCount) {
		this.loginLockCount = loginLockCount;
	}

	public int getLoginFailures() {
		return loginFailures;
	}

	public void setLoginFailures(int loginFailures) {
		this.loginFailures = loginFailures;
	}

	public Date getLastLoginFailedDateTime() {
		return lastLoginFailedDateTime;
	}

	public void setLastLoginFailedDateTime(Date lastLoginFailedDateTime) {
		this.lastLoginFailedDateTime = lastLoginFailedDateTime;
	}

	public boolean isUseIdleTimeout() {
		return useIdleTimeout;
	}

	public void setUseIdleTimeout(boolean useIdleTimeout) {
		this.useIdleTimeout = useIdleTimeout;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public Date getLastLoginDateTime() {
		return lastLoginDateTime;
	}

	public void setLastLoginDateTime(Date lastLoginDateTime) {
		this.lastLoginDateTime = lastLoginDateTime;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isUseOtp() {
		return useOtp;
	}

	public void setUseOtp(boolean useOtp) {
		this.useOtp = useOtp;
	}

	public String getOtpSeed() {
		return otpSeed;
	}

	public void setOtpSeed(String otpSeed) {
		this.otpSeed = otpSeed;
	}

	public boolean isUseAcl() {
		return useAcl;
	}

	public void setUseAcl(boolean useAcl) {
		this.useAcl = useAcl;
	}

	public List<String> getTrustHosts() {
		return trustHosts;
	}

	public void setTrustHosts(List<String> trustHosts) {
		this.trustHosts = trustHosts;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}
}