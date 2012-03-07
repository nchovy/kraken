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
package org.krakenapps.pcap.decoder.snmp.v2;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.snmp.ErrorStatus;
import org.krakenapps.pcap.decoder.snmp.types.Integer32;
import org.krakenapps.pcap.decoder.snmp.types.ObjectIdentifier;
import org.krakenapps.pcap.decoder.snmp.types.OctetString;
import org.krakenapps.pcap.decoder.snmp.types.PduType;
import org.krakenapps.pcap.decoder.snmp.types.RawPdu;
import org.krakenapps.pcap.decoder.snmp.types.Sequence;
import org.krakenapps.pcap.decoder.snmp.types.Variable;

public class Pdu {
	private String community;
	private PduType pduType;
	private long requestId; // -214783648..214783647
	private List<VariableBinding> variableBindings;

	// common case
	private ErrorStatus errorStatus;
	private long errorIndex; // 0..2147483647

	// bulk case
	private long nonRepeaters; // 0..214783647
	private long maxRepetitions; // 0..214783647

	public static Pdu parse(Sequence seq) {
		String community = ((OctetString) seq.get(1)).get();
		RawPdu rawPdu = (RawPdu) seq.get(2);

		long requestId = ((Integer32) rawPdu.get(0)).get();
		long field1 = ((Integer32) rawPdu.get(1)).get();
		long field2 = ((Integer32) rawPdu.get(1)).get();

		Pdu pdu = new Pdu(community, PduType.parse(rawPdu.getType()), requestId, field1, field2);

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

	public Pdu(String community, PduType pduType, long requestId, long field1, long field2) {
		this.community = community;
		this.pduType = pduType;
		this.requestId = requestId;

		if (pduType == PduType.GetBulkRequest) {
			this.nonRepeaters = field1;
			this.maxRepetitions = field2;
		} else {
			this.errorStatus = ErrorStatus.parse((int) field1);
			this.errorIndex = field2;
		}

		this.variableBindings = new ArrayList<VariableBinding>();
	}

	public PduType getPduType() {
		return pduType;
	}

	public String getCommunity() {
		return community;
	}

	public long getRequestId() {
		return requestId;
	}

	public ErrorStatus getErrorStatus() {
		if (pduType == PduType.GetBulkRequest)
			throw new RuntimeException("not supported field. check pdu type.");

		return errorStatus;
	}

	public long getErrorIndex() {
		if (pduType == PduType.GetBulkRequest)
			throw new RuntimeException("not supported field. check pdu type.");

		return errorIndex;
	}

	public long getNonRepeaters() {
		if (pduType != PduType.GetBulkRequest)
			throw new RuntimeException("not supported field. check pdu type.");

		return nonRepeaters;
	}

	public long getMaxRepetitions() {
		if (pduType != PduType.GetBulkRequest)
			throw new RuntimeException("not supported field. check pdu type.");

		return maxRepetitions;
	}

	public List<VariableBinding> getVariableBindings() {
		return variableBindings;
	}

}
