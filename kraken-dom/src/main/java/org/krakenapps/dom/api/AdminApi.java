/*
 * Copyright 2011 Future Systems, Inc.
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

import java.util.List;

import org.krakenapps.dom.exception.InvalidPasswordException;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Admin;

public interface AdminApi extends EntityEventProvider<Admin> {
	Admin login(String nick, String hash, String nonce) throws AdminNotFoundException, InvalidPasswordException;

	List<Admin> getAdmins(int organizationId);

	Admin getAdmin(int organizationId, int adminId);

	Admin getAdminByUser(int organizationId, int userId);

	void createAdmin(int organizationId, Integer requestAdminId, Admin admin);

	void updateAdmin(int organizationId, Integer requestAdminId, Admin admin);

	void removeAdmin(int organizationId, Integer requestAdminId, int adminId);

	boolean matchPassword(int organizationId, int adminId, String password);

	String hash(String text);

	String hashPassword(String salt, String text);
}
