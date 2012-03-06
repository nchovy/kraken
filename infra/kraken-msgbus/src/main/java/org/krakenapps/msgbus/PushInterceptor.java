package org.krakenapps.msgbus;

import java.util.Map;

public interface PushInterceptor {
	Map<String, Object> onPush(PushCondition condition, Map<String, Object> msg);
}
