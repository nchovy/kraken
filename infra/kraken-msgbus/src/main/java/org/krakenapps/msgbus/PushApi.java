package org.krakenapps.msgbus;

import java.util.Map;

public interface PushApi {
	@Deprecated
	void subscribe(int organizationId, int sessionId, int processId, String callback);

	void subscribe(String orgDomain, int sessionId, int processId, String callback);

	@Deprecated
	void subscribe(int organizationId, int sessionId, int processId, String callback, Map<String, Object> options);

	void subscribe(String orgDomain, int sessionId, int processId, String callback, Map<String, Object> options);

	@Deprecated
	void unsubscribe(int organizationId, int sessionId, int processId, String callback);

	void unsubscribe(String orgDomain, int sessionId, int processId, String callback);

	void addInterceptor(String callback, PushInterceptor interceptor);

	void removeInterceptor(String callback);

	@Deprecated
	void push(int organizationId, String callback, Map<String, Object> m);

	void push(String orgDomain, String callback, Map<String, Object> m);

	void push(Session session, String callback, Map<String, Object> m);

	@Deprecated
	void sessionClosed(int organizationId, int sessionId);

	void sessionClosed(String orgDoamin, int sessionId);
}
