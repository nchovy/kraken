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
package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LookupHandler;

public class Lookup extends LogQueryCommand {
	private LogQueryService service;
	private String tableName;
	private String srcField;
	private String localSrcField;
	private String dstField;
	private String localDstField;

	public Lookup(String tableName, String srcField, String dstField) {
		this(tableName, srcField, srcField, dstField, dstField);
	}

	public Lookup(String tableName, String localSrcField, String srcField, String dstField, String localDstField) {
		this.tableName = tableName;
		this.srcField = srcField;
		this.localSrcField = localSrcField;
		this.dstField = dstField;
		this.localDstField = localDstField;
	}

	public void setLogQueryService(LogQueryService service) {
		this.service = service;
	}

	@Override
	public void push(Map<String, Object> m) {
		Object value = getData(localSrcField, m);
		LookupHandler handler = service.getLookupHandler(tableName);
		if (handler != null)
			m.put(localDstField, handler.lookup(srcField, dstField, value));
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}
