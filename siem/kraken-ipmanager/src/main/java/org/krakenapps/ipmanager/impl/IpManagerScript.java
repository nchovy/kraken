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
package org.krakenapps.ipmanager.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.krakenapps.api.DefaultScript;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ipmanager.ArpScanner;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.ipmanager.IpQueryCondition;
import org.krakenapps.ipmanager.LogQueryCondition;
import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.ipmanager.model.AllowedMac;
import org.krakenapps.ipmanager.model.AuditLog;
import org.krakenapps.ipmanager.model.DeniedMac;
import org.krakenapps.ipmanager.model.DetectedMac;
import org.krakenapps.ipmanager.model.HostEntry;
import org.krakenapps.ipmanager.model.HostNic;
import org.krakenapps.ipmanager.model.IpEntry;
import org.krakenapps.ipmanager.model.IpEventLog;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.lookup.mac.MacLookupService;
import org.krakenapps.lookup.mac.Vendor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpManagerScript extends DefaultScript {
	private final String JPA_FACTORY_NAME = "ipm";

	private Logger logger = LoggerFactory.getLogger(IpManagerScript.class);
	private BundleContext bc;
	private JpaService jpa;
	private IpManager ipManager;
	private ArpScanner arpScanner;
	private MacLookupService macLookup;

	public IpManagerScript(BundleContext bc, JpaService jpa, MacLookupService macLookup) {
		this.bc = bc;
		this.jpa = jpa;
		this.macLookup = macLookup;
		loadService();
	}

	public void load(String[] args) {
		try {
			String host = readLine("Database Host", "localhost", false);
			String databaseName = readLine("Database Name", "kraken", false);
			String user = readLine("Database User", "kraken", false);
			String password = readLine("Database Password", null, true);

			Properties props = new Properties();
			props.put("hibernate.connection.url", "jdbc:mysql://" + host + "/" + databaseName
					+ "??useUnicode=true&amp;characterEncoding=utf8");
			props.put("hibernate.connection.username", user);
			props.put("hibernate.connection.password", password);

			jpa.registerEntityManagerFactory(JPA_FACTORY_NAME, props, bc.getBundle().getBundleId());
			loadService();
			context.println("ipm loaded");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("cannot load jpa model", e.getMessage());
		}
	}

	public void install(String[] args) {
		EntityManagerFactory emf = jpa.getEntityManagerFactory(JPA_FACTORY_NAME);
		if (emf == null) {
			context.println("run load first");
			return;
		}

		EntityManager em = emf.createEntityManager();
		if (em.createQuery("FROM Agent").getResultList().size() == 0) {
			Agent agent = new Agent();
			agent.setOrgId(1);
			agent.setAreaId(1);
			agent.setName("local");
			agent.setGuid("local");
			try {
				agent.setIp(InetAddress.getLocalHost().getHostAddress());
				agent.setNetmask("");
			} catch (UnknownHostException e) {
			}
			agent.setPreventNewIp(true);
			agent.setPreventNewMac(true);
			agent.setProtectMode(true);
			agent.setProtectAll(true);
			agent.setCreateDateTime(new Date());
			em.getTransaction().begin();
			em.persist(agent);
			em.getTransaction().commit();
		}
		context.println("default agent is intalled");
	}

	public void unload(String[] args) {
		jpa.unregisterEntityManagerFactory(JPA_FACTORY_NAME);
		context.println("ipm unloaded");
	}

	private String readLine(String label, String def, boolean isPassword) throws InterruptedException {
		context.print(label);
		if (def != null)
			context.print("(default: " + def + ")");

		context.print("? ");
		String line = null;
		if (isPassword)
			line = context.readPassword();
		else
			line = context.readLine();

		if (line != null && line.isEmpty())
			return def;

		return line;
	}

	private void loadService() {
		this.ipManager = getService(IpManager.class);
		this.arpScanner = getService(ArpScanner.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> cls) {
		ServiceReference ref = bc.getServiceReference(cls.getName());
		if (ref == null)
			return null;

		return (T) bc.getService(ref);
	}

	@ScriptUsage(description = "list all agents", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void agents(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		int orgId = Integer.valueOf(args[0]);
		List<Agent> agents = ipManager.getAgents(orgId);

		context.println("Agents");
		context.println("--------");

		for (Agent agent : agents)
			context.println(agent);
	}

	@ScriptUsage(description = "list all hosts", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void hosts(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		int orgId = Integer.valueOf(args[0]);
		List<HostEntry> hosts = ipManager.getHosts(orgId);

		context.println("Hosts");
		context.println("-------");

		for (HostEntry host : hosts)
			context.println(host);
	}

	@ScriptUsage(description = "list all ip entries", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void iplist(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		int orgId = Integer.valueOf(args[0]);
		List<IpEntry> ipEntries = ipManager.getIpEntries(new IpQueryCondition(orgId));
		Collections.sort(ipEntries, new Comparator<IpEntry>() {
			@Override
			public int compare(IpEntry o1, IpEntry o2) {
				String[] t1 = o1.getIp().split("\\.");
				String[] t2 = o2.getIp().split("\\.");
				for (int i = 0; i < 4; i++) {
					if (!t1[i].equals(t2[i]))
						return (Integer.parseInt(t1[i]) - Integer.parseInt(t2[i]));
				}
				return 0;
			}
		});

		context.println("IP Entries");
		context.println("------------");

		for (IpEntry ip : ipEntries) {
			Vendor vendor = macLookup.findByMac(ip.getCurrentMac());
			String line = ip.toString();
			if (vendor != null)
				line += ", vendor=" + vendor.getName();

			context.println(line);
		}
	}

	@ScriptUsage(description = "get/set arp timeout", arguments = { @ScriptArgument(type = "int", name = "timeout", description = "arp timeout", optional = true) })
	public void arptimeout(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		if (args.length == 0) {
			int timeout = arpScanner.getTimeout();
			context.println(timeout + "msec");
		} else if (args.length == 1) {
			int timeout = Integer.valueOf(args[0]);
			arpScanner.setTimeout(timeout);
			context.println("set");
		}
	}

	public void arpscan(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		arpScanner.run();
		context.println("arp scan completed");
	}

	@ScriptUsage(description = "list all logs", arguments = {
			@ScriptArgument(type = "int", name = "org id", description = "organization id "),
			@ScriptArgument(type = "int", name = "page", description = "page number"),
			@ScriptArgument(type = "int", name = "page size", description = "page size") })
	public void logs(String[] args) {
		if (ipManager == null) {
			context.println("run load first");
			return;
		}

		int orgId = Integer.valueOf(args[0]);
		int page = Integer.valueOf(args[1]);
		int pageSize = Integer.valueOf(args[2]);

		LogQueryCondition condition = new LogQueryCondition(orgId, page, pageSize);
		List<IpEventLog> logs = ipManager.getLogs(condition);

		context.println("IP Event Logs");
		context.println("--------------");

		for (IpEventLog log : logs)
			context.println(log);
	}

	@SuppressWarnings("unchecked")
	public void purge(String[] args) {
		context.print("really? ");
		try {
			String ans = context.readLine();
			if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
				EntityManagerFactory emf = jpa.getEntityManagerFactory(JPA_FACTORY_NAME);
				if (emf == null) {
					context.println("run load first");
					return;
				}

				EntityManager em = emf.createEntityManager();
				em.getTransaction().begin();
				List<AllowedMac> allowedMacs = em.createQuery("FROM AllowedMac").getResultList();
				for (AllowedMac am : allowedMacs)
					em.remove(am);
				List<AuditLog> auditLogs = em.createQuery("FROM AuditLog").getResultList();
				for (AuditLog al : auditLogs)
					em.remove(al);
				List<DeniedMac> deniedMacs = em.createQuery("FROM DeniedMac").getResultList();
				for (DeniedMac dm : deniedMacs)
					em.remove(dm);
				List<DetectedMac> detectedMacs = em.createQuery("FROM DetectedMac").getResultList();
				for (DetectedMac dm : detectedMacs)
					em.remove(dm);
				List<HostEntry> hostEntries = em.createQuery("FROM HostEntry").getResultList();
				for (HostEntry he : hostEntries)
					em.remove(he);
				List<HostNic> hostNics = em.createQuery("FROM HostNic").getResultList();
				for (HostNic hn : hostNics)
					em.remove(hn);
				// List<IpAllocationRequest> ipAllocationRequests =
				// em.createQuery("FROM IpAllocationRequest")
				// .getResultList();
				// for (IpAllocationRequest iar : ipAllocationRequests)
				// em.remove(iar);
				List<IpEntry> ipEntries = em.createQuery("FROM IpEntry").getResultList();
				for (IpEntry ie : ipEntries)
					em.remove(ie);
				List<IpEventLog> ipEventLogs = em.createQuery("FROM IpEventLog").getResultList();
				for (IpEventLog iel : ipEventLogs)
					em.remove(iel);
				em.getTransaction().commit();
				return;
			}
		} catch (InterruptedException e) {
		}
		context.println("cancel");
	}
}
