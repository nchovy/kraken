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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogResultSet;
import org.krakenapps.logdb.query.command.Fields;

public class LogQueryHelper {
	private LogQueryHelper() {
	}

	public static List<Object> getQueries(LogQueryService service) {
		List<Object> result = new ArrayList<Object>();
		for (LogQuery lq : service.getQueries()) {
			Long sec = null;
			if (lq.getLastStarted() != null)
				sec = new Date().getTime() - lq.getLastStarted().getTime();

			List<Object> commands = new ArrayList<Object>();

			if (lq.getCommands() != null) {
				for (LogQueryCommand cmd : lq.getCommands()) {
					Map<String, Object> c = new HashMap<String, Object>();
					c.put("command", cmd.getQueryString());
					c.put("status", cmd.getStatus());
					c.put("push_count", cmd.getPushCount());
					commands.add(c);
				}
			}

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", lq.getId());
			m.put("query_string", lq.getQueryString());
			m.put("is_end", lq.isEnd());
			m.put("last_started", lq.getLastStarted());
			m.put("elapsed", sec);
			m.put("commands", commands);

			result.add(m);
		}
		return result;
	}

	public static Map<String, Object> getResultData(LogQueryService qs, int id, int offset, int limit) throws IOException {
		LogQuery query = qs.getQuery(id);
		if (query != null) {
			Map<String, Object> m = new HashMap<String, Object>();

			m.put("result", getPage(query, offset, limit));
			m.put("count", query.getResultCount());

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

	private static List<Object> getPage(LogQuery query, int offset, int limit) throws IOException {
		List<Object> l = new LinkedList<Object>();
		LogResultSet rs = query.getResult();
		try {
			rs.skip(offset);

			long count = 0;
			while (rs.hasNext()) {
				if (count >= limit)
					break;

				l.add(rs.next());
				count++;
			}
		} finally {
			rs.close();
		}
		return l;
	}

}
