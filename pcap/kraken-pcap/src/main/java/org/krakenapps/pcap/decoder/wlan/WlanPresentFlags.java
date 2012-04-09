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

public class WlanPresentFlags {
	public static final int TSFT = 1;
	public static final int FLAGS = 1 << 1;
	public static final int RATE = 1 << 2;
	public static final int CHANNEL = 1 << 3;
	public static final int FHSS = 1 << 4;
	public static final int DBM_ANTENNA_SIGNAL = 1 << 5;
	public static final int DBM_ANTENNA_NOISE = 1 << 6;
	public static final int LOCK_QUALITY = 1 << 7;
	public static final int TX_ATTENUATION = 1 << 8;
	public static final int DB_TX_ATTENUATION = 1 << 9;
	public static final int DBM_TX_ATTENUATION = 1 << 10;
	public static final int ANTENNA = 1 << 11;
	public static final int DB_ANTENNA_SIGNAL = 1 << 12;
	public static final int DB_ANTENNA_NOISE = 1 << 13;
	public static final int RX_FLAGS = 1 << 14;
	public static final int CHANNEL2 = 1 << 15;
	public static final int HT_INFO = 1 << 16;
	public static final int RADIOTAP_NS_NEXt = 1 << 29;
	public static final int VENDOR_NS_NEXT = 1 << 30;
	public static final int EXT = 1 << 31;

	private WlanPresentFlags() {
	}
}
