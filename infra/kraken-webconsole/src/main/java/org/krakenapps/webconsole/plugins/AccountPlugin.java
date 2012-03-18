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
