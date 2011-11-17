/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.api;

import java.util.Collection;

import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationGroup;
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;

public interface ApplicationApi extends EntityEventProvider<Application> {
	Collection<Vendor> getVendors(String domain);

	Vendor findVendor(String domain, String guid);

	Vendor getVendor(String domain, String guid);

	void createVendor(String domain, Vendor vendor);

	void updateVendor(String domain, Vendor vendor);

	void removeVendor(String domain, String guid);

	Collection<Application> getApplications(String domain);

	Application findApplication(String domain, String guid);

	Application getApplication(String domain, String guid);

	void createApplication(String domain, Application application);

	void updateApplication(String domain, Application application);

	void removeApplication(String domain, String guid);

	Collection<ApplicationVersion> getApplicationVersions(String domain);

	ApplicationVersion findApplicationVersion(String domain, String guid);

	ApplicationVersion getApplicationVersion(String domain, String guid);

	void createApplicationVersion(String domain, ApplicationVersion version);

	void updateApplicationVersion(String domain, ApplicationVersion version);

	void removeApplicationVersion(String domain, String guid);

	Collection<ApplicationGroup> getApplicationGroups(String domain);

	ApplicationGroup findApplicationGroup(String domain, String guid);

	ApplicationGroup getApplicationGroup(String domain, String guid);

	void createApplicationGroup(String domain, ApplicationGroup group);

	void updateApplicationGroup(String domain, ApplicationGroup group);

	void removeApplicationGroup(String domain, String guid);
}
