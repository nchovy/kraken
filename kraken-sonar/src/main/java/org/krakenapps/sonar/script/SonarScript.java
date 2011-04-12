package org.krakenapps.sonar.script;

import java.util.Collection;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.pcap.decoder.tcp.TcpSession;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class SonarScript implements Script {
	private BundleContext bc;
	private PassiveScanner scanner;
	private ScriptContext context;
	private JpaService jpa;
	private Metabase metabase;

	public SonarScript(BundleContext bc, PassiveScanner scanner, JpaService jpa, Metabase metabase) {
		this.bc = bc;
		this.scanner = scanner;
		this.jpa = jpa;
		this.metabase = metabase;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void load(String[] args) {
		EntityManagerFactory emf = jpa.getEntityManagerFactory("sonar");
		if (emf == null) {
			Properties props = new Properties();
			try {
				jpa.registerEntityManagerFactory("sonar", props, bc.getBundle().getBundleId());
			} catch (BundleException e) {
				context.println(e.getMessage());
			}
		} else {
			context.println("sonar jpa model is already registered.");
		}
	}

	public void tcpSessions(String[] args) {
		for (String name : scanner.getDeviceNames()) {
			context.println(name);
			context.println("----------------");
			PcapLiveRunner runner = scanner.getDevice(name);

			Collection<TcpSession> sessions = runner.getTcpDecoder().getCurrentSessions();
			for (TcpSession session : sessions) {
				context.println(session.toString());
			}
		}
	}

	@ScriptUsage(description = "reset all kraken-sonar database records.", arguments = { @ScriptArgument(name = "isForced", description = "use 'force' to run this method.", optional = false, type = "force or not") })
	public void resetDatabase(String[] args) {
		if (!args[0].equals("force")) {
			context.printf("Ignored. Use 'force' as argument.\n");
			return;
		}
		if (metabase == null) {
			context.printf("Metabase undefined. Please make sure the sonar jpa model is properly registered.\n");
			return;
		}
		metabase.clearIpEndpoints();
		metabase.clearEnvironments();
		metabase.clearApplications();
		metabase.clearIdsLog();
		metabase.clearVendors();
	}

}
