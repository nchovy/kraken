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
package org.krakenapps.dom.api;

import java.util.Date;
import java.util.Map;

import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;

public class UserConfigParser extends ConfigParser {
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Object obj, PrimitiveParseCallback callback) {
		if (!(obj instanceof Map))
			return null;

		User user = new User();
		Map<String, Object> m = (Map<String, Object>) obj;
		user.setLoginName((String) m.get("login_name"));
		if (m.get("org_unit") != null)
			user.setOrgUnit(callback.onParse(OrganizationUnit.class, (Map<String, Object>) m.get("org_unit")));
		user.setName((String) m.get("name"));
		user.setDescription((String) m.get("description"));
		user.setPassword((String) m.get("password"));
		user.setSalt((String) m.get("salt"));
		user.setTitle((String) m.get("title"));
		user.setEmail((String) m.get("email"));
		user.setPhone((String) m.get("phone"));
		user.setExt((Map<String, Object>) m.get("ext"));
		user.setCreated((Date) m.get("created"));
		user.setUpdated((Date) m.get("updated"));
		user.setLastPasswordChange((Date) m.get("last_password_change"));
		return user;
	}
}
