package org.krakenapps.btree;

import java.io.IOException;

public interface CursorCallback {
	public void onDelete(CursorContext context) throws IOException;
}
