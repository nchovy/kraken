/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.pcap.decoder.wlan;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.wlan.tag.DsParameterSet;
import org.krakenapps.pcap.decoder.wlan.tag.ErpInformation;
import org.krakenapps.pcap.decoder.wlan.tag.ExtendedSupportedRates;
import org.krakenapps.pcap.decoder.wlan.tag.RsnInformation;
import org.krakenapps.pcap.decoder.wlan.tag.SsidParameterSet;
import org.krakenapps.pcap.decoder.wlan.tag.SupportedRates;
import org.krakenapps.pcap.decoder.wlan.tag.TaggedParameter;
import org.krakenapps.pcap.decoder.wlan.tag.TrafficIndicationMap;
import org.krakenapps.pcap.decoder.wlan.tag.UnknownParameter;
import org.krakenapps.pcap.decoder.wlan.tag.WlanControlFrame;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class WlanDecoder {
	public WlanFrame decode(PcapPacket pkt) {
		Buffer buf = pkt.getPacketData();

		RadiotapHeader rh = decodeRadiotapHeader(buf);
		WlanFrameControl frameControl = new WlanFrameControl(buf.getShort());
		WlanFrame frame = null;
		switch (frameControl.getType()) {
		case 0:
			frame = decodeManagement(frameControl, buf);
			break;
		case 1:
			frame = decodeControl(frameControl, buf);
			break;
		case 2:
			frame = decodeData(frameControl, buf);
			break;
		}

		if (frame != null) {
			frame.setRadiotapHeader(rh);
			frame.setFrameControl(frameControl);
		}
		return frame;
	}

	private WlanManagementFrame decodeManagement(WlanFrameControl frameControl, Buffer buf) {
		byte subtype = frameControl.getSubtype();
		if (subtype == 4)
			return decodeProbeRequest(frameControl, buf);
		else if (subtype == 5)
			return decodeProbeResponse(frameControl, buf);
		else if (subtype == 8)
			return decodeBeacon(buf);
		return null;
	}

	private void decodeManagementCommon(WlanManagementFrame f, Buffer buf) {
		byte[] dst = new byte[6];
		byte[] src = new byte[6];
		byte[] bssid = new byte[6];

		f.setDuration(buf.getShort());

		buf.gets(dst);
		buf.gets(src);
		buf.gets(bssid);

		f.setDestination(new MacAddress(dst));
		f.setSource(new MacAddress(src));
		f.setBssid(new MacAddress(bssid));

		short seqfrag = ByteOrderConverter.swap(buf.getShort());
		f.setSeq((seqfrag >> 4) & 0xfff);
		f.setFragment(seqfrag & 0xf);

	}

	private WlanProbeRequest decodeProbeRequest(WlanFrameControl frameControl, Buffer buf) {
		WlanProbeRequest f = new WlanProbeRequest();
		decodeManagementCommon(f, buf);
		decodeTaggedParameters(f, buf);
		return f;
	}

	private WlanProbeResponse decodeProbeResponse(WlanFrameControl frameControl, Buffer buf) {
		WlanProbeResponse f = new WlanProbeResponse();
		decodeManagementCommon(f, buf);
		decodeFixedParameters(f, buf);
		decodeTaggedParameters(f, buf);
		return f;
	}

	private WlanBeaconFrame decodeBeacon(Buffer buf) {
		WlanBeaconFrame f = new WlanBeaconFrame();
		decodeManagementCommon(f, buf);
		decodeFixedParameters(f, buf);
		decodeTaggedParameters(f, buf);

		f.setFrameCheckSeq(buf.getInt());

		return f;
	}

	private void decodeFixedParameters(WlanManagementFrame f, Buffer buf) {
		f.setTimestamp(buf.getLong());
		f.setBeaconInterval(buf.getShort());
		f.setCapabilities(buf.getShort());
	}

	private void decodeTaggedParameters(WlanManagementFrame f, Buffer buf) {
		int parsed = 0;
		int len = buf.readableBytes() - 4; // except fcs

		while (parsed < len) {
			int type = buf.get() & 0xff;
			int taglen = buf.get() & 0xff;

			// System.out.println("type=" + type + ", len=" + taglen);
			byte[] data = new byte[taglen];
			buf.gets(data);
			parsed += 2 + taglen;

			f.getParameters().add(decodeTaggedParameter(type, taglen, data));
		}
	}

	private WlanControlFrame decodeControl(WlanFrameControl frameControl, Buffer buf) {
		if (frameControl.getSubtype() == 10) {
			byte[] bssid = new byte[6];
			byte[] transmitter = new byte[6];
			short assocId = buf.getShort();
			buf.gets(bssid);
			buf.gets(transmitter);

			WlanPowerSavePollFrame f = new WlanPowerSavePollFrame();
			f.setAssociationId(assocId);
			f.setBssid(new MacAddress(bssid));
			f.setTransmitterAddress(new MacAddress(transmitter));
			return f;
		} else if (frameControl.getSubtype() == 12) {
			int duration = ByteOrderConverter.swap(buf.getShort()) & 0xffff;
			byte[] receiver = new byte[6];
			buf.gets(receiver);

			WlanCtsFrame f = new WlanCtsFrame();
			f.setDuration(duration);
			f.setReceiver(new MacAddress(receiver));
			return f;
		} else if (frameControl.getSubtype() == 13) {
			int duration = ByteOrderConverter.swap(buf.getShort()) & 0xffff;
			byte[] receiver = new byte[6];
			buf.gets(receiver);

			WlanAckFrame f = new WlanAckFrame();
			f.setDuration(duration);
			f.setReceiver(new MacAddress(receiver));
			return f;
		}

		return null;
	}

	private WlanDataFrame decodeData(WlanFrameControl frameControl, Buffer buf) {
		// normal data frame or null data frame
		if (frameControl.getSubtype() == 0 || frameControl.getSubtype() == 4) {
			byte[] dst = new byte[6];
			byte[] bssid = new byte[6];
			byte[] src = new byte[6];

			int duration = ByteOrderConverter.swap(buf.getShort()) & 0xffff;
			buf.gets(dst);
			buf.gets(bssid);
			buf.gets(src);
			short seqfrag = ByteOrderConverter.swap(buf.getShort());
			int wepParameters = buf.getInt();

			WlanDataFrame f = new WlanDataFrame();
			f.setDuration(duration);
			f.setDestination(new MacAddress(dst));
			f.setBssid(new MacAddress(bssid));
			f.setSource(new MacAddress(src));
			f.setSeq((seqfrag >> 4) & 0xfff);
			f.setFragment(seqfrag & 0xf);
			f.setIv((wepParameters >> 8) & 0xffffff);
			f.setKeyIndex(wepParameters & 0xff);

			return f;
		}
		
		return null;
	}

	private TaggedParameter decodeTaggedParameter(int type, int len, byte[] data) {
		switch (type) {
		case 0:
			return new SsidParameterSet(new String(data));
		case 1:
			return new SupportedRates();
		case 3:
			return new DsParameterSet((int) data[0]);
		case 48:
			return new RsnInformation();
		case 5:
			return new TrafficIndicationMap();
		case 42:
			return new ErpInformation();
		case 50:
			return new ExtendedSupportedRates();
		}

		return new UnknownParameter(data);
	}

	private RadiotapHeader decodeRadiotapHeader(Buffer buf) {
		RadiotapHeader rh = new RadiotapHeader();

		buf.getShort(); // padding
		rh.setHeaderLength(buf.getShort());
		rh.setPresentFlags(buf.getInt());
		rh.setMacTimestamp(buf.getLong());
		rh.setFlags(buf.get());
		rh.setDataRate(buf.get());
		rh.setChannelFrequency(ByteOrderConverter.swap(buf.getShort()));
		rh.setChannelType(ByteOrderConverter.swap(buf.getShort()));
		rh.setSsiSignal(buf.get());
		rh.setSsiNoise(buf.get());
		rh.setAntenna(buf.get());
		rh.setSsiSignal2(buf.get());
		return rh;
	}
}
