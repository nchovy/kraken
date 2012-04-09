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
package org.krakenapps.logparser.syslog.sourcefire;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.util.ProtocolNumbers;

public class SnortLogNormalizer implements LogNormalizer {

	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			m.put("type", "intrusion");
			m.put("category", "unknown");
			m.put("rule", params.get("msg"));
			m.put("severity", (Integer) params.get("priority"));
			m.put("src_ip", params.get("src_ip"));
			m.put("dst_ip", params.get("dst_ip"));
			m.put("src_port", params.get("src_port"));
			m.put("dst_port", params.get("dst_port"));
			m.put("proto", ProtocolNumbers.getNumber((String) params.get("proto")));

			return m;
		} catch (Exception e) {
			return null;
		}
	}
}
