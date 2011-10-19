package org.krakenapps.confdb;

import java.util.Date;

public interface CommitLog {
	int getRev();

	String getCommitter();

	String getMessage();

	Date getCreated();
}
