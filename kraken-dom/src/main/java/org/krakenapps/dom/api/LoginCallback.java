package org.krakenapps.dom.api;

import org.krakenapps.dom.model.Admin;
import org.krakenapps.msgbus.Session;

public interface LoginCallback {
	void onLoginSuccess(Admin admin, Session session);

	void onLoginFailed(Admin admin, Session session);

	void onLoginLocked(Admin admin, Session session);
	
	void onLogout(Admin admin, Session session);
}
