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
package org.krakenapps.logdb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.query.command.Fields;

public class LogQueryHelper {
	private LogQueryHelper() {
	}

	public static List<Object> getQueries(LogQueryService service) {
		List<Object> result = new ArrayList<Object>();
		for (LogQuery lq : service.getQueries()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", lq.getId());
			m.put("query_string", lq.getQueryString());
			m.put("is_end", lq.isEnd());
			result.add(m);
		}
		return result;
	}

	public static Map<String, Object> getResultData(LogQueryService qs, int id, int offset, int limit) {
		LogQuery query = qs.getQuery(id);
		if (query != null) {
			Map<String, Object> m = new HashMap<String, Object>();

			m.put("result", query.getResult(offset, limit));
			m.put("count", query.getResult().size());

			Fields fields = null;
			for (LogQueryCommand command : query.getCommands()) {
				if (command instanceof Fields) {
					if (!((Fields) command).isRemove())
						fields = (Fields) command;
				}
			}
			if (fields != null)
				m.put("fields", fields.getFields());

			return m;
		}
		return null;
	}

}
