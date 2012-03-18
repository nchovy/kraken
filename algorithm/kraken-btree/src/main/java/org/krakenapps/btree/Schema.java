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

public class Schema {
	private int pageSize;
	private Class<?>[] keyTypes;
	private RowKeyFactory keyFactory;
	private RowValueFactory valueFactory;

	public Schema(int pageSize, Class<?>[] keyTypes) {
		this.pageSize = pageSize;
		this.keyTypes = keyTypes;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Class<?>[] getKeyTypes() {
		return keyTypes;
	}

	public RowKeyFactory getRowKeyFactory() {
		return keyFactory;
	}

	public void setRowKeyFactory(RowKeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}

	public RowValueFactory getRowValueFactory() {
		return valueFactory;
	}

	public void setRowValueFactory(RowValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

}
