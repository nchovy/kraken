package org.krakenapps.util;

import java.io.File;
import java.io.FileFilter;

public class WildcardFileFilter implements FileFilter {
	private WildcardPathMatcher wildcardMatcher;

	public WildcardFileFilter(String token) {
		wildcardMatcher = new WildcardPathMatcher(token);
	}

	@Override
	public boolean accept(File pathname) {
		return wildcardMatcher.isMatch(pathname.getName());
	}
}