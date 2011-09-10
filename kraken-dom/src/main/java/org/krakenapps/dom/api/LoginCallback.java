package org.krakenapps.dom.api;

import org.krakenapps.dom.model.Admin;
import org.krakenapps.msgbus.Session;

public interface LoginCallback {
	void onLoginSuccessCallback(Admin admin, Session session);

	void onLoginFailedCallback(Admin admin, Session session);

	void onLoginLockedCallback(Admin admin, Session session);
	
	void onLogout(Admin admin, Session session);
}
