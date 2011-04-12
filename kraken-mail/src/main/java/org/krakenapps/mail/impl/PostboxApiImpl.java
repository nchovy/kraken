package org.krakenapps.mail.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.mail.PostboxApi;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "postbox-api")
@Provides
public class PostboxApiImpl implements PostboxApi {
	private final Logger logger = LoggerFactory.getLogger(PostboxApiImpl.class.getName());

	@Requires
	private PreferencesService prefsvc;

	@Override
	public Map<String, Properties> getConfigs() {
		Map<String, Properties> m = new HashMap<String, Properties>();

		try {
			Preferences root = getPreferences();
			for (String name : root.childrenNames()) {
				m.put(name, getConfig(name));
			}
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage());
		}

		return m;
	}

	@Override
	public Properties getConfig(String name) {
		try {
			Preferences root = getPreferences();
			if (!root.nodeExists(name)) {
				throw new IllegalStateException("imap server not found: " + name);
			}

			Properties props = new Properties();

			Preferences p = root.node(name);
			for (String key : p.keys()) {
				String value = p.get(key, null);
				props.put(key, value);
			}
			return props;
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void register(String name, Properties props) {
		try {
			Preferences root = getPreferences();
			if (root.nodeExists(name))
				throw new IllegalStateException("duplicated imap configuration name found");

			Preferences p = root.node(name);

			for (Object key : props.keySet()) {
				String k = key.toString();
				p.put(k, props.getProperty(k));
			}

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken-mail: cannot add config", e);
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void unregister(String name) {
		try {
			Preferences root = getPreferences();
			if (!root.nodeExists(name))
				throw new IllegalStateException("imap config not found: " + name);

			root.node(name).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken-mail: cannot remove config", e);
			throw new IllegalStateException(e.getMessage());
		}
	}

	private Preferences getPreferences() {
		return prefsvc.getSystemPreferences().node("imap");
	}

	@Override
	public Store connect(String name) {
		Properties props = getConfig(name);
		Store store = null;
		Session session = Session.getInstance(props);

		try {
			String host = props.getProperty("mail.imap.host");
			String user = props.getProperty("mail.imap.user");
			String password = props.getProperty("mail.imap.password");
			store = session.getStore("imaps");
			store.connect(host, user, password);

			return store;
		} catch (NoSuchProviderException e) {
			logger.error("kraken-mail: cannot open imap", e);
		} catch (MessagingException e) {
			logger.error("kraken-mail: cannot open imap", e);
		}

		return null;
	}

}
