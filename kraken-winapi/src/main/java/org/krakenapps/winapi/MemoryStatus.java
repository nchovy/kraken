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
package org.krakenapps.winapi;

public class MemoryStatus {
	static {
		System.loadLibrary("winapi");
	}

	private long totalPhysical;
	private long availablePhysical;
	private long totalPageFile;
	private long availablePageFile;
	private long totalVirtual;
	private long availableVirtual;

	public MemoryStatus() {
		getMemoryStatus(this);
	}

	private native void getMemoryStatus(MemoryStatus stats);

	public long getTotalPhysical() {
		return totalPhysical;
	}

	public long getAvailablePhysical() {
		return availablePhysical;
	}

	public long getTotalPageFile() {
		return totalPageFile;
	}

	public long getAvailablePageFile() {
		return availablePageFile;
	}

	public long getTotalVirtual() {
		return totalVirtual;
	}

	public long getAvailableVirtual() {
		return availableVirtual;
	}

	@Override
	public String toString() {
		return String.format("Physical: %d/%d, PageFile: %d/%d, Virtual: %d/%d", availablePhysical, totalPhysical,
				availablePageFile, totalPageFile, availableVirtual, totalVirtual);
	}
}
