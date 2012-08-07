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

public class LdapOrgUnit {
	private String distinguishedName;
	private String name;
	private Date whenCreated;
	private Date whenChanged;

	@SuppressWarnings("unused")
	private LdapOrgUnit() {
		// for primitive parse
	}

	public LdapOrgUnit(LDAPEntry entry) {
		LDAPAttributeSet attrs = entry.getAttributeSet();
		this.distinguishedName = getString(attrs, "distinguishedName");
		this.name = getString(attrs, "name");
		if (this.name == null)
			this.name = getString(attrs, "ou");
		
		this.whenCreated = getDate(attrs, "whenCreated");
		this.whenChanged = getDate(attrs, "whenChanged");
	}

	private String getString(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : attr.getStringValue();
	}

	private Date getDate(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : DateFormat.parse("yyyyMMddHHmmss", attr.getStringValue());
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public String getName() {
		return name;
	}

	public Date getWhenCreated() {
		return whenCreated;
	}

	public Date getWhenChanged() {
		return whenChanged;
	}

	@Override
	public String toString() {
		return String.format("name=%s, whenCreated=%s, whenChanged=%s", name,
				DateFormat.format("yyyy-MM-dd HH:mm:ss", whenCreated), DateFormat.format("yyyy-MM-dd HH:mm:ss", whenChanged));
	}
}
