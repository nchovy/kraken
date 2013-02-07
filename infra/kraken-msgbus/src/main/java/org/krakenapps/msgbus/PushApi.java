/*
 * Copyright 2011 Future Systems, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
