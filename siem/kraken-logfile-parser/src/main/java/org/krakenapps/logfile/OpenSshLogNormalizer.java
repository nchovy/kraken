/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.logfile;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogNormalizer;

public class OpenSshLogNormalizer implements LogNormalizer {
	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		if (!params.containsKey("type"))
			return null;

		String type = (String) params.get("type");
		if (!type.equals("login"))
			return null;

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("category", "login");
		m.put("date", params.get("date"));
		m.put("result", params.get("result"));
		m.put("account", params.get("account"));
		m.put("src_ip", params.get("src_ip"));
		m.put("src_port", params.get("src_port"));
		m.put("protocol", params.get("protocol"));

		return m;
	}

}
