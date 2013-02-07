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
package org.krakenapps.report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;

public class BirtScript implements Script {

	private ReportPrintMachine machine;
	private ScriptContext context;

	public static final String BIRT_BASE_DIR_KEY = "birtEngineBaseDir";

	private BirtScript(ReportPrintMachine machine) {
		this.machine = machine;
	}

	public static BirtScript newInstance(ReportPrintMachine machine) {
		return new BirtScript(machine);
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}
	
	@ScriptUsage(description = "set report machine path", arguments = { @ScriptArgument(name = "path", type = "string", description = "birt report machine base dir") })
	public void setReportMachinePath(String[] args) {
		machine.setReportMachinePath(new File(args[0]));
		boolean result = machine.restart();
		context.println("home directory changed and engine restart " +(result ? "succeed.":"failed."));
	}

	@ScriptUsage(description = "set report machine parameter", arguments = {
			@ScriptArgument(name = "key", type = "string", description = "parameter key"),
			@ScriptArgument(name = "value", type = "string", description = "parameter value") })
	public void setParam(String[] args) {
		Properties properties = this.context.getEnvironmentVariables();
		properties.put(args[0], args[1]);
		this.context.setEnvironmentVariables(properties);
	}

	@ScriptUsage(description = "show engine path and list all report parameters")
	public void status(String[] args) {
		context.println("=== BIRT Engine Path ===");
		context.println(machine.getReportMachinePath());
		Properties properties = this.context.getEnvironmentVariables();
		context.println("=== BIRT Report Parameters ===");
		if (properties.isEmpty())  {
			context.println(" No parameter.");
		} else {

	 		for(Entry<Object, Object> e : properties.entrySet()) {
				context.println(e.getKey()+" : "+e.getValue());
			}
		}
		context.println("===============");
	}
	
	@ScriptUsage(description = "print a report", arguments = {
			@ScriptArgument(name = "template name", type = "string", description = "template name"),
			@ScriptArgument(name = "type name", type = "string", description = "printing type name: pdf or doc"),
			@ScriptArgument(name = "output file name", type = "string", description = "the name of output report file") })
	public void print(String[] args) throws FileNotFoundException, IOException {

		String templateName = args[0];
		String typeName = args[1];
		String outputFileName = args[2];

		BirtReportOutputType type = BirtReportOutputType.valueOf(typeName
				.toUpperCase());
		if (type == null) {
			context.getOutputStream().println(
					"Type " + typeName + " does not exist.");
			return;
		}

		InputStream in = new BufferedInputStream(new FileInputStream(
				templateName));

		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				outputFileName));

		Properties properties = this.context.getEnvironmentVariables();
		Map<String, Object> reportParameters = new HashMap<String, Object>(properties.size());
		for(Entry<Object, Object> e: properties.entrySet()) {
			reportParameters.put(e.getKey().toString(), e.getValue());
		}
		
		machine.print(in, reportParameters, type, out, null);
		out.flush();
		in.close();
		out.close();
	}

}
