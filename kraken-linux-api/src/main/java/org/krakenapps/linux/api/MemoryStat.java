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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MemoryStat {
	private MemoryStat() {
	}

	private long memTotal;
	private long memFree;
	private long buffers;
	private long cached;
	private long swapCached;
	private long active;
	private long inactive;
	private long anonActive;
	private long anonInactive;
	private long fileActive;
	private long fileInactive;
	private long unevictable;
	private long mlocked;
	private long swapTotal;
	private long swapFree;
	private long dirty;
	private long writeback;
	private long anonPages;
	private long mapped;
	private long slab;
	private long sReclaimable;
	private long sUnreclaim;
	private long pageTables;
	private long nfsUnstable;
	private long bounce;
	private long writebackTmp;
	private long commitLimit;
	private long committedAs;
	private long vmallocTotal;
	private long vmallocUsed;
	private long vmallocChunk;
	private long hugePagesTotal;
	private long hugePagesFree;
	private long hugePagesRsvd;
	private long hugePagesSurp;
	private long hugePageSize;
	private long directMap4k;
	private long directMap2M;

	public long getMemTotal() {
		return memTotal;
	}

	public long getMemFree() {
		return memFree;
	}

	public long getBuffers() {
		return buffers;
	}

	public long getCached() {
		return cached;
	}

	public long getSwapCached() {
		return swapCached;
	}

	public long getActive() {
		return active;
	}

	public long getInactive() {
		return inactive;
	}

	public long getAnonActive() {
		return anonActive;
	}

	public long getAnonInactive() {
		return anonInactive;
	}

	public long getFileActive() {
		return fileActive;
	}

	public long getFileInactive() {
		return fileInactive;
	}

	public long getUnevictable() {
		return unevictable;
	}

	public long getMlocked() {
		return mlocked;
	}

	public long getSwapTotal() {
		return swapTotal;
	}

	public long getSwapFree() {
		return swapFree;
	}

	public long getDirty() {
		return dirty;
	}

	public long getWriteback() {
		return writeback;
	}

	public long getAnonPages() {
		return anonPages;
	}

	public long getMapped() {
		return mapped;
	}

	public long getSlab() {
		return slab;
	}

	public long getsReclaimable() {
		return sReclaimable;
	}

	public long getsUnreclaim() {
		return sUnreclaim;
	}

	public long getPageTables() {
		return pageTables;
	}

	public long getNfsUnstable() {
		return nfsUnstable;
	}

	public long getBounce() {
		return bounce;
	}

	public long getWritebackTmp() {
		return writebackTmp;
	}

	public long getCommitLimit() {
		return commitLimit;
	}

	public long getCommittedAs() {
		return committedAs;
	}

	public long getVmallocTotal() {
		return vmallocTotal;
	}

	public long getVmallocUsed() {
		return vmallocUsed;
	}

	public long getVmallocChunk() {
		return vmallocChunk;
	}

	public long getHugePagesTotal() {
		return hugePagesTotal;
	}

	public long getHugePagesFree() {
		return hugePagesFree;
	}

	public long getHugePagesRsvd() {
		return hugePagesRsvd;
	}

	public long getHugePagesSurp() {
		return hugePagesSurp;
	}

	public long getHugePageSize() {
		return hugePageSize;
	}

	public long getDirectMap4k() {
		return directMap4k;
	}

	public long getDirectMap2M() {
		return directMap2M;
	}

	public static MemoryStat getMemoryStat() throws IOException {
		MemoryStat memory = new MemoryStat();
		BufferedReader br = null;
		FileInputStream is = null;

		try {
			is = new FileInputStream(new File("/proc/meminfo"));
			br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				parse(memory, line);
			}

			return memory;
		} finally {
			if (is != null)
				is.close();
			if (br != null)
				br.close();
		}
	}

	private static void parse(MemoryStat memory, String line) {
		int colon = line.indexOf(":");
		String name = line.substring(0, colon).trim();
		long value = Long.parseLong(line.substring(colon + 1).replace("kB", "").trim());

		if (name.equals("MemTotal"))
			memory.memTotal = value;
		else if (name.equals("MemFree"))
			memory.memFree = value;
		else if (name.equals("Buffers"))
			memory.buffers = value;
		else if (name.equals("Cached"))
			memory.cached = value;
		else if (name.equals("SwapCached"))
			memory.swapCached = value;
		else if (name.equals("Active"))
			memory.active = value;
		else if (name.equals("Inactive"))
			memory.inactive = value;
		else if (name.equals("Active(anon)"))
			memory.anonActive = value;
		else if (name.equals("Inactive(anon)"))
			memory.anonInactive = value;
		else if (name.equals("Active(file)"))
			memory.fileActive = value;
		else if (name.equals("Inactive(file)"))
			memory.fileInactive = value;
		else if (name.equals("Unevictable"))
			memory.unevictable = value;
		else if (name.equals("Mlocked"))
			memory.mlocked = value;
		else if (name.equals("SwapTotal"))
			memory.swapTotal = value;
		else if (name.equals("SwapFree"))
			memory.swapFree = value;
		else if (name.equals("Dirty"))
			memory.dirty = value;
		else if (name.equals("Writeback"))
			memory.writeback = value;
		else if (name.equals("AnonPages"))
			memory.anonPages = value;
		else if (name.equals("Mapped"))
			memory.mapped = value;
		else if (name.equals("Slab"))
			memory.slab = value;
		else if (name.equals("SReclaimable"))
			memory.sReclaimable = value;
		else if (name.equals("SUnreclaim"))
			memory.sUnreclaim = value;
		else if (name.equals("PageTables"))
			memory.pageTables = value;
		else if (name.equals("NFS_Unstable"))
			memory.nfsUnstable = value;
		else if (name.equals("Bounce"))
			memory.bounce = value;
		else if (name.equals("WritebackTmp"))
			memory.writebackTmp = value;
		else if (name.equals("CommitLimit"))
			memory.commitLimit = value;
		else if (name.equals("Committed_AS"))
			memory.committedAs = value;
		else if (name.equals("VmallocTotal"))
			memory.vmallocTotal = value;
		else if (name.equals("VmallocUsed"))
			memory.vmallocUsed = value;
		else if (name.equals("VmallocChunk"))
			memory.vmallocChunk = value;
		else if (name.equals("HugePages_Total"))
			memory.hugePagesTotal = value;
		else if (name.equals("HugePages_Free"))
			memory.hugePagesFree = value;
		else if (name.equals("HugePages_Rsvd"))
			memory.hugePagesRsvd = value;
		else if (name.equals("HugePages_Surp"))
			memory.hugePagesSurp = value;
		else if (name.equals("Hugepagesize"))
			memory.hugePageSize = value;
		else if (name.equals("DirectMap4k"))
			memory.directMap4k = value;
		else if (name.equals("DirectMap2M"))
			memory.directMap2M = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(String.format("MemTotal:\t%8d kB\n", memTotal));
		builder.append(String.format("MemFree:\t%8d kB\n", memFree));
		builder.append(String.format("Buffers:\t%8d kB\n", buffers));
		builder.append(String.format("Cached:\t\t%8d kB\n", cached));
		builder.append(String.format("SwapCached:\t%8d kB\n", swapCached));
		builder.append(String.format("Active:\t\t%8d kB\n", active));
		builder.append(String.format("Inactive:\t%8d kB\n", inactive));
		builder.append(String.format("Active(anon):\t%8d kB\n", anonActive));
		builder.append(String.format("Inactive(anon):\t%8d kB\n", anonInactive));
		builder.append(String.format("Active(file):\t%8d kB\n", fileActive));
		builder.append(String.format("Inactive(file):\t%8d kB\n", fileInactive));
		builder.append(String.format("Unevictable:\t%8d kB\n", unevictable));
		builder.append(String.format("Mlocked:\t%8d kB\n", mlocked));
		builder.append(String.format("SwapTotal:\t%8d kB\n", swapTotal));
		builder.append(String.format("SwapFree:\t%8d kB\n", swapFree));
		builder.append(String.format("Dirty:\t\t%8d kB\n", dirty));
		builder.append(String.format("Writeback:\t%8d kB\n", writeback));
		builder.append(String.format("AnonPages:\t%8d kB\n", anonPages));
		builder.append(String.format("Mapped:\t\t%8d kB\n", mapped));
		builder.append(String.format("Slab:\t\t%8d kB\n", slab));
		builder.append(String.format("SReclaimable:\t%8d kB\n", sReclaimable));
		builder.append(String.format("SUnreclaim:\t%8d kB\n", sUnreclaim));
		builder.append(String.format("PageTables:\t%8d kB\n", pageTables));
		builder.append(String.format("NFS_Unstable:\t%8d kB\n", nfsUnstable));
		builder.append(String.format("Bounce:\t\t%8d kB\n", bounce));
		builder.append(String.format("WritebackTmp:\t%8d kB\n", writebackTmp));
		builder.append(String.format("CommitLimit:\t%8d kB\n", commitLimit));
		builder.append(String.format("Committed_AS:\t%8d kB\n", committedAs));
		builder.append(String.format("VmallocTotal:\t%8d kB\n", vmallocTotal));
		builder.append(String.format("VmallocUsed:\t%8d kB\n", vmallocUsed));
		builder.append(String.format("VmallocChunk:\t%8d kB\n", vmallocChunk));
		builder.append(String.format("HugePages_Total:%8d\n", hugePagesTotal));
		builder.append(String.format("HugePages_Free:\t%8d\n", hugePagesFree));
		builder.append(String.format("HugePages_Rsvd:\t%8d\n", hugePagesRsvd));
		builder.append(String.format("HugePages_Surp:\t%8d\n", hugePagesSurp));
		builder.append(String.format("Hugepagesize:\t%8d kB\n", hugePageSize));
		builder.append(String.format("DirectMap4k:\t%8d kB\n", directMap4k));
		builder.append(String.format("DirectMap2M:\t%8d kB\n", directMap2M));

		return builder.toString();
	}

}
