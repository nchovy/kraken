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
package org.krakenapps.http;

import java.net.URL;

import org.krakenapps.http.internal.util.MimeTypes;
import org.osgi.service.http.HttpContext;
import org.osgi.framework.Bundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class BundleHttpContext implements HttpContext {
	private Bundle bundle;
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public BundleHttpContext(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getMimeType(String name) {
		int pos = name.lastIndexOf('.');
		if (pos < 0)
			return DEFAULT_MIME_TYPE;

		String extension = name.substring(pos + 1);
		String mimeType = MimeTypes.instance().getByExtension(extension);
		if (mimeType != null) {
			return mimeType;
		}

		return DEFAULT_MIME_TYPE;
	}

	public URL getResource(String name) {
		if (name.startsWith("/")) {
			name = name.substring(1);
		}

		return this.bundle.getEntry(name);
	}

	public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) {
		return true;
	}
}
