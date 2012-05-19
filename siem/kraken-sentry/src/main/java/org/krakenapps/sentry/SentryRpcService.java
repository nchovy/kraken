/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry;

import java.util.Collection;

import org.krakenapps.rpc.RpcService;

public interface SentryRpcService extends RpcService {
	Collection<String> getFeatures();

	void registerFeature(String feature);

	void unregisterFeature(String feature);

	Collection<String> getMethods();

	void register(String alias, Object service, String methodName);

	void unregister(String alias, Object service, String methodName);

	Object run(String method, Object[] params);
}
