package org.krakenapps.dom.api;

import java.util.Map;

public interface PushInterceptor {
	Map<String, Object> onPush(PushCondition condition, Map<String, Object> msg);
}
