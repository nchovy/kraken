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
package org.krakenapps.rule;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractRule implements Rule {
	private String type;
	private String id;
	private String message;
	private Collection<String> cveNames;
	private Collection<URL> references;

	public AbstractRule(String type, String id, String message) {
		this.type = type;
		this.id = id;
		this.message = message;
		this.cveNames = new ArrayList<String>();
		this.references = new ArrayList<URL>();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Collection<String> getCveNames() {
		return cveNames;
	}

	@Override
	public Collection<URL> getReferences() {
		return references;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (String cveName : cveNames) {
			sb.append(", cve=");
			sb.append(cveName);
		}

		for (URL reference : references) {
			sb.append(", reference=");
			sb.append(reference.toString());
		}

		return String.format("type=%s, id=%s, msg=%s%s", type, id, message, sb.toString());
	}
}
