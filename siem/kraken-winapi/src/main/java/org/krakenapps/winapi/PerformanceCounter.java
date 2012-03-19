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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PerformanceCounter {
	static {
		System.loadLibrary("winapi");
	}

	public enum DetailLevel {
		Novice(100), Advanced(200), Expert(300), Wizard(400);

		private int code;

		DetailLevel(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	};

	private int queryHandle;
	private int counterHandle;

	public static native String[] getMachines();

	public static String[] getCategories() {
		return getCategories(null, DetailLevel.Wizard);
	}

	public static String[] getCategories(String machine) {
		return getCategories(machine, DetailLevel.Wizard);
	}

	public static String[] getCategories(DetailLevel detailLevel) {
		return getCategories(null, detailLevel);
	}

	public static String[] getCategories(String machine, DetailLevel detailLevel) {
		return getCategories(machine, detailLevel.getCode());
	}

	private static native String[] getCategories(String machine, int detail);

	public static List<String> getInstances(String category) {
		return Arrays.asList(get(category).get("instances"));
	}

	public static List<String> getInstances(String category, String machine) {
		return Arrays.asList(get(category, machine).get("instances"));
	}

	public static List<String> getInstances(String category, DetailLevel detailLevel) {
		return Arrays.asList(get(category, detailLevel).get("instances"));
	}

	public static List<String> getInstances(String category, String machine, DetailLevel detailLevel) {
		return Arrays.asList(get(category, machine, detailLevel).get("instances"));
	}

	public static List<String> getCounters(String category) {
		return getCounters(category, null, DetailLevel.Wizard);
	}

	public static List<String> getCounters(String category, String machine) {
		return getCounters(category, machine, DetailLevel.Wizard);
	}

	public static List<String> getCounters(String category, DetailLevel detailLevel) {
		return getCounters(category, null, detailLevel);
	}

	public static List<String> getCounters(String category, String machine, DetailLevel detailLevel) {
		return Arrays.asList(get(category, machine, detailLevel).get("counters"));
	}

	private static Map<String, String[]> get(String category) {
		return get(category, null, DetailLevel.Wizard);
	}

	private static Map<String, String[]> get(String category, String machine) {
		return get(category, machine, DetailLevel.Wizard);
	}

	private static Map<String, String[]> get(String category, DetailLevel detailLevel) {
		return get(category, null, detailLevel);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String[]> get(String category, String machine, DetailLevel detailLevel) {
		return (Map<String, String[]>) getCounters(category, machine, detailLevel.getCode());
	}

	private static native Object getCounters(String category, String machine, int detail);

	public PerformanceCounter() {
		this(null, null);
	}

	public PerformanceCounter(String category, String counter) {
		this(category, counter, null);
	}

	public PerformanceCounter(String category, String counter, String instance) {
		this(category, counter, instance, null);
	}

	public PerformanceCounter(String category, String counter, String instance, String machine) {
		queryHandle = open();
		counterHandle = addCounter(queryHandle, category, counter, instance, machine);
		if (queryHandle == 0 || counterHandle == 0)
			throw new IllegalStateException();
	}

	private native int open();

	private native int addCounter(int queryHandle, String category, String counter, String instance, String machine);

	public double nextValue() {
		if (queryHandle == 0 || counterHandle == 0)
			throw new IllegalStateException("Already Closed");

		return nextValue(queryHandle, counterHandle);
	}

	public double nextValue(int interval) throws InterruptedException {
		if (queryHandle == 0 || counterHandle == 0)
			throw new IllegalStateException("Already Closed");

		Thread.sleep(interval);
		return nextValue(queryHandle, counterHandle);
	}

	private native double nextValue(int queryHandle, int counterHandle);

	public void close() {
		if (queryHandle == 0 || counterHandle == 0)
			throw new IllegalStateException("Already Closed");

		close(queryHandle, counterHandle);
		queryHandle = 0;
		counterHandle = 0;
	}

	private native void close(int queryHandle, int counterHandle);

}
