package org.krakenapps.siem.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.mail.MailerRegistry;
import org.krakenapps.siem.response.AbstractResponseActionManager;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseConfigOption;
import org.krakenapps.siem.response.StringConfigOption;

@Component(name = "siem-mail-response")
@Provides
public class MailResponseActionManager extends AbstractResponseActionManager {
	@Requires
	private MailerRegistry registry;

	@Override
	public String getName() {
		return "mail";
	}

	@Override
	protected ResponseAction createResponseAction(String namespace, String name, String description, Properties config) {
		return new MailResponseAction(this, registry, namespace, name, description, config);
	}

	@Override
	public Collection<ResponseConfigOption> getConfigOptions() {
		List<ResponseConfigOption> list = new ArrayList<ResponseConfigOption>();
		list.add(new StringConfigOption("mailer_name", "Mailer Name", "Mailer Name"));
		list.add(new StringConfigOption("from", "From Address", "From Address"));
		list.add(new StringConfigOption("to", "To Address", "To Address"));
		list.add(new StringConfigOption("subject_prefix", "Subject Prefix", "Subject Prefix"));
		return list;
	}

}
