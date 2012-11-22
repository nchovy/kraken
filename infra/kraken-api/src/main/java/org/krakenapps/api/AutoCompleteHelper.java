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
