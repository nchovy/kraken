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
package org.krakenapps.logparser.syslog.juniper.attack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.krakenapps.logparser.syslog.internal.PatternFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JuniperAttackLogParser {
	private final Logger logger = LoggerFactory.getLogger(JuniperAttackLogParser.class.getName());
	private PatternFinder<JuniperAttackLogPattern> patternMap;

	public JuniperAttackLogParser() throws IOException {
		patternMap = getPatternMapDataFromStream(new InputStreamReader(
				JuniperAttackLogParser.class.getResourceAsStream("attack_log_format.txt")));
	}

	private JuniperAttackLogParser(PatternFinder<JuniperAttackLogPattern> patternMap) {
		this.patternMap = patternMap;
	}

	public static JuniperAttackLogParser newInstance() {
		try {
			return new JuniperAttackLogParser();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JuniperAttackLogParser newInstance(Reader reader) throws IOException {
		return new JuniperAttackLogParser(getPatternMapDataFromStream(reader));
	}

	private static PatternFinder<JuniperAttackLogPattern> getPatternMapDataFromStream(Reader reader) throws IOException {
		PatternFinder<JuniperAttackLogPattern> patternMap = PatternFinder.newInstance();

		BufferedReader br = new BufferedReader(reader);

		while (true) {
			if (br.readLine() == null)
				break;
			String category = br.readLine();
			if (category == null)
				break;
			if (br.readLine() == null)
				break;
			String patternString = br.readLine();
			if (patternString == null)
				break;
			JuniperAttackLogPattern pattern = JuniperAttackLogPattern.from(category, patternString);
			patternMap.register(pattern.getConstElements().get(0), pattern);
			if (br.readLine() == null)
				break;
		}
		return patternMap;
	}

	public Map<String, Object> parse(String line) {
		Set<JuniperAttackLogPattern> patterns = patternMap.find(line);
		for (JuniperAttackLogPattern pattern : patterns) {
			Map<String, Object> result = null;
			try {
				result = pattern.parse(line);
				if (result != null)
					return result;
			} catch (Throwable t) {
				logger.warn("kraken syslog parser: cannot parse juniper attack log", t);
			}
		}

		return null;
	}

	public Set<String> getPatternKeySet() {
		return patternMap.fingetPrints();
	}
}
