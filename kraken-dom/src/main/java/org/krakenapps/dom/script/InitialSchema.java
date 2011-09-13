package org.krakenapps.dom.script;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.krakenapps.dom.api.impl.Sha1;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.GlobalParameter;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.dom.model.Permission;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.Role;
import org.krakenapps.dom.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialSchema {
	private static final String SCHEMA_VERSION = "1";

	public static void generate(EntityManager em) {
		final Logger logger = LoggerFactory.getLogger(InitialSchema.class.getName());

		// TODO: check schema version, add vendor & applications
		try {
			em.getTransaction().begin();

			GlobalParameter gp = null;
			try {
				gp = (GlobalParameter) em.createQuery("FROM GlobalParameter g WHERE g.name = ?")
						.setParameter(1, "initial_schema_version").getSingleResult();
			} catch (NoResultException e) {
			}

			if (gp == null || !gp.getValue().equals(SCHEMA_VERSION)) {
				Organization org = createOrganization(em);
				createArea(em, org);
				ProgramPack systemPack = createSystemPack(em, org);
				ProgramProfile pp = createProgramProfile(em, org);
				Role masterRole = createRoles(em);
				createSuperAdmin(em, org, masterRole, pp);
				createPrograms(em, systemPack, pp);

				if (gp == null) {
					gp = new GlobalParameter();
					gp.setName("initial_schema_version");
					gp.setValue(SCHEMA_VERSION);
					gp.setHidden(true);
					em.persist(gp);
				} else {
					gp.setValue(SCHEMA_VERSION);
					em.merge(gp);
				}
			}
		} catch (Exception e) {
			logger.error("kraken dom: schema init failed", e);
			throw new RuntimeException("create schema failed", e);
		} finally {
			em.getTransaction().commit();
		}

	}

	public static Organization createOrganization(EntityManager em) {
		try {
			return (Organization) em.createQuery("FROM Organization o WHERE o.name = ?").setParameter(1, "krakenapps")
					.getSingleResult();
		} catch (NoResultException e) {
		}

		Organization org = new Organization();
		org.setName("krakenapps");
		org.setCreateDateTime(new Date());
		org.setEnabled(true);
		em.persist(org);
		return org;
	}

	public static void createArea(EntityManager em, Organization org) {
		// skip area generation if exists
		try {
			em.createQuery("FROM Area a WHERE a.name = ?").setParameter(1, "/").getSingleResult();
			return;
		} catch (NoResultException e) {
		}

		Area area = new Area();
		area.setOrganization(org);
		area.setName("/");
		area.setCreateDateTime(new Date());
		em.persist(area);
	}

	public static ProgramPack createSystemPack(EntityManager em, Organization org) {
		try {
			return (ProgramPack) em.createQuery("FROM ProgramPack p WHERE p.name = ?").setParameter(1, "System")
					.getSingleResult();
		} catch (NoResultException e) {
		}

		ProgramPack pack = new ProgramPack();
		pack.setName("System");
		pack.setDll("Nchovy.WatchCat.Plugins.Core.dll");
		pack.setSeq(1);
		em.persist(pack);

		org.getProgramPacks().add(pack);
		em.merge(org);

		return pack;
	}

	public static void createPrograms(EntityManager em, ProgramPack pack, ProgramProfile pp) {
		createProgram(em, pack, pp, "Account Manager", "Nchovy.WatchCat.Plugins.Core.AccountManager.AccountPlugin", 1);
		createProgram(em, pack, pp, "Task Manager", "Nchovy.WatchCat.Plugins.Core.TaskManager.TaskManager", 2);
		createProgram(em, pack, pp, "Run", "Nchovy.WatchCat.Plugins.Core.Run.Run", 3);
		createProgram(em, pack, pp, "Developer Console", "Nchovy.WatchCat.Plugins.Core.MessagePrompt.MessagePrompt", 4);
	}

	public static void createProgram(EntityManager em, ProgramPack pack, ProgramProfile pp, String name, String type,
			int seq) {
		try {
			// skip if exists
			em.createQuery("FROM Program p WHERE p.typeName = ?").setParameter(1, type).getSingleResult();
			return;
		} catch (NoResultException e) {
		}

		Program p = new Program();
		p.setName(name);
		p.setPack(pack);
		p.getProgramProfiles().add(pp);
		p.setSeq(seq);
		p.setTypeName(type);
		p.setVisible(true);
		em.persist(p);

		pp.getPrograms().add(p);
		em.merge(p);
	}

	public static Role createRoles(EntityManager em) {
		Role master = createRole(em, "master", 4);
		Permission permission = new Permission();
		permission.setPermissionGroup("dom.org");
		permission.setPermission("manage");
		em.persist(permission);
		master.getPermissions().add(permission);
		em.merge(master);
		createRole(em, "admin", 3);
		createRole(em, "member", 2);
		createRole(em, "guest", 1);
		return master;
	}

	public static Role createRole(EntityManager em, String name, int level) {
		try {
			return (Role) em.createQuery("FROM Role r WHERE r.name = ?").setParameter(1, name).getSingleResult();
		} catch (NoResultException e) {
		}

		Role role = new Role();
		role.setName(name);
		role.setLevel(level);
		em.persist(role);
		return role;
	}

	public static Admin createSuperAdmin(EntityManager em, Organization org, Role role, ProgramProfile pp) {
		User user = null;

		try {
			user = (User) em.createQuery("FROM User u WHERE u.loginName = ?").setParameter(1, "admin")
					.getSingleResult();
		} catch (NoResultException e) {
			user = new User();
			user.setOrganization(org);
			user.setLoginName("admin");
			user.setName("xeraph");
			user.setPassword(Sha1.hashPassword(null, "kraken"));
			user.setCreateDateTime(new Date());
			user.setUpdateDateTime(user.getCreateDateTime());
			em.persist(user);
		}

		Admin admin = null;
		try {
			admin = (Admin) em.createQuery("FROM Admin a WHERE a.user.id = ?").setParameter(1, user.getId())
					.getSingleResult();
		} catch (NoResultException e) {
			admin = new Admin();
			admin.setRole(role);
			admin.setProgramProfile(pp);
			admin.setLang("en");
			admin.setEnabled(true);
			admin.setCreateDateTime(new Date());
			admin.setUser(user);
			em.persist(admin);
		}

		return admin;
	}

	public static ProgramProfile createProgramProfile(EntityManager em, Organization org) {
		try {
			return (ProgramProfile) em.createQuery("FROM ProgramProfile p WHERE p.name = ?").setParameter(1, "all")
					.getSingleResult();
		} catch (NoResultException e) {
		}

		ProgramProfile pp = new ProgramProfile();
		pp.setName("all");
		pp.setOrganization(org);
		em.persist(pp);

		try {
			em.createQuery("FROM OrganizationParameter p WHERE p.name = ?")
					.setParameter(1, "default_program_profile_id").getSingleResult();
		} catch (NoResultException e) {
			OrganizationParameter op = new OrganizationParameter();
			op.setOrganization(org);
			op.setName("default_program_profile_id");
			op.setValue(String.valueOf(pp.getId()));
			em.persist(op);
		}

		return pp;
	}
}
