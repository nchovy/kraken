package org.krakenapps.webconsole.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "webconsole-thread-monitor-plugin")
@MsgbusPlugin
public class ThreadMonitorPlugin {

	@MsgbusMethod
	public void getThreads(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();

		for (Thread t : traces.keySet()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("thread", marshal(t));
			m.put("stacktrace", marshal(traces.get(t)));
			l.add(m);
		}

		resp.put("threads", l);
	}

	private Object marshal(Thread t) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", t.getId());
		m.put("name", t.getName());
		m.put("priority", t.getPriority());
		m.put("state", t.getState().toString());
		return m;
	}

	private Object marshal(StackTraceElement[] stack) {
		List<Object> l = new ArrayList<Object>();
		for (StackTraceElement e : stack)
			l.add(marshal(e));
		return l;
	}

	private Map<String, Object> marshal(StackTraceElement e) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("class_name", e.getClassName());
		m.put("method_name", e.getMethodName());
		m.put("file_name", e.getFileName());
		m.put("line_number", e.getLineNumber());
		return m;
	}
}
