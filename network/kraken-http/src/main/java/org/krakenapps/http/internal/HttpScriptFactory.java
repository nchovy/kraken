/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.http.internal;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.http.HttpServiceManager;
import org.krakenapps.http.FileUploadService;
import org.osgi.framework.BundleContext;

public class HttpScriptFactory implements ScriptFactory {
	private BundleContext context;
	
	private HttpServiceManager httpServiceManager;
	private FileUploadService uploadFileService;
	
	public HttpScriptFactory(BundleContext context) {
		this.context = context;
	}

	@Override
	public Script createScript() {
		return new HttpScript(context, httpServiceManager, uploadFileService);
	}
}
