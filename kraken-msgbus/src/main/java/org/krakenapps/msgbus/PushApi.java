package org.krakenapps.msgbus;

import java.util.Map;

public interface PushApi {
	void subscribe(int organizationId, int sessionId, int processId, String callback);

	void subscribe(int organizationId, int sessionId, int processId, String callback, Map<String, Object> options);

	void unsubscribe(int organizationId, int sessionId, int processId, String callback);

	void addInterceptor(String callback, PushInterceptor interceptor);

	void removeInterceptor(String callback);

	void push(int organizationId, String callback, Map<String, Object> m);

	void push(Session session, String callback, Map<String, Object> m);

	void sessionClosed(int organizationId, int sessionId);
}
