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
package org.krakenapps.pcap.decoder.snmp.v1;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.snmp.ErrorStatus;
import org.krakenapps.pcap.decoder.snmp.types.Integer32;
import org.krakenapps.pcap.decoder.snmp.types.NetworkAddress;
import org.krakenapps.pcap.decoder.snmp.types.ObjectIdentifier;
import org.krakenapps.pcap.decoder.snmp.types.OctetString;
import org.krakenapps.pcap.decoder.snmp.types.PduType;
import org.krakenapps.pcap.decoder.snmp.types.RawPdu;
import org.krakenapps.pcap.decoder.snmp.types.Sequence;
import org.krakenapps.pcap.decoder.snmp.types.TimeTicks;
import org.krakenapps.pcap.decoder.snmp.types.Variable;
import org.krakenapps.pcap.decoder.snmp.v2.VariableBinding;

public class Pdu {
	private String community;
	private PduType pduType;

	// pdu
	private long requestId;
	private ErrorStatus errorStatus;
	private long errorIndex;

	// trap pdu
	private ObjectIdentifier enterprise;
	private NetworkAddress agentAddr;
	private GenericTrap genericTrap;
	private long specificTrap;
	private TimeTicks timestamp;

	private List<VariableBinding> variableBindings;

	public static Pdu parse(Sequence seq) {
		String community = ((OctetString) seq.get(1)).get();
		RawPdu rawPdu = ((RawPdu) seq.get(2));
		Pdu pdu = null;

		if (rawPdu.getPduType() == PduType.Trap) {
			ObjectIdentifier enterprise = (ObjectIdentifier) rawPdu.get(0);
			NetworkAddress agentAddr = (NetworkAddress) rawPdu.get(1);
			GenericTrap genericTrap = GenericTrap.parse((int) ((Integer32) rawPdu.get(2)).get());
			long specificTrap = ((Integer32) rawPdu.get(3)).get();
			TimeTicks timestamp = (TimeTicks) rawPdu.get(4);

			pdu = new Pdu(community, rawPdu.getPduType(), enterprise, agentAddr, genericTrap, specificTrap, timestamp);
		} else {
			long requestId = ((Integer32) rawPdu.get(0)).get();
			ErrorStatus errorStatus = ErrorStatus.parse((int) ((Integer32) rawPdu.get(1)).get());
			long errorIndex = ((Integer32) rawPdu.get(1)).get();
			pdu = new Pdu(community, rawPdu.getPduType(), requestId, errorStatus, errorIndex);
		}

		Sequence vbs = (Sequence) rawPdu.get(3);
		for (int i = 0; i < vbs.size(); i++) {
			Sequence vb = (Sequence) vbs.get(i);
			ObjectIdentifier oid = (ObjectIdentifier) vb.get(0);
			Variable value = (Variable) vb.get(1);

			// add to pdu's variable binding list
			VariableBinding binding = new VariableBinding(oid, value);
			pdu.getVariableBindings().add(binding);
		}
		return pdu;
	}

	public Pdu(String community, PduType pduType, long requestId, ErrorStatus errorStatus, long errorIndex) {
		this.community = community;
		this.pduType = pduType;
		this.requestId = requestId;
		this.errorStatus = errorStatus;
		this.errorIndex = errorIndex;
		this.variableBindings = new ArrayList<VariableBinding>();
	}

	public Pdu(String community, PduType pduType, ObjectIdentifier enterprise, NetworkAddress agentAddr,
			GenericTrap genericTrap, long specificTrap, TimeTicks timestamp) {
		this.community = community;
		this.pduType = pduType;
		this.enterprise = enterprise;
		this.agentAddr = agentAddr;
		this.genericTrap = genericTrap;
		this.specificTrap = specificTrap;
		this.timestamp = timestamp;
		this.variableBindings = new ArrayList<VariableBinding>();
	}

	public String getCommunity() {
		return community;
	}

	public PduType getPduType() {
		return pduType;
	}

	public long getRequestId() {
		if (pduType == PduType.Trap)
			throw new RuntimeException("not supported field in trap");

		return requestId;
	}

	public ErrorStatus getErrorStatus() {
		if (pduType == PduType.Trap)
			throw new RuntimeException("not supported field in trap");

		return errorStatus;
	}

	public long getErrorIndex() {
		if (pduType == PduType.Trap)
			throw new RuntimeException("not supported field in trap");

		return errorIndex;
	}

	public ObjectIdentifier getEnterprise() {
		validateTrap();
		return enterprise;
	}

	public NetworkAddress getAgentAddr() {
		validateTrap();
		return agentAddr;
	}

	public GenericTrap getGenericTrap() {
		validateTrap();
		return genericTrap;
	}

	public long getSpecificTrap() {
		validateTrap();
		return specificTrap;
	}

	public TimeTicks getTimestamp() {
		validateTrap();
		return timestamp;
	}

	private void validateTrap() {
		if (pduType != PduType.Trap)
			throw new RuntimeException("not supported field. check pdu type.");
	}

	public List<VariableBinding> getVariableBindings() {
		return variableBindings;
	}

}
