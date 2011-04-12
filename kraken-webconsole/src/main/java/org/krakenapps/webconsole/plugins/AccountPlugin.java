package org.krakenapps.webconsole.plugins;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.AccountManager;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.exception.InvalidPasswordException;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.AllowGuestAccess;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-account-plugin")
@MsgbusPlugin
public class AccountPlugin {
	private final Logger logger = LoggerFactory.getLogger(AccountPlugin.class.getName());

	@Requires
	private AccountManager accountManager;

	@Requires
	private AdminApi adminApi;

	@AllowGuestAccess
	@MsgbusMethod
	public void login(Request req, Response resp) throws AdminNotFoundException, InvalidPasswordException {
		String user = req.getString("user");
		String password = req.getString("password");

		logger.trace("kraken webconsole: login attempt user [{}], password [{}]", new Object[] { user, password });

		String hash = adminApi.hash(adminApi.hashPassword(password));
		String nonce = "";
		Admin admin = adminApi.login(user, hash, nonce);

		resp.put("result", true);
		req.getSession().setProperty("org_id", admin.getUser().getOrganization().getId());
		req.getSession().setProperty("admin_id", admin.getId());
	}

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
