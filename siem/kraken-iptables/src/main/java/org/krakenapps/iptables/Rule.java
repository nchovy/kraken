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
package org.krakenapps.iptables;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.iptables.NetworkAddress;
import org.krakenapps.iptables.Rule;
import org.krakenapps.iptables.match.MatchOption;
import org.krakenapps.iptables.target.TargetOption;

public class Rule {
	private String in;
	private String out;
	private NetworkAddress source;
	private NetworkAddress destination;
	private String protocol;
	private String target;
	private List<MatchOption> matchOptions;
	private List<TargetOption> targetOptions;

	public Rule() {
		matchOptions = new ArrayList<MatchOption>();
		targetOptions = new ArrayList<TargetOption>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((in == null) ? 0 : in.hashCode());
		result = prime * result + ((matchOptions == null) ? 0 : matchOptions.hashCode());
		result = prime * result + ((out == null) ? 0 : out.hashCode());
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((targetOptions == null) ? 0 : targetOptions.hashCode());
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
		Rule other = (Rule) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (in == null) {
			if (other.in != null)
				return false;
		} else if (!in.equals(other.in))
			return false;
		if (matchOptions == null) {
			if (other.matchOptions != null)
				return false;
		} else if (!deepEquals(matchOptions, other.matchOptions))
			return false;
		if (out == null) {
			if (other.out != null)
				return false;
		} else if (!out.equals(other.out))
			return false;
		if (protocol == null) {
			if (other.protocol != null)
				return false;
		} else if (!protocol.equals(other.protocol))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (targetOptions == null) {
			if (other.targetOptions != null)
				return false;
		} else if (!deepEquals(targetOptions, other.targetOptions))
			return false;
		return true;
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public NetworkAddress getSource() {
		return source;
	}

	public void setSource(NetworkAddress source) {
		this.source = source;
	}

	public NetworkAddress getDestination() {
		return destination;
	}

	public void setDestination(NetworkAddress destination) {
		this.destination = destination;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<MatchOption> getMatchOptions() {
		return matchOptions;
	}

	public void setMatchOptions(List<MatchOption> matchOptions) {
		this.matchOptions = matchOptions;
	}

	public List<TargetOption> getTargetOptions() {
		return targetOptions;
	}

	public void setTargetOptions(List<TargetOption> targetOptions) {
		this.targetOptions = targetOptions;
	}

	private <T> boolean deepEquals(List<T> lhs, List<T> rhs) {
		if (lhs == null && rhs == null)
			return true;

		if (lhs == null || rhs == null)
			return false;

		if (lhs.size() != rhs.size())
			return false;

		for (int i = 0; i < lhs.size(); i++) {
			T l = lhs.get(i);
			T r = rhs.get(i);

			if (l == null && r == null)
				continue;

			if (l == null || r == null)
				return false;

			if (!l.equals(r))
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		String s = "";

		String m = "";
		for (int i = 0; i < matchOptions.size(); i++) {
			if (i != 0)
				m += " ";

			m += matchOptions.get(i);
		}

		String t = "";
		for (int i = 0; i < targetOptions.size(); i++) {
			if (i != 0)
				t += " ";

			t += targetOptions.get(i);
		}

		if (in != null)
			s += "-i " + in + " ";

		if (out != null)
			s += "-o " + in + " ";

		if (source != null)
			s += "-s " + source + " ";

		if (destination != null)
			s += "-d " + destination + " ";

		if (protocol != null)
			s += "-p " + protocol + " ";

		if (m.length() > 0)
			s += m + " ";

		s += "-j " + target + " ";

		if (t.length() > 0)
			s += t;

		return s;
	}
}
