package org.krakenapps.siem.engine;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.krakenapps.event.api.Event;
import org.krakenapps.mail.MailerRegistry;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseType;

public class MailResponseAction implements ResponseAction {
	private ResponseActionManager manager;
	private String namespace;
	private String name;
	private String description;
	private Properties config;
	private String mailerName;
	private Session session;
	private InternetAddress fromAddr;
	private InternetAddress toAddr;
	private String prefix;

	public MailResponseAction(ResponseActionManager manager, MailerRegistry registry, String namespace, String name,
			String description, Properties config) {
		this.manager = manager;
		this.namespace = namespace;
		this.name = name;
		this.description = description;
		this.config = config;
		this.mailerName = config.getProperty("mailer_name");
		this.session = registry.getSession(mailerName);
		try {
			this.fromAddr = new InternetAddress(config.getProperty("from"));
			this.toAddr = new InternetAddress(config.getProperty("to"));
		} catch (AddressException e) {
			e.printStackTrace();
		}
		this.prefix = config.getProperty("subject_prefix");
	}

	@Override
	public Properties getConfig() {
		return config;
	}

	@Override
	public ResponseActionManager getManager() {
		return manager;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.Alert;
	}

	@Override
	public void handle(Event event) {
		try {
			MimeMessage msg = new MimeMessage(session);
			String source = event.getSourceIp().getHostAddress()
					+ (event.getSourcePort() != null ? (":" + event.getSourcePort()) : "");
			String dest = event.getDestinationIp().getHostAddress()
					+ (event.getDestinationPort() != null ? (":" + event.getDestinationPort()) : "");
			String subject = String.format("[%s] %s (%s -> %s)", prefix, event.getMessageKey(), source, dest);
			StringBuffer content = new StringBuffer();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			content.append("<html><body><ul>");
			append(content, "Event Key (Source, ID)", event.getKey().getSource() + ", " + event.getKey().getId());
			append(content, "First Seen", dateFormat.format(event.getFirstSeen()));
			append(content, "Last Seen", dateFormat.format(event.getLastSeen()));
			append(content, "Category", event.getCategory());
			append(content, "Severity", event.getSeverityString() + " (" + event.getSeverity() + ")");
			append(content, "Host ID", String.valueOf(event.getHostId()));
			append(content, "Source", source);
			append(content, "Destination", dest);
			append(content, "Message", event.getMessageKey());
			append(content, "Count", String.valueOf(event.getCount()));
			content.append("</ul></body></html>");

			msg.setFrom(fromAddr);
			msg.setRecipient(RecipientType.TO, toAddr);
			msg.setSubject(subject);
			msg.setContent(content.toString(), "text/html; charset=utf-8");

			Transport.send(msg);
		} catch (Exception e) {
		}
	}

	private void append(StringBuffer sb, String key, String value) {
		if (value == null || value.equals("null"))
			value = "N/A";
		sb.append(String.format("<li>%s: %s</li>", key, value));
	}

	@Override
	public String toString() {
		return String.format("mail response, mailer [%s], from [%s], to [%s], subject prefix [%s]", mailerName,
				fromAddr, toAddr, prefix);
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("manager", manager.getName());
		m.put("namespace", namespace);
		m.put("name", name);
		m.put("description", description);
		m.put("mailer_name", mailerName);
		m.put("from", fromAddr);
		m.put("to", toAddr);
		m.put("subject_prefix", prefix);
		return m;
	}
}
