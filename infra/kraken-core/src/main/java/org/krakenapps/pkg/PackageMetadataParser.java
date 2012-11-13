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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.api.PackageMetadata;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.Version;

public class PackageMetadataParser {
	private PackageMetadataParser() {
	}

	public static PackageMetadata parse(String doc) {
		PackageMetadata metadata = new PackageMetadata();

		String[] lines = doc.split("\n");

		String subject = null;
		for (String line : lines) {
			line = line.trim();

			if (line.length() == 0 || line.startsWith(";"))
				continue;

			if (line.startsWith("[")) {
				subject = line;
				continue;
			}

			if (subject.equals("[description]")) {
				metadata.setDescription(line);
			} else if (subject.equals("[version]")) {
				try {
					int pos = line.indexOf(' ');
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Version version = new Version(line.substring(0, pos));
					Date lastUpdated = dateFormat.parse(line.substring(pos + 1).trim());
					PackageVersionHistory versionHistory = new PackageVersionHistory(version, lastUpdated);
					metadata.getVersions().add(versionHistory);
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
			} else if (subject.equals("[maven repository]")) {
				try {
					// normalize url (and do not use URL. see URL.equals()
					// javadoc)
					URL uri = new URL(line);
					metadata.getMavenRepositories().add(uri.toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		return metadata;
	}
}
