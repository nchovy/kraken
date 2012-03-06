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
package org.krakenapps.jpa.impl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.krakenapps.jpa.JpaProfile;
import org.krakenapps.jpa.JpaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping for mysql connection refresh job. You will see
 * "Last packet sent to the server was N ms ago." caused by idle timeout.
 * 
 * @author xeraph
 * 
 */
public class ConnectionKeeper implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(ConnectionKeeper.class.getName());
	private JpaService jpa;
	private Thread t;
	private boolean doStop;

	public void start() {
		t = new Thread(this, "JPA Connection Keeper");
		t.start();
	}

	public void stop() {
		doStop = true;
		t.interrupt();
	}

	@Override
	public void run() {
		logger.info("kraken jpa: connection keeper started");

		while (!doStop) {
			checkProfiles();
		}

		logger.info("kraken jpa: connection keeper stopped");
	}

	private void checkProfiles() {
		try {
			for (String profileName : jpa.getProfileNames()) {
				JpaProfile profile = jpa.getProfile(profileName);
				String connUrl = profile.getProperties().getProperty("hibernate.connection.url");

				// ping mysql connections
				if (connUrl.startsWith("jdbc:mysql")) {
					pingMysql(profile);
				}
			}

			// sleep 1min
			Thread.sleep(60 * 1000);
		} catch (Exception e) {
			logger.error("kraken jpa: cannot ping", e);
		}
	}

	private void pingMysql(JpaProfile profile) {
		EntityManager em = null;
		try {
			EntityManagerFactory factory = jpa.getEntityManagerFactory(profile.getName());
			em = factory.createEntityManager();
			em.createNativeQuery("select 1").getSingleResult();
		} finally {
			if (em != null && em.isOpen())
				em.close();
		}
	}
}
