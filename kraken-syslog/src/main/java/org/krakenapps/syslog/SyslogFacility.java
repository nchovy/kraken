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
package org.krakenapps.syslog;

public enum SyslogFacility {
	Kernel(0), User(1), Mail(2), System(3), Security(4), Syslogd(5), LinePrinter(6), News(7), Uucp(8), Clock(9), Ftp(11), Ntp(
			12), Local0(16), Local1(17), Local2(18), Local3(19), Local4(20), Local5(21), Local6(22), Local7(23);

	private int code;

	private SyslogFacility(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
