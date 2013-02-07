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
package org.krakenapps.webconsole.plugins;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.AccountManager;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "webconsole-account-plugin")
@MsgbusPlugin
public class AccountPlugin {

	@Requires
	private AccountManager accountManager;

	@MsgbusMethod
	public void getAccounts(Request req, Response resp) {
		resp.put("accounts", accountManager.getAccounts());
	}

	@MsgbusPermission(group = "account", code = "manage")
	@MsgbusMethod
	public void createAccount(Request req, Response resp) {
		String user = req.getString("user");
		String password = req.getString("password");

		accountManager.createAccount(user, password);
	}

	@MsgbusMethod
	public void changePassword(Request req, Response resp) {
		String user = req.getString("user");
		String oldPassword = req.getString("old_password");
		String newPassword = req.getString("new_password");

		accountManager.changePassword(user, oldPassword, newPassword);
	}

	@MsgbusPermission(group = "account", code = "manage_account")
	@MsgbusMethod
	public void removeAccount(Request req, Response resp) {
		String user = req.getString("user");
		accountManager.removeAccount(user);
	}

}
