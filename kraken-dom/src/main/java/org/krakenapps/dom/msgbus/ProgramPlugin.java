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
package org.krakenapps.dom.msgbus;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-program-plugin")
@MsgbusPlugin
public class ProgramPlugin {
	@Requires
	private ProgramApi programApi;

	@MsgbusMethod
	public void getProgramProfiles(Request req, Response resp) {
		List<ProgramProfile> profiles = programApi.getAvailableProgramProfiles(req.getOrgId());
		resp.put("profiles", Marshaler.marshal(profiles));
	}

	@MsgbusMethod
	public void getAvailablePrograms(Request req, Response resp) {
		Session session = req.getSession();
		resp.put("packs", fetchProgramPacks(session.getOrgId()));
		resp.put("programs", fetchPrograms(session));
	}

	private List<Object> fetchProgramPacks(int organizationId) {
		return Marshaler.marshal(programApi.getAvailableProgramPacks(organizationId));
	}

	private List<Object> fetchPrograms(Session session) {
		List<Program> programs = programApi.getAvailablePrograms(session.getOrgId(), session.getAdminId());
		return Marshaler.marshal(programs);
	}

}
