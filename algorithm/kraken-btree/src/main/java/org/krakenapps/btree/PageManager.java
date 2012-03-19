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
import java.util.HashMap;
import java.util.Map;

public class PageManager {
	private Schema schema;
	private PageFile pf;
	private int nextId;
	private Map<Integer, Page> pageMap;

	public PageManager(Schema schema, PageFile pf) throws IOException {
		this.schema = schema;
		this.pf = pf;
		this.nextId = pf.getPageCount() + 1;
		this.pageMap = new HashMap<Integer, Page>();
	}

	public void setRootPage(int pageNumber) throws IOException {
		pf.setRootPage(pageNumber);
	}
	
	public int getRootPage() {
		return pf.getRootPage();
	}

	public Page get(int pageNumber) throws IOException {
		if (pageMap.containsKey(pageNumber)) {
			Page page = pageMap.get(pageNumber);
			if (page.getFlag() != 0)
				return page;
			else
				return null;
		}

		Page page = pf.read(pageNumber);
		pageMap.put(pageNumber, page);
		return page;
	}

	public Page allocate(int flag) {
		int id = nextId++;

		byte[] b = new byte[schema.getPageSize()];
		b[1] = (byte) flag;

		Page page = new Page(id, schema, b);
		pageMap.put(id, page);
		return page;
	}

	public void sync() throws IOException {
		for (Page p : pageMap.values()) {
			if (p.isDirty()) {
				pf.write(p);
				p.clearDirty();
			}
		}
	}

	public void purge(int number) throws IOException {
		Page p = get(number);
		p.clearAllFlag();
		p.setLeftPage(0);
		p.setRightPage(0);
		p.setRightChildPage(0);
		p.setUpperPage(0);
	}
}
