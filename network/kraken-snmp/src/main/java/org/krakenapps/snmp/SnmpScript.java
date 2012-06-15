/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.snmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpScript implements Script {
	private SnmpTrapService trap;
	private ScriptContext context;

	public SnmpScript(SnmpTrapService trap) {
		this.trap = trap;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void trapbindings(String[] args) {
		context.println("Trap Bindings");
		context.println("---------------");
		for (String name : trap.getBindingNames())
			context.println(name + " => " + trap.getBinding(name));
	}

	@ScriptUsage(description = "trace snmp trap packet")
	public void trace(String[] args) {
		Map<String, String> filters = new HashMap<String, String>();

		for (String arg : args) {
			int p = arg.indexOf("=");
			String key = arg.substring(0, p);
			String value = arg.substring(p + 1);
			filters.put(key, value);

			context.println("applying filter " + key + " => " + value);
		}

		context.println("press ctrl-c to stop");
		context.println("----------------------");

		Tracer tracer = new Tracer(filters);
		trap.addReceiver(tracer);

		try {
			while (true) {
				context.readLine();
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			trap.removeReceiver(tracer);
		}

	}

	private class Tracer implements SnmpTrapReceiver {
		private Map<String, String> filters;

		public Tracer(Map<String, String> filters) {
			this.filters = filters;
		}

		@Override
		public void handle(SnmpTrap trap) {
			String remote = trap.getRemoteAddress().getAddress().getHostAddress() + ":" + trap.getRemoteAddress().getPort();

			if (filters != null && !checkFilter(trap))
				return;

			context.println(remote + " " + trap.getVariableBindings());
		}

		private boolean checkFilter(SnmpTrap trap) {
			for (String key : filters.keySet()) {
				String expected = filters.get(key);
				Object value = trap.getVariableBindings().get(key);
				String actual = null;
				if (value != null)
					actual = value.toString();

				if ((expected == null || expected.isEmpty())) {
					if (actual == null)
						continue;
					else
						return false;
				}

				if (actual == null)
					return false;

				if (!expected.equals(actual))
					return false;
			}

			return true;
		}
	}

	public void get(String[] args) {
		Map<String, String> options = parseOptions(args);
		String version = options.get("-v");
		String community = options.get("-c");
		String host = options.get("-h");
		String port = options.get("-p");
		String oid = args[args.length - 1];

		// .1.3.6.1.2.1.25.1.1.0
		if (version.equals("2c")) {
			getV2(host, port, community, oid);
		}
	}

	private static HashMap<String, Integer> methodMap = new HashMap<String, Integer>();
	static {
		methodMap.put("GET", PDU.GET);
		methodMap.put("GETNEXT", PDU.GETNEXT);
		methodMap.put("GETBULK", PDU.GETBULK);
	}

	private int getMethod(String method) {
		Integer ret = methodMap.get(method.toUpperCase());
		if (ret != null) {
			return ret;
		} else {
			return -1;
		}
	}

	public void getsubtree(String[] args) {

		Map<String, String> options = parseOptions(args);
		String community = options.get("-c");
		String host = options.get("-h");
		String port = options.get("-p");
		String oid = args[args.length - 1];

		Snmp snmp = null;
		TransportMapping transport = null;

		try {
			CommunityTarget commTarget = new CommunityTarget();
			commTarget.setCommunity(new OctetString(community));
			commTarget.setVersion(SnmpConstants.version2c);
			commTarget.setAddress(new UdpAddress(host + "/" + port));
			commTarget.setRetries(2);
			commTarget.setTimeout(3000);

			transport = new DefaultUdpTransportMapping();
			transport.listen();

			snmp = new Snmp(transport);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(10);

			ArrayList<VariableBinding> results = new ArrayList<VariableBinding>();

			OID ifEntryOid = new OID(oid);
			while (true) {
				ResponseEvent response = null;
				response = snmp.getBulk(pdu, commTarget);

				PDU responsePdu = response.getResponse();
				if (responsePdu == null) {
					throw new RuntimeException(response.getError());
				}

				@SuppressWarnings("unchecked")
				Vector<VariableBinding> variableBindings = responsePdu.getVariableBindings();
				VariableBinding lastElement = variableBindings.lastElement();
				if (lastElement != null && lastElement.getOid().startsWith(ifEntryOid)) {
					results.addAll(variableBindings);
					pdu.remove(0);
					pdu.add(new VariableBinding(lastElement.getOid()));
					// continue to retrieve next bulk
					continue;
				} else {
					// break loop.
					// find last element startsWith ifEntryOid
					for (VariableBinding vb : variableBindings) {
						if (vb.getOid().startsWith(ifEntryOid))
							results.add(vb);
						else {
							break;
						}
					}
					// break while loop
					break;
				}
			}

			for (VariableBinding vb : results) {
				context.println(vb.toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
				}
			}

			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void getv2c(String[] args) {
		Map<String, String> options = parseOptions(args);
		String methodStr = options.get("-m");
		String community = options.get("-c");
		String host = options.get("-h");
		String port = options.get("-p");
		String bulkNumber = options.get("-n");

		int method = getMethod(methodStr);
		if (method == -1)
			throw new RuntimeException("not supported method: " + methodStr);

		String oid = args[args.length - 1];

		Snmp snmp = null;
		TransportMapping transport = null;
		try {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			target.setVersion(SnmpConstants.version2c);
			target.setAddress(new UdpAddress(host + "/" + port));
			target.setRetries(2);
			target.setTimeout(3000);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(method);
			if (pdu.getType() == PDU.GETBULK)
				pdu.setMaxRepetitions(Integer.parseInt(bulkNumber));

			transport = new DefaultUdpTransportMapping();
			transport.listen();

			snmp = new Snmp(transport);

			ResponseEvent response = null;
			switch (method) {
			case PDU.GETNEXT:
				response = snmp.getNext(pdu, target);
				break;
			case PDU.GETBULK:
				response = snmp.getBulk(pdu, target);
				break;
			default:
				response = snmp.get(pdu, target);
			}

			PDU responsePdu = response.getResponse();
			if (responsePdu == null) {
				context.println("request timeout.");
				return;
			}

			Iterator<?> it = responsePdu.getVariableBindings().iterator();
			while (it.hasNext()) {
				context.println(it.next().toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
				}
			}

			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void getV2(String host, String port, String community, String oid) {
		Snmp snmp = null;
		TransportMapping transport = null;
		try {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(community));
			target.setVersion(SnmpConstants.version2c);
			target.setAddress(new UdpAddress(host + "/" + port));
			target.setRetries(2);
			target.setTimeout(3000);

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oid)));
			pdu.setType(PDU.GET);

			transport = new DefaultUdpTransportMapping();
			transport.listen();

			snmp = new Snmp(transport);

			ResponseEvent response = snmp.get(pdu, target);

			PDU responsePdu = response.getResponse();
			if (responsePdu == null) {
				context.println("request timeout.");
				return;
			}

			Iterator<?> it = responsePdu.getVariableBindings().iterator();
			while (it.hasNext()) {
				context.println(it.next().toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (IOException e) {
				}
			}

			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Map<String, String> parseOptions(String[] args) {
		Map<String, String> options = new HashMap<String, String>();
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) == '-') {
				if (i + 1 < args.length)
					options.put(args[i], args[++i]);
			}
		}
		return options;
	}
}
