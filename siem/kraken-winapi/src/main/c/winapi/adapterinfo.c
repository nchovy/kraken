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
#include "org_krakenapps_winapi_AdapterInfo.h"

JNIEXPORT jobject JNICALL Java_org_krakenapps_winapi_AdapterInfo_nativeGetAdapterInfos(JNIEnv *env, jobject obj) {
	jclass clzArrayList = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID arrayListInit = (*env)->GetMethodID(env, clzArrayList, "<init>", "()V");
	jmethodID arrayListAdd = (*env)->GetMethodID(env, clzArrayList, "add", "(Ljava/lang/Object;)Z");
	jobject list = (*env)->NewObject(env, clzArrayList, arrayListInit);

	PIP_ADAPTER_INFO pAdapterInfo = NULL;
	ULONG ulOutBufLen = sizeof(IP_ADAPTER_INFO);
	DWORD dwRetVal = 0;

	pAdapterInfo = (PIP_ADAPTER_INFO) malloc (ulOutBufLen);
	if(pAdapterInfo == NULL)
		return NULL;

	if(GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
		free(pAdapterInfo);
		pAdapterInfo = (PIP_ADAPTER_INFO) malloc (ulOutBufLen);
		if(pAdapterInfo == NULL)
			return NULL;
	}

	dwRetVal = GetAdaptersInfo(pAdapterInfo, &ulOutBufLen);
	if(dwRetVal == NO_ERROR) {
		PIP_ADAPTER_INFO info = NULL;

		for(info=pAdapterInfo; info; info=info->Next)
			(*env)->CallVoidMethod(env, list, arrayListAdd, getJavaObject(env, info));
	} else {
		return NULL;
	}

	return list;
}

jobject getJavaObject(JNIEnv *env, PIP_ADAPTER_INFO info) {
	jclass clzAdapterInfo = (*env)->FindClass(env, "org/krakenapps/winapi/AdapterInfo");
	jmethodID adapterInfoInit = (*env)->GetMethodID(env, clzAdapterInfo, "<init>", "(Ljava/lang/String;Ljava/lang/String;[BIIZZ)V");
	jstring name = (*env)->NewStringUTF(env, info->AdapterName);
	jstring description = (*env)->NewStringUTF(env, info->Description);
	jbyteArray address = (*env)->NewByteArray(env, info->AddressLength);
	jint index = (jint) info->Index;
	jint type = 0;
	jboolean dhcpEnabled = (jboolean) info->DhcpEnabled;
	jboolean haveWins = (jboolean) info->HaveWins;

	(*env)->SetByteArrayRegion(env, address, 0, info->AddressLength, info->Address);

	switch(info->Type) {
	case MIB_IF_TYPE_ETHERNET:
		type = 1; break;
	case MIB_IF_TYPE_TOKENRING:
		type = 2; break;
	case MIB_IF_TYPE_FDDI:
		type = 3; break;
	case MIB_IF_TYPE_PPP:
		type = 4; break;
	case MIB_IF_TYPE_LOOPBACK:
		type = 5; break;
	case MIB_IF_TYPE_SLIP:
		type = 6; break;
	default:
		type = 0; break;
	}

	return (*env)->NewObject(env, clzAdapterInfo, adapterInfoInit, name, description, address, index, type, dhcpEnabled, haveWins);
}