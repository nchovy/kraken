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
#include "org_krakenapps_winapi_ArpCache.h"

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_ArpCache_getArpEntries(JNIEnv *env, jobject obj) {
	jclass clzArpEntry = (*env)->FindClass(env, "org/krakenapps/winapi/ArpEntry");
	jmethodID arpCacheInit = (*env)->GetMethodID(env, clzArpEntry, "<init>", "(I[B[BLjava/lang/String;)V");
	jobjectArray cache = NULL;
	PMIB_IPNETTABLE pIpNetTable = NULL;
	DWORD dwSize = 0;
	WORD i;

	GetIpNetTable(NULL, &dwSize, TRUE);
	if(dwSize == 0) {
		fprintf(stderr, "Error in GetIpNetTable\n");
		return 0;
	}
	pIpNetTable = (PMIB_IPNETTABLE)malloc(dwSize);
	GetIpNetTable(pIpNetTable, &dwSize, TRUE);

	cache = (*env)->NewObjectArray(env, pIpNetTable->dwNumEntries, clzArpEntry, NULL);
	for(i=0; i<pIpNetTable->dwNumEntries; i++) {
		MIB_IPNETROW row = pIpNetTable->table[i];
		jobject c = NULL;
		jint index = row.dwIndex;
		jbyteArray physAddr = (*env)->NewByteArray(env, row.dwPhysAddrLen);
		jbyteArray addr = (*env)->NewByteArray(env, 4);
		jstring type = NULL;
		const char *t = "Other";

		(*env)->SetByteArrayRegion(env, physAddr, 0, row.dwPhysAddrLen, row.bPhysAddr);
		(*env)->SetByteArrayRegion(env, addr, 0, 4, (const jbyte *)&row.dwAddr);
		switch(row.dwType) {
			case MIB_IPNET_TYPE_STATIC:
				t = "Static"; break;
			case MIB_IPNET_TYPE_DYNAMIC:
				t = "Dynamic"; break;
			case MIB_IPNET_TYPE_INVALID:
				t = "Invalid"; break;
		}
		type = (*env)->NewStringUTF(env, t);

		c = (*env)->NewObject(env, clzArpEntry, arpCacheInit, index, physAddr, addr, type);
		(*env)->SetObjectArrayElement(env, cache, i, c);
	}
	free(pIpNetTable);

	return cache;
}