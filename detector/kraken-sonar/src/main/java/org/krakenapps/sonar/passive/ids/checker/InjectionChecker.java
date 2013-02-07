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
 package org.krakenapps.sonar.passive.ids.checker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.krakenapps.ahocorasick.AhoCorasickSearch;
import org.krakenapps.ahocorasick.Pair;
import org.krakenapps.ahocorasick.SearchContext;
import org.krakenapps.sonar.passive.ids.rule.Rule;
import org.krakenapps.sonar.passive.ids.rule.RuleSyntax;

public class InjectionChecker {

	final String FILE_EXT = ".ijt";

	private String homedir;
	private int ruleCount;
	private AhoCorasickSearch acsInjection;

	public InjectionChecker() {
		homedir = "";
		ruleCount = 0;
	}

	public void setHomeDir(String path) {
		homedir = path;
	}

	public void load() {
		System.out.println("KrakenSonar: HttpAttackDetector: Load Injection data...");
		acsInjection = new AhoCorasickSearch();

		// Find Injection rule files
		File[] ruleFiles = (new File(homedir)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(FILE_EXT);
			}
		});

		boolean bFileExist = true;
		if (ruleFiles == null)
			bFileExist = false;
		else if (ruleFiles.length == 0)
			bFileExist = false;

		if (bFileExist) {
			RuleSyntax s = new RuleSyntax();
			Rule r;

			for (File f : ruleFiles) {
				Scanner fileScan = GetFileScanner(f);
				while (fileScan.hasNextLine()) {
					try {
						// r = s.eval( fileScan.nextLine() );
						String temp = fileScan.nextLine();
						r = s.eval(temp);
						acsInjection.addKeyword(new HttpPathPattern(r));
						++ruleCount;
					} catch (ParseException e) {
						System.out.println(" rule is currupted! - " + f.getName());
					}
				}
				fileScan.close();
			}
		} else {
			System.out.println("KrakenSonar: HttpAttackDetector: Injection script data not found!");
		}
		acsInjection.compile();
	}

	public void Update() {
	}

	public List<Rule> check(String inputData) {
		List<Rule> result = new ArrayList<Rule>();
		SearchContext ctx = new SearchContext();
		Rule r;
		for (Pair p : acsInjection.search(inputData.getBytes(Charset.forName("utf-8")), ctx)) {
			r = ((HttpPathPattern) p.getPattern()).getRule();
			if (result.contains(r) == false)
				result.add(r);
		}
		return result;
	}

	private Scanner GetFileScanner(File file) {
		Scanner s = null;
		try {
			s = new Scanner(new FileInputStream(file));
		} catch (Exception e) {
			e.getStackTrace();
		}
		return s;
	}
}
