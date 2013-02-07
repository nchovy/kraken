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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.webconsole.PackageApi;
import org.krakenapps.webconsole.Program;
import org.krakenapps.webconsole.ProgramApi;

@Component(name = "webconsole-program-plugin")
@MsgbusPlugin
public class ProgramPlugin {
	@Requires
	private PackageApi packageApi;
	@Requires
	private ProgramApi programApi;

	@MsgbusMethod
	public void getPackages(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();
		List<Object> packages = getPackages(locale);
		resp.put("packages", packages);
	}

	@MsgbusMethod
	public void getPrograms(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();

		resp.put("packages", getPackages(locale));
		resp.put("programs", getPrograms(locale));
	}

	private List<Object> getPackages(Locale locale) {
		List<Object> packages = new ArrayList<Object>();

		for (String id : packageApi.getPackages()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", id);
			m.put("name", packageApi.getLabel(id, locale));
			packages.add(m);
		}
		return packages;
	}

	private List<Object> getPrograms(Locale locale) {
		List<Object> programs = new LinkedList<Object>();

		for (Program program : programApi.getPrograms()) {
			String name = programApi.getLabel(program.getPackageId(), program.getProgramId(), locale);
			programs.add(serialize(program, name));
		}
		return programs;
	}

	private Map<String, Object> serialize(Program program, String name) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("package_id", program.getPackageId());
		m.put("program_id", program.getProgramId());
		m.put("path", program.getPath());
		m.put("name", name);
		return m;
	}
}
