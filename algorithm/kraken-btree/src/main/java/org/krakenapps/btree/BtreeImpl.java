/*
 * 
 * Copyright 2011 Future Systems
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
package org.krakenapps.btree;

import java.io.File;
import java.io.IOException;

import org.krakenapps.btree.types.IntegerValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Known issues: 
 * 	* linear bruteforce search for a key in page.
 *  * ... 
 */

public class BtreeImpl implements CursorCallback, Btree {
	private final Logger logger = LoggerFactory.getLogger(BtreeImpl.class.getName());

	private PageManager pageManager;
	private PageFile pf;

	public static Btree create(File file, Schema schema) throws IOException {
		PageFile pf = PageFile.create(file, schema);
		pf.close();
		return new BtreeImpl(file);
	}

	public BtreeImpl(File file) throws IOException {
		this.pf = new PageFile(file);
		this.pageManager = new PageManager(pf.getSchema(), pf);
	}

	@Override
	public void setRowValueFactory(RowValueFactory valueFactory) {
		pf.setRowValueFactory(valueFactory);
	}

	@Override
	public PageManager getPageManager() {
		return pageManager;
	}

	@Override
	public PageFile getPageFile() {
		return pf;
	}

	@Override
	public Cursor openCursor(int order) throws IOException {
		Page p = pageManager.get(pf.getRootPage());

		if (order == Cursor.ASC) {
			// find leftmost page in ascending
			while (!p.getFlag(PageType.LEAF)) {
				int left = ((IntegerValue) p.getValue(0)).getValue();
				p = pageManager.get(left);
			}

			CursorContext context = new CursorContext(this, p, 0, true, null);
			context.addListener(this);
			return new Cursor(context);
		} else if (order == Cursor.DESC) {
			// find rightmost page in descending
			while (!p.getFlag(PageType.LEAF)) {
				if (p.getFlag(PageType.INDEX) && p.getRightChildPage() != 0)
					p = pageManager.get(p.getRightChildPage());
			}

			CursorContext context = new CursorContext(this, p, p.getRecordCount() - 1, false, null);
			context.addListener(this);
			return new Cursor(context);
		}

		throw new IllegalArgumentException("invalid sort order: " + order);
	}

	@Override
	public Cursor openCursor(RowKey searchKey, int order) throws IOException {
		return get(pf.getRootPage(), searchKey, order == Cursor.ASC);
	}

	@Override
	public void insert(RowKey key, RowEntry value) throws IOException {
		Page root = pageManager.get(pf.getRootPage());
		Page newPage = insert(root, key, value);
		if (newPage != null) {
			// create new root
			Page newRoot = pageManager.allocate(PageType.INDEX);
			RowKey smallestKey = newPage.getKey(0);
			newRoot.insert(smallestKey, new IntegerValue(root.getNumber()));
			newRoot.setRightChildPage(newPage.getNumber());

			int newRootPageNumber = newRoot.getNumber();
			root.setUpperPage(newRootPageNumber);
			newPage.setUpperPage(newRootPageNumber);

			// change root pointer
			pageManager.setRootPage(newRootPageNumber);
		}
	}

	private Page insert(Page page, RowKey key, RowEntry value) throws IOException {
		if (page.getFlag(PageType.LEAF))
			return insertLeaf(page, key, value);
		else if (page.getFlag(PageType.INDEX))
			return insertIndex(page, key, value);
		else
			throw new IllegalArgumentException("unsupported page type: " + page.getFlag());
	}

	@Override
	public void delete(RowKey key) throws IOException {
		if (key == null)
			throw new IllegalArgumentException("key must not be null");

		Cursor cursor = openCursor(key, Cursor.ASC);
		if (cursor == null)
			return;

		try {
			cursor.delete();
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	private Page split(int pageType, Page page, RowKey key, RowEntry value) throws IOException {
		Page newPage = pageManager.allocate(pageType);
		redistribute(page, newPage);

		RowKey smallestKey = newPage.getKey(0);
		if (key.compareTo(smallestKey) <= 0)
			page.insert(key, value);
		else
			newPage.insert(key, value);

		// set left/right link
		if (page.getRightPage() != 0) {
			Page right = pageManager.get(page.getRightPage());
			newPage.setRightPage(right.getNumber());
			right.setLeftPage(newPage.getNumber());
		}

		page.setRightPage(newPage.getNumber());
		newPage.setLeftPage(page.getNumber());
		newPage.setUpperPage(page.getUpperPage());

		if (logger.isDebugEnabled())
			traceAfterSplit(page, newPage);

		return newPage;
	}

	private void traceAfterSplit(Page page, Page newPage) {
		String buffer = "";

		buffer += "OLD=" + page.getNumber() + "\n";
		for (int k = 0; k < page.getRecordCount(); k++)
			buffer += page.getKey(k) + "," + page.getValue(k) + "\n";

		buffer += "NEW=" + newPage.getNumber() + "\n";
		for (int k = 0; k < newPage.getRecordCount(); k++)
			buffer += newPage.getKey(k) + "," + newPage.getValue(k) + "\n";

		logger.debug(buffer);
	}

	private Page insertLeaf(Page page, RowKey key, RowEntry value) throws IOException {
		boolean added = page.insert(key, value);
		if (!added)
			return split(PageType.LEAF, page, key, value);

		return null;
	}

	private Page insertIndex(Page page, RowKey key, RowEntry value) throws IOException {
		int nextPageNumber = -1;

		// traverse and find page
		for (int i = 0; i < page.getRecordCount(); i++) {
			RowKey k = page.getKey(i);
			if (key.compareTo(k) <= 0) {
				nextPageNumber = ((IntegerValue) page.getValue(i)).getValue();
				break;
			}
		}

		int currentRightChildPageNumber = page.getRightChildPage();
		if (nextPageNumber == -1)
			nextPageNumber = currentRightChildPageNumber;

		Page nextPage = pageManager.get(nextPageNumber);
		if (nextPage == null)
			throw new IOException("page not found: " + nextPageNumber);

		// try insert to leaf page
		Page newPage = insert(nextPage, key, value);
		if (newPage != null) {
			// replace right child if it was rightmost page
			if (nextPageNumber == currentRightChildPageNumber) {
				page.setRightChildPage(newPage.getNumber());
				newPage.setUpperPage(page.getNumber());

				currentRightChildPageNumber = newPage.getNumber();
			}

			// prepare new index item
			RowKey smallestKey = newPage.getKey(0);
			RowEntry pageLink = new IntegerValue(newPage.getNumber());

			// find slot to append or insert
			int slot = page.findSlotBefore(smallestKey) + 1;
			RowKey oldKey = page.getKey(slot);
			IntegerValue oldLeft = (IntegerValue) page.getValue(slot);

			// check if duplicated key exists
			if (oldKey != null && oldKey.equals(smallestKey)) {
				int minimum = Math.min(newPage.getNumber(), oldLeft.getValue());

				// replace page link to minimum (logic should be enhanced later)
				page.delete(slot);
				page.insert(oldKey, new IntegerValue(minimum));
			} else {
				// update old key
				if (slot < page.getRecordCount()) {
					// replace page link (logic should be enhanced later)
					page.delete(slot);
					page.insert(oldKey, pageLink);
					pageLink = oldLeft;
				}

				// try insert new key to index page
				boolean added = page.insert(smallestKey, pageLink);
				if (!added) {
					Page splitPage = split(PageType.INDEX, page, smallestKey, pageLink);

					// update right child
					int newRightChildPageNumber = ((IntegerValue) splitPage.getValue(0)).getValue();
					page.setRightChildPage(newRightChildPageNumber);
					splitPage.setRightChildPage(currentRightChildPageNumber);

					// update up link
					Page currentRightChildPage = pageManager.get(currentRightChildPageNumber);
					currentRightChildPage.setUpperPage(splitPage.getNumber());

					return splitPage;
				}
			}
		}

		return null;
	}

	private void redistribute(Page oldPage, Page newPage) throws IOException {
		int total = oldPage.getSchema().getPageSize() - Page.PAGE_HEADER_SIZE;
		int half = total / 2;
		int count = oldPage.getRecordCount();
		long sum = 0;

		// calculate half position
		int p = 0;
		for (int i = 0; i < count; i++) {
			sum += Page.SLOT_SIZE + Page.RECORD_HEADER_SIZE;
			sum += oldPage.getKey(i).getBytes().length;
			sum += oldPage.getValue(i).getBytes().length;

			if (sum > half) {
				p = i;
				break;
			}
		}

		// copy to new page
		for (int i = p; i < count; i++) {
			oldPage.getKey(i);
			RowKey key = oldPage.getKey(i);
			RowEntry value = oldPage.getValue(i);
			newPage.insert(key, value);

			// update up link
			if (newPage.getFlag() == PageType.INDEX) {
				Page child = pageManager.get(((IntegerValue) value).getValue());
				child.setUpperPage(newPage.getNumber());
			}
		}

		// delete (descending delete is efficient)
		for (int j = count - 1; j >= p; j--)
			oldPage.delete(j);
	}

	@Override
	public RowEntry get(RowKey searchKey) throws IOException {
		Cursor cursor = null;
		try {
			cursor = get(pf.getRootPage(), searchKey, true);
			if (cursor == null)
				return null;

			return cursor.getValue();
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	private Cursor get(int pageNumber, RowKey searchKey, boolean asc) throws IOException {
		Page page = pageManager.get(pageNumber);

		if (page.getFlag(PageType.LEAF)) {
			return findFirstKey(page, searchKey, asc);
		} else if (page.getFlag(PageType.INDEX)) {
			for (int i = 0; i < page.getRecordCount(); i++) {
				RowKey k = page.getKey(i);
				if (searchKey.compareTo(k) <= 0) {
					int nextPageNumber = ((IntegerValue) page.getValue(i)).getValue();
					return get(nextPageNumber, searchKey, asc);
				}
			}

			return get(page.getRightChildPage(), searchKey, asc);
		}

		return null;
	}

	private Cursor findFirstKey(Page page, RowKey searchKey, boolean asc) throws IOException {
		int recordCount = page.getRecordCount();

		Page lastPage = null;
		int lastSlot = -1;

		while (true) {
			for (int i = 0; i < recordCount; i++) {
				RowKey key = page.getKey(i);
				int ret = key.compareTo(searchKey);
				if (ret == 0) {
					if (asc) {
						CursorContext context = new CursorContext(this, page, i, asc, searchKey);
						context.addListener(this);
						return new Cursor(context);
					} else {
						// do not return immediately, find all duplicates in
						// descending mode
						lastPage = page;
						lastSlot = i;
					}
				} else if (ret > 0) {
					// if key is larger than search key, you should stop here
					if (!asc && lastPage != null && lastSlot != -1) {
						CursorContext context = new CursorContext(this, lastPage, lastSlot, asc, searchKey);
						context.addListener(this);
						return new Cursor(context);
					} else
						return null;
				}
			}

			if (page.getRightPage() == 0)
				return null;

			page = pageManager.get(page.getRightPage());
		}
	}

	@Override
	public void sync() throws IOException {
		pageManager.sync();
	}

	@Override
	public void close() throws IOException {
		sync();
		pf.close();
	}

	//
	// Handle cursor events
	//

	@Override
	public void onDelete(CursorContext context) throws IOException {
		Page page = context.getPage();
		int slot = context.getSlot();
		RowKey currentKey = page.getKey(slot);

		// delete slot
		page.delete(slot);

		// begin index update
		int upper = page.getUpperPage();
		if (upper == 0)
			return;

		Page upperPage = pageManager.get(upper);
		Page currentPage = page;

		// record count after deletion
		int count = page.getRecordCount();

		// if slot was 0, update smallest key recursively
		if (slot == 0 && count > 0) {
			while (true) {
				// find current smallest key at index page
				int upperSlot = upperPage.findSlot(currentKey);
				if (upperSlot < 0)
					throw new IllegalStateException("bug check. cannot find index slot for " + currentKey);

				RowKey newSmallestKey = currentPage.getKey(0);

				// update page link (can be enhanced later)
				upperPage.delete(upperSlot);
				upperPage.insert(newSmallestKey, new IntegerValue(currentPage.getNumber()));

				// recursive update if upper slot is also smallest
				if (upperSlot != 0 || upperPage.getUpperPage() == 0)
					break;

				currentKey = upperPage.getKey(0);
				currentPage = upperPage;
				upperPage = pageManager.get(upperPage.getUpperPage());
			}
		}

		// check empty page
		if (count > 0)
			return;

		cascadeDelete(page, currentKey);
	}

	private void cascadeDelete(Page page, RowKey currentKey) throws IOException {
		Page currentPage = page;

		while (true) {
			// break condition
			if (currentPage.getRecordCount() != 0)
				break;

			int rightPage = currentPage.getRightPage();
			int rightChildPage = currentPage.getRightChildPage();
			int upPage = currentPage.getUpperPage();

			deletePage(currentPage);

			// change root and break
			if (upPage == 0) {
				pageManager.setRootPage(rightChildPage);
				break;
			}

			Page upperPage = pageManager.get(upPage);
			int upperSlot = upperPage.findSlot(currentKey);

			if (upperSlot < 0) {
				// update page link (e.g. leftmost page deletion)
				upperSlot = upperPage.findSlotBefore(currentKey) + 1;
				RowKey firstKey = upperPage.getKey(upperSlot);

				// you should use right page. because right page may not be
				// found from index node. (especially in case of many
				// duplicated keys)
				Page right = null;
				if (rightPage != 0) {
					right = pageManager.get(rightPage);
					RowKey smallest = right.getKey(0);

					// if first key of upper keys is larger than smallest of
					// right page, replace page link
					if (smallest.compareTo(firstKey) <= 0) {
						// replace it (can be enhanced later)
						upperPage.delete(upperSlot);
						upperPage.insert(smallest, new IntegerValue(rightPage));
					}
				}
				break;
			}

			upperPage.delete(upperSlot);

			// update right-child if current right child is deleted
			if (upperPage.getRightChildPage() == currentPage.getNumber()) {
				RowEntry value = upperPage.getValue(upperPage.getRecordCount() - 1);
				int newRightmostChild = ((IntegerValue) value).getValue();
				upperPage.setRightChildPage(newRightmostChild);
			}

			// up!
			currentPage = upperPage;
		}
	}

	private void deletePage(Page page) throws IOException {
		// unlink between sibling pages
		Page leftPage = null;
		if (page.getLeftPage() != 0) {
			leftPage = pageManager.get(page.getLeftPage());
			leftPage.setRightPage(page.getRightPage());
		}

		Page rightPage = null;
		if (page.getRightPage() != 0) {
			rightPage = pageManager.get(page.getRightPage());
			rightPage.setLeftPage(page.getLeftPage());
		}

		// purge page (add to free page list)
		pageManager.purge(page.getNumber());
	}
}
