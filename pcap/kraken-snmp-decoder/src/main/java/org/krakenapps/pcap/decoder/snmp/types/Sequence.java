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
package org.krakenapps.pcap.decoder.snmp.types;

import java.util.ArrayList;
import java.util.List;

public class Sequence extends Variable {
	protected List<Variable> sequence;

	public Sequence(byte[] buffer, int offset, int length) {
		sequence = new ArrayList<Variable>();

		for (int i = offset; i < offset + length;) {
			int nextLength = getNextLength(buffer, i + 1);
			sequence.add(decode(buffer, i, nextLength));

			i += nextLength + 1 + getLengthByteCount(nextLength);
		}

		type = Variable.SEQUENCE;
	}

	public Variable get(int index) {
		return sequence.get(index);
	}

	public int size() {
		return sequence.size();
	}

	public String toString() {
		String out = "SEQUENCE { ";

		for (int i = 0; i < sequence.size(); i++) {
			Variable item = sequence.get(i);
			if (item != null)
				out += item.toString();
		}

		out += "}";
		return out;
	}
}