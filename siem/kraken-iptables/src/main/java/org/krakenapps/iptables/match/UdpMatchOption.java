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
package org.krakenapps.iptables.match;

public class UdpMatchOption implements MatchOption {
	private PortRange sourcePortRange;
	private PortRange destinationPortRange;

	public PortRange getSourcePortRange() {
		return sourcePortRange;
	}

	public void setSourcePortRange(PortRange sourcePortRange) {
		this.sourcePortRange = sourcePortRange;
	}

	public PortRange getDestinationPortRange() {
		return destinationPortRange;
	}

	public void setDestinationPortRange(PortRange destinationPortRange) {
		this.destinationPortRange = destinationPortRange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationPortRange == null) ? 0 : destinationPortRange.hashCode());
		result = prime * result + ((sourcePortRange == null) ? 0 : sourcePortRange.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UdpMatchOption other = (UdpMatchOption) obj;
		if (destinationPortRange == null) {
			if (other.destinationPortRange != null)
				return false;
		} else if (!destinationPortRange.equals(other.destinationPortRange))
			return false;
		if (sourcePortRange == null) {
			if (other.sourcePortRange != null)
				return false;
		} else if (!sourcePortRange.equals(other.sourcePortRange))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String repr = "-m udp ";

		if (sourcePortRange != null)
			repr += "--sport " + sourcePortRange + " ";

		if (destinationPortRange != null)
			repr += "--dport " + destinationPortRange + " ";

		return repr.substring(0, repr.length() - 1);
	}

}
