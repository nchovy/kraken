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
package org.krakenapps.servlet.xml;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.osgi.service.http.NamespaceException;

public interface XmlHttpServiceApi {
	void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException;

	Object invokeXmlMethod(String serviceName, String methodName, Map<String, Object> params) throws Exception;

	Map<String, Set<String>> getXmlHttpMappings();
}
