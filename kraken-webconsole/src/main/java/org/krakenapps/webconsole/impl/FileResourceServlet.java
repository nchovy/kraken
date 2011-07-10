/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.webconsole.ResourceServlet;

@Component(name = "webconsole-file-resource-servlet")
@Provides
public class FileResourceServlet extends ResourceServlet {
	private static final long serialVersionUID = 1L;
	private File basePath;

	public FileResourceServlet(File basePath) {
		this.basePath = basePath;
	}

	@Override
	protected InputStream getInputStream(HttpServletRequest req) {
		try {
			return new FileInputStream(new File(basePath, req.getPathInfo()));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("filesystem resource: %s", basePath);
	}
}
