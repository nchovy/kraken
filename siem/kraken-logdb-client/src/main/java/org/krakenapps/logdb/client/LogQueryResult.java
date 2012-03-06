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
package org.krakenapps.logdb.client;

import java.util.Iterator;
import java.util.List;

public class LogQueryResult {
	private int offset;
	private int limit;
	private int totalCount;
	private List<Object> result;

	public LogQueryResult(int offset, int limit, int totalCount, List<Object> result) {
		this.offset = offset;
		this.limit = limit;
		this.totalCount = totalCount;
		this.result = result;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public Iterator<Object> getResult() {
		return result.iterator();
	}
}
