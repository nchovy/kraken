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

public class SearchContext {
	private int length;
	private int lastNodeId;
	private int nowResultCount;
	private int maxResultCount;
	private boolean includeFailurePatterns;

	public SearchContext() {
		this(0);
	}

	public SearchContext(int maxResultCount) {
		this.length = 0;
		this.lastNodeId = 0;
		this.nowResultCount = 0;
		this.maxResultCount = maxResultCount;
		this.includeFailurePatterns = false;
	}

	public int getLength() {
		return length;
	}

	public int getLastNodeId() {
		return lastNodeId;
	}

	public void setLastNodeId(int lastNodeId) {
		this.lastNodeId = lastNodeId;
	}

	public boolean isIncludeFailurePatterns() {
		return includeFailurePatterns;
	}

	public void setIncludeFailurePatterns(boolean includeFailurePatterns) {
		this.includeFailurePatterns = includeFailurePatterns;
	}

	public int getNeedResultCount() {
		if (maxResultCount == 0)
			return -1;
		else
			return maxResultCount - nowResultCount;
	}

	public void addLength(int length) {
		this.length += length;
	}

	public void addResultCount(int count) {
		this.nowResultCount += count;
	}

}
