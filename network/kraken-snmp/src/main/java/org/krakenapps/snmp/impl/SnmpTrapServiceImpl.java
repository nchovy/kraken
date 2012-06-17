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
package org.krakenapps.snmp.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.snmp.SnmpTrap;
import org.krakenapps.snmp.SnmpTrapBinding;
import org.krakenapps.snmp.SnmpTrapReceiver;
import org.krakenapps.snmp.SnmpTrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

@Component(name = "snmp-trap-service")
@Provides
public class SnmpTrapServiceImpl implements SnmpTrapService, CommandResponder {
	private final Logger logger = LoggerFactory.getLogger(SnmpTrapServiceImpl.class.getName());
	private final Charset charset = Charset.forName("utf-8");
	private ConcurrentMap<String, SnmpTrapBinding> bindings;
	private ConcurrentMap<String, SnmpListener> listeners;

	private CopyOnWriteArraySet<SnmpTrapReceiver> callbacks;
	private ConcurrentMap<String, CopyOnWriteArraySet<SnmpTrapReceiver>> bindingCallbacks;

	public SnmpTrapServiceImpl() {
		bindings = new ConcurrentHashMap<String, SnmpTrapBinding>();
		listeners = new ConcurrentHashMap<String, SnmpListener>();
		callbacks = new CopyOnWriteArraySet<SnmpTrapReceiver>();
		bindingCallbacks = new ConcurrentHashMap<String, CopyOnWriteArraySet<SnmpTrapReceiver>>();
	}

	@Validate
	public void start() {
	}

	@Invalidate
	public void stop() {
		// close all listeners
		for (SnmpListener listener : listeners.values()) {
			try {
				listener.snmp.close();
				listener.threadPool.stop();
			} catch (IOException e) {
				logger.error("kraken snmp: close failed", e);
			}
		}

		listeners.clear();
		bindings.clear();
		callbacks.clear();
		bindingCallbacks.clear();
	}

	@Override
	public List<String> getBindingNames() {
		return new ArrayList<String>(bindings.keySet());
	}

	@Override
	public SnmpTrapBinding getBinding(String name) {
		return bindings.get(name);
	}

	@Override
	public SnmpTrapBinding getBinding(InetSocketAddress listenAddress) {
		for (SnmpTrapBinding b : bindings.values())
			if (b.getListenAddress().equals(listenAddress))
				return b;

		return null;
	}

	@Override
	public void open(SnmpTrapBinding binding) throws IOException {
		if (binding.getName() == null || binding.getName().isEmpty())
			throw new IllegalArgumentException("empty binding name");

		SnmpTrapBinding old = bindings.putIfAbsent(binding.getName(), binding);
		if (old != null)
			throw new IllegalStateException("duplicated binding name: " + binding.getName());

		String addr = binding.getListenAddress().getAddress().getHostAddress();
		int port = binding.getListenAddress().getPort();
		UdpAddress udpAddress = new UdpAddress(addr + "/" + port);
		try {
			String threadPoolName = "SNMP Trap [" + binding.getName() + "]";
			ThreadPool threadPool = ThreadPool.create(threadPoolName, binding.getThreadCount());
			MessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
			TransportMapping transport = new DefaultUdpTransportMapping(udpAddress);

			Snmp snmp = new Snmp(dispatcher, transport);
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.addCommandResponder(this);
			snmp.listen();

			listeners.put(binding.getName(), new SnmpListener(snmp, threadPool));
			logger.info("kraken snmp: opened {} [{}]", binding.getName(), binding.getListenAddress());
		} catch (IOException e) {
			bindings.remove(binding.getName());
			throw e;
		}
	}

	@Override
	public void close(String name) throws IOException {
		SnmpListener listener = listeners.remove(name);
		if (listener != null) {
			listener.snmp.close();
			listener.threadPool.stop();
		}

		SnmpTrapBinding binding = bindings.remove(name);
		if (binding == null)
			throw new IllegalStateException("snmp trap binding not found: " + name);

		logger.info("kraken snmp: closed {} [{}]", binding.getName(), binding.getListenAddress());
	}

	@Override
	public void addReceiver(SnmpTrapReceiver callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeReceiver(SnmpTrapReceiver callback) {
		callbacks.remove(callback);
	}

	@Override
	public void addReceiver(String name, SnmpTrapReceiver callback) {
		CopyOnWriteArraySet<SnmpTrapReceiver> set = new CopyOnWriteArraySet<SnmpTrapReceiver>();
		CopyOnWriteArraySet<SnmpTrapReceiver> old = bindingCallbacks.putIfAbsent(name, set);
		if (old != null)
			set = old;

		set.add(callback);
	}

	@Override
	public void removeReceiver(String name, SnmpTrapReceiver callback) {
		CopyOnWriteArraySet<SnmpTrapReceiver> set = new CopyOnWriteArraySet<SnmpTrapReceiver>();
		CopyOnWriteArraySet<SnmpTrapReceiver> old = bindingCallbacks.putIfAbsent(name, set);
		if (old != null)
			set = old;

		set.remove(callback);
	}

	public void processPdu(CommandResponderEvent e) {
		PDU command = e.getPDU();
		if (command == null)
			return;

		if (logger.isTraceEnabled())
			logger.trace("kraken snmp: trap [{}]", e.toString());

		InetSocketAddress remote = toInetSocketAddress(e.getPeerAddress());
		InetSocketAddress local = toInetSocketAddress(e.getTransportMapping().getListenAddress());

		SnmpTrap trap = new SnmpTrap();
		trap.setRemoteAddress(remote);
		trap.setLocalAddress(local);

		if (command instanceof PDUv1) {
			PDUv1 v1 = (PDUv1) command;
			trap.setEnterpriseOid(v1.getEnterprise().toString());
			trap.setGenericTrap(v1.getGenericTrap());
			trap.setSpecificTrap(v1.getSpecificTrap());
		}

		for (Object o : command.getVariableBindings()) {
			VariableBinding binding = (VariableBinding) o;
			String oid = binding.getOid().toString();
			Object value = toPrimitive(binding.getVariable());
			trap.getVariableBindings().put(oid, value);
		}

		// invoke callbacks
		SnmpTrapBinding binding = getBinding(local);
		if (binding != null) {
			CopyOnWriteArraySet<SnmpTrapReceiver> callbacks = bindingCallbacks.get(binding.getName());
			if (callbacks != null)
				dispatchCallbacks(trap, callbacks);
		}

		dispatchCallbacks(trap, callbacks);
	}

	private void dispatchCallbacks(SnmpTrap trap, CopyOnWriteArraySet<SnmpTrapReceiver> callbacks) {
		for (SnmpTrapReceiver callback : callbacks) {
			try {
				callback.handle(trap);
			} catch (Throwable t) {
				logger.warn("kraken snmp: callback should not throw any exception", t);
			}
		}
	}

	private InetSocketAddress toInetSocketAddress(Address address) {
		try {
			String[] tokens = address.toString().split("/");
			int port = Integer.parseInt(tokens[1]);
			InetAddress addr = InetAddress.getByName(tokens[0]);
			return new InetSocketAddress(addr, port);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	private Object toPrimitive(Variable var) {
		switch (var.getSyntax()) {
		case SMIConstants.SYNTAX_COUNTER32:
			return var.toInt();
		case SMIConstants.SYNTAX_COUNTER64:
			return var.toLong();
		case SMIConstants.SYNTAX_GAUGE32:
			return var.toLong();
		case SMIConstants.SYNTAX_INTEGER:
			return var.toInt();
		case SMIConstants.SYNTAX_IPADDRESS:
			try {
				return InetAddress.getByName(var.toString());
			} catch (UnknownHostException e) {
				return null;
			}
		case SMIConstants.SYNTAX_NULL:
			return null;
		case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
			return var.toString();
		case SMIConstants.SYNTAX_OCTET_STRING:
			return new String(((OctetString) var).getValue(), charset);
		case SMIConstants.SYNTAX_TIMETICKS:
			return null;
		default:
			return null;
		}
	}

	private class SnmpListener {
		private Snmp snmp;
		private ThreadPool threadPool;

		public SnmpListener(Snmp snmp, ThreadPool threadPool) {
			this.snmp = snmp;
			this.threadPool = threadPool;
		}
	}
}
