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
package org.krakenapps.iptables.impl;

import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.iptables.Chain;
import org.krakenapps.iptables.Iptables;
import org.krakenapps.iptables.NetworkAddress;
import org.krakenapps.iptables.Rule;
import org.krakenapps.iptables.RulePreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IptablesScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(IptablesScript.class.getName());

	private Iptables iptables;
	private ScriptContext context;

	public IptablesScript(Iptables iptables) {
		this.iptables = iptables;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		try {
			for (String chainName : iptables.getChainNames()) {
				int index = 1;
				context.println("Chain " + chainName);
				context.println("-----------------");
				for (Rule rule : iptables.getRules(chainName)) {
					context.println("[" + index++ + "] -A " + chainName + " " + rule.toString());
				}
				context.println("");
			}
		} catch (Exception e) {
			context.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@ScriptUsage(description = "add source blocking rule", arguments = { @ScriptArgument(name = "source ip", type = "string", description = "source ip") })
	public void blockSourceIp(String[] args) {
		try {
			NetworkAddress source = new NetworkAddress(args[0]);
			Rule rule = RulePreset.createSourceBlockRule(source);

			iptables.addRule(Chain.INPUT, 1, rule);
			context.println("blocked " + source);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken iptables: cannot block source ip " + args[0], e);
		}
	}

	@ScriptUsage(description = "remove source blocking rule", arguments = { @ScriptArgument(name = "source ip", type = "string", description = "source ip") })
	public void unblockSourceIp(String[] args) {
		try {
			NetworkAddress source = new NetworkAddress(args[0]);
			Rule target = RulePreset.createSourceBlockRule(source);

			int index = 1;
			List<Rule> rules = iptables.getRules(Chain.INPUT);
			for (Rule rule : rules) {
				if (rule.equals(target)) {
					iptables.removeRule(Chain.INPUT, index);
					break;
				}
				index++;
			}

			context.println("unblocked " + source);
		} catch (Exception e) {
			logger.error("kraken iptables: cannot unblock source ip " + args[0], e);
		}
	}

	@ScriptUsage(description = "remove rule of iptables", arguments = {
			@ScriptArgument(name = "chain name", type = "string", description = "chain name"),
			@ScriptArgument(name = "rule index", type = "int", description = "rule index number") })
	public void remove(String[] args) {
		try {
			String chainName = args[0];
			int index = Integer.valueOf(args[1]);

			iptables.removeRule(chainName, index);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken iptables: cannot remove rule", e);
		}
	}

}
