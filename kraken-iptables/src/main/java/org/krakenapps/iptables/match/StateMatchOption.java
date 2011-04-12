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

import java.util.Arrays;

public class StateMatchOption implements MatchOption {
	private String[] states;

	public StateMatchOption(String[] states) {
		this.states = states;
	}

	public String[] getStates() {
		return states;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(states);
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
		StateMatchOption other = (StateMatchOption) obj;
		if (!Arrays.equals(states, other.states))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String repr = "-m state --state ";
		for (int i = 0; i < states.length; i++) {
			if (i != 0)
				repr += ",";

			repr += states[i];
		}

		return repr;
	}

}
