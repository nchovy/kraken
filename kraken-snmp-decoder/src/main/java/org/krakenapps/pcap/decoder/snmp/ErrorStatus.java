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
package org.krakenapps.pcap.decoder.snmp;

public enum ErrorStatus {
	NoError(0), TooBig(1), NoSuchName(2), BadValue(3), ReadOnly(4), GenErr(5), NoAccess(6), WrongType(7), WrongLength(8), WrongEncoding(
			9), WrongValue(10), NoCreation(11), InconsistentValue(12), ResourceUnavailable(13), CommitFailed(14), UndoFailed(
			15), AuthorizationError(16), NotWritable(17), InconsistentName(18), Unknown(-1);

	private ErrorStatus(int code) {
		this.code = code;
	}

	public static ErrorStatus parse(int code) {
		switch (code) {
		case 0:
			return NoError;
		case 1:
			return TooBig;
		case 2:
			return NoSuchName;
		case 3:
			return BadValue;
		case 4:
			return ReadOnly;
		case 5:
			return GenErr;
		case 6:
			return NoAccess;
		case 7:
			return WrongType;
		case 8:
			return WrongLength;
		case 9:
			return WrongEncoding;
		case 10:
			return WrongValue;
		case 11:
			return NoCreation;
		case 12:
			return InconsistentValue;
		case 13:
			return ResourceUnavailable;
		case 14:
			return CommitFailed;
		case 15:
			return UndoFailed;
		case 16:
			return AuthorizationError;
		case 17:
			return NotWritable;
		case 18:
			return InconsistentName;
		default:
			return Unknown;
		}
	}

	public int getCode() {
		return code;
	}

	private int code;
}
