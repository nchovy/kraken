package org.krakenapps.datasource.impl;

import java.util.Map.Entry;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.datasource.DataConverter;
import org.krakenapps.datasource.DataConverterRegistry;
import org.krakenapps.datasource.DataSource;
import org.krakenapps.datasource.DataSourceRegistry;

public class DataSourceScript implements Script {
	private ScriptContext context;
	private DataSourceRegistry sourceRegistry;
	private DataConverterRegistry converterRegistry;

	public DataSourceScript(DataSourceRegistry registry, DataConverterRegistry converterRegistry) {
		this.sourceRegistry = registry;
		this.converterRegistry = converterRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "query data sources", arguments = { @ScriptArgument(name = "query string", type = "string", description = "directory path that can containing wildcard (*)") })
	public void query(String[] args) {
		String query = args[0];

		context.println("Data Sources");
		context.println("---------------------");
		for (Entry<String, DataSource> pair : sourceRegistry.query(query)) {
			context.println(pair.getKey());
		}
	}

	@ScriptUsage(description = "query converters", arguments = {
			@ScriptArgument(name = "input type", type = "string", description = "input type", optional = true),
			@ScriptArgument(name = "output type", type = "string", description = "output type", optional = true) })
	public void converters(String[] args) {
		String inputType = null;
		String outputType = null;

		if (args.length > 0)
			inputType = args[0];

		if (args.length > 1)
			outputType = args[1];

		context.println("Data Converters");
		context.println("---------------------");

		for (DataConverter conv : converterRegistry.getDataConverters(inputType, outputType)) {
			context.printf("name=%s, input type=%s, output type=%s\n", conv.getName(), conv.getInputType(), conv
					.getOutputType());
		}
	}
}
