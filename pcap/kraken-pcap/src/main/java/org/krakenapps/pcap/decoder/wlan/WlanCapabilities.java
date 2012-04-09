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

public class WlanCapabilities {
	public static final int ESS_CAPABILITIES = 1;
	public static final int IBSS_STATUS = 1 << 1;
	public static final int PRIVACY = 1 << 4;
	public static final int SHORT_PREAMBLE = 1 << 5;
	public static final int PBCC_MODULATION = 1 << 6;
	public static final int CHANNEL_AGILITY = 1 << 7;
	public static final int SPECTRUM_MANAGEMENT = 1 << 8;
	public static final int SHORT_SLOT_TIME = 1 << 10;
	public static final int AUTO_POWER_SAVE_DELIVERY = 1 << 11;
	public static final int DSSS_OFSDM = 1 << 13;
	public static final int DELAYED_BLOCK_ACK = 1 << 14;
	public static final int IMMEDIATE_BLOCK_ACK = 1 << 15;

	private WlanCapabilities() {
	}
}
