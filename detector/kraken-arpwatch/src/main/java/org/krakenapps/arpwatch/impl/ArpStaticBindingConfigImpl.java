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
package org.krakenapps.arpwatch.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.arpwatch.ArpStaticBinding;
import org.krakenapps.arpwatch.ArpStaticBindingConfig;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

public class ArpStaticBindingConfigImpl implements ArpStaticBindingConfig {
	private Preferences prefs;
	private final ConcurrentHashMap<InetAddress, ArpStaticBinding> bindings;

	public ArpStaticBindingConfigImpl(BundleContext bc) {
		bindings = new ConcurrentHashMap<InetAddress, ArpStaticBinding>();

		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		if (ref == null) {
			System.out.println("arpwatch: prefs service not found");
			return;
		}

		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		prefs = prefsService.getSystemPreferences();
		prefs = prefs.node("/kraken-arpwatch");
		// load all
		try {
			Preferences root = getStaticBindingRoot();
			if (root == null) {
				return;
			}

			if (root.childrenNames() == null)
				return;

			for (String name : root.childrenNames()) {
				try {
					System.out.println("name!!! " + name + " ");
					Preferences p = root.node(name);
					MacAddress mac = new MacAddress(p.get("mac", null));
					InetAddress ip = InetAddress.getByName(name);
					bindings.put(ip, new ArpStaticBindingImpl(mac, ip));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		} catch (BackingStoreException e) {

		}
	}

	@Override
	public ArpStaticBinding find(InetAddress ip) {
		return bindings.get(ip);
	}

	@Override
	public Collection<ArpStaticBinding> getStaticBindings() {
		return Collections.unmodifiableCollection(bindings.values());
	}

	@Override
	public void addStaticBinding(ArpStaticBinding binding) {
		Preferences root = getStaticBindingRoot();
		try {
			ArpStaticBinding old = bindings.putIfAbsent(binding.getIpAddress(), binding);
			String ip = binding.getIpAddress().getHostAddress();

			if (old != null)
				throw new IllegalStateException("duplicated binding: " + ip);

			Preferences p = root.node(ip);
			p.put("mac", binding.getMacAddress().toString());
			p.flush();
			p.sync();

			bindings.put(binding.getIpAddress(), binding);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private Preferences getStaticBindingRoot() {
		return prefs.node("static");
	}

	@Override
	public void removeStaticBinding(InetAddress ip) {
		Preferences root = getStaticBindingRoot();
		try {
			if (!root.nodeExists(ip.getHostAddress()))
				return;

			root.node(ip.getHostAddress()).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
		}

		bindings.remove(ip);
	}

}
