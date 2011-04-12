/*
 * Copyright 2010 NCHOVY
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArpCache {
	private ArpCache() {
	}

	public static List<ArpEntry> getEntries() {
		List<ArpEntry> entries = new ArrayList<ArpEntry>();

		Scanner scanner;
		try {
			scanner = new Scanner(new File("/proc/net/arp"));
			// skip header
			scanner.nextLine();

			while (scanner.hasNextLine()) {
				if (!scanner.hasNext())
					break;

				String ip = scanner.next();
				int hwType = Integer.valueOf(scanner.next().substring(2), 16);
				String flags = null;
				switch (Integer.valueOf(scanner.next().substring(2), 16)) {
				case 0x02:
					flags = "Dynamic";
					break;
				case 0x04:
					flags = "Static";
					break;
				case 0x10:
					flags = "Invalid";
					break;
				default:
					flags = "";
				}
				String mac = scanner.next();
				String mask = scanner.next();
				String device = scanner.next();

				ArpEntry entry = new ArpEntry(ip, hwType, flags, mac, mask, device);
				entries.add(entry);
			}

		} catch (FileNotFoundException e) {
		}

		return entries;
	}
}
