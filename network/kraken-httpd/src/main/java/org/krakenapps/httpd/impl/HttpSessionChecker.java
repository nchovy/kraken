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
package org.krakenapps.httpd.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "httpd-session-checker")
@Provides
@PeriodicJob("* * * * *")
public class HttpSessionChecker implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(HttpSessionChecker.class.getName());

	@Requires
	private HttpService httpd;

	@Override
	public void run() {
		HttpContextRegistry r = httpd.getContextRegistry();
		for (String name : r.getContextNames()) {
			HttpContext ctx = r.findContext(name);
			checkContext(ctx);
		}
	}

	private void checkContext(HttpContext ctx) {
		List<String> evicts = new ArrayList<String>();

		long now = new Date().getTime();
		ConcurrentMap<String, HttpSession> sessions = ctx.getHttpSessions();
		for (String key : sessions.keySet()) {
			HttpSession session = sessions.get(key);
			long idle = (now - session.getLastAccessedTime()) / 1000;
			long max = session.getMaxInactiveInterval();
			logger.trace("kraken httpd: checking sesison [{}] idle [{}]", key, idle);

			// never timeout
			if (max <= 0)
				continue;

			// over timeout seconds
			if (idle >= max) {
				logger.trace("kraken httpd: evict sesison [{}] by timeout [{}]", key, max);
				evicts.add(key);
			}
		}

		// clean all timeouts
		for (String key : evicts)
			sessions.remove(key);
	}
}
