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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrieNode {
	private final int id;
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

	public int getId() {
		return id;
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
		TrieNode n = next[(int) nextBody + 128];
		return (n != null);
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

	public TrieNode addNext(int id, byte nextBody) throws IOException {
		if (hasNext(nextBody))
			throw new IOException("already added");

		TrieNode node = new TrieNode(id, nextBody, this);
		next[(int) nextBody + 128] = node;
		return node;
	}

	public TrieNode getFailure() {
		return failure;
	}

	public void setFailure(TrieNode failure) {
		this.failure = failure;
	}

	public Set<Pattern> getPatterns() {
		return getPatterns(false);
	}

	public Set<Pattern> getPatterns(boolean includeFailureSet) {
		Set<Pattern> p = null;

		if (includeFailureSet) {
			p = new HashSet<Pattern>();
			if (id != 0) {
				p.addAll(patterns);
				p.addAll(failure.getPatterns(true));
			}
		} else
			p = patterns;

		return p;
	}

	public void addPattern(Pattern pattern) {
		this.patterns.add(pattern);
	}

	public byte[] getKeyword() {
		return getKeywordInternal(0);
	}

	private byte[] getKeywordInternal(int depth) {
		if (id == 0)
			return new byte[depth];

		byte[] b = failure.getKeywordInternal(depth + 1);
		b[depth] = body;
		return b;
	}
}
