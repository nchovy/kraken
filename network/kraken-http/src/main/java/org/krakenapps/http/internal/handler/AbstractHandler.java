/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.http.internal.handler;

import javax.servlet.ServletException;

import org.krakenapps.http.internal.context.KrakenServletContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractHandler {
	private final static AtomicInteger ID = new AtomicInteger();

	private final String id;
	private final KrakenServletContext context;
	private final Map<String, String> initParams;

	public AbstractHandler(KrakenServletContext context) {
		this.id = "" + ID.incrementAndGet();
		this.context = context;
		this.initParams = new HashMap<String, String>();
	}

	public final String getId() {
		return this.id;
	}

	protected final KrakenServletContext getContext() {
		return this.context;
	}

	public final Map<String, String> getInitParams() {
		return this.initParams;
	}

	public final void setInitParams(Dictionary<?, ?> map) {
		this.initParams.clear();
		if (map == null) {
			return;
		}

		Enumeration<?> e = map.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object value = map.get(key);

			if ((key instanceof String) && (value instanceof String)) {
				this.initParams.put((String) key, (String) value);
			}
		}
	}

	public abstract void init() throws ServletException;

	public abstract void destroy();
}
