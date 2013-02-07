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
#include <pdh.h>
#include "org_krakenapps_winapi_PerformanceCounter.h"

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getMachines(JNIEnv *env, jobject obj) {
	jobjectArray machineList = NULL;
	LPTSTR lpMachineNameList = NULL;
	DWORD dwBufferLength = 0;
	PDH_STATUS stat = 0;

	PdhEnumMachines(NULL, NULL, &dwBufferLength);
	if(dwBufferLength == 0) {
		fprintf(stderr, "Error in PdhEnumMachines\n");
		return NULL;
	}

	lpMachineNameList = (LPTSTR)malloc(sizeof(TCHAR)*dwBufferLength);
	stat = PdhEnumMachines(NULL, lpMachineNameList, &dwBufferLength);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhEnumMachines: 0x%x\n", stat);
		return NULL;
	}

	machineList = convertStringArray(env, lpMachineNameList, dwBufferLength);

	free(lpMachineNameList);

	return machineList;
}

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getCategories(JNIEnv *env, jobject obj, jstring machine, jint detail) {
	jobjectArray categoryList = NULL;
	LPTSTR machineName = machine ? (LPTSTR)(*env)->GetStringChars(env, machine, JNI_FALSE) : NULL;
	LPTSTR lpCategoryNameList = NULL;
	DWORD dwBufferLength = 0;
	PDH_STATUS stat = 0;

	if(machineName) {
		stat = PdhConnectMachine(machineName);
		if(stat != ERROR_SUCCESS) {
			fprintf(stderr, "Error in PdhConnectMachine:, 0x%x\n", stat);
		if(machineName)
			(*env)->ReleaseStringChars(env, machine, machineName);
			return NULL;
		}
	}

	PdhEnumObjects(NULL, machineName, NULL, &dwBufferLength, detail, TRUE);
	if(dwBufferLength == 0) {
		fprintf(stderr, "Error in PdhEnumObjects\n");
		return NULL;
	}

	lpCategoryNameList = (LPTSTR)malloc(sizeof(TCHAR)*dwBufferLength);
	stat = PdhEnumObjects(NULL, machineName, lpCategoryNameList, &dwBufferLength, detail, TRUE);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhEnumObjects: 0x%x\n", stat);
		return NULL;
	}
	if(machineName)
		(*env)->ReleaseStringChars(env, machine, machineName);

	categoryList = convertStringArray(env, lpCategoryNameList, dwBufferLength);

	free(lpCategoryNameList);

	return categoryList;
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getCounters(JNIEnv *env, jobject obj, jstring category, jstring machine, jint detail) {
	jclass clzHashMap = (*env)->FindClass(env, "java/util/HashMap");
	jmethodID hashMapInit = (*env)->GetMethodID(env, clzHashMap, "<init>", "()V");
	jmethodID hashMapPut = (*env)->GetMethodID(env, clzHashMap, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	jobject counter = NULL;
	LPTSTR categoryName = (LPTSTR)(*env)->GetStringChars(env, category, JNI_FALSE);
	LPTSTR machineName = machine ? (LPTSTR)(*env)->GetStringChars(env, machine, JNI_FALSE) : NULL;
	jobjectArray counters = NULL;
	jobjectArray instances = NULL;
	LPTSTR lpCounterList = NULL;
	DWORD dwCounterListLength = 0;
	LPTSTR lpInstanceList = NULL;
	DWORD dwInstanceListLength = 0;
	DWORD stat = 0;

	if(machineName) {
		stat = PdhConnectMachine(machineName);
		if(stat != ERROR_SUCCESS) {
			fprintf(stderr, "Error in PdhConnectMachine:, 0x%x\n", stat);
		if(machineName)
			(*env)->ReleaseStringChars(env, machine, machineName);
			return NULL;
		}
	}

	PdhEnumObjectItems(NULL, machineName, categoryName, NULL, &dwCounterListLength, NULL, &dwInstanceListLength, detail, 0);

	lpCounterList = (LPTSTR)malloc(sizeof(TCHAR)*dwCounterListLength);
	lpInstanceList = (LPTSTR)malloc(sizeof(TCHAR)*dwInstanceListLength);
	stat = PdhEnumObjectItems(NULL, machineName, categoryName, lpCounterList, &dwCounterListLength, lpInstanceList, &dwInstanceListLength, detail, 0);
	(*env)->ReleaseStringChars(env, category, categoryName);
	if(machineName)
		(*env)->ReleaseStringChars(env, machine, machineName);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhEnumObjectItems\n");
		return NULL;
	}

	counter = counter = (*env)->NewObject(env, clzHashMap, hashMapInit);
	counters = convertStringArray(env, lpCounterList, dwCounterListLength);
	instances = convertStringArray(env, lpInstanceList, dwInstanceListLength);

	free(lpCounterList);
	free(lpInstanceList);

	(*env)->CallObjectMethod(env, counter, hashMapPut, (*env)->NewStringUTF(env, "counters"), counters);
	(*env)->CallObjectMethod(env, counter, hashMapPut, (*env)->NewStringUTF(env, "instances"), instances);

	return counter;
}

jobjectArray convertStringArray(JNIEnv *env, LPTSTR source, DWORD dwLength) {
	jclass clzString = (*env)->FindClass(env, "java/lang/String");
	jobjectArray ary = NULL;
	LPTSTR tempStr = NULL;
	DWORD strCount = 0;
	DWORD i;

	for(tempStr=source; tempStr<source+dwLength;) {
		if(wcslen(tempStr) > 0)
			strCount++;
		if(strCount == 109)
			fwprintf(stderr, L"%s\n", tempStr);
		tempStr += wcslen(tempStr) + 1;
	}

	ary = (*env)->NewObjectArray(env, strCount, clzString, NULL);
	tempStr = source;
	for(i=0; i<strCount; i++) {
		jstring str = NULL;

		str = (*env)->NewString(env, tempStr, (jsize)wcslen(tempStr));
		(*env)->SetObjectArrayElement(env, ary, i, str);
		do {
			tempStr += wcslen(tempStr) + 1;
		} while(wcslen(tempStr) == 0 && tempStr < source+dwLength);
	}

	return ary;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_PerformanceCounter_open(JNIEnv *env, jobject obj) {
	PDH_HQUERY phQuery = NULL;
	PDH_STATUS stat = 0;

	stat = PdhOpenQuery(NULL, 0, &phQuery);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhOpenQuery: 0x%x\n", stat);
		return 0;
	}

	return (jint)phQuery;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_PerformanceCounter_addCounter(JNIEnv *env, jobject obj, jint queryHandle, jstring category, jstring counter, jstring instance, jstring machine) {
	PDH_HCOUNTER phCounter = NULL;
	PDH_HQUERY phQuery = (PDH_HQUERY)queryHandle;
	LPTSTR counterPath = NULL;
	PDH_COUNTER_PATH_ELEMENTS pathElement;
	DWORD dwSize = 0;
	PDH_STATUS stat = 0;
	jboolean isCopy = JNI_FALSE;

	memset(&pathElement, 0, sizeof(pathElement));
	pathElement.szObjectName = category ? (LPTSTR)(*env)->GetStringChars(env, category, &isCopy) : NULL;
	pathElement.szCounterName = counter ? (LPTSTR)(*env)->GetStringChars(env, counter, &isCopy) : NULL;
	pathElement.szInstanceName = instance ? (LPTSTR)(*env)->GetStringChars(env, instance, &isCopy) : NULL;
	pathElement.szMachineName = machine ? (LPTSTR)(*env)->GetStringChars(env, machine, &isCopy) : NULL;

	if(pathElement.szMachineName) {
		stat = PdhConnectMachine(pathElement.szMachineName);
		if(stat != ERROR_SUCCESS) {
			fprintf(stderr, "Error in PdhConnectMachine:, 0x%x\n", stat);
		if(pathElement.szMachineName)
			(*env)->ReleaseStringChars(env, category, pathElement.szObjectName);
			(*env)->ReleaseStringChars(env, counter, pathElement.szCounterName);
			(*env)->ReleaseStringChars(env, instance, pathElement.szInstanceName);
			(*env)->ReleaseStringChars(env, machine, pathElement.szMachineName);
			return 0;
		}
	}

	PdhMakeCounterPath(&pathElement, NULL, &dwSize, 0);
	if(dwSize == 0) {
		fprintf(stderr, "Error in PdhMakeCounterPath\n");
		(*env)->ReleaseStringChars(env, category, pathElement.szObjectName);
		(*env)->ReleaseStringChars(env, counter, pathElement.szCounterName);
		(*env)->ReleaseStringChars(env, instance, pathElement.szInstanceName);
		(*env)->ReleaseStringChars(env, machine, pathElement.szMachineName);
		return 0;
	}

	counterPath = (LPTSTR)malloc(sizeof(TCHAR)*dwSize);
	stat = PdhMakeCounterPath(&pathElement, counterPath, &dwSize, 0);
	(*env)->ReleaseStringChars(env, category, pathElement.szObjectName);
	(*env)->ReleaseStringChars(env, counter, pathElement.szCounterName);
	(*env)->ReleaseStringChars(env, instance, pathElement.szInstanceName);
	(*env)->ReleaseStringChars(env, machine, pathElement.szMachineName);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhMakeCounterPath: 0x%x\n", stat);
		return 0;
	}

	stat = PdhAddCounter(phQuery, counterPath, 0, &phCounter);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhAddCounter: 0x%x\n", stat);
		return 0;
	}
	free(counterPath);

	PdhCollectQueryData(phQuery);

	return (jint)phCounter;
}

JNIEXPORT jdouble JNICALL Java_org_krakenapps_winapi_PerformanceCounter_nextValue(JNIEnv *env, jobject obj, jint queryHandle, jint counterHandle) {
	PDH_HQUERY phQuery = (PDH_HQUERY)queryHandle;
	PDH_HCOUNTER phCounter = (PDH_HCOUNTER)counterHandle;
	PDH_FMT_COUNTERVALUE pValue;
	PDH_STATUS stat = 0;

	stat = PdhCollectQueryData(phQuery);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "PdhCollectQueryData: 0x%x\n", stat);
		return 0;
	}

	stat = PdhGetFormattedCounterValue(phCounter, PDH_FMT_DOUBLE, NULL, &pValue);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "PdhGetFormattedCounterValue: 0x%x\n", stat);
		return 0;
	}

	return pValue.doubleValue;
}

JNIEXPORT void JNICALL Java_org_krakenapps_winapi_PerformanceCounter_close(JNIEnv *env, jobject obj, jint queryHandle, jint counterHandle) {
	PDH_STATUS stat = 0;

	stat = PdhRemoveCounter((PDH_HCOUNTER)counterHandle);
	if(stat != ERROR_SUCCESS) {
		fprintf(stderr, "Error in PdhRemoveCounter: 0x%x\n", stat);
		return;
	}

	stat = PdhCloseQuery((PDH_HQUERY)queryHandle);
	if(stat != ERROR_SUCCESS)
		fprintf(stderr, "Error in PdhCloseQuery: 0x%x\n", stat);
}