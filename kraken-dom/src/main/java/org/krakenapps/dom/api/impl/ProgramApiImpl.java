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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-program-api")
@Provides
@JpaConfig(factory = "dom")
public class ProgramApiImpl extends AbstractApi<ProgramProfile> implements ProgramApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;
	@Requires
	private AdminApi adminApi;

	@Transactional
	@Override
	public ProgramProfile getProgramProfile(int organizationId, int profileId) {
		EntityManager em = entityManagerService.getEntityManager();

		ProgramProfile profile = em.find(ProgramProfile.class, profileId);
		if (profile == null || profile.getOrganization().getId() != organizationId)
			return null;

		return profile;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<ProgramProfile> getAvailableProgramProfiles(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM ProgramProfile p WHERE p.organization.id = ?").setParameter(1, organizationId)
				.getResultList();
	}

	@Transactional
	@Override
	public List<ProgramPack> getAvailableProgramPacks(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		Organization organization = em.find(Organization.class, organizationId);
		organization.getProgramPacks().size(); // enforce fetch
		return new ArrayList<ProgramPack>(organization.getProgramPacks());
	}

	@Transactional
	@Override
	public List<Program> getAvailablePrograms(int organizationId, int adminId) throws AdminNotFoundException {
		Admin admin = adminApi.getAdmin(organizationId, adminId);
		if (admin == null)
			throw new AdminNotFoundException(adminId);

		Set<Program> programs = admin.getProgramProfile().getPrograms();
		programs.size(); // enforce fetch
		return new ArrayList<Program>(programs);
	}

	@Transactional
	@Override
	public Program getProgram(int programId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(Program.class, programId);
	}

	@Override
	public ProgramProfile createProgramProfile(ProgramProfile profile) {
		ProgramProfile pp = createProgramProfileInternal(profile);
		fireEntityAdded(pp);
		return pp;
	}

	@Transactional
	private ProgramProfile createProgramProfileInternal(ProgramProfile profile) {
		EntityManager em = entityManagerService.getEntityManager();
		em.persist(profile);
		return profile;
	}

	@Override
	public ProgramProfile updateProgramProfile(ProgramProfile profile) {
		ProgramProfile pp = updateProgramProfileInternal(profile);
		fireEntityUpdated(pp);
		return pp;
	}

	@Transactional
	private ProgramProfile updateProgramProfileInternal(ProgramProfile profile) {
		EntityManager em = entityManagerService.getEntityManager();
		if (profile.getId() == 0)
			throw new IllegalArgumentException("check program profile id");

		ProgramProfile pp = em.find(ProgramProfile.class, profile.getId());
		pp.setName(profile.getName());
		pp.setDescription(profile.getDescription());
		em.merge(pp);
		return pp;
	}

	@Override
	public ProgramProfile removeProgramProfile(int programProfileId) {
		ProgramProfile profile = removeProgramProfileInternal(programProfileId);
		fireEntityRemoved(profile);
		return profile;
	}

	@Transactional
	private ProgramProfile removeProgramProfileInternal(int programProfileId) {
		EntityManager em = entityManagerService.getEntityManager();
		ProgramProfile profile = em.find(ProgramProfile.class, programProfileId);
		em.remove(profile);
		return profile;
	}

}
