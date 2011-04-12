package org.krakenapps.iptables.msgbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.iptables.Chain;
import org.krakenapps.iptables.Iptables;
import org.krakenapps.iptables.Rule;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "iptables-plugin")
@Provides
@MsgbusPlugin
public class IptablesPlugin {
	private final Logger logger = LoggerFactory.getLogger(IptablesPlugin.class);
	@Requires
	private Iptables iptables;

	@MsgbusMethod
	public void getChainNames(Request req, Response resp) {
		try {
			resp.put("chain_names", iptables.getChainNames());
		} catch (IOException e) {
			logger.error("kraken-iptabls: failed to get chain names.", e);
		}
	}

	@MsgbusMethod
	public void getRules(Request req, Response resp) {
		try {
			List<Rule> rules = null;

			if (req.has("chain_type"))
				rules = iptables.getRules(Chain.valueOf(req.getString("chain_type")));
			else if (req.has("chain_name"))
				rules = iptables.getRules(req.getString("chain_name"));

			if (rules != null) {
				List<Object> r = new ArrayList<Object>();
				for (Rule rule : rules)
					r.add(marshal(rule));
				resp.put("rules", r);
			}
		} catch (IOException e) {
			logger.error("kraken-iptabls: failed to get rules.", e);
		}
	}

	private Map<String, Object> marshal(Rule rule) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("in", rule.getIn());
		m.put("out", rule.getOut());
		m.put("source", rule.getSource().getNetworkAddress().getHostAddress());
		m.put("destination", rule.getDestination().getNetworkAddress().getHostAddress());
		m.put("protocol", rule.getProtocol());
		m.put("target", rule.getTarget());
		m.put("match_options", rule.getMatchOptions());
		m.put("target_options", rule.getTargetOptions());
		return m;
	}

	@MsgbusMethod
	public void addRule(Request req, Response resp) {
		Integer index = req.getInteger("index");
		Rule rule = convertRule(req.get("rule"));

		if (req.has("chain_type")) {
			Chain chain = Chain.valueOf(req.getString("chain_type"));
			if (index == null)
				iptables.addRule(chain, rule);
			else
				iptables.addRule(chain, index, rule);
		} else if (req.has("chain_name")) {
			String chainName = req.getString("chain_name");
			if (index == null)
				iptables.addRule(chainName, rule);
			else
				iptables.addRule(chainName, index, rule);
		}
	}

	private Rule convertRule(Object obj) {
		return null;
	}

	@MsgbusMethod
	public void removeRule(Request req, Response resp) {
		int index = req.getInteger("index");

		try {
			if (req.has("chain_type"))
				iptables.removeRule(Chain.valueOf(req.getString("chain_type")), index);
			else if (req.has("chain_name"))
				iptables.removeRule(req.getString("chain_name"), index);
		} catch (IOException e) {
			logger.error("kraken-iptabls: failed to remove rule.", e);
		}
	}
}
