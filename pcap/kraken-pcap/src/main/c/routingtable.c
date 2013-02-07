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
#ifdef WIN32
  #include <Winsock2.h>
#include <IpHlpApi.h>
#include <windows.h>
#else
#include <string.h>
#endif
#include "org_krakenapps_pcap_routing_RoutingTable.h"

#ifdef WIN32

JNIEXPORT jobject JNICALL Java_org_krakenapps_pcap_routing_RoutingTable_getNativeRoutingEntries(JNIEnv *env, jobject obj) {
	jclass clzList = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID listInit = (*env)->GetMethodID(env, clzList, "<init>", "()V");
	jmethodID listAdd = (*env)->GetMethodID(env, clzList, "add", "(Ljava/lang/Object;)Z");
	jobject tables = (*env)->NewObject(env, clzList, listInit);
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

	for(i = 0; i < pIpForwardTable->dwNumEntries; i++) {
		jobject entry = GetRoutingEntry(env, pIpForwardTable->table+i);
		if (entry != NULL) {
			(*env)->CallVoidMethod(env, tables, listAdd, entry);
		}
	}

	free(pIpForwardTable);

	return tables;
}


jobject GetRoutingEntry(JNIEnv *env, MIB_IPFORWARDROW *row) {
	jclass clzRoutingEntry = (*env)->FindClass(env, "org/krakenapps/pcap/routing/RoutingEntry");
	jmethodID routingEntryInit = (*env)->GetMethodID(env, clzRoutingEntry, "<init>", "(Ljava/lang/String;[B[B[BI)V");
	jstring ifname = NULL;
	jbyteArray destination = (*env)->NewByteArray(env, 4);
	jbyteArray subnet = (*env)->NewByteArray(env, 4);
	jbyteArray gateway = (*env)->NewByteArray(env, 4);
	jint ifIndex = row->dwForwardIfIndex;
	jint metric = row->dwForwardMetric1;
    PIP_ADAPTER_ADDRESSES AdapterAddresses = NULL;
    ULONG OutBufferLength = 0;
    ULONG RetVal = 0, i;    
	jobject retobj = 0;

	if (row->dwForwardType == MIB_IPROUTE_TYPE_INVALID)
		return NULL;

	(*env)->SetByteArrayRegion(env, destination, 0, 4, (const jbyte *)&row->dwForwardDest);
	(*env)->SetByteArrayRegion(env, subnet, 0, 4, (const jbyte *)&row->dwForwardMask);
	(*env)->SetByteArrayRegion(env, gateway, 0, 4, (const jbyte *)&row->dwForwardNextHop);
    
    // The size of the buffer can be different 
    // between consecutive API calls.
    // In most cases, i < 2 is sufficient;
    // One call to get the size and one call to get the actual parameters.
    // But if one more interface is added or addresses are added, 
    // the call again fails with BUFFER_OVERFLOW. 
    // So the number is picked slightly greater than 2. 
    // We use i <5 in the example
    for (i = 0; i < 5; i++) {
        RetVal = 
            GetAdaptersAddresses(
                AF_INET, 
                0,
                NULL, 
                AdapterAddresses, 
                &OutBufferLength);
        
        if (RetVal != ERROR_BUFFER_OVERFLOW) {
            break;
        }

        if (AdapterAddresses != NULL) {
            free(AdapterAddresses);
        }
        
        AdapterAddresses = (PIP_ADAPTER_ADDRESSES) 
            malloc(OutBufferLength);
        if (AdapterAddresses == NULL) {
            RetVal = GetLastError();
            break;
        }
    }
    
    if (RetVal == NO_ERROR) {
      // If successful, output some information from the data we received
      PIP_ADAPTER_ADDRESSES AdapterList = AdapterAddresses;
      while (AdapterList) {
		if ( AdapterList->IfIndex == ifIndex )
		{
			// removes "\Device\TCPIP_" prefix
			ifname = (*env)->NewStringUTF(env, AdapterList->AdapterName);
			retobj = (*env)->NewObject(env, clzRoutingEntry, routingEntryInit, ifname, destination, gateway, subnet, metric);
		}
		AdapterList = AdapterList->Next;
      }
    }
    else { 
      LPVOID MsgBuf;
      
      fprintf(stderr, "Call to GetAdaptersAddresses failed.\n");
      if (FormatMessage( 
        FORMAT_MESSAGE_ALLOCATE_BUFFER | 
        FORMAT_MESSAGE_FROM_SYSTEM | 
        FORMAT_MESSAGE_IGNORE_INSERTS,
        NULL,
        RetVal,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
        (LPTSTR) &MsgBuf,
        0,
        NULL )) {
        fprintf(stderr, "\tError: %s", MsgBuf);
      }
      LocalFree(MsgBuf);
    }  

    if (AdapterAddresses != NULL) {
        free(AdapterAddresses);
    }

	return retobj;
}

#else

#define PROC_IPV4_ROUTE_FILE	"/proc/net/route"
#define PROC_IPV6_ROUTE_FILE	"/proc/net/ipv6_route"
#define BUF_SIZE 256

JNIEXPORT jobject JNICALL Java_org_krakenapps_pcap_routing_RoutingTable_getNativeRoutingEntries(JNIEnv *env, jobject obj) {
	jclass clzList = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID listInit = (*env)->GetMethodID(env, clzList, "<init>", "()V");
	jmethodID listAdd = (*env)->GetMethodID(env, clzList, "add", "(Ljava/lang/Object;)Z");
	jobject tables = (*env)->NewObject(env, clzList, listInit);
	FILE *fp;
	char buf[BUF_SIZE];
	jclass clzRoutingEntry = (*env)->FindClass(env, "org/krakenapps/pcap/routing/RoutingEntry");
	jmethodID routingEntryInit = (*env)->GetMethodID(env, clzRoutingEntry, "<init>", "(Ljava/lang/String;[B[B[BI)V");

	if((fp = fopen(PROC_IPV4_ROUTE_FILE, "r")) != NULL) {
		char ifbuf[16];
		int iflags, refcnt, use, metric, mss, win, irtt;
		int dest, gate, mask;

		while(fgets(buf, sizeof(buf), fp) != NULL) {
			int ret;
			jobject entry = NULL;
			jstring iface = NULL;
			jbyteArray destination = (*env)->NewByteArray(env, 4);
			jbyteArray gateway = (*env)->NewByteArray(env, 4);
			jbyteArray subnet = (*env)->NewByteArray(env, 4);

			ret = sscanf(buf, "%16s %X %X %X %d %d %d %X %d %d %d\n",
				ifbuf, &dest, &gate, &iflags, &refcnt, &use, &metric, &mask, &mss, &win, &irtt);

			if(ret < 10) continue;

			iface = (*env)->NewStringUTF(env, (const char *)ifbuf);
			(*env)->SetByteArrayRegion(env, destination, 0, 4, (const jbyte *)&dest);
			(*env)->SetByteArrayRegion(env, gateway, 0, 4, (const jbyte *)&gate);
			(*env)->SetByteArrayRegion(env, subnet, 0, 4, (const jbyte *)&mask);

			entry = (*env)->NewObject(env, clzRoutingEntry, routingEntryInit, iface, destination, gateway, subnet, (jint) metric);
			
			(*env)->CallVoidMethod(env, tables, listAdd, entry);
		}
		
		fclose(fp);
	}

	return tables;
}

#endif // _WIN32
