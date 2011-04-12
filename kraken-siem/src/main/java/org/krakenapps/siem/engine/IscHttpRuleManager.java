package org.krakenapps.siem.engine;

import java.util.Date;

public interface IscHttpRuleManager {
	Date getLastUpdateDate();

	void update() throws Exception;
}
