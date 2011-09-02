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
package org.krakenapps.linux.api;

public class CpuUsage {
	private int idle;
	private int user;
	private int system;

	public CpuUsage(long deltaUser, long deltaNice, long deltaSystem, long deltaIdle) {
		long deltaSum = deltaUser + deltaNice + deltaSystem + deltaIdle;

		if (deltaSum != 0) {
			this.idle = (int) (100 * deltaIdle / deltaSum);
			this.user = (int) (100 * (deltaUser + deltaNice) / deltaSum);
		}
		this.system = 100 - (idle + user);
	}

	public int getIdle() {
		return idle;
	}

	public int getUser() {
		return user;
	}

	public int getSystem() {
		return system;
	}

	public int getUsage() {
		return user + system;
	}

	@Override
	public String toString() {
		return "idle=" + idle + ", system=" + system + ", user=" + user;
	}

}
