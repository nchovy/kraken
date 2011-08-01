/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.fluxmon.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.fluxmon.FluxDatabase;
import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxHost;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "flux-database")
@Provides
public class FluxDatabaseImpl implements FluxDatabase {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
	private Logger logger = LoggerFactory.getLogger(FluxDatabaseImpl.class.getName());
	private Preferences prefs;

	public FluxDatabaseImpl(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		prefs = prefsService.getSystemPreferences();
	}

	@Override
	public Collection<String> loadTrackingDomains() {
		try {
			List<String> domains = new LinkedList<String>();
			Preferences root = prefs.node("tracking");
			for (String domain : root.childrenNames()) {
				domains.add(domain);
			}
			return domains;
		} catch (BackingStoreException e) {
			logger.warn("flux database: failed to list tracking domains", e);
			return new ArrayList<String>();
		}
	}

	@Override
	public void addTrackingDomain(String domain) {
		try {
			Preferences root = prefs.node("tracking");
			if (root.nodeExists(domain))
				throw new IllegalStateException("already added: " + domain);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Preferences node = root.node(domain);
			node.put("created", dateFormat.format(new Date()));

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("flux database: add tracking failed", e);
		}
	}

	@Override
	public void removeTrackingDomain(String domain) {
		try {
			Preferences root = prefs.node("tracking");
			if (!root.nodeExists(domain))
				throw new IllegalStateException("domain not found: " + domain);

			root.node(domain).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("flux database: remove tracking failed", e);
		}
	}

	@Override
	public Collection<FluxDomain> loadFluxDomains() {
		List<FluxDomain> fluxDomains = new LinkedList<FluxDomain>();
		File base = new File(System.getProperty("kraken.data.dir"), "kraken-fluxmon/");
		base.mkdirs();

		for (File f : base.listFiles()) {
			FluxDomain fluxDomain = readFluxDomain(f.getName(), f);
			fluxDomains.add(fluxDomain);
		}

		return fluxDomains;
	}

	private FluxDomain readFluxDomain(String domain, File f) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line = null;
			String header = br.readLine();
			String[] headerTokens = header.split(",");
			Map<InetAddress, FluxHost> hosts = new HashMap<InetAddress, FluxHost>();

			do {
				line = br.readLine();
				if (line == null)
					break;

				String[] tokens = line.split(",");

				InetAddress address = InetAddress.getByName(tokens[0]);
				Date hostCreated = dateFormat.parse(tokens[1]);
				Date hostUpdated = dateFormat.parse(tokens[2]);
				FluxHost host = new FluxHostImpl(address, hostCreated, hostUpdated);

				hosts.put(address, host);
			} while (line != null);

			Date domainCreated = dateFormat.parse(headerTokens[1]);
			Date domainUpdated = dateFormat.parse(headerTokens[2]);
			return new FluxDomainImpl(domain, domainCreated, domainUpdated, hosts);
		} catch (FileNotFoundException e) {
			logger.warn("flux database: file not found", e);
		} catch (IOException e) {
			logger.warn("flux database: host read error", e);
		} catch (ParseException e) {
			logger.warn("flux database: header parse error", e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}

		return null;
	}

	@Override
	public void updateFluxDomain(FluxDomain domain) {
		File base = new File(System.getProperty("kraken.data.dir"), "kraken-fluxmon/");
		base.mkdirs();

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		BufferedWriter bw = null;
		try {
			File f = new File(base, domain.getName());
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));

			bw.append(domain.getName());
			bw.append(',');
			bw.append(dateFormat.format(domain.getCreateDateTime()));
			bw.append(',');
			bw.append(dateFormat.format(domain.getUpdateDateTime()));
			bw.newLine();

			for (FluxHost host : domain.getHosts()) {
				bw.append(host.getAddress().getHostAddress());
				bw.append(',');
				bw.append(dateFormat.format(host.getCreateDateTime()));
				bw.append(',');
				bw.append(dateFormat.format(host.getUpdateDateTime()));
				bw.newLine();
			}
		} catch (FileNotFoundException e) {
			logger.warn("flux database: domain file not found", e);
		} catch (IOException e) {
			logger.warn("flux database: update domain failed", e);
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
				}
		}
	}

	@Override
	public void purgeFluxDomain(FluxDomain domain) {
		File base = new File(System.getProperty("kraken.data.dir"), "kraken-fluxmon/");
		base.mkdirs();
	}

	@Override
	public void flush() {
	}

}
