/*
 * Copyright 2008 NCHOVY
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
package org.krakenapps.xmlrpc;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlRpcMethodResponseBuilder {
	public static Document result(Object target) {
		Document document = XmlUtil.newDocument();
		Element methodResponse = document.createElement("methodResponse");
		Element params = document.createElement("params");
		Element param = document.createElement("param");
		param.appendChild(XmlRpcBuilderUtil.buildValueElement(document, target));
		params.appendChild(param);
		methodResponse.appendChild(params);
		document.appendChild(methodResponse);
		return document;
	}

	public static Document fault(Throwable t) {
		Map<String, Object> faultMap = new HashMap<String, Object>();
		faultMap.put("faultCode", 0);
		faultMap.put("faultString", t.getMessage());

		Document document = XmlUtil.newDocument();
		Element methodResponse = document.createElement("methodResponse");
		Element fault = document.createElement("fault");
		fault.appendChild(XmlRpcBuilderUtil.buildValueElement(document, faultMap));
		methodResponse.appendChild(fault);
		document.appendChild(methodResponse);
		return document;
	}

}