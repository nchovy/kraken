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

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;

public interface SyslogServerRegistry {
	boolean contains(String name);

	Collection<String> getNames();

	SyslogServer getServer(String name);

	SyslogServer findServer(InetSocketAddress local);

	Collection<SyslogProfile> getSyslogProfiles();

	void open(SyslogProfile profile) throws SocketException;

	void close(String name);

	void register(String name, SyslogServer server);

	void unregister(String name);

	void addSyslogListener(SyslogListener callback);

	void removeSyslogListener(SyslogListener callback);

	void addEventListener(SyslogServerRegistryEventListener callback);

	void removeEventListener(SyslogServerRegistryEventListener callback);
}
