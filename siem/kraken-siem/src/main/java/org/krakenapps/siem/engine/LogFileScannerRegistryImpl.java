/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.siem.LogFileScanner;
import org.krakenapps.siem.LogFileScannerRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-logfile-scanner-registry")
@Provides
public class LogFileScannerRegistryImpl extends ServiceTracker implements LogFileScannerRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogFileScannerRegistryImpl.class.getName());

	private ConcurrentMap<String, LogFileScanner> scannerMap;

	public LogFileScannerRegistryImpl(BundleContext bc) {
		super(bc, LogFileScanner.class.getName(), null);
	}

	@Validate
	public void start() {
		scannerMap = new ConcurrentHashMap<String, LogFileScanner>();
		super.open();
	}

	@Invalidate
	public void stop() {
		super.close();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		LogFileScanner scanner = (LogFileScanner) super.addingService(reference);
		scannerMap.put(scanner.getName(), scanner);

		logger.info("kraken-siem: adding log file scanner [{}]", scanner.getName());
		return scanner;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LogFileScanner scanner = (LogFileScanner) service;
		scannerMap.remove(scanner.getName());

		logger.info("kraken-siem: removed log file scanner [{}]", scanner.getName());
		super.removedService(reference, service);
	}

	@Override
	public Collection<LogFileScanner> getScanners() {
		return Collections.unmodifiableCollection(scannerMap.values());
	}

	@Override
	public LogFileScanner getScanner(String name) {
		return scannerMap.get(name);
	}
}
