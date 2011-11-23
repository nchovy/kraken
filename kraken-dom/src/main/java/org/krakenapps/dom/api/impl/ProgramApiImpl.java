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
package org.krakenapps.dom.api.impl;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;

@Component(name = "dom-program-api")
@Provides
public class ProgramApiImpl extends DefaultEntityEventProvider<Program> implements ProgramApi {
	private static final Class<ProgramProfile> prof = ProgramProfile.class;
	private static final String PROF_NOT_FOUND = "program-profile-not-found";
	private static final String PROF_ALREADY_EXIST = "program-profile-already-exist";
	private static DefaultEntityEventProvider<ProgramProfile> profileEventProvider = new DefaultEntityEventProvider<ProgramProfile>();

	private static final Class<ProgramPack> pack = ProgramPack.class;
	private static final String PACK_NOT_FOUND = "program-pack-not-found";
	private static final String PACK_ALREADY_EXIST = "program-pack-already-exist";
	private static DefaultEntityEventProvider<ProgramPack> packEventProvider = new DefaultEntityEventProvider<ProgramPack>();

	private static final Class<Program> prog = Program.class;
	private static final String PROG_NOT_FOUND = "program-not-found";
	private static final String PROG_ALREADY_EXIST = "program-already-exist";

	@Requires
	private ConfigManager cfg;

	private Predicate getPred(String name) {
		return Predicates.field("name", name);
	}

	@Override
	public Collection<ProgramProfile> getProgramProfiles(String domain) {
		return cfg.all(domain, prof);
	}

	@Override
	public ProgramProfile findProgramProfile(String domain, String name) {
		return cfg.find(domain, prof, getPred(name));
	}

	@Override
	public ProgramProfile getProgramProfile(String domain, String name) {
		return cfg.get(domain, prof, getPred(name), PROF_NOT_FOUND);
	}

	@Override
	public void createProgramProfile(String domain, ProgramProfile profile) {
		cfg.add(domain, prof, getPred(profile.getName()), profile, PROF_ALREADY_EXIST, profileEventProvider);
	}

	@Override
	public void updateProgramProfile(String domain, ProgramProfile profile) {
		cfg.update(domain, prof, getPred(profile.getName()), profile, PROF_NOT_FOUND, profileEventProvider);
	}

	@Override
	public void removeProgramProfile(String domain, String name) {
		cfg.remove(domain, prof, getPred(name), PROF_NOT_FOUND, profileEventProvider);
	}

	@Override
	public Collection<ProgramPack> getProgramPacks(String domain) {
		return cfg.all(domain, pack);
	}

	@Override
	public ProgramPack findProgramPack(String domain, String name) {
		return cfg.find(domain, pack, getPred(name));
	}

	@Override
	public ProgramPack getProgramPack(String domain, String name) {
		return cfg.get(domain, pack, getPred(name), PACK_NOT_FOUND);
	}

	@Override
	public void createProgramPack(String domain, ProgramPack pack) {
		cfg.add(domain, ProgramApiImpl.pack, getPred(pack.getName()), pack, PACK_ALREADY_EXIST, packEventProvider);
	}

	@Override
	public void updateProgramPack(String domain, ProgramPack pack) {
		cfg.update(domain, ProgramApiImpl.pack, getPred(pack.getName()), pack, PACK_NOT_FOUND, packEventProvider);
	}

	@Override
	public void removeProgramPack(String domain, String name) {
		cfg.remove(domain, ProgramApiImpl.pack, getPred(name), PACK_NOT_FOUND, packEventProvider);
	}

	@Override
	public Collection<Program> getPrograms(String domain) {
		return cfg.all(domain, prog);
	}

	@Override
	public Program findProgram(String domain, String name) {
		return cfg.find(domain, prog, getPred(name));
	}

	@Override
	public Program getProgram(String domain, String name) {
		return cfg.get(domain, prog, getPred(name), PROG_NOT_FOUND);
	}

	@Override
	public void createProgram(String domain, Program program) {
		cfg.add(domain, prog, getPred(program.getName()), program, PROG_ALREADY_EXIST, this);
	}

	@Override
	public void updateProgram(String domain, Program program) {
		cfg.update(domain, prog, getPred(program.getName()), program, PROG_NOT_FOUND, this);
	}

	@Override
	public void removeProgram(String domain, String name) {
		cfg.remove(domain, prog, getPred(name), PROG_NOT_FOUND, this);
	}
}
