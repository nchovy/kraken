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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_admins")
public class Admin implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@ManyToOne
	@JoinColumn(name = "profile_id", nullable = false)
	private ProgramProfile programProfile;

	@Column(length = 16)
	private String lang;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@Column(name = "last_login_at")
	private Date lastLoginDateTime;

	@Column(name = "is_enabled", nullable = false)
	private boolean isEnabled;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "admin")
	private List<Widget> widgets = new ArrayList<Widget>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "admin")
	private List<AdminSetting> settings = new ArrayList<AdminSetting>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public ProgramProfile getProgramProfile() {
		return programProfile;
	}

	public void setProgramProfile(ProgramProfile programProfile) {
		this.programProfile = programProfile;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}

	public List<AdminSetting> getSettings() {
		return settings;
	}

	public void setSettings(List<AdminSetting> settings) {
		this.settings = settings;
	}

	public void validate() throws IllegalArgumentException {
		if (role == null)
			throw new IllegalArgumentException("role");
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("user_id", user.getId());
		m.put("role_id", role.getId());
		m.put("role", role.getName());
		m.put("profile_id", programProfile.getId());
		m.put("created_at", dateFormat.format(createDateTime));
		m.put("last_login", lastLoginDateTime == null ? null : dateFormat.format(lastLoginDateTime));
		m.put("is_enabled", isEnabled);
		return m;
	}
}