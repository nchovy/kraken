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
package org.krakenapps.api;

import java.util.List;

public class AutoCompleteHelper {
	public static String extractCommonPrefix(List<String> terms) {
		if (terms.size() == 0)
			return new String("");
		else if (terms.size() == 1)
			return terms.get(0);
		else {
			String commonPrefix = terms.get(0);

			for (int i = 1; i < terms.size(); ++i) {
				String rhs = terms.get(i);
				for (int endPos = commonPrefix.length(); endPos >= 0; --endPos) {
					if (endPos == 0) {
						return new String("");
					}
					if (rhs.regionMatches(0, commonPrefix, 0, endPos)) {
						commonPrefix = commonPrefix.substring(0, endPos);
						break;
					}
				}
				if (commonPrefix.length() == 0)
					return commonPrefix;
			}
			return commonPrefix;
		}
	}

}
