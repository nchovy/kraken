package org.krakenapps.servlet.csv;

import java.util.Map;
import java.util.Set;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

public class CsvHttpScript implements Script {
	private ScriptContext context;
	private CsvHttpServiceApi api;

	public CsvHttpScript(CsvHttpServiceApi api) {
		this.api = api;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void register(String[] args) {
		String servletId = args[0];
		String pathSpec = args[1];

		try {
			api.registerServlet(servletId, pathSpec);
			context.printf("csv servlet registered: %s\n", servletId);
		} catch (Exception e) {
			context.println("failed to register servlet: " + e.toString());
		}
	}

	public void filters(String[] args) {
		Map<String, Set<String>> filters = api.getAvailableFilters();
		context.println("Available CSV Methods");
		context.println("===========================");

		for (String filterId : filters.keySet()) {
			context.println(filterId + ":");

			Set<String> csvMethods = filters.get(filterId);
			if (csvMethods == null)
				continue;

			for (String csvMethod : csvMethods) {
				context.println("\t" + csvMethod);
			}
		}
	}

}
