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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AhoCorasickSearch {
	private TrieNode root;
	private List<TrieNode> nodes;

	public AhoCorasickSearch() {
		this.root = new TrieNode(0, null, null);
		this.root.setFailure(root);
		this.nodes = new ArrayList<TrieNode>();
		this.nodes.add(root);
	}

	public AhoCorasickSearch addKeyword(Pattern pattern) {
		byte[] keyword = pattern.getKeyword();
		TrieNode currentNode = root;

		for (int i = 0; i < keyword.length; i++) {
			byte b = keyword[i];
			if (!currentNode.hasNext(b)) {
				try {
					TrieNode node = currentNode.addNext(nodes.size(), b);
					nodes.add(node);
				} catch (IOException e) {
				}
			}
			currentNode = currentNode.getNext(b);
		}
		currentNode.addPattern(pattern);

		return this;
	}

	public void compile() {
		Queue<TrieNode> queue = new LinkedList<TrieNode>();

		queue.add(root);
		while (!queue.isEmpty()) {
			TrieNode current = queue.poll();
			current.setFailure(findFailureNode(current));
			queue.addAll(current.getAllNext());
		}
	}

	private TrieNode findFailureNode(TrieNode node) {
		TrieNode current = node.getPrevious();
		Byte body = node.getBody();

		if (current == root || current == null)
			return root;

		do {
			current = current.getFailure();
			if (current.hasNext(body))
				return current.getNext(body);
		} while (current != root);

		return root;
	}

	public List<Pair> search(byte[] buf) {
		return search(buf, 0, buf.length);
	}

	public List<Pair> search(byte[] buf, int offset, int limit) {
		return search(buf, offset, limit, new SearchContext());
	}

	public List<Pair> search(byte[] buf, SearchContext ctx) {
		return search(buf, 0, buf.length, ctx);
	}

	public List<Pair> search(byte[] buf, int offset, int limit, SearchContext ctx) {
		List<Pair> result = new ArrayList<Pair>();
		TrieNode node = nodes.get(ctx.getLastNodeId());
		int length = ctx.getLength();
		int needResultCount = ctx.getNeedResultCount();
		boolean includeFailureSet = ctx.isIncludeFailurePatterns();

		if (needResultCount == 0)
			return result;

		if (offset < 0)
			offset = 0;
		if (buf.length < offset)
			return result;
		if (buf.length < offset + limit)
			limit = buf.length - offset;
		int searchLimit = offset + limit;

		for (int i = offset; i < searchLimit; i++) {
			TrieNode nextNode = node.getNext(buf[i]);

			if (nextNode != null) {
				node = nextNode;

				for (Pattern p : node.getPatterns(includeFailureSet)) {
					int pos = length - offset + i - p.getKeyword().length + 1;
					result.add(new Pair(pos, p));
					needResultCount--;
					if (needResultCount == 0)
						break;
				}
			} else {
				node = root;
			}
		}
		ctx.setLastNodeId(node.getId());
		ctx.addLength(limit);
		ctx.addResultCount(result.size());

		return result;
	}
}
