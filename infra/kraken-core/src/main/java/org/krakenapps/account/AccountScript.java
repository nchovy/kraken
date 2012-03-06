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
package org.krakenapps.account;

import org.krakenapps.api.AccountManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(AccountScript.class.getName());
	private AccountManager manager;
	private ScriptContext context;

	public AccountScript(AccountManager manager) {
		this.manager = manager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "list all accounts")
	public void list(String[] args) {
		context.println("Accounts");
		context.println("==========");
		for (String name : manager.getAccounts()) {
			context.println(name);
		}
	}

	@ScriptUsage(description = "change password", arguments = { @ScriptArgument(name = "name", description = "name of the account") })
	public void passwd(String[] args) {
		try {
			String name = args[0];
			context.print("current password: ");
			String currentPassword = context.readPassword();
			context.print("new password: ");
			String newPassword = context.readPassword();
			context.print("confirm password: ");
			String confirm = context.readPassword();

			if (newPassword.equals(confirm)) {
				try {
					manager.changePassword(name, currentPassword, newPassword);
					context.println("password changed successfully");
				} catch (Exception e) {
					context.printf("change failed: %s\n", e.getMessage());
					logger.warn("error: ", e);
				}
			} else {
				context.println("password does not match");
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "create the account", arguments = { @ScriptArgument(name = "name", description = "name of the account") })
	public void create(String[] args) {
		try {
			String name = args[0];
			context.turnEchoOff();
			context.print("password: ");
			String password = context.readLine();
			context.print("confirm password: ");
			String confirm = context.readLine();
			context.turnEchoOn();

			if (password.equals(confirm)) {
				try {
					manager.createAccount(name, password);
				} catch (Exception e) {
					context.printf("create failed: %s\n", e.getMessage());
					logger.warn("error: ", e);
				}
			} else {
				context.println("password does not match");
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "remove the account", arguments = { @ScriptArgument(name = "name", description = "name of the account") })
	public void remove(String[] args) {
		String name = args[0];
		try {
			manager.removeAccount(name);
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
			logger.warn("remove account error: ", e);
		}
	}
}
