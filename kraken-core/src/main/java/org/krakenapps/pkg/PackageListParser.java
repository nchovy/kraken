package org.krakenapps.pkg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.Version;

public class PackageListParser {
	private PackageListParser() {
	}

	@SuppressWarnings("unchecked")
	public static PackageList parse(PackageRepository repo, byte[] doc) throws IOException, ClassNotFoundException {
		PackageList pl = new PackageList();

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
			pm.setMavenRepositories((Set<URL>) obj.get("maven_repositories"));
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
