/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.dns;

/**
 * @author mindori
 */
public class DnsQuery {
	private String domainName;
	private short queryType;
	private short queryClass;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public short getQueryType() {
		return queryType;
	}

	public void setQueryType(short queryType) {
		this.queryType = queryType;
	}

	public short getQueryClass() {
		return queryClass;
	}

	public void setQueryClass(short queryClass) {
		this.queryClass = queryClass;
	}

	@Override
	public String toString() {
		return String.format("%s, type %d, class %d", domainName, queryType, queryClass);
	}
	
	
}
