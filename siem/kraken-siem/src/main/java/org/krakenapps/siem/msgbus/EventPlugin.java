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
package org.krakenapps.siem.msgbus;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventPipe;
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "siem-event-plugin")
@MsgbusPlugin
public class EventPlugin implements EventPipe {
	@Requires
	private PushApi pushApi;

	@Requires
	private EventDispatcher dispatcher;

	@Validate
	public void start() {
		dispatcher.addEventPipe(this);
	}

	@Invalidate
	public void stop() {
		if (dispatcher != null)
			dispatcher.removeEventPipe(this);
	}

	private Map<String, Object> marshalEvent(Event event) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("key_id", event.getKey().getId());
		m.put("key_source", event.getKey().getSource());
		m.put("first_seen", dateFormat.format(event.getFirstSeen()));
		m.put("last_seen", dateFormat.format(event.getLastSeen()));
		m.put("category", event.getCategory());
		m.put("severity", event.getSeverity());
		m.put("host", event.getHostId());
		m.put("src_ip", event.getSourceIp() != null ? event.getSourceIp().getHostAddress() : null);
		m.put("src_port", event.getSourcePort());
		m.put("dst_ip", event.getDestinationIp() != null ? event.getDestinationIp().getHostAddress() : null);
		m.put("dst_port", event.getDestinationPort());
		m.put("detail", event.getDetail());
		m.put("msg_key", event.getMessageKey());
		m.put("msg_values", event.getMessageValues());
		m.put("rule", event.getRule());
		m.put("cve", event.getCve());
		m.put("count", event.getCount());
		return m;
	}

	@Override
	public void onEvent(Event event) {
		Map<String, Object> m = marshalEvent(event);
		pushApi.push(event.getOrgDomain(), "siem-event", m);
	}

	@Override
	public void onEventAcked(Event event) {
		Map<String, Object> m = marshalEvent(event);
		pushApi.push(event.getOrgDomain(), "siem-event-ack", m);
	}
}
