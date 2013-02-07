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
#include <windows.h>
#include <iphlpapi.h>
#include "org_krakenapps_winapi_IpGlobalProperties.h"

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_IpGlobalProperties_getTcpConnections(JNIEnv *env, jobject obj, jboolean isIpv4) {
	jclass clzTcpStat = (*env)->FindClass(env, "org/krakenapps/winapi/TcpConnectionInformation");
	jmethodID tcpStatInit = (*env)->GetMethodID(env, clzTcpStat, "<init>", "([BII[BIILjava/lang/String;I)V");
	jobjectArray stats = NULL;
	DWORD dwSize = sizeof (MIB_TCPTABLE_OWNER_PID);
	PMIB_TCPTABLE_OWNER_PID tcpTable = (PMIB_TCPTABLE_OWNER_PID) malloc(dwSize);
	DWORD ret = NO_ERROR;
	jsize addrSize = isIpv4 ? 4 : 16;
	WORD i;

	ret = GetExtendedTcpTable(tcpTable, &dwSize, TRUE, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0);
	if(ret == ERROR_INSUFFICIENT_BUFFER) {
		free(tcpTable);
		tcpTable = (PMIB_TCPTABLE_OWNER_PID) malloc(dwSize);
		ret = GetExtendedTcpTable(tcpTable, &dwSize, TRUE, isIpv4 ? AF_INET : AF_INET6, TCP_TABLE_OWNER_PID_ALL, 0);
	}
	if(ret != NO_ERROR)
		return (*env)->NewObjectArray(env, 0, clzTcpStat, NULL);

	stats = (*env)->NewObjectArray(env, tcpTable->dwNumEntries, clzTcpStat, NULL);
	for(i=0; i<tcpTable->dwNumEntries; i++) {
		jobject stat = NULL;
		jbyteArray localAddr = (*env)->NewByteArray(env, addrSize);
		jint localPort = 0;
		jbyteArray remoteAddr = (*env)->NewByteArray(env, addrSize);
		jint remotePort = 0;
		jstring state = NULL;
		jint pid = tcpTable->table[i].dwOwningPid;
		char *stateStr = NULL;

		(*env)->SetByteArrayRegion(env, localAddr, 0, addrSize, (const jbyte *)&(tcpTable->table[i].dwLocalAddr));
		localPort = ((tcpTable->table[i].dwLocalPort & 0xff) << 8) | ((tcpTable->table[i].dwLocalPort & 0xff00) >> 8);

		(*env)->SetByteArrayRegion(env, remoteAddr, 0, addrSize, (const jbyte *)&(tcpTable->table[i].dwRemoteAddr));
		remotePort = ((tcpTable->table[i].dwRemotePort & 0xff) << 8) | ((tcpTable->table[i].dwRemotePort & 0xff00) >> 8);

		stateStr = "Unknown";
		switch(tcpTable->table[i].dwState) {
			case MIB_TCP_STATE_CLOSED:
				stateStr = "Closed"; break;
			case MIB_TCP_STATE_LISTEN:
				stateStr = "Listen"; break;
			case MIB_TCP_STATE_SYN_SENT:
				stateStr = "SynSent"; break;
			case MIB_TCP_STATE_SYN_RCVD:
				stateStr = "SynReceived"; break;
			case MIB_TCP_STATE_ESTAB:
				stateStr = "Established"; break;
			case MIB_TCP_STATE_FIN_WAIT1:
				stateStr = "FinWait1"; break;
			case MIB_TCP_STATE_FIN_WAIT2:
				stateStr = "FinWait2"; break;
			case MIB_TCP_STATE_CLOSE_WAIT:
				stateStr = "CloseWait"; break;
			case MIB_TCP_STATE_CLOSING:
				stateStr = "Closing"; break;
			case MIB_TCP_STATE_LAST_ACK:
				stateStr = "LastACK"; break;
			case MIB_TCP_STATE_TIME_WAIT:
				stateStr = "TimeWait"; break;
			case MIB_TCP_STATE_DELETE_TCB:
				stateStr = "DeleteTCB"; break;
		}
		state = (*env)->NewStringUTF(env, stateStr);

		stat = (*env)->NewObject(env, clzTcpStat, tcpStatInit, localAddr, 0, localPort, remoteAddr, 0, remotePort, state, pid);
		(*env)->SetObjectArrayElement(env, stats, i, stat);
	}

	free(tcpTable);

	return stats;
}

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_IpGlobalProperties_getUdpListeners(JNIEnv *env, jobject obj, jboolean isIpv4) {
	jclass clzUdpStat = (*env)->FindClass(env, "org/krakenapps/winapi/UdpListenerInformation");
	jmethodID udpStatInit = (*env)->GetMethodID(env, clzUdpStat, "<init>", "([BIII)V");
	jobjectArray stats = NULL;
	PMIB_UDPTABLE_OWNER_PID udpTable = NULL;
	DWORD dwSize = 0;
	jsize addrSize = isIpv4 ? 4 : 16;
	WORD i;

	GetExtendedUdpTable(NULL, &dwSize, TRUE, isIpv4 ? AF_INET : AF_INET6, UDP_TABLE_OWNER_PID, 0);
	if(dwSize == 0) {
		fprintf(stderr, "Error in GetExtendedUdpTable\n");
		return (*env)->NewObjectArray(env, 0, clzUdpStat, NULL);
	}
	udpTable = (PMIB_UDPTABLE_OWNER_PID)malloc(dwSize);
	GetExtendedUdpTable(udpTable, &dwSize, TRUE, AF_INET, UDP_TABLE_OWNER_PID, 0);

	stats = (*env)->NewObjectArray(env, udpTable->dwNumEntries, clzUdpStat, NULL);
	for(i=0; i<udpTable->dwNumEntries; i++) {
		MIB_UDPROW_OWNER_PID row = udpTable->table[i];
		jobject stat = NULL;
		jbyteArray addr = (*env)->NewByteArray(env, addrSize);
		jint port = 0;
		jint pid = row.dwOwningPid;

		(*env)->SetByteArrayRegion(env, addr, 0, addrSize, (const jbyte *)&row.dwLocalAddr);
		port = ((row.dwLocalPort & 0xff) << 8) | ((row.dwLocalPort & 0xff00) >> 8);

		stat = (*env)->NewObject(env, clzUdpStat, udpStatInit, addr, 0, port, pid);
		(*env)->SetObjectArrayElement(env, stats, i, stat);
	}
	free(udpTable);

	return stats;
}