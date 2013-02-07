/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NetworkInterfaceInformation {
	private static final String DEFAULT_PATH = "/proc/net/dev";
	private String name;
	private long rxBytes;
	private long rxPackets;
	private long rxErrs;
	private long rxDrop;
	private long rxFifo;
	private long rxFrame;
	private long rxCompressed;
	private long rxMulticast;

	private long txBytes;
	private long txPackets;
	private long txErrs;
	private long txDrop;
	private long txFifo;
	private long txColls;
	private long txCarrier;
	private long txCompressed;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRxBytes() {
		return rxBytes;
	}

	public void setRxBytes(long rxBytes) {
		this.rxBytes = rxBytes;
	}

	public long getRxPackets() {
		return rxPackets;
	}

	public void setRxPackets(long rxPackets) {
		this.rxPackets = rxPackets;
	}

	public long getRxErrs() {
		return rxErrs;
	}

	public void setRxErrs(long rxErrs) {
		this.rxErrs = rxErrs;
	}

	public long getRxDrop() {
		return rxDrop;
	}

	public void setRxDrop(long rxDrop) {
		this.rxDrop = rxDrop;
	}

	public long getRxFifo() {
		return rxFifo;
	}

	public void setRxFifo(long rxFifo) {
		this.rxFifo = rxFifo;
	}

	public long getRxFrame() {
		return rxFrame;
	}

	public void setRxFrame(long rxFrame) {
		this.rxFrame = rxFrame;
	}

	public long getRxCompressed() {
		return rxCompressed;
	}

	public void setRxCompressed(long rxCompressed) {
		this.rxCompressed = rxCompressed;
	}

	public long getRxMulticast() {
		return rxMulticast;
	}

	public void setRxMulticast(long rxMuticast) {
		this.rxMulticast = rxMuticast;
	}

	public long getTxBytes() {
		return txBytes;
	}

	public void setTxBytes(long txBytes) {
		this.txBytes = txBytes;
	}

	public long getTxPackets() {
		return txPackets;
	}

	public void setTxPackets(long txPackets) {
		this.txPackets = txPackets;
	}

	public long getTxErrs() {
		return txErrs;
	}

	public void setTxErrs(long txErrs) {
		this.txErrs = txErrs;
	}

	public long getTxDrop() {
		return txDrop;
	}

	public void setTxDrop(long txDrop) {
		this.txDrop = txDrop;
	}

	public long getTxFifo() {
		return txFifo;
	}

	public void setTxFifo(long txFifo) {
		this.txFifo = txFifo;
	}

	public long getTxColls() {
		return txColls;
	}

	public void setTxColls(long txColls) {
		this.txColls = txColls;
	}

	public long getTxCarrier() {
		return txCarrier;
	}

	public void setTxCarrier(long txCarrier) {
		this.txCarrier = txCarrier;
	}

	public long getTxCompressed() {
		return txCompressed;
	}

	public void setTxCompressed(long txCompressed) {
		this.txCompressed = txCompressed;
	}

	public static List<NetworkInterfaceInformation> getNetworkInterfaceInformations() throws IOException {
		return getNetworkInterfaceInformations(new File(DEFAULT_PATH));
	}

	public static List<NetworkInterfaceInformation> getNetworkInterfaceInformations(File f) throws IOException {
		List<NetworkInterfaceInformation> informs = new ArrayList<NetworkInterfaceInformation>();
		NetworkInterfaceInformation inform = null;
		BufferedReader br = null;
		String line = null;
		try {
			if (f == null)
				throw new IllegalArgumentException("file should not be null");

			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith("Inter") || line.trim().startsWith("face"))
					continue;

				inform = new NetworkInterfaceInformation();
				String[] splitData = line.split(":");
				if (splitData.length != 2)
					continue;

				inform.setName(splitData[0].trim());

				String regex = "\t| {1,}";
				String[] datas = splitData[1].trim().split(regex);

				int index = 0;

				inform.setRxBytes(Long.parseLong(datas[index++].trim().trim()));
				inform.setRxPackets(Long.parseLong(datas[index++].trim()));
				inform.setRxErrs(Long.parseLong(datas[index++].trim()));
				inform.setRxDrop(Long.parseLong(datas[index++].trim()));
				inform.setRxFifo(Long.parseLong(datas[index++].trim()));
				inform.setRxFrame(Long.parseLong(datas[index++].trim()));
				inform.setRxCompressed(Long.parseLong(datas[index++].trim()));
				inform.setRxMulticast(Long.parseLong(datas[index++].trim()));
				inform.setTxBytes(Long.parseLong(datas[index++].trim()));
				inform.setTxPackets(Long.parseLong(datas[index++].trim()));
				inform.setTxErrs(Long.parseLong(datas[index++].trim()));
				inform.setTxDrop(Long.parseLong(datas[index++].trim()));
				inform.setTxFifo(Long.parseLong(datas[index++].trim()));
				inform.setTxColls(Long.parseLong(datas[index++].trim()));
				inform.setTxCarrier(Long.parseLong(datas[index++].trim()));
				inform.setTxCompressed(Long.parseLong(datas[index++].trim()));

				informs.add(inform);
			}

			return informs;
		} finally {
			if (br != null)
				br.close();
		}
	}

	@Override
	public String toString() {
		return "NetworkInterfaceInformation [name=" + name + ", rxBytes=" + rxBytes + ", rxPackets=" + rxPackets + ", rxErrs="
				+ rxErrs + ", rxDrop=" + rxDrop + ", rxFifo=" + rxFifo + ", rxFrame=" + rxFrame + ", rxCompressed="
				+ rxCompressed + ", rxMuticast=" + rxMulticast + ", txBytes=" + txBytes + ", txPackets=" + txPackets + ", txErrs="
				+ txErrs + ", txDrop=" + txDrop + ", txFifo=" + txFifo + ", txColls=" + txColls + ", txCarrier=" + txCarrier
				+ ", txCompressed=" + txCompressed + "]";
	}

}
