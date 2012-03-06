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
package org.krakenapps.xmlrpc.script;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.xmlrpc.XmlRpcClient;

public class XmlRpcClientScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "xml-rpc client call\n\tex) xmlrpc-client.call http://localhost/xmlrpc test.call -12, true, 'hello, world!', -12.214, 20110311T20:14:33", arguments = {
			@ScriptArgument(name = "server", type = "string", description = "xml-rpc server address"),
			@ScriptArgument(name = "method", type = "string", description = "xml-rpc method name"),
			@ScriptArgument(name = "arguments", description = "method arguments", optional = true) })
	public void call(String[] args) {
		String server = args[0];
		String method = args[1];

		StringBuilder builder = new StringBuilder();
		for (int i = 2; i < args.length; i++)
			builder.append(args[i]);

		try {
			Object result = XmlRpcClient.call(server, method, parse(builder.toString()));
			print(result, 0, false);
			context.println("");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private void print(Object obj, int tabCount, boolean isMapValue) {
		String t = "";
		if (!isMapValue)
			t = makeTab(tabCount);

		if (obj instanceof Collection<?>) {
			context.println("[");
			int size = ((Collection<?>) obj).size();
			for (int i = 0; i < size; i++) {
				Object o = ((Collection<?>) obj).toArray()[i];
				print(o, tabCount + 1, false);
				if (i < size - 1)
					context.println(",");
			}
			context.println("");
			context.print(makeTab(tabCount) + "]");
		} else if (obj instanceof Object[]) {
			context.println("[");
			int size = ((Object[]) obj).length;
			for (int i = 0; i < size; i++) {
				Object o = ((Object[]) obj)[i];
				print(o, tabCount + 1, false);
				if (i < size - 1)
					context.println(",");
			}
			context.println("");
			context.print(makeTab(tabCount) + "]");
		} else if (obj instanceof Map<?, ?>) {
			context.println(t + "{");
			Map<?, ?> m = (Map<?, ?>) obj;
			Object[] keys = m.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				Object o = keys[i];
				context.print(makeTab(tabCount + 1) + "\"" + o + "\"" + ": ");
				print(m.get(o), tabCount + 1, true);
				if (i < keys.length - 1)
					context.print(",");
				context.println("");
			}
			context.print(makeTab(tabCount) + "}");
		} else if (obj instanceof String) {
			context.print(t + "\"" + obj + "\"");
		} else {
			context.print(t + obj);
		}
	}

	private String makeTab(int tabCount) {
		String t = "";
		for (int i = 0; i < tabCount; i++)
			t += "  ";
		return t;
	}

	private Object[] parse(String str) throws ParseException {
		List<Object> args = new ArrayList<Object>();
		String[] tokens = str.split(",");

		if (tokens.length == 1 && tokens[0].trim().isEmpty())
			return args.toArray();

		for (String token : tokens) {
			args.add(convert(token.trim()));
		}

		return args.toArray();
	}

	// base64, struct, array are not supported
	private Object convert(String str) throws ParseException {
		// Integer
		try {
			Integer i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException e) {
		}

		// Double
		try {
			Double d = Double.parseDouble(str);
			return d;
		} catch (NumberFormatException e) {
		}

		// Boolean
		if (str.equalsIgnoreCase("true") || str.equals("1"))
			return new Boolean(true);
		else if (str.equalsIgnoreCase("false") || str.equals("0"))
			return new Boolean(false);

		// String
		if (str.startsWith("'") && str.endsWith("'"))
			return str.substring(1, str.length() - 1);

		// DateTime
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
			Date date = dateFormat.parse(str);
			return date;
		} catch (ParseException e) {
		}

		throw new ParseException("unknown type: " + str, 0);
	}
}
