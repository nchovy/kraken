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
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;

public interface ApplicationApi {
	Collection<Vendor> getVendors();

	Vendor getVendor(String vendorName);

	Vendor createVendor(String name);

	void updateVendor(int id, String name);

	void removeVendor(int id);

	Collection<Application> getApplications(String vendorName);
	
	Application getApplication(String vendorName, String name);

	Application createApplication(String vendorName, String name);

	void updateApplication(int id, String name);

	void removeApplication(int id);
	
	Collection<ApplicationVersion> getApplicationVersions(String vendorName, String appName);
	
	ApplicationVersion createApplicationVersion(String vendorName, String appName, String version);

	void updateApplicationVersion(int id, String version);

	void removeApplicationVersion(int id);
}
