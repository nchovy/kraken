package org.krakenapps.btree;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CursorContext {
	private Btree btree;
	private Page page;
	private int slot;
	private boolean asc;
	private RowKey searchKey;

	private Set<CursorCallback> callbacks = new HashSet<CursorCallback>();

	public CursorContext(Btree btree, Page page, int slot, boolean asc, RowKey searchKey) {
		this.btree = btree;
		this.page = page;
		this.slot = slot;
		this.asc = asc;
		this.searchKey = searchKey;
	}

	public RowKey getSearchKey() {
		return searchKey;
	}

	public Btree getBtree() {
		return btree;
	}

	public Page getPage() {
		return page;
	}

	public int getSlot() {
		return slot;
	}

	public boolean isAsc() {
		return asc;
	}

	public Collection<CursorCallback> getCallbacks() {
		return callbacks;
	}

	public void addListener(CursorCallback callback) {
		callbacks.add(callback);
	}

	public void removeListener(CursorCallback callback) {
		callbacks.remove(callback);
	}

	public boolean moveRight() throws IOException {
		// is last slot?
		if (slot >= page.getRecordCount() - 1) {
			if (page.getRightPage() == 0)
				return false;

			page = btree.getPageManager().get(page.getRightPage());
			slot = 0;
		} else {
			slot++;
		}

		return true;
	}

	public boolean moveLeft() throws IOException {
		// is first slot?
		if (slot == 0) {
			if (page.getLeftPage() == 0)
				return false;

			page = btree.getPageManager().get(page.getLeftPage());
			slot = page.getRecordCount() - 1;
		} else {
			slot--;
		}

		return true;
	}

}
