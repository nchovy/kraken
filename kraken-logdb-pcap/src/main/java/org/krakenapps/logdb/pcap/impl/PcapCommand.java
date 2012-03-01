package org.krakenapps.logdb.pcap.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.packet.PacketHeader;
import org.krakenapps.pcap.util.PcapFileRunner;

public class PcapCommand extends LogQueryCommand {
	private PcapFileRunner runner;

	public PcapCommand(File f) {
		headerColumn.put("date", "_time");
		runner = new PcapFileRunner(f);
		runner.getEthernetDecoder().register(new EthernetProcessor() {

			@Override
			public void process(EthernetFrame frame) {
				LogMap m = new LogMap();
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
	protected void startProcess() {
		try {
			runner.run();
		} catch (IOException e) {
		} finally {
			eof();
		}
	}

	@Override
	public void push(LogMap m) {
	}

	@Override
	public boolean isReducer() {
		return false;
	}

}
