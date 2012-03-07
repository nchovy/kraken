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

import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;

public interface ProgramApi extends EntityEventProvider<Program> {
	Collection<ProgramProfile> getProgramProfiles(String domain);

	ProgramProfile findProgramProfile(String domain, String name);

	ProgramProfile getProgramProfile(String domain, String name);

	void createProgramProfiles(String domain, Collection<ProgramProfile> profiles);

	void createProgramProfile(String domain, ProgramProfile profile);

	void updateProgramProfiles(String domain, Collection<ProgramProfile> profiles);

	void updateProgramProfile(String domain, ProgramProfile profile);

	void removeProgramProfiles(String domain, Collection<String> names);

	void removeProgramProfile(String domain, String name);
	
	EntityEventProvider<ProgramProfile> getProgramProfileEventProvider();

	Collection<ProgramPack> getProgramPacks(String domain);

	ProgramPack findProgramPack(String domain, String name);

	ProgramPack getProgramPack(String domain, String name);

	void createProgramPacks(String domain, Collection<ProgramPack> packs);

	void createProgramPack(String domain, ProgramPack pack);

	void updateProgramPacks(String domain, Collection<ProgramPack> packs);

	void updateProgramPack(String domain, ProgramPack pack);

	void removeProgramPacks(String domain, Collection<String> names);

	void removeProgramPack(String domain, String name);
	
	EntityEventProvider<ProgramPack> getProgramPackEventProvider();

	Collection<Program> getPrograms(String domain);

	Collection<Program> getPrograms(String domain, String packName);

	Program findProgram(String domain, String packName, String name);

	Program getProgram(String domain, String packName, String name);

	void createPrograms(String domain, Collection<Program> programs);

	void createProgram(String domain, Program program);

	void updatePrograms(String domain, Collection<Program> programs);

	void updateProgram(String domain, Program program);

	void removePrograms(String domain, Collection<String> names);

	void removeProgram(String domain, String packName, String name);
}
