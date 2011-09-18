package org.krakenapps.dom.script;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.jpa.JpaService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomScript implements Script {
	private static final String FACTORY_NAME = "dom";
	private final Logger logger = LoggerFactory.getLogger(DomScript.class.getName());
	private ScriptContext context;
	private BundleContext bc;
	private JpaService jpa;

	public DomScript(BundleContext bc, JpaService jpa) {
		this.bc = bc;
		this.jpa = jpa;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void load(String[] args) {
		try {
			String host = readLine("Database Host", "localhost");
			String databaseName = readLine("Database Name", "kraken");
			String user = readLine("Database User", "kraken");
			String password = readLine("Database Password", null, true);

			Properties props = new Properties();
			props.put("hibernate.connection.url", "jdbc:mysql://" + host + "/" + databaseName
					+ "?useUnicode=true&characterEncoding=utf8");
			props.put("hibernate.connection.username", user);
			props.put("hibernate.connection.password", password);
			props.put("hibernate.is-connection-validation-required", "true");
			props.put("hibernate.autoReconnect", "true");
			props.put("hibernate.autoReconnectForPools", "true");

			jpa.registerEntityManagerFactory(FACTORY_NAME, props, bc.getBundle().getBundleId());
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("cannot load jpa model", e.getMessage());
		}
	}

	public void install(String[] args) {
		EntityManagerFactory emf = jpa.getEntityManagerFactory(FACTORY_NAME);
		if (emf == null)
			return;

		EntityManager em = emf.createEntityManager();
		InitialSchema.generate(em);
		context.println("default schema is installed");
	}

	private String readLine(String label, String def) throws InterruptedException {
		return readLine(label, def, false);
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
}
