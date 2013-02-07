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
package org.krakenapps.siem.analyzer;

import java.util.Iterator;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventProvider;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.rule.http.HttpRequestContext;
import org.krakenapps.rule.http.HttpRequestRule;
import org.krakenapps.rule.http.HttpRuleEngine;
import org.krakenapps.rule.http.URLParser;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.NormalizedLog;
import org.krakenapps.siem.NormalizedLogListener;

@Component(name = "siem-http-attack-analyzer")
@Provides
public class HttpAttackAnalyzerEngine implements NormalizedLogListener, HttpAttackAnalyzer, EventProvider {
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpAttackAnalyzer.class.getName());

	@Requires
	private LogServer logServer;

	@Requires
	private EventDispatcher eventDispatcher;

	@Requires
	private HttpRuleEngine ruleEngine;

	@Override
	public String getName() {
		return "http-attack";
	}

	@Validate
	public void start() {
		logServer.addNormalizedLogListener("httpd", this);
	}

	@Invalidate
	public void stop() {
		if (logServer != null)
			logServer.removeNormalizedLogListener("httpd", this);
	}

	@Override
	public void onLog(NormalizedLog log) {
		String httpRequest = log.getString("request");
		String method = httpRequest.split(" ")[0];
		String url = httpRequest.split(" ")[1];

		HttpRequestContext req = URLParser.parse(method, url);
		HttpRequestRule rule = ruleEngine.match(req);

		if (rule == null)
			return;

		logger.trace("kraken siem: http attack detected! [{}] - {}", rule.getId(), httpRequest);

		Iterator<String> cve = rule.getCveNames().iterator();
		Event e = new Event();
		e.setCategory("Attack");
		e.setFirstSeen(log.getDate("date"));
		e.setLastSeen(log.getDate("date"));
		e.setSourceIp(log.getIp("src_ip"));
		e.setDestinationIp(log.getIp("dst_ip"));
		e.setDestinationPort(log.getInteger("dst_port"));
		e.setOrgDomain(log.getOrgDomain());
		e.setRule(rule.getId());
		e.setSeverity(EventSeverity.Critical); // TODO: severity from rule
		e.setMessageKey("http-attack");
		e.setDetail(httpRequest);
		e.setCve(cve.hasNext() ? cve.next() : null);
		e.setCount(1);

		eventDispatcher.dispatch(e);
	}

}
