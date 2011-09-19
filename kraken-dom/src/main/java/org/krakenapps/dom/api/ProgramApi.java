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

import java.util.List;

import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;

public interface ProgramApi {
	ProgramProfile getProgramProfile(int organizationId, int profileId);

	List<ProgramProfile> getAvailableProgramProfiles(int organizationId);

	List<ProgramPack> getAvailableProgramPacks(int organizationId);

	List<Program> getAvailablePrograms(int organizationId, int adminId) throws AdminNotFoundException;

	ProgramProfile createProgramProfile(ProgramProfile profile);

	ProgramProfile updateProgramProfile(ProgramProfile profile);

	ProgramProfile removeProgramProfile(int programProfileId);

	Program getProgram(int programId);
}
