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
import java.net.InetSocketAddress;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.FilterChain;
import org.krakenapps.filter.MessageBuilder;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.exception.ConfigurationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrapReceiver extends ActiveFilter implements SnmpTrapReceiver {
	private final Logger logger = LoggerFactory.getLogger(TrapReceiver.class.getName());
	private final MessageSpec outputSpec = new DefaultMessageSpec("kraken.snmp.trap", 1, 0);

	private FilterChain filterChain;
	private SnmpTrapBinding binding;
	private SnmpTrapService trapService;
	private boolean created;

	public TrapReceiver(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(SnmpTrapService.class.getName());
		this.trapService = (SnmpTrapService) bc.getService(ref);
	}

	@Override
	public MessageSpec getOutputMessageSpec() {
		return outputSpec;
	}

	@Override
	public void open() throws ConfigurationException {
		// bind address
		String bindAddress = (String) getProperty("address");
		if (bindAddress == null)
			bindAddress = "0.0.0.0";

		// port
		int port = 162;
		if (getProperty("port") != null)
			port = Integer.parseInt((String) getProperty("port"));

		// thread count
		int threadCount = 1;
		if (getProperty("thread") != null)
			threadCount = Integer.parseInt((String) getProperty("thread"));

		binding = trapService.getBinding(new InetSocketAddress(bindAddress, port));
		if (binding != null) {
			trapService.addReceiver(binding.getName(), this);
			return;
		}

		String name = bindAddress + "/" + port;
		binding = new SnmpTrapBinding();
		binding.setName(name);
		binding.setBindAddress(new InetSocketAddress(bindAddress, port));
		binding.setThreadCount(threadCount);

		try {
			trapService.open(binding);
			trapService.addReceiver(name, this);
			created = true;
		} catch (IOException e) {
			throw new ConfigurationException("port");
		}
	}

	@Override
	public void close() {
		try {
			trapService.removeReceiver(binding.getName(), this);
			if (created) {
				trapService.close(binding.getName());
				created = false;
			}
		} catch (IOException e) {
			logger.warn("trap: close error", e);
		}
	}

	@Override
	public void run() {
	}

	@Override
	public void handle(SnmpTrap trap) {
		if (trap == null)
			return;

		MessageBuilder mb = new MessageBuilder(outputSpec);
		mb.setHeader("remote_ip", trap.getRemoteAddress().getAddress());
		mb.setHeader("remote_port", trap.getRemoteAddress().getPort());
		mb.setHeader("local_port", trap.getLocalAddress().getPort());

		if (trap.getVersion() == 1) {
			mb.setHeader("enterprise", trap.getEnterpriseOid());
			mb.setHeader("generic_trap", trap.getGenericTrap());
			mb.setHeader("specific_trap", trap.getSpecificTrap());
		}

		for (String oid : trap.getVariableBindings().keySet()) {
			Object value = trap.getVariableBindings().get(oid);
			mb.set(oid, value);
		}

		filterChain.process(mb.build());

		if (logger.isTraceEnabled())
			logger.trace("kraken snmp: trap [{}]", trap);

	}
}
