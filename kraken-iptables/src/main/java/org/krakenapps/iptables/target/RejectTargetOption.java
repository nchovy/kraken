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
package org.krakenapps.iptables.target;

public class RejectTargetOption implements TargetOption {
	public enum Type {
		IcmpNetUnreachable("icmp-net-unreachable"),
		IcmpHostUnreachable("icmp-host-unreachable"),
		IcmpPortUnreachable("icmp-port-unreachable"),
		IcmpProtoUnreachable("icmp-proto-unreachable"),
		IcmpNetProhibited("icmp-net-prohibited"),
		IcmpHostProhibited("icmp-host-prohibited"),
		IcmpAdminProhibited("icmp-admin-prohibited");

		private String name;

		Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private Type type;

	public RejectTargetOption(String type) {
		for (Type t : Type.values())
			if (t.getName().equals(type))
				this.type = t;

		if (type == null)
			throw new IllegalArgumentException("invalid reject-with option: " + type);
	}

	public RejectTargetOption(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "--reject-with " + type.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		RejectTargetOption other = (RejectTargetOption) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
