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
package org.krakenapps.webconsole.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.webconsole.Program;
import org.krakenapps.webconsole.ProgramApi;

@Component(name = "webconsole-program-api")
@Provides
public class ProgramApiImpl implements ProgramApi {
	private ConcurrentMap<ProgramKey, Program> programs;

	public ProgramApiImpl() {
		init();
	}

	@Validate
	public void start() {
		init();
	}

	private void init() {
		programs = new ConcurrentHashMap<ProgramKey, Program>();
	}

	@Override
	public Collection<Program> getPrograms() {
		return programs.values();
	}

	@Override
	public String getLabel(String packageId, String programId, Locale locale) {
		ProgramKey key = new ProgramKey(packageId, programId);
		Program program = programs.get(key);
		if (program == null)
			return null;

		return program.getLabels().get(locale);
	}

	@Override
	public void register(long bundleId, String packageId, String programId, String path) {
		Program program = new Program(bundleId, packageId, programId, path);
		programs.putIfAbsent(new ProgramKey(packageId, programId), program);
	}

	@Override
	public void unregister(long bundleId) {
		List<ProgramKey> targets = new ArrayList<ProgramKey>();

		for (ProgramKey key : programs.keySet()) {
			Program program = programs.get(key);
			if (program.getBundleId() == bundleId)
				targets.add(key);
		}

		for (ProgramKey key : targets)
			programs.remove(key);
	}

	@Override
	public void localize(long bundleId, String packageId, String programId, Locale locale, String label) {
		Program program = programs.get(new ProgramKey(packageId, programId));
		if (program == null)
			return;

		program.getLabels().put(locale, label);
	}

	private static class ProgramKey {
		private String packageId;
		private String programId;

		public ProgramKey(String packageId, String programId) {
			this.packageId = packageId;
			this.programId = programId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((packageId == null) ? 0 : packageId.hashCode());
			result = prime * result + ((programId == null) ? 0 : programId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProgramKey other = (ProgramKey) obj;
			if (packageId == null) {
				if (other.packageId != null)
					return false;
			} else if (!packageId.equals(other.packageId))
				return false;
			if (programId == null) {
				if (other.programId != null)
					return false;
			} else if (!programId.equals(other.programId))
				return false;
			return true;
		}
	}

}
