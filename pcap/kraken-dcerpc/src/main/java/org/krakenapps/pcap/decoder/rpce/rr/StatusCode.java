/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {

	NCA_S_COMM_FAILURE(0x1c010001),
	NCA_S_OP_RNG_ERROR(0x1c010002),
	NCA_S_UNK_IF(0x1c010003),
	NCA_S_WRONG_BOOT_TIME(0x1c010006),
	NCA_S_YOU_CRASHED(0x1c010009),
	NCA_S_PROTO_ERROR(0x1c01000B),
	NCA_S_OUT_ARGS_TOO_BIG(0x1c010013),
	NCA_S_SERVER_TOO_BUSY(0x1c010014),
	NCA_S_FAULT_STRING_TOO_LONG(0x1c010015),
	NCA_S_UNSUPPORTED_TYPE(0x1c010017),
	NCA_S_FAULT_INT_DIV_BY_ZERO(0x1c000001),
	NCA_S_FAULT_ADDR_ERROR(0x1c000002),
	NCA_S_FAULT_FP_DIV_ZERO(0x1c000003),
	NCA_S_FAULT_FP_UNDERFLOW(0x1c000004),
	NCA_S_FAULT_FP_OVERFLOW(0x1c000005),
	NCA_S_FAULT_INVALID_TAG(0x1c000006),
	NCA_S_FAULT_INVALID_BOUND(0x1c000007),
	NCA_S_FAULT_VERSION_MISMATCH(0x1c000008),
	NCA_S_UNSPEC_REJECT(0x1c000009),
	NCA_S_BAD_ACTID(0x1c00000A),
	NCA_S_WHO_ARE_YOU_FAILED(0x1c00000B),
	NCA_S_MANAGER_NOT_ENTERED(0x1c00000C),
	NCA_S_FAULT_CANCEL(0x1c00000D),
	NCA_S_FAULT_ILL_INST(0x1c00000E),
	NCA_S_FAULT_FP_ERROR(0x1c00000F),
	NCA_S_FAULT_INT_OVERFLOW(0x1c000010),
	NCA_S_FAULT_UNSPEC(0x1c000012),
	NCA_S_FAULT_REMOTE_COMM_FAILURE(0x1c000013),
	NCA_S_FAULT_PIPE_EMPTY(0x1c000014),
	NCA_S_FAULT_PIPE_CLOSED(0x1c000015),
	NCA_S_FAULT_PIPE_ORDER(0x1c000016),
	NCA_S_FAULT_PIPE_DISCIPLINE(0x1c000017),
	NCA_S_FAULT_PIPE_COMM_ERROR(0x1c000018),
	NCA_S_FAULT_PIPE_MEMORY(0x1c000019),
	NCA_S_FAULT_CONTEXT_MISMATCH(0x1c00001A),
	NCA_S_FAULT_REMOTE_NO_MEMORY(0x1c00001B),
	NCA_S_INVALID_PRES_CONTEXT_ID(0x1c00001C),
	NCA_S_UNSUPPORTED_AUTHN_LEVEL(0x1c00001D),
	NCA_S_INVALID_CHECKSUM(0x1c00001F),
	NCA_S_INVALID_CRC(0x1c000020),
	NCA_S_FAULT_USER_DEFINED(0x1c000021),
	NCA_S_FAULT_TX_OPEN_FAILED(0x1c000022),
	NCA_S_FAULT_CODESET_CONV_ERROR(0x1c000023),
	NCA_S_FAULT_OBJECT_NOT_FOUND(0x1c000024),
	NCA_S_FAULT_NO_CLIENT_STUB(0x1c000025);
	private int code;
	StatusCode(int code) {
		this.code = code;
	}
	static Map<Integer, StatusCode> codeMap = new HashMap<Integer, StatusCode>();
	static{
		for(StatusCode code : StatusCode.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static StatusCode parse(int code){
		return codeMap.get(code);
	}
}
