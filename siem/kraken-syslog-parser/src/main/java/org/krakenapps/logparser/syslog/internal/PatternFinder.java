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
package org.krakenapps.logparser.syslog.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PatternFinder<T> {
	private ConcurrentMap<String, Set<T>> map = new ConcurrentHashMap<String, Set<T>>();

	private PatternFinder() {
	}

	public static <T> PatternFinder<T> newInstance() {
		return new PatternFinder<T>();
	}

	public void register(String fingerPrint, T object) {
		Set<T> set = map.get(fingerPrint);
		if (set == null) {
			set = new HashSet<T>();
			Set<T> oldSet = map.putIfAbsent(fingerPrint, set);
			if (oldSet != null)
				set = oldSet;
		}
		set.add(object);
	}

	public Set<String> fingetPrints() {
		return map.keySet();
	}

	public Set<T> find(String text) {
		return find(text, 0, text.length());
	}

	public Set<T> find(String text, int offset) {
		return find(text, offset, text.length());
	}

	public Set<T> find(String text, int offset, int limit) {
		Set<String> keySet = map.keySet();
		Set<T> result = new HashSet<T>();

		for (String fingerPrint : keySet) {
			if (text.contains(fingerPrint)) {
				result.addAll(map.get(fingerPrint));
			}
		}
		return result;
	}
}
