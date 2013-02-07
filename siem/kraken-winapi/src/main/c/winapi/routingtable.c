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
#include <IpHlpApi.h>
#include "org_krakenapps_winapi_RoutingTable.h"

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_RoutingTable_getRoutingEntries(JNIEnv *env, jobject obj) {
	jclass clzRoutingEntry = (*env)->FindClass(env, "org/krakenapps/winapi/RoutingEntry");
	jobjectArray entries = NULL;
	PMIB_IPFORWARDTABLE pIpForwardTable = NULL;
	ULONG dwSize = 0;
	DWORD error = 0;
	DWORD i;

	pIpForwardTable = (PMIB_IPFORWARDTABLE)malloc(sizeof(MIB_IPFORWARDTABLE));
	if((error = GetIpForwardTable(pIpForwardTable, &dwSize, TRUE)) != NO_ERROR) {
		LPTSTR errorMsg = L"Error in GetIpForwardTable";
		free(pIpForwardTable);

		switch(error) {
			case ERROR_INSUFFICIENT_BUFFER:
				pIpForwardTable = (PMIB_IPFORWARDTABLE)malloc(dwSize);
				if((error = GetIpForwardTable(pIpForwardTable, &dwSize, TRUE)) == NO_ERROR) {
					break;
				}

			case ERROR_INVALID_PARAMETER:
				errorMsg = L"Invalid parameter error in GetIpForwardTable";

			case ERROR_NO_DATA:
				errorMsg = L"No data error in GetIpForwardTable";

			case ERROR_NOT_SUPPORTED:
				errorMsg = L"Not supported error in GetIpForwardTable";

			default:
				fwprintf(stderr, L"%s\n", errorMsg);
				return NULL;
		}
	}

	entries = (*env)->NewObjectArray(env, pIpForwardTable->dwNumEntries, clzRoutingEntry, NULL);
	for(i=0; i<pIpForwardTable->dwNumEntries; i++) {
		(*env)->SetObjectArrayElement(env, entries, i, getForwardRow(env, pIpForwardTable->table[i]));
	}
	free(pIpForwardTable);

	return entries;
}

jobject getForwardRow(JNIEnv *env, MIB_IPFORWARDROW row) {
	jclass clzRoutingEntry = (*env)->FindClass(env, "org/krakenapps/winapi/RoutingEntry");
	jmethodID routingEntryInit = (*env)->GetMethodID(env, clzRoutingEntry, "<init>", "([B[BII[BILjava/lang/String;Ljava/lang/String;IIIIII)V");
	jbyteArray destination = (*env)->NewByteArray(env, 4);
	jbyteArray subnet = (*env)->NewByteArray(env, 4);
	jint policy = row.dwForwardPolicy;
	jint nextHop = row.dwForwardNextHop;
	jbyteArray interfaceAddress = (*env)->NewByteArray(env, 4);
	jint ifIndex = row.dwForwardIfIndex;
	jstring type = NULL;
	jstring protocol = NULL;
	jint age = row.dwForwardAge;
	jint metric1 = row.dwForwardMetric1;
	jint metric2 = row.dwForwardMetric2;
	jint metric3 = row.dwForwardMetric3;
	jint metric4 = row.dwForwardMetric4;
	jint metric5 = row.dwForwardMetric5;
	const char *temp = NULL;

	(*env)->SetByteArrayRegion(env, destination, 0, 4, (const jbyte *)&row.dwForwardDest);
	(*env)->SetByteArrayRegion(env, subnet, 0, 4, (const jbyte *)&row.dwForwardMask);
	(*env)->SetByteArrayRegion(env, interfaceAddress, 0, 4, (const jbyte *)&row.dwForwardNextHop);
	
	temp = "Other";
	switch(row.dwForwardType) {
		case MIB_IPROUTE_TYPE_DIRECT:
			temp = "Direct"; break;
		case MIB_IPROUTE_TYPE_INDIRECT:
			temp = "Indirect"; break;
		case MIB_IPROUTE_TYPE_INVALID:
			temp = "Invalid"; break;
	}
	type = (*env)->NewStringUTF(env, temp);

	temp = "Other";
	switch(row.dwForwardProto) {
		case MIB_IPPROTO_LOCAL:
			temp = "Local"; break;
		case MIB_IPPROTO_NETMGMT:
			temp = "NetMgmt"; break;
		case MIB_IPPROTO_ICMP:
			temp = "ICMP"; break;
		case MIB_IPPROTO_EGP:
			temp = "EGP"; break;
		case MIB_IPPROTO_GGP:
			temp = "GGP"; break;
		case MIB_IPPROTO_HELLO:
			temp = "Hello"; break;
		case MIB_IPPROTO_RIP:
			temp = "RIP"; break;
		case MIB_IPPROTO_IS_IS:
			temp = "IS_IS"; break;
		case MIB_IPPROTO_ES_IS:
			temp = "ES_IS"; break;
		case MIB_IPPROTO_CISCO:
			temp = "IGRP"; break;
		case MIB_IPPROTO_BBN:
			temp = "BBN"; break;
		case MIB_IPPROTO_OSPF:
			temp = "OSPF"; break;
		case MIB_IPPROTO_BGP:
			temp = "BGP"; break;
		case MIB_IPPROTO_NT_AUTOSTATIC:
			temp = "Autostatic"; break;
		case MIB_IPPROTO_NT_STATIC:
			temp = "Static"; break;
		case MIB_IPPROTO_NT_STATIC_NON_DOD:
			temp = "StaticNonDOD"; break;
	}
	protocol = (*env)->NewStringUTF(env, temp);

	return (*env)->NewObject(env, clzRoutingEntry, routingEntryInit, destination, subnet, policy, nextHop, interfaceAddress, ifIndex, type, protocol, age, metric1, metric2, metric3, metric4, metric5);
}