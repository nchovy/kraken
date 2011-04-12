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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrieNode {
	public final int id;
	private Byte body;
	private TrieNode previous;
	private TrieNode[] next;
	private TrieNode failure;
	private Set<Pattern> patterns;

	public TrieNode(int id, Byte body, TrieNode previous) {
		this.id = id;
		this.body = body;
		this.previous = previous;
		this.next = new TrieNode[256];
		this.patterns = new HashSet<Pattern>();
	}

	public Byte getBody() {
		return body;
	}

	public TrieNode getPrevious() {
		return previous;
	}

	public Collection<TrieNode> getAllNext() {
		List<TrieNode> list = new ArrayList<TrieNode>();

		for (int i = 0; i < 256; i++)
			if (next[i] != null)
				list.add(next[i]);

		return list;
	}

	public boolean hasNext(byte nextBody) {
		return next[(int) nextBody + 128] != null ? true : false;
	}

	public TrieNode getNext(byte nextBody) {
		TrieNode node = this;

		while (node.next[(int) nextBody + 128] == null) {
			node = node.failure;
			if (node.id == 0) // root
				break;
		}

		return node.next[(int) nextBody + 128];
	}

	public void addNext(int id, byte nextBody) {
		next[(int) nextBody + 128] = new TrieNode(id, nextBody, this);
	}

	public TrieNode getFailure() {
		return failure;
	}

	public void setFailure(TrieNode failure) {
		this.failure = failure;
	}

	public Set<Pattern> getPatterns() {
		return patterns;
	}

	public void addPattern(Pattern pattern) {
		this.patterns.add(pattern);
	}

	public String getWord() {
		if (previous == null)
			return "";
		else
			return previous.getWord() + (char) body.byteValue();
	}
}
