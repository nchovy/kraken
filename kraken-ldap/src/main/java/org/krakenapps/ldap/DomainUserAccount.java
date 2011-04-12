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
import java.util.Set;
import java.util.TreeSet;

public class DomainUserAccount {
	private boolean domainAdmin;
	private boolean allowDialIn;
	private int loginCount;
	private Set<String> memberOf = new TreeSet<String>();
	private String distinguishedName;
	private String userPrincipalName;
	private String organizationUnitName;
	private String displayName;
	private String surname;
	private String givenName;
	private String accountName;
	private String title;
	private String department;
	private String mail;
	private String mobile;
	private Date lastLogon;
	private Date whenCreated;
	private Date lastPasswordChange;
	private Date accountExpires;

	public boolean isDomainAdmin() {
		return domainAdmin;
	}

	public void setDomainAdmin(boolean domainAdmin) {
		this.domainAdmin = domainAdmin;
	}

	public boolean isAllowDialIn() {
		return allowDialIn;
	}

	public void setAllowDialIn(boolean allowDialIn) {
		this.allowDialIn = allowDialIn;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public Set<String> getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(Set<String> memberOf) {
		this.memberOf = memberOf;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	public String getOrganizationUnitName() {
		return organizationUnitName;
	}

	public void setOrganizationUnitName(String organizationUnitName) {
		this.organizationUnitName = organizationUnitName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Date getLastLogon() {
		return lastLogon;
	}

	public void setLastLogon(Date lastLogon) {
		this.lastLogon = lastLogon;
	}

	public Date getWhenCreated() {
		return whenCreated;
	}

	public void setWhenCreated(Date whenCreated) {
		this.whenCreated = whenCreated;
	}

	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}

	public void setLastPasswordChange(Date lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}

	public Date getAccountExpires() {
		return accountExpires;
	}

	public void setAccountExpires(Date accountExpires) {
		this.accountExpires = accountExpires;
	}

	@Override
	public String toString() {
		return String.format("account=%s, name=%s, title=%s, dept=%s", accountName, displayName, nullToEmpty(title),
				nullToEmpty(department));
	}

	private String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

}
