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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class PackageDescWriter {
	public BundleContext bc;

	public PackageDescWriter(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// bundle section
		sb.append("[bundle]\n");

		for (Bundle bundle : bc.getBundles()) {
			if (bundle.getBundleId() == 0)
				continue;

			sb.append(bundle.getSymbolicName() + "\t" + bundle.getVersion());
			sb.append("\n");
		}

		// start section
		sb.append("\n[start]\n");

		for (Bundle bundle : bc.getBundles()) {
			if (bundle.getBundleId() == 0)
				continue;

			if (bundle.getState() != Bundle.ACTIVE)
				continue;

			sb.append(bundle.getSymbolicName());
			sb.append("\n");
		}

		// maven section
		sb.append("\n[maven]\n");

		for (Bundle bundle : bc.getBundles()) {
			if (bundle.getBundleId() == 0)
				continue;

			String location = bundle.getLocation();

			// remove root path
			if (!location.contains(getDownloadRoot()))
				continue;

			location = location.replace("file://", "").replace(getDownloadRoot(), "");
			List<String> tokens = new ArrayList<String>(Arrays.asList(location.split("/")));

			// remove jar name
			tokens.remove(tokens.size() - 1);

			String version = tokens.remove(tokens.size() - 1);
			String artifact = tokens.remove(tokens.size() - 1);

			int i = 0;
			String groupId = "";

			for (String t : tokens) {
				if (t.isEmpty())
					continue;

				if (i != 0)
					groupId += ".";

				groupId += t;
				i++;
			}

			sb.append(groupId);
			sb.append("\t");
			sb.append(artifact);
			sb.append("\t");
			sb.append(version);
			sb.append("\n");
		}

		return sb.toString();
	}

	private String getDownloadRoot() {
		String path = new File(System.getProperty("kraken.download.dir")).getAbsolutePath();
		return path.replaceAll("\\\\", "/");
	}
}
