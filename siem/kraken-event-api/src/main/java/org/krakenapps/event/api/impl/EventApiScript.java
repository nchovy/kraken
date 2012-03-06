/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.event.api.impl;

import java.net.InetAddress;
import java.util.Date;
import java.util.Locale;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventKey;
import org.krakenapps.event.api.EventMessageKey;
import org.krakenapps.event.api.EventMessageTable;
import org.krakenapps.event.api.EventProvider;
import org.krakenapps.event.api.EventProviderRegistry;
import org.krakenapps.event.api.EventSeverity;

public class EventApiScript implements Script {
	private EventDispatcher dispatcher;
	private EventProviderRegistry providerRegistry;
	private EventMessageTable messageTable;
	private ScriptContext context;

	public EventApiScript(EventDispatcher dispatcher, EventProviderRegistry providerRegistry,
			EventMessageTable messageTable) {
		this.dispatcher = dispatcher;
		this.providerRegistry = providerRegistry;
		this.messageTable = messageTable;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void providers(String[] args) {
		context.println("Event Providers");
		context.println("------------------");

		for (String name : providerRegistry.getNames()) {
			EventProvider provider = providerRegistry.get(name);
			context.println("[" + provider.getName() + "] " + provider.toString());
		}
	}

	public void templates(String[] args) {
		context.println("Message Templates");
		context.println("-------------------------");

		for (EventMessageKey key : messageTable.keySet()) {
			String template = messageTable.getTemplate(key);
			context.println(key + ": " + template);
		}
	}

	@ScriptUsage(description = "print localized template", arguments = {
			@ScriptArgument(name = "key", type = "string", description = "message key"),
			@ScriptArgument(name = "locale", type = "string", description = "two-letter locale") })
	public void template(String[] args) {
		try {
			String key = args[0];
			Locale locale = new Locale(args[1]);

			String template = messageTable.getTemplate(new EventMessageKey(key, locale));
			if (template == null)
				context.println("template not found");
			else
				context.println(template);
		} catch (Exception e) {
			context.println("invalid locale");
		}
	}

	public void generate(String[] args) {
		try {
			context.print("ID? ");
			int id = Integer.valueOf(context.readLine());
			context.print("Category? ");
			String category = context.readLine();
			context.print("Source IP? ");
			InetAddress sourceIp = InetAddress.getByName(context.readLine());
			context.print("Source Port? ");
			Integer sourcePort = Integer.valueOf(context.readLine());
			context.print("Destination IP? ");
			InetAddress destinationIp = InetAddress.getByName(context.readLine());
			context.print("Destination Port? ");
			Integer destinationPort = Integer.valueOf(context.readLine());
			context.print("Message Key? ");
			String messageKey = context.readLine();

			Event e = new Event();
			e.setKey(new EventKey(id));
			e.setFirstSeen(new Date());
			e.setLastSeen(new Date());
			e.setCategory(category);
			e.setSourceIp(sourceIp);
			e.setSourcePort(sourcePort);
			e.setDestinationIp(destinationIp);
			e.setDestinationPort(destinationPort);
			e.setMessageKey(messageKey);
			e.setSeverity(EventSeverity.Notice);
			e.setCount(1);

			dispatcher.dispatch(e);
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}
}
