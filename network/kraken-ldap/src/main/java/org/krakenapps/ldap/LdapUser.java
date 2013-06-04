/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ldap;

import java.util.Date;

import org.krakenapps.api.DateFormat;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

public class LdapUser {
	private String accountName;
	private boolean domainAdmin;
	private boolean allowDialIn;
	private int logonCount;
	private int userAccountControl;
	private String[] memberOf;
	private String distinguishedName;
	private String userPrincipalName;
	private String organizationUnitName;
	private String displayName;
	private String surname;
	private String givenName;
	private String title;
	private String department;
	private String departmentNumber;
	private String mail;
	private String mobile;
	private Date lastLogon;
	private Date whenCreated;
	private Date pwdLastSet;
	private Date accountExpires;

	@SuppressWarnings("unused")
	private LdapUser() {
		// for primitive parse
	}

	public LdapUser(LDAPEntry entry, String idAttr) {
		LDAPAttributeSet attrs = entry.getAttributeSet();
		this.accountName = getString(attrs, "sAMAccountName");
		if (accountName == null)
			accountName = getString(attrs, idAttr);
		this.domainAdmin = getInt(attrs, "adminCount") > 0;
		this.userAccountControl = getInt(attrs, "userAccountControl");
		this.allowDialIn = "TRUE".equals(getString(attrs, "msNPAllowDialin"));
		this.logonCount = getInt(attrs, "logonCount");
		this.memberOf = getStringArray(attrs, "memberOf");
		this.distinguishedName = getString(attrs, "distinguishedName");
		this.userPrincipalName = getString(attrs, "userPrincipalName");
		this.displayName = getString(attrs, "displayName");
		if (displayName == null)
			displayName = getString(attrs, "cn");
		this.surname = getString(attrs, "sn");
		this.givenName = getString(attrs, "givenName");
		this.title = getString(attrs, "title");
		this.department = getString(attrs, "department");
		this.departmentNumber = getString(attrs, "departmentNumber");
		this.mail = getString(attrs, "mail");
		this.mobile = getString(attrs, "mobile");
		this.lastLogon = getTimestamp(attrs, "lastLogon");
		this.whenCreated = getDate(attrs, "whenCreated");
		this.pwdLastSet = getTimestamp(attrs, "pwdLastSet");
		long expire = getLong(attrs, "accountExpires");
		if (expire != 0L && expire != 0x7FFFFFFFFFFFFFFFL)
			this.accountExpires = getTimestamp(attrs, "accountExpires");

		if (distinguishedName != null) {
			for (String token : distinguishedName.split("(?<!\\\\),")) {
				String attr = token.split("=")[0];
				String value = token.split("=")[1];
				if (attr.equals("OU")) {
					this.organizationUnitName = value;
					break;
				}
			}
		}
	}

	private int getInt(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? 0 : Integer.parseInt(attr.getStringValue());
	}

	private long getLong(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? 0L : Long.parseLong(attr.getStringValue());
	}

	private Date getDate(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : DateFormat.parse("yyyyMMddHHmmss", attr.getStringValue());
	}

	private Date getTimestamp(LDAPAttributeSet attrs, String attrName) {
		Long attr = getLong(attrs, attrName);
		return (attr == null) ? null : new Date(attr / 10000L - 11644473600000L);
	}

	private String getString(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : attr.getStringValue();
	}

	private String[] getStringArray(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : attr.getStringValueArray();
	}

	public String getAccountName() {
		return accountName;
	}

	public boolean isDomainAdmin() {
		return domainAdmin;
	}

	public boolean isAllowDialIn() {
		return allowDialIn;
	}

	public int getLogonCount() {
		return logonCount;
	}

	public int getUserAccountControl() {
		return userAccountControl;
	}

	public String[] getMemberOf() {
		return memberOf;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public String getOrganizationUnitName() {
		return organizationUnitName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSurname() {
		return surname;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getTitle() {
		return title;
	}

	public String getDepartment() {
		return department;
	}

	public String getDepartmentNumber() {
		return departmentNumber;
	}

	public String getMail() {
		return mail;
	}

	public String getMobile() {
		return mobile;
	}

	public Date getLastLogon() {
		return lastLogon;
	}

	public Date getWhenCreated() {
		return whenCreated;
	}

	public Date getPwdLastSet() {
		return pwdLastSet;
	}

	public Date getAccountExpires() {
		return accountExpires;
	}

	@Override
	public String toString() {
		return String.format("account=%s, name=%s, title=%s, dept=%s, mail=%s", accountName, displayName, nullToEmpty(title),
				nullToEmpty(department), nullToEmpty(mail));
	}

	private String nullToEmpty(String str) {
		return (str == null) ? "" : str;
	}
}
