package org.krakenapps.msgbus;

public interface PermissionChecker {
	/**
	 * check permission
	 * 
	 * @param session
	 *            the admin session
	 * @param group
	 *            the permission group code
	 * @param code
	 *            the permission code
	 * @return false if permission is not allowed
	 */
	boolean check(Session session, String group, String permission);
}
