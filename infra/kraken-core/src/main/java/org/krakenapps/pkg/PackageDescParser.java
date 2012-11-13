/*
 * Copyright 2009 NCHOVY
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

import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.MavenArtifact;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageMetadata;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.Version;
import org.krakenapps.api.VersionRange;

public class PackageDescParser {
	private PackageDescParser() {
	}

	public static PackageDescriptor parse(PackageMetadata metadata, PackageVersionHistory version,
			String body) {
		String[] lines = body.split("\n");
		String subject = null;

		PackageDescriptor pkgDesc = new PackageDescriptor(metadata.getName(), version.getVersion(), version
				.getLastUpdated(), metadata.getDescription());

		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith(";"))
				continue;

			if (line.startsWith("[")) {
				subject = line;
				continue;
			}

			if (subject.equals("[bundle]")) {
				BundleRequirement bundleDesc = parseBundleRequirement(line);
				pkgDesc.getBundleRequirements().add(bundleDesc);

			} else if (subject.equals("[maven]")) {
				MavenArtifact artifact = parseMavenArtifact(line);
				pkgDesc.getMavenArtifacts().add(artifact);
			} else if (subject.equals("[start]")) {
				pkgDesc.getStartBundleNames().add(line);
			}

		}

		return pkgDesc;
	}

	private static BundleRequirement parseBundleRequirement(String line) {
		int pos = line.indexOf(" ");
		if (pos < 0)
			pos = line.indexOf("\t");
		
		if (pos < 0)
			throw new IllegalStateException("version not found, check syntax");

		String symbolicName = line.substring(0, pos).trim();
		String version = line.substring(pos + 1).trim();

		VersionRange range = null;
		if (version.startsWith("[")) {
			String[] pair = version.substring(1, version.length() - 1).split(",");
			Version low = new Version(pair[0].trim());
			Version high = new Version(pair[1].trim());
			range = new VersionRange(low, high);
		} else {
			range = new VersionRange(new Version(version.trim()));
		}

		return new BundleRequirement(symbolicName, range);
	}

	private static MavenArtifact parseMavenArtifact(String line) {
		String tokens[] = removeEmptyString(line.split("[ \t]"));

		MavenArtifact artifact = new MavenArtifact();
		artifact.setGroupId(tokens[0].trim());
		artifact.setArtifactId(tokens[1].trim());
		artifact.setVersion(new Version(tokens[2].trim()));

		return artifact;
	}

	private static String[] removeEmptyString(String[] tokens) {
		int emptyCount = 0;

		for (String token : tokens)
			if (token.isEmpty())
				emptyCount++;

		String[] fullArray = new String[tokens.length - emptyCount];

		int i = 0;

		for (String token : tokens)
			if (token.isEmpty() == false)
				fullArray[i++] = token;

		return fullArray;
	}
}
