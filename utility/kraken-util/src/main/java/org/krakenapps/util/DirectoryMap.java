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
package org.krakenapps.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class DirectoryMap<T> implements ConcurrentMap<String, T> {

	private Node<T> root = new Node<T>(0);

	@Override
	public T put(String key, T value) {
		String[] path = key.split("/");
		return root.put(path, value);
	}

	@Override
	public T get(Object key) {
		if (!String.class.isInstance(key))
			return null;
		String[] path = ((String) key).split("/");
		return root.get(path);
	}

	@Override
	public T putIfAbsent(String key, T value) {
		String[] path = key.split("/");
		return root.putIfAbsent(path, value);
	}

	@Override
	public int size() {
		return root.size();
	}

	/**
	 * Get all items of selected directory.
	 * 
	 * @param path
	 *            selected directory.
	 * 
	 */
	public Set<Entry<String, T>> getItems(String path) {
		Node<T> current = currentDir(path);
		if (current == null)
			return Collections.emptySet();
		
		TreeMap<String, T> m = new TreeMap<String, T>(current.itemMap);
		return m.entrySet();
	}

	public Set<String> keySet(String path) {
		Node<T> current = currentDir(path);
		if (current == null)
			return Collections.emptySet();

		return new TreeSet<String>(current.subnodeMap.keySet());
	}

	private Node<T> currentDir(String keyDir) {
		String[] dirs = keyDir.split("/");
		Node<T> current = root;
		for (String dir : dirs) {
			if (!current.subnodeMap.containsKey(dir))
				return null;
			current = current.subnodeMap.get(dir);
		}
		return current;
	}

	public Set<Entry<String, T>> entrySet(String query) {
		return root.entrySet(query);
	}

	@Override
	public Set<Entry<String, T>> entrySet() {
		return entrySet("*");
	}

	@Override
	public T remove(Object key) {
		if (!String.class.isInstance(key))
			return null;
		return root.remove(((String) key).split("/"));
	}

	@Override
	public boolean remove(Object key, Object value) {
		if (!String.class.isInstance(key))
			return false;
		return root.remove(((String) key).split("/"), value);
	}

	/**
	 * Remove all elements in the specified path and its subs.
	 * 
	 * @return true if successfully remove the node, false otherwise.
	 */
	public boolean removeNode(String path) {
		if (path == null)
			return false;
		return root.removeNode(path.split("/"));
	}

	@Override
	public T replace(String key, T value) {
		return root.replace(key.split("/"), value);
	}

	@Override
	public boolean replace(String key, T oldValue, T newValue) {
		return root.replace(key.split("/"), oldValue, newValue);
	}

	@Override
	public void clear() {
		root = new Node<T>(0);

	}

	@Override
	public boolean containsKey(Object key) {
		if (!String.class.isInstance(key))
			return false;
		String[] path = ((String) key).split("/");
		return root.containsKey(path);
	}

	@Override
	public boolean containsValue(Object value) {
		return root.containsValue(value);
	}

	@Override
	public boolean isEmpty() {
		return root.size() == 0;
	}

	@Override
	public Set<String> keySet() {
		return root.keySet();
	}

	@Override
	public void putAll(Map<? extends String, ? extends T> m) {
		for (Entry<? extends String, ? extends T> e : m.entrySet()) {
			root.put(e.getKey().split("/"), e.getValue());
		}
	}

	@Override
	public Collection<T> values() {
		return root.values();
	}

	@Override
	public String toString() {
		return root.toString();
	}

	private static class Node<T> {
		private ConcurrentMap<String, T> itemMap = new ConcurrentHashMap<String, T>();
		private ConcurrentMap<String, Node<T>> subnodeMap = new ConcurrentHashMap<String, Node<T>>();
		private ConcurrentMap<String, NodeGuard<T>> nodeGuardMap = new ConcurrentHashMap<String, NodeGuard<T>>();
		private int depth;

		public Node(int depth) {
			this.depth = depth;
		}

		public Collection<T> values() {
			Collection<T> collection = new ArrayList<T>(itemMap.values());
			for (Entry<String, Node<T>> e : subnodeMap.entrySet()) {
				collection.addAll(e.getValue().values());
			}
			return collection;
		}

		public Set<String> keySet() {
			Set<String> set = new HashSet<String>(itemMap.keySet());
			for (Entry<String, Node<T>> e : subnodeMap.entrySet()) {
				String dir = e.getKey();
				for (String subs : e.getValue().keySet()) {
					set.add(dir + "/" + subs);
				}
			}
			return set;
		}

		public boolean containsKey(String[] path) {
			if (depth == path.length - 1) {
				return itemMap.containsKey(path[depth]);
			} else {
				Node<T> sub = subnodeMap.get(path[depth]);
				if (sub != null) {
					return sub.containsKey(path);
				} else {
					return false;
				}
			}
		}

		public boolean containsValue(Object value) {
			if (itemMap.containsValue(value))
				return true;
			for (Entry<String, Node<T>> e : subnodeMap.entrySet()) {
				if (e.getValue().containsValue(value))
					return true;
			}
			return false;
		}

		public boolean remove(String[] path, Object value) {
			if (depth == path.length - 1) {
				return itemMap.remove(path[depth], value);
			} else {
				Node<T> sub = subnodeMap.get(path[depth]);
				if (sub != null) {
					boolean isRemoved = sub.remove(path, value);
					if (isRemoved && sub.size() == 0) {
						removeSubdir(path, sub);
					}
					return isRemoved;
				} else {
					return false;
				}
			}
		}

		public T remove(String[] path) {
			if (depth == path.length - 1) {
				return itemMap.remove(path[depth]);
			} else {
				Node<T> sub = subnodeMap.get(path[depth]);
				if (sub != null) {
					T removedItem = sub.remove(path);
					if (removedItem != null && sub.size() == 0) {
						removeSubdir(path, sub);
					}
					return removedItem;
				} else {
					return null;
				}
			}
		}

		public boolean removeNode(String[] path) {
			if (depth == path.length - 1) {
				return subnodeMap.remove(path[depth]) != null;

			} else {
				return subnodeMap.get(path[depth]).removeNode(path);
			}
		}

		public T get(String[] path) {
			if (depth == path.length - 1) {
				return itemMap.get(path[depth]);
			} else {
				if (!subnodeMap.containsKey(path[depth])) {
					return null;
				}
				Node<T> sub = subnodeMap.get(path[depth]);
				return sub.get(path);
			}
		}

		public T put(String[] path, T value) {
			if (depth == path.length - 1) {
				return itemMap.put(path[depth], value);
			} else {
				if (itemMap.containsKey(path[depth])) {
					throw new InvalidHierarchyException(String.format("there exists key (%s, depth: %d)", path[depth],
							depth));
				}
				if (nodeGuardMap.containsKey(path[depth])) {
					NodeGuard<T> nodeGuard = nodeGuardMap.get(path[depth]);
					if (nodeGuard != null) {
						if (nodeGuard.offer(new NodeItem<T>(path, value)))
							return null;
					}
				}
				if (!subnodeMap.containsKey(path[depth])) {
					subnodeMap.putIfAbsent(path[depth], new Node<T>(this.depth + 1));
				}
				Node<T> sub = subnodeMap.get(path[depth]);
				return sub.put(path, value);
			}
		}

		public T putIfAbsent(String[] path, T value) {
			if (depth == path.length - 1) {
				return itemMap.putIfAbsent(path[depth], value);
			} else {
				if (nodeGuardMap.containsKey(path[depth])) {
					NodeGuard<T> nodeGuard = nodeGuardMap.get(path[depth]);
					if (nodeGuard != null) {
						if (nodeGuard.offer(new NodeItem<T>(path, value)))
							return null;
					}
				}
				if (!subnodeMap.containsKey(path[depth])) {
					subnodeMap.putIfAbsent(path[depth], new Node<T>(this.depth + 1));
				}
				Node<T> sub = subnodeMap.get(path[depth]);
				return sub.putIfAbsent(path, value);
			}
		}

		public T replace(String[] path, T value) {
			if (depth == path.length - 1) {
				return itemMap.replace(path[depth], value);
			} else {
				if (!subnodeMap.containsKey(path[depth])) {
					return null;
				}
				Node<T> sub = subnodeMap.get(path[depth]);
				return sub.replace(path, value);
			}
		}

		public boolean replace(String[] path, T oldValue, T newValue) {
			if (depth == path.length - 1) {
				return itemMap.replace(path[depth], oldValue, newValue);
			} else {
				if (!subnodeMap.containsKey(path[depth])) {
					return false;
				}
				Node<T> sub = subnodeMap.get(path[depth]);
				return sub.replace(path, oldValue, newValue);
			}
		}

		public Set<Entry<String, T>> entrySet() {
			return new NodeEntrySet<T>(this, "*");
		}

		public Set<Entry<String, T>> entrySet(String query) {
			return new NodeEntrySet<T>(this, query);
		}

		public int size() {
			int size = itemMap.size();
			for (Entry<String, Node<T>> e : subnodeMap.entrySet()) {
				size += e.getValue().size();
			}
			return size;
		}

		private void removeSubdir(String[] path, Node<T> sub) {
			// raise node guard
			if (nodeGuardMap.containsKey(path[depth]))
				return;
			NodeGuard<T> guard = new NodeGuard<T>();
			if (nodeGuardMap.putIfAbsent(path[depth], guard) != null)
				return;
			// guard completed

			// unlink sub-node
			subnodeMap.remove(path[depth]);
			// remove sub-node
			nodeGuardMap.remove(path[depth], guard);
			// stop guard
			guard.close();

			// if any element exists between subnode and guard, re-insert it.
			if (sub.size() > 0)
				for (Entry<String, T> e : sub.entrySet()) {
					put(e.getKey().split("/"), e.getValue());
				}

			while (guard.peek() != null) {
				NodeItem<T> item = guard.poll();
				put(item.path, item.item);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemMap == null) ? 0 : itemMap.entrySet().hashCode());
			result = prime * result + ((subnodeMap == null) ? 0 : subnodeMap.entrySet().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			Node<?> other = (Node<?>) obj;
			if (itemMap == null) {
				if (other.itemMap != null)
					return false;
			} else if (!itemMap.equals(other.itemMap))
				return false;
			if (subnodeMap == null) {
				if (other.subnodeMap != null)
					return false;
			} else if (!subnodeMap.equals(other.subnodeMap))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + itemMap + ";" + subnodeMap + "]";
		}

	}

	private static class NodeGuard<T> {
		private AtomicReference<Queue<NodeItem<T>>> queueHolder = new AtomicReference<Queue<NodeItem<T>>>();
		private volatile boolean isClosed = false;

		public boolean offer(NodeItem<T> node) {
			if (isClosed)
				return false;
			if (queueHolder.get() == null) {
				queueHolder.compareAndSet(null, new ConcurrentLinkedQueue<NodeItem<T>>());
			}
			return queueHolder.get().offer(node);
		}

		public NodeItem<T> poll() {
			if (queueHolder.get() == null)
				return null;
			return queueHolder.get().poll();
		}

		public NodeItem<T> peek() {
			if (queueHolder.get() == null)
				return null;
			return queueHolder.get().peek();
		}

		public void close() {
			isClosed = true;
		}

	}

	private static class NodeItem<T> {
		String[] path;
		T item;

		public NodeItem(String[] path, T item) {
			this.path = path;
			this.item = item;
		}
	}

	private static class NodeEntrySet<T> extends AbstractSet<Entry<String, T>> {

		private Node<T> node;
		private String[] query;

		public NodeEntrySet(Node<T> node, String query) {
			if (query == null)
				throw new IllegalArgumentException("query must be not null");

			this.node = node;
			this.query = query.split("/");
		}

		@Override
		public Iterator<Entry<String, T>> iterator() {

			return new NodeIterator<T>(node, query, 0);
		}

		@Override
		public int size() {
			if ((query.length == 1) && "*".equals(query[0]))
				return node.size();
			int size = 0;
			for (Iterator<Entry<String, T>> iter = iterator(); iter.hasNext(); iter.next())
				size++;
			return size;
		}

	}

	private static class NodeIterator<T> implements Iterator<Entry<String, T>> {

		private String[] query;
		private int depth;

		private Iterator<Entry<String, T>> iter;
		private Iterator<Entry<String, Node<T>>> subdirIter;
		private Entry<String, T> next = null;
		private Entry<String, Node<T>> nextNode = null;
		private NodeIterator<T> nextNodeIter = null;

		public NodeIterator(Node<T> node, String[] query, int depth) {
			this.query = query;
			this.depth = depth;
			iter = node.itemMap.entrySet().iterator();
			subdirIter = node.subnodeMap.entrySet().iterator();
		}

		@Override
		public boolean hasNext() {
			int lastQueryIndex = query.length - 1;
			if (depth >= lastQueryIndex) {

				while (next == null && iter.hasNext()) {
					next = iter.next();
					if (!match(next.getKey())) {
						next = null;
					}
				}
			}
			if (next == null) {
				while ((nextNodeIter == null || !nextNodeIter.hasNext()) && subdirIter.hasNext()) {
					nextNode = subdirIter.next();
					if (!match(nextNode.getKey())) {
						nextNode = null;
					} else {
						nextNodeIter = new NodeIterator<T>(nextNode.getValue(), query, depth + 1);
					}
				}
			}
			return ((nextNodeIter != null && nextNodeIter.hasNext()) || next != null);
		}

		@Override
		public Entry<String, T> next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			if (nextNodeIter != null) {
				Entry<String, T> e = nextNodeIter.next();
				return new SubdirEntry<T>(nextNode.getKey() + "/" + e.getKey(), e.getValue());
			}

			Entry<String, T> current = next;
			next = null;
			return current;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private boolean match(String s) {
			String query;
			if (depth < this.query.length) {
				query = this.query[depth];
			} else if ("*".equals(this.query[this.query.length - 1])) {
				query = "*";
			} else {
				throw new RuntimeException("Matching has some problem.");
			}
			// System.out.println(Arrays.toString(this.query)+":"+depth+"==>"+query);
			if (query.contains("*")) {
				return getMatcher(query).isMatch(s);
			} else {
				return query.equals(s);
			}
		}
	}

	private static class SubdirEntry<T> implements Entry<String, T> {

		private String key;
		private T value;

		public SubdirEntry(String key, T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public T setValue(T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			SubdirEntry<?> other = (SubdirEntry<?>) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}

	public static ConcurrentHashMap<String, WildcardPathMatcher> matchers = new ConcurrentHashMap<String, WildcardPathMatcher>();

	public static WildcardPathMatcher getMatcher(String query) {
		if (matchers.containsKey(query)) {
			return matchers.get(query);
		} else {
			matchers.putIfAbsent(query, new WildcardPathMatcher(query));
			return matchers.get(query);
		}
	}
}
