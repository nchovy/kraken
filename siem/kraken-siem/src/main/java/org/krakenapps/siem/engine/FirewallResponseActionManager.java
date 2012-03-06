/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.firewall.api.FirewallController;
import org.krakenapps.siem.response.AbstractResponseActionManager;
import org.krakenapps.siem.response.IntegerConfigOption;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseConfigOption;
import org.krakenapps.siem.response.StringConfigOption;

@Component(name = "siem-firewall-response")
@Provides
public class FirewallResponseActionManager extends AbstractResponseActionManager {
	@Requires
	private FirewallController controller;

	@Override
	public String getName() {
		return "firewall";
	}

	@Override
	public Collection<ResponseConfigOption> getConfigOptions() {
		List<ResponseConfigOption> options = new ArrayList<ResponseConfigOption>();
		options.add(new StringConfigOption("group_name", "Group Name", "The name of firewall response group"));
		options.add(new IntegerConfigOption("minutes", "Block Interval", "Block interval in minutes"));

		return options;
	}

	@Override
	protected ResponseAction createResponseAction(String namespace, String name, String description, Properties config) {
		return new FirewallResponseAction(this, controller, namespace, name, description, config);
	}
}
