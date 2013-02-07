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
package org.krakenapps.ftp.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.ftp.FtpConnectProfile;
import org.krakenapps.ftp.FtpProfileService;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@MsgbusPlugin
public class FtpMsgbusPlugin {
	@Requires
	private FtpProfileService service;

	@MsgbusMethod
	public void getProfiles(Request req, Response resp) {
		List<Object> profiles = new ArrayList<Object>();
		for (FtpConnectProfile profile : service.getProfiles())
			profiles.add(marshalProfile(profile));
		resp.put("profiles", profiles);
	}

	@MsgbusMethod
	public void createProfile(Request req, Response resp) {
		String name = req.getString("name");
		String host = req.getString("host");
		int port = req.getInteger("port");
		String account = req.getString("account");
		String password = req.getString("password");
		FtpConnectProfile profile = new FtpConnectProfile(name, host, port, account, password);
		service.createProfile(profile);
	}

	@MsgbusMethod
	public void removeProfile(Request req, Response resp) {
		String name = req.getString("name");
		service.removeProfile(name);
	}

	private Map<String, Object> marshalProfile(FtpConnectProfile profile) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", profile.getName());
		m.put("host", profile.getHost());
		m.put("port", profile.getPort());
		m.put("account", profile.getAccount());
		m.put("password", profile.getPassword());
		return m;
	}
}
