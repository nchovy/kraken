package org.krakenapps.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.FilterChain;
import org.krakenapps.filter.MessageBuilder;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.exception.ConfigurationException;
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
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class TrapReceiver extends ActiveFilter implements CommandResponder {
	private final Logger logger = LoggerFactory.getLogger(TrapReceiver.class
			.getName());
	private final MessageSpec outputSpec = new DefaultMessageSpec(
			"kraken.snmp.trap", 1, 0);
	private final Charset charset = Charset.forName("utf-8");

	private Snmp snmp;
	private FilterChain filterChain;
	private String bindAddress;
	private int port;
	private int threadCount;

	@Override
	public MessageSpec getOutputMessageSpec() {
		return outputSpec;
	}

	@Override
	public void open() throws ConfigurationException {
		// bind address
		bindAddress = (String) getProperty("address");
		if (bindAddress == null)
			bindAddress = "0.0.0.0";

		// port
		if (getProperty("port") == null)
			port = 162;
		else
			port = Integer.parseInt((String) getProperty("port"));

		// thread count
		if (getProperty("thread") == null)
			threadCount = 1;
		else
			threadCount = Integer.parseInt((String) getProperty("thread"));

		UdpAddress udpAddress = new UdpAddress(bindAddress + "/" + port);
		try {
			ThreadPool threadPool = ThreadPool.create("Trap", threadCount);
			MessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(
					threadPool, new MessageDispatcherImpl());
			TransportMapping transport = new DefaultUdpTransportMapping(
					udpAddress);

			snmp = new Snmp(dispatcher, transport);
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.addCommandResponder(this);
			snmp.listen();
		} catch (IOException e) {
			throw new ConfigurationException("bind trap port", e.getMessage());
		}
	}

	@Override
	public void close() {
		try {
			snmp.close();
		} catch (IOException e) {
			logger.warn("trap: close error", e);
		}
	}

	@Override
	public void run() {

	}

	@Override
	public void processPdu(CommandResponderEvent e) {
		PDU command = e.getPDU();
		if (command == null)
			return;

		MessageBuilder mb = new MessageBuilder(outputSpec);
		try {
			String[] tokens = e.getPeerAddress().toString().split("/");
			mb.setHeader("remote_ip", InetAddress.getByName(tokens[0]));
			mb.setHeader("remote_port", Integer.parseInt(tokens[1]));
			mb.setHeader("local_port", port);

			if (command instanceof PDUv1) {
				PDUv1 v1 = (PDUv1) command;
				mb.setHeader("enterprise", v1.getEnterprise().toString());
				mb.setHeader("generic_trap", v1.getGenericTrap());
				mb.setHeader("specific_trap", v1.getSpecificTrap());
			}
		} catch (UnknownHostException e1) {
			System.out.println("remote ip: " + e.getPeerAddress());
			e1.printStackTrace();
		}

		for (Object o : command.getVariableBindings()) {
			VariableBinding binding = (VariableBinding) o;
			String oid = binding.getOid().toString();
			Object value = toPrimitive(binding.getVariable());

			mb.set(oid, value);
		}

		filterChain.process(mb.build());

		if (logger.isTraceEnabled())
			logger.trace("snmp trap: " + e.toString());
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
}
