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
package org.krakenapps.ahocorasick;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AhoTest {

	@Test
	public void Test() {
		String[] keywords = { "a", "aa", "abc" };
		String[] descriptions = { "test1", "test2", "test3" };

		AhoCorasickSearch acSearch = new AhoCorasickSearch();
		List<Pair> result1 = new ArrayList<Pair>();
		SearchContext ctx1 = new SearchContext();
		List<Pair> result2 = new ArrayList<Pair>();
		SearchContext ctx2 = new SearchContext(1);

		for (int i = 0; i < keywords.length; i++)
			acSearch.addKeyword(new CustomPattern(keywords[i], descriptions[i]));
		acSearch.compile();

		result1.addAll(acSearch.search("aaaab".getBytes(), ctx1));
		result1.addAll(acSearch.search("acaabca".getBytes(), 1, 5, ctx1));
		result1.addAll(acSearch.search("bc".getBytes(), ctx1));

		result2.addAll(acSearch.search("aaaab".getBytes(), ctx2));
		result2.addAll(acSearch.search("acaabca".getBytes(), 1, 5, ctx2));
		result2.addAll(acSearch.search("bc".getBytes(), ctx2));

		for (Pair pair : result1)
			System.out.println(pair);
		
		System.out.println("--------------------");
		
		for (Pair pair : result2)
			System.out.println(pair);
	}
}
