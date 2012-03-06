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
package org.krakenapps.iptables.match;

public class TcpMatchOption implements MatchOption {
	private PortRange sourcePortRange;
	private PortRange destinationPortRange;
	private boolean isSyn;
	private boolean isInvertedSyn;

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

	public boolean isSyn() {
		return isSyn;
	}

	public void setSyn(boolean isSyn) {
		this.isSyn = isSyn;
	}

	public boolean isInvertedSyn() {
		return isInvertedSyn;
	}

	public void setInvertedSyn(boolean isInvertedSyn) {
		this.isInvertedSyn = isInvertedSyn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationPortRange == null) ? 0 : destinationPortRange.hashCode());
		result = prime * result + (isInvertedSyn ? 1231 : 1237);
		result = prime * result + (isSyn ? 1231 : 1237);
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
		TcpMatchOption other = (TcpMatchOption) obj;
		if (destinationPortRange == null) {
			if (other.destinationPortRange != null)
				return false;
		} else if (!destinationPortRange.equals(other.destinationPortRange))
			return false;
		if (isInvertedSyn != other.isInvertedSyn)
			return false;
		if (isSyn != other.isSyn)
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
		String repr = "-m tcp ";

		if (sourcePortRange != null)
			repr += "--sport " + sourcePortRange + " ";

		if (destinationPortRange != null)
			repr += "--dport " + destinationPortRange + " ";

		return repr.substring(0, repr.length() - 1);
	}

}
