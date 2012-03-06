/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.httpd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("virtual_hosts")
public class VirtualHost {
	/**
	 * http context name. context name is virtual host identifier.
	 */
	@FieldOption(nullable = false)
	private String httpContextName;

	/**
	 * hostname regex patterns
	 */
	@CollectionTypeHint(String.class)
	private List<String> hostNames;

	@FieldOption(skip = true)
	private List<Pattern> hostNamePatterns;

	public VirtualHost() {
		hostNames = new ArrayList<String>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((httpContextName == null) ? 0 : httpContextName.hashCode());
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
		VirtualHost other = (VirtualHost) obj;
		if (httpContextName == null) {
			if (other.httpContextName != null)
				return false;
		} else if (!httpContextName.equals(other.httpContextName))
			return false;
		return true;
	}

	public boolean matches(String host) {
		for (Pattern p : hostNamePatterns)
			if (p.matcher(host).matches())
				return true;

		return false;
	}

	public String getHttpContextName() {
		return httpContextName;
	}

	public void setHttpContextName(String httpContextName) {
		this.httpContextName = httpContextName;
	}

	public List<String> getHostNames() {
		return Collections.unmodifiableList(hostNames);
	}

	public void setHostNames(List<String> hostNames) {
		this.hostNames = hostNames;

		hostNamePatterns = new ArrayList<Pattern>();
		for (String name : hostNames) {
			hostNamePatterns.add(Pattern.compile(name));
		}
	}

	@Override
	public String toString() {
		return "virtual host: " + hostNames + ", context: " + httpContextName;
	}
}
