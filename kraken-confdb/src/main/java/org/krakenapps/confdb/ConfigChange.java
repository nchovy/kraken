/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb;

import org.krakenapps.api.FieldOption;

/**
 * changed config entry for changeset log
 * 
 * @author xeraph
 * 
 */
public class ConfigChange {
	private CommitOp operation;

	@FieldOption(skip = true)
	private String colName;

	private int colId;

	private int docId;

	public ConfigChange() {
	}

	public ConfigChange(CommitOp operation, String colName, int colId, int docId) {
		this.operation = operation;
		this.colName = colName;
		this.colId = colId;
		this.docId = docId;
	}

	public CommitOp getOperation() {
		return operation;
	}

	public void setOperation(CommitOp operation) {
		this.operation = operation;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public int getColId() {
		return colId;
	}

	public void setColId(int colId) {
		this.colId = colId;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	@Override
	public String toString() {
		return "op=" + operation + ", col=" + colName + ", doc=" + docId;
	}
}
