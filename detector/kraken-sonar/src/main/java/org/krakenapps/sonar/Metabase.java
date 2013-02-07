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
 package org.krakenapps.sonar;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.sonar.metabase.model.Application;
import org.krakenapps.sonar.metabase.model.Environment;
import org.krakenapps.sonar.metabase.model.AttackLog;
import org.krakenapps.sonar.metabase.model.IpEndPoint;
import org.krakenapps.sonar.metabase.model.Vendor;

public interface Metabase {

	Collection<Vendor> getVendors();

	Vendor getVendor(String name);

	Vendor updateVendor(String name);

	void clearVendors();

	Collection<Environment> getEnvironments();

	Environment getEnvironment(Vendor vendor, String family, String description);

	Environment updateEnvironment(Vendor vendor, String family, String description);

	void clearEnvironments();

	Collection<IpEndPoint> getIpEndPoints();

	IpEndPoint getIpEndPoint(MacAddress mac);

	IpEndPoint updateIpEndPoint(MacAddress mac, InetAddress ip);

	IpEndPoint updateIpEndpoint(MacAddress mac, Environment environment);

	IpEndPoint updateIpEndPoint(InetSocketAddress localAddress);

	void clearIpEndpoints();

	void alert(AttackLog log);

	void clearIdsLog();

	Collection<Application> getApplications();

	Application getApplication(Vendor vendor, String name);

	Application updateApplication(Vendor vendor, String name, String version, IpEndPoint endpoint);

	void clearApplications();
}
