/*
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

import java.io.IOException;

public class Cursor {
	public static final int ASC = 1;
	public static final int DESC = 2;

	private CursorContext context;

	public Cursor(CursorContext context) {
		this.context = context;
	}

	public RowKey getKey() {
		Page page = context.getPage();
		int slot = context.getSlot();
		return page.getKey(slot);
	}

	public RowEntry getValue() {
		Page page = context.getPage();
		int slot = context.getSlot();
		return page.getValue(slot);
	}

	public void delete() throws IOException {
		for (CursorCallback callback : context.getCallbacks()) {
			callback.onDelete(context);
		}
	}

	public boolean next() throws IOException {
		boolean asc = context.isAsc();
		if (asc) {
			return context.moveRight();
		} else {
			return context.moveLeft();
		}
	}

	public void close() {
		// do nothing yet.
	}
}
