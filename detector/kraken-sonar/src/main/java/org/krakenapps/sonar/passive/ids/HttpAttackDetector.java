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
 package org.krakenapps.sonar.passive.ids;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.malwaredomains.MalwareDomain;
import org.krakenapps.malwaredomains.MalwareDomainService;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.http.DefaultHttpProcessor;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.krakenapps.sonar.passive.ids.checker.InjectionChecker;
import org.krakenapps.sonar.passive.ids.rule.Rule;
import org.krakenapps.sonar.passive.safebrowsing.GoogleSafeBrowsing;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sonar-http-attack-detector")
@Provides
public class HttpAttackDetector extends DefaultHttpProcessor {
	private final Logger logger = LoggerFactory.getLogger(HttpAttackDetector.class.getName());

	@Requires
	private PassiveScanner scanner;

	@Requires
	private MalwareDomainService malwareDomainService;

	private InjectionChecker injectionChecker;
	private GoogleSafeBrowsing gsb;

	final String DATA_PATH = System.getProperty("kraken.data.dir") + "/kraken-sonar/ids-rules/http/";

	@Validate
	public void start() {
		// Create Data Location
		prepareDataPath();

		// Load RFI Injection script
		injectionChecker = new InjectionChecker();
		injectionChecker.setHomeDir(DATA_PATH);
		injectionChecker.load();

		// Load GoogleSafeBrowsing
		gsb = new GoogleSafeBrowsing(DATA_PATH);
		gsb.update();

		scanner.addTcpSniffer(Protocol.HTTP, this);

		logger.info("kraken sonar: http attack detector started.");
	}

	private void prepareDataPath() {
		File dir = new File(DATA_PATH);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
	}

	@Invalidate
	public void stop() {
		if (scanner != null)
			scanner.removeTcpSniffer(Protocol.HTTP, this);
	}

	@Override
	public void onRequest(HttpRequest req) {
		String rawUrl = req.getURL().toString();
		String url = normalizeUrl(rawUrl);

		logger.trace("kraken sonar: check url {}", url);

		List<Rule> injections = injectionChecker.check(url);
		if (!injections.isEmpty()) {
			trace("Injection", rawUrl, injections);
			// alert(rawUrl, "Injection",
			// metabase.updateIpEndPoint(req.getLocalAddress()));
		}

		MalwareDomain malwareDomain = malwareDomainService.match(req.getURL());
		if (malwareDomain != null) {
			logger.info("kraken sonar: malware domain detected [{}]", malwareDomain);
		}

		int result = gsb.SafeCheck(url);
		if (result == 1) {
			// alert(rawUrl, "GSB-Malware",
			// metabase.updateIpEndPoint(req.getLocalAddress()));
		}
		if (result == 2) {
			// alert(rawUrl, "GSB-BlackList",
			// metabase.updateIpEndPoint(req.getLocalAddress()));
		}
	}

	@Override
	public void onResponse(HttpRequest req, HttpResponse resp) {
	}

	private static String normalizeUrl(String originalURL) {
		String url = "";
		String[] tempSplit = originalURL.split("/");
		String tempToken = tempSplit[0];
		for (int i = 1; i < tempSplit.length; ++i) {
			if (tempSplit[i].equals(".")) {
				// this token "./" found! -> remove this token
			} else if (tempSplit[i].equals("..")) {
				// next token "../" found! -> remove this token and next token
				tempToken = "[DEL]";
			} else {
				if (tempToken.equals("[DEL]") == false) {
					url += tempToken + "/";
				}
				tempToken = tempSplit[i];
			}
		}
		if (tempToken.isEmpty() == false) {
			url += tempToken + "/";
		}
		return url;
	}

	private void trace(String title, String url, List<Rule> result) {
		if (result.isEmpty() == false) {
			System.out.println(title + " found! - '" + url + "'");
			for (Rule r : result)
				System.out.println("    >> " + r);
		}
	}
}
