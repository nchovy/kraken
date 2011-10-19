package org.krakenapps.confdb;

import java.util.Date;
import java.util.List;

public interface CommitLog {
	long getRev();

	String getCommitter();

	String getMessage();

	Date getCreated();

	List<ConfigChange> getChangeSet();
}
