package org.krakenapps.logstorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.logstorage.query.FileBufferList;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.Result;
import org.krakenapps.logstorage.query.command.Timechart;
import org.krakenapps.logstorage.query.command.Timechart.Span;

@Ignore
public class FlowLogGenerator {
	@Test
	public void generate() throws IOException {
		long begin = System.currentTimeMillis();
		Timechart tc = new Timechart(Span.Hour, 1, new Function[] { Function.getFunction("per_hour", "tx_packets",
				Timechart.func) }, "eos");
		tc.setDataHeader(new String[] {});

		Result result = new Result();
		tc.setNextCommand(result);
		long end = System.currentTimeMillis();
		System.out.println("initialize: " + (end - begin) + " ms");

		int count = 2000000;
		Random r = new Random();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -4);
		List<FlowLog> logs = new ArrayList<FlowLog>();
		for (int i = 0; i < count; i++) {
			FlowLog fl = new FlowLog();
			fl.date = c.getTime();
			fl.tunnel = Math.abs(r.nextLong() % 100000);
			fl.session = Math.abs(r.nextLong() % 100000);
			fl.clientIp = String.format("%d.%d.%d.%d", r.nextInt(256), r.nextInt(256), r.nextInt(256), r.nextInt(256));
			fl.clientPort = r.nextInt(65536);
			fl.serverIp = String.format("%d.%d.%d.%d", r.nextInt(256), r.nextInt(256), r.nextInt(256), r.nextInt(256));
			fl.serverPort = r.nextInt(65536);
			fl.protocol = "http";
			fl.txBytes = Math.abs(r.nextLong() % 10000);
			fl.txPackets = Math.abs(r.nextLong() % 100);
			fl.rxBytes = Math.abs(r.nextLong() % 10000);
			fl.rxPackets = Math.abs(r.nextLong() % 100);
			fl.eos = r.nextBoolean();
			logs.add(fl);

			c.add(Calendar.SECOND, r.nextInt(20000000 / count));
		}

		begin = System.currentTimeMillis();

		for (FlowLog fl : logs)
			write(tc, fl.toLog());
		tc.eof();

		FileBufferList<Map<String, Object>> fbl = result.getResult();
		for (Map<String, Object> m : fbl)
			System.out.println(m);
		fbl.close();

		end = System.currentTimeMillis();
		System.out.println("process: " + (end - begin) + " ms");
	}

	private void write(LogQueryCommand cmd, Log log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("_table", log.getTableName());
		m.put("_id", log.getId());
		m.put("_time", log.getDate());
		m.putAll(log.getData());
		cmd.push(m);
	}

	public class FlowLog {
		private Date date;
		private long tunnel;
		private long session;
		private String clientIp;
		private int clientPort;
		private String serverIp;
		private int serverPort;
		private String protocol;
		private long txBytes;
		private long txPackets;
		private long rxBytes;
		private long rxPackets;
		private boolean eos;

		public Log toLog() {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("tunnel", tunnel);
			m.put("session", session);
			m.put("client_ip", clientIp);
			m.put("client_port", clientPort);
			m.put("server_ip", serverIp);
			m.put("server_port", serverPort);
			m.put("protocol", protocol);
			m.put("tx_bytes", txBytes);
			m.put("rx_bytes", rxBytes);
			m.put("tx_packets", txPackets);
			m.put("rx_packets", rxPackets);
			m.put("eos", eos);

			return new Log("flow", date, m);
		}
	}
}
