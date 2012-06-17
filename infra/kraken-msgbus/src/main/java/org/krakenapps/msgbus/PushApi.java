package org.krakenapps.msgbus;

import java.util.Map;

public interface PushApi {
	@Deprecated
	void subscribe(String orgDomain, int sessionId, int processId, String callback);

	@Deprecated
	void subscribe(String orgDomain, int sessionId, int processId, String callback, Map<String, Object> options);

	void subscribe(String orgDomain, String sessionId, int processId, String callback, Map<String, Object> options);

	@Deprecated
	void unsubscribe(String orgDomain, int sessionId, int processId, String callback);

	void unsubscribe(String orgDomain, String sessionId, int processId, String callback);

	void addInterceptor(String callback, PushInterceptor interceptor);

	void removeInterceptor(String callback);

	void push(String orgDomain, String callback, Map<String, Object> m);

	void push(Session session, String callback, Map<String, Object> m);

	@Deprecated
	void sessionClosed(String orgDoamin, int sessionId);

	void sessionClosed(String orgDoamin, String sessionId);
}
