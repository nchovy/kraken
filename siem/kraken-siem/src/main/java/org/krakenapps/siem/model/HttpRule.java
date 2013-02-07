/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.krakenapps.msgbus.Marshalable;
import org.krakenapps.rule.Rule;

public class HttpRule implements Rule, Marshalable {
	private String name; // e.g. NCHOVY-2011-0001 format

	private String rule;

	private Date createDateTime;

	private Date updateDateTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRule() {
		return rule;
	}

	@Override
	public String toString() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	@Override
	public String getType() {
		return getToken("type");
	}

	@Override
	public String getId() {
		return getToken("id");
	}

	@Override
	public String getMessage() {
		return getToken("msg");
	}

	@Override
	public Collection<String> getCveNames() {
		Collection<String> cveNames = new ArrayList<String>();
		String cveName = getToken("cve");
		if (cveName != null)
			cveNames.add(cveName);
		return cveNames;
	}

	@Override
	public Collection<URL> getReferences() {
		Collection<URL> references = new ArrayList<URL>();
		String ref = getToken("reference");
		try {
			if (ref != null)
				references.add(new URL(ref));
		} catch (MalformedURLException e) {
		}
		return references;
	}

	private String getToken(String key) {
		String regex = "(?<=" + key + ":).+?(?=;)";
		Matcher m = Pattern.compile(regex).matcher(rule);
		if (!m.find())
			return null;
		return m.group().trim();
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", name);
		m.put("rule", rule);
		m.put("created_at", dateFormat.format(createDateTime));
		m.put("updated_at", dateFormat.format(updateDateTime));
		return m;
	}

}
