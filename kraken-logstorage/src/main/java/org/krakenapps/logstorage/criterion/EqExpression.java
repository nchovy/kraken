/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.criterion;

import org.krakenapps.logstorage.Log;

public class EqExpression implements Criterion {
	private String field;
	private String expect;

	public EqExpression(String field, String expect) {
		this.field = field;
		this.expect = expect;
	}

	public String getExpect() {
		return expect;
	}

	@Override
	public boolean match(Log log) {
		try {
			String value = (String) log.getData().get(field);
			if (value == null)
				return false;

			return value.equalsIgnoreCase(expect);
		} catch (Exception e) {
			return false;
		}
	}
}
