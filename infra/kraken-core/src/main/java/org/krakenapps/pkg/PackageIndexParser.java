/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.pkg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.krakenapps.api.PackageIndex;
import org.krakenapps.api.PackageMetadata;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.Version;

public class PackageIndexParser {
	private PackageIndexParser() {
	}

	@SuppressWarnings("unchecked")
	public static PackageIndex parse(PackageRepository repo, byte[] doc) throws IOException, ClassNotFoundException {
		PackageIndex pl = new PackageIndex();

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(doc));
		pl.setDescription((String) ois.readObject());
		List<Map<String, Object>> objs = (List<Map<String, Object>>) ois.readObject();
		pl.setCreated((Date) ois.readObject());
		ois.close();

		List<PackageMetadata> packages = new ArrayList<PackageMetadata>();
		for (Map<String, Object> obj : objs) {
			PackageMetadata pm = new PackageMetadata();
			pm.setRepository(repo);
			pm.setName((String) obj.get("name"));
			pm.setDescription((String) obj.get("description"));
			pm.setMavenRepositories((Set<String>) obj.get("maven_repositories"));
			List<PackageVersionHistory> versions = new ArrayList<PackageVersionHistory>();
			List<Map<String, Object>> v = (List<Map<String, Object>>) obj.get("versions");
			for (Map<String, Object> m : v) {
				Version version = new Version((String) m.get("version"));
				Date lastUpdated = (Date) m.get("last_updated");
				versions.add(new PackageVersionHistory(version, lastUpdated));
			}
			pm.setVersions(versions);
			packages.add(pm);
		}
		pl.setPackages(packages);
		pl.setRepository(repo);

		return pl;
	}
}
