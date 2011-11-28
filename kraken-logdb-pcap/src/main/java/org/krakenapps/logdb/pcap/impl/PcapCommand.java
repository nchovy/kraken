package org.krakenapps.logdb.pcap.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.packet.PacketHeader;
import org.krakenapps.pcap.util.PcapFileRunner;

public class PcapCommand extends LogQueryCommand {
	private PcapFileRunner runner;

	public PcapCommand(File f) {
		dateColumnName = "_time";
		runner = new PcapFileRunner(f);
		runner.getEthernetDecoder().register(new EthernetProcessor() {

			@Override
			public void process(EthernetFrame frame) {
				Map<String, Object> m = new HashMap<String, Object>();
				PacketHeader h = frame.getPcapPacket().getPacketHeader();
				Date date = new Date(h.getTsSec() * 1000L + h.getTsUsec());
				m.put("_time", date);
				m.put("proto", "eth");
				m.put("ether_type", frame.getType());
				m.put("frame_size", frame.getData().readableBytes());
				m.put("dst", frame.getDestination().toString());
				m.put("src", frame.getSource().toString());
				write(m);
			}
		});
	}

	@Override
	public void start() {
		status = Status.Running;
		try {
			runner.run();
		} catch (IOException e) {
		} finally {
			eof();
		}
	}

	@Override
	public void push(Map<String, Object> m) {
	}

	@Override
	public boolean isReducer() {
		return false;
	}

}
