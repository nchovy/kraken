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
package org.krakenapps.dom.api.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.file.FileConfigService;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.dom.model.Organization;

import static org.junit.Assert.*;

public class HostApiTest {

	private ConfigService conf;
	private ConfigManagerImpl cfg;
	private HostApiImpl hostApi;
	private OrganizationApiImpl orgApi;

	@Before
	public void setup() throws IOException {

		conf = new FileConfigService();
		if (conf.getDatabase("kraken-dom") != null)
			conf.dropDatabase("kraken-dom");
		if (conf.getDatabase("kraken-dom-localhost") != null)
			conf.dropDatabase("kraken-dom-localhost");

		cfg = new ConfigManagerImpl();
		cfg.setConfigService(conf);

		hostApi = new HostApiImpl();
		hostApi.setConfigManager(cfg);

		Organization org = new Organization();
		org.setDomain("localhost");
		org.setName("localhost");

		orgApi = new OrganizationApiImpl();
		orgApi.setConfigManager(cfg);
		orgApi.setConfigService(conf);
		orgApi.createOrganization(org);

	}

	@Test
	public void testCreate() {
		HostType hostType = createHostType("windows", "7");

		Collection<HostType> types = hostApi.getHostTypes("localhost");
		assertEquals(1, types.size());

		createHost(hostType, "test-guid", "Unit Test");
		assertEquals("test-guid", hostApi.getHost("localhost", "test-guid").getGuid());
	}

	private void createHost(HostType hostType, String guid, String name) {
		Host host = newHost(hostType, guid, name);
		hostApi.createHost("localhost", host);
	}

	private Host newHost(HostType hostType, String guid, String name) {
		Host host = new Host();
		host.setType(hostType);
		host.setGuid(guid);
		host.setName(name);
		return host;
	}

	private HostType createHostType(String name, String version) {
		HostType hostType = new HostType();
		hostType.setName(name);
		hostType.setVersion(version);
		hostApi.createHostType("localhost", hostType);
		return hostType;
	}

	@Test
	public void testBatchUpdate() {
		HostType hostType = createHostType("windows", "XP");

		// create 10 hosts
		for (int i = 0; i < 10; i++) {
			createHost(hostType, "host" + i, "host" + i);
		}

		// verify initial state
		int i = 0;
		for (Host host : hostApi.getHosts("localhost")) {
			assertEquals("host" + i, host.getGuid());
			assertEquals("host" + i, host.getName());
			i++;
		}

		// update host 5~9
		List<Host> updateHosts = Arrays.asList(newHost(hostType, "host9", "new host9"),
				newHost(hostType, "host5", "new host5"), newHost(hostType, "host6", "new host6"),
				newHost(hostType, "host7", "new host7"), newHost(hostType, "host8", "new host8"));

		hostApi.updateHosts("localhost", updateHosts);

		// verify
		i = 0;
		for (Host host : hostApi.getHosts("localhost")) {
			if (i < 5) {
				assertEquals("host" + i, host.getGuid());
				assertEquals("host" + i, host.getName());
			} else {
				assertEquals("host" + i, host.getGuid());
				assertEquals("new host" + i, host.getName());
			}
			i++;
		}
	}
}
