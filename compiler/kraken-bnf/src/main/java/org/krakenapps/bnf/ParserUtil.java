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
package org.krakenapps.bnf;

import java.util.List;


public class ParserUtil {
	private ParserUtil() {
	}

	@SuppressWarnings("unchecked")
	public static <T> void buildList(Binding b, List<T> list) {
		try {
			if (b == null)
				return;

			if (b.getRule() instanceof Reference && b.getValue() != null)
				list.add((T) b.getValue());

			if (b.getChildren() == null)
				return;

			for (Binding c : b.getChildren())
				buildList(c, list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
