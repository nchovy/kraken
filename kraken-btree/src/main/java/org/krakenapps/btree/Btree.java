package org.krakenapps.btree;

import java.io.IOException;

public interface Btree {

	public abstract void setRowValueFactory(RowValueFactory valueFactory);

	public abstract PageManager getPageManager();

	public abstract PageFile getPageFile();

	public abstract Cursor openCursor(int order) throws IOException;

	public abstract Cursor openCursor(RowKey searchKey, int order) throws IOException;

	public abstract void insert(RowKey key, RowEntry value) throws IOException;

	public abstract void delete(RowKey key) throws IOException;

	public abstract RowEntry get(RowKey searchKey) throws IOException;

	public abstract void sync() throws IOException;

	public abstract void close() throws IOException;

	public abstract void onDelete(CursorContext context) throws IOException;

}
