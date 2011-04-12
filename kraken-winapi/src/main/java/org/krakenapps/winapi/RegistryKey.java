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

import java.nio.charset.Charset;
import java.util.Date;

import org.krakenapps.winapi.impl.ObjectRef;
import org.krakenapps.winapi.impl.IntegerRef;

public class RegistryKey {
	static {
		System.loadLibrary("winapi");
	}

	private int hkey;

	public static RegistryKey currentUser() {
		return new RegistryKey(0x80000001);
	}

	public static RegistryKey localMachine() {
		return new RegistryKey(0x80000002);
	}

	public static RegistryKey users() {
		return new RegistryKey(0x80000003);
	}

	public static RegistryKey performanceData() {
		return new RegistryKey(0x80000004);
	}

	private RegistryKey(int hkey) {
		this.hkey = hkey;
	}

	public int getSubKeyCount() {
		QueryInfo info = queryInfoKey();
		return info.subKeyCount;
	}

	public int getValueCount() {
		QueryInfo info = queryInfoKey();
		return info.valuesCount;
	}

	public String[] getSubKeyNames() {
		QueryInfo info = queryInfoKey();
		ObjectRef name = new ObjectRef();
		ObjectRef lastWriteTime = new ObjectRef();

		String[] names = new String[info.subKeyCount];
		for (int i = 0; i < info.subKeyCount; i++) {
			RegEnumKeyEx(hkey, i, name, info.maxSubKeyLen, lastWriteTime);
			names[i] = (String) name.value;
		}
		return names;
	}

	public String[] getValueNames() {
		QueryInfo info = queryInfoKey();
		ObjectRef name = new ObjectRef();

		String[] names = new String[info.valuesCount];
		for (int i = 0; i < info.valuesCount; i++) {
			RegEnumValue(hkey, i, name, info.maxValueNameLen);
			names[i] = (String) name.value;
		}
		return names;
	}

	private QueryInfo queryInfoKey() {
		IntegerRef subKeyCount = new IntegerRef();
		IntegerRef maxSubKeyLen = new IntegerRef();
		IntegerRef maxClassLen = new IntegerRef();
		IntegerRef valuesCount = new IntegerRef();
		IntegerRef maxValueNameLen = new IntegerRef();
		IntegerRef maxValueLen = new IntegerRef();
		IntegerRef securityDescriptorSize = new IntegerRef();
		ObjectRef lastWriteTime = new ObjectRef();
		RegQueryInfoKey(hkey, subKeyCount, maxSubKeyLen, maxClassLen, valuesCount, maxValueNameLen, maxValueLen,
				securityDescriptorSize, lastWriteTime);

		QueryInfo i = new QueryInfo();
		i.subKeyCount = subKeyCount.value;
		i.maxSubKeyLen = maxSubKeyLen.value;
		i.maxClassLen = maxClassLen.value;
		i.valuesCount = valuesCount.value;
		i.maxValueNameLen = maxValueNameLen.value;
		i.maxValueLen = maxValueLen.value;
		i.securityDescriptorSize = securityDescriptorSize.value;
		return i;
	}

	public Object getValue(String name) {
		IntegerRef type = new IntegerRef();
		ObjectRef buffer = new ObjectRef();
		RegQueryValueEx(hkey, name, type, buffer);
		byte[] b = (byte[]) buffer.value;

		if (type.value == 0) {
			// REG_NONE
			return null;
		} else if (type.value == 1) {
			// REG_SZ
			return new String(b, 0, b.length - 2, Charset.forName("UTF-16LE"));
		} else if (type.value == 2) {
			// REG_EXPAND_SZ
			return new String(b, 0, b.length - 2, Charset.forName("UTF-16LE"));
		} else if (type.value == 3) {
			// REG_BINARY
			return b;
		} else if (type.value == 4) {
			// REG_DWORD
			return ((b[3] << 24) & 0xFF000000) | ((b[2] << 16) & 0xFF0000) | ((b[1] << 8) & 0xFF00) | (b[0] & 0xFF);
		} else if (type.value == 5) {
			// REG_DWORD_BIG_ENDIAN
			return ((b[0] << 24) & 0xFF000000) | ((b[1] << 16) & 0xFF0000) | ((b[2] << 8) & 0xFF00) | (b[3] & 0xFF);
		} else if (type.value == 6) {
			// REG_LINK
		} else if (type.value == 7) {
			// REG_MULTI_SZ
		} else if (type.value == 8) {
			// REG_RESOURCE_LIST
		} else if (type.value == 9) {
			// REG_FULL_RESOURCE_DESCRIPTOR
		} else if (type.value == 10) {
			// REG_RESOURCE_REQUIREMENTS_LIST
		} else if (type.value == 11) {
			// QWORD
		}
		return new UnsupportedOperationException("reg type: " + type.value);
	}

	public RegistryKey openSubKey(String path) {
		IntegerRef ref = new IntegerRef();
		RegOpenKeyEx(hkey, path, 0, ref);
		return new RegistryKey(ref.value);
	}

	public void close() {
		RegCloseKey(hkey);
	}

	@Override
	public String toString() {
		return "0x" + Integer.toHexString(hkey);
	}

	private native int RegOpenKeyEx(int hkey, String subKey, int samDesired, IntegerRef hkeyResult);

	private native int RegQueryValueEx(int hkey, String valueName, IntegerRef type, ObjectRef buffer);

	private native int RegCloseKey(int hkey);

	private native int RegQueryInfoKey(int hkey, IntegerRef subKeyCount, IntegerRef maxSubKeyLen,
			IntegerRef maxClassLen, IntegerRef valuesCount, IntegerRef maxValueNameLen, IntegerRef maxValueLen,
			IntegerRef securityDescriptorSize, ObjectRef lastWriteTime);

	private native int RegEnumKeyEx(int hkey, int index, ObjectRef name, int maxNameLen, ObjectRef lastWriteTime);

	private native int RegEnumValue(int hkey, int index, ObjectRef name, int maxNameLen);

	private static class QueryInfo {
		int subKeyCount;
		int maxSubKeyLen;
		int maxClassLen;
		int valuesCount;
		int maxValueNameLen;
		int maxValueLen;
		int securityDescriptorSize;
		Date lastWriteTime;
	}
}
