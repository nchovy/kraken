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

	void createProgramProfile(String domain, ProgramProfile profile);

	void updateProgramProfile(String domain, ProgramProfile profile);

	void removeProgramProfile(String domain, String name);

	Collection<ProgramPack> getProgramPacks(String domain);

	ProgramPack findProgramPack(String domain, String name);

	ProgramPack getProgramPack(String domain, String name);

	void createProgramPack(String domain, ProgramPack pack);

	void updateProgramPack(String domain, ProgramPack pack);

	void removeProgramPack(String domain, String name);

	Collection<Program> getPrograms(String domain);

	Collection<Program> getPrograms(String domain, String packName);

	Program findProgram(String domain, String packName, String name);

	Program getProgram(String domain, String packName, String name);

	void createProgram(String domain, Program program);

	void updateProgram(String domain, Program program);

	void removeProgram(String domain, String packName, String name);
}
