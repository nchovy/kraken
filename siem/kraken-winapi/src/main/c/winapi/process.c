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
#include <psapi.h>
#include <mmsystem.h> 
#include "org_krakenapps_winapi_Process.h"

static void SetLongRef(JNIEnv *env, jobject obj, __int64 value)
{
	jclass cls;
	jfieldID fid;
	cls = (*env)->GetObjectClass(env, obj);
	fid = (*env)->GetFieldID(env, cls, "value", "J");
	(*env)->SetLongField(env, obj, fid, (jlong)value);
}

static void SetObjectRef(JNIEnv *env, jobject obj, jobject value)
{
	jclass cls;
	jfieldID fid;
	cls = (*env)->GetObjectClass(env, obj);
	fid = (*env)->GetFieldID(env, cls, "value", "Ljava/lang/Object;");
	(*env)->SetObjectField(env, obj, fid, value);
}

static jobject NewInteger(JNIEnv *env, jint value)
{
	jclass cls = (*env)->FindClass(env, "java/lang/Integer");
	jmethodID methodID = (*env)->GetMethodID(env, cls, "<init>", "(I)V");
	return (*env)->NewObject(env, cls, methodID, value);
}

static __int64 GetTime(FILETIME ftTime)
{
	__int64 t;
	t = ((__int64)ftTime.dwHighDateTime << 32) + ftTime.dwLowDateTime;

	return t;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessMemoryInfo
(JNIEnv *env, jobject obj, jint hProcess, jobject pageFaultCount, jobject peakWorkingSetSize, 
 jobject workingSetSize, jobject quotaPeakPagedPoolUsage, jobject quotaPagedPoolUsage, 
 jobject quotaPeakNonPagedPoolUsage, jobject quotaNonPagedPoolUsage, jobject pagefileUsage, 
 jobject peakPagefileUsage, jobject privateUsage)
{
	BOOL ret;
	PROCESS_MEMORY_COUNTERS_EX counters;
	ret = GetProcessMemoryInfo(hProcess, &counters, sizeof(PROCESS_MEMORY_COUNTERS_EX));

	SetLongRef(env, pageFaultCount, counters.PageFaultCount);
	SetLongRef(env, peakWorkingSetSize, counters.PeakWorkingSetSize);
	SetLongRef(env, workingSetSize, counters.WorkingSetSize);
	SetLongRef(env, quotaPeakPagedPoolUsage, counters.QuotaPeakPagedPoolUsage);
	SetLongRef(env, quotaPagedPoolUsage, counters.QuotaPagedPoolUsage);
	SetLongRef(env, quotaPeakNonPagedPoolUsage, counters.QuotaPeakNonPagedPoolUsage);
	SetLongRef(env, quotaNonPagedPoolUsage, counters.QuotaNonPagedPoolUsage);
	SetLongRef(env, pagefileUsage, counters.PagefileUsage);
	SetLongRef(env, peakPagefileUsage, counters.PeakPagefileUsage);
	SetLongRef(env, privateUsage, counters.PrivateUsage);

	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessorCount
(JNIEnv *env, jobject obj)
{
	SYSTEM_INFO info;
	GetSystemInfo(&info);
	return info.dwNumberOfProcessors;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_GetProcessTimes
(JNIEnv *env, jobject obj, jint hProcess, jobject creationTime, jobject exitTime, jobject kernelTime, jobject userTime)
{
	int ret = 0;
	FILETIME creation;
	FILETIME exit;
	FILETIME kernel;
	FILETIME user;

	ret = GetProcessTimes(hProcess, &creation, &exit, &kernel, &user);

	SetLongRef(env, creationTime, GetTime(creation));
	SetLongRef(env, exitTime, GetTime(exit));
	SetLongRef(env, kernelTime, GetTime(kernel));
	SetLongRef(env, userTime, GetTime(user));

	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_EnumProcesses
(JNIEnv *env, jobject obj, jobject pids)
{
	jobjectArray pidArray;
	DWORD processes[1024], needed, processCount;
	unsigned int i;

	if ( !EnumProcesses( processes, sizeof(processes), &needed ) )
		return 0;

	// Calculate how many process identifiers were returned.
	processCount = needed / sizeof(DWORD);

	pidArray = (*env)->NewObjectArray(env, processCount, (*env)->FindClass(env, "java/lang/Integer"), 0);
	for (i = 0; i < processCount; i++) 
	{
		(*env)->SetObjectArrayElement(env, pidArray, i, NewInteger(env, processes[i]));
	}

	SetObjectRef(env, pids, pidArray);

	return 1;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_OpenProcess
(JNIEnv *env, jobject obj, jint desiredAccess, jint inheritHandle, jint processId)
{
	// NOTE: temporary privilege escalation code
	HANDLE hToken = NULL;
	TOKEN_PRIVILEGES tokenPriv;
	LUID luidDebug;
	if(OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES, &hToken) != FALSE) 
	{
		if(LookupPrivilegeValue(NULL, SE_DEBUG_NAME, &luidDebug) != FALSE)
		{
			tokenPriv.PrivilegeCount           = 1;
			tokenPriv.Privileges[0].Luid       = luidDebug;
			tokenPriv.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
			AdjustTokenPrivileges(hToken, FALSE, &tokenPriv, 0, NULL, NULL);
		}
	}

	CloseHandle(hToken);

	return OpenProcess(desiredAccess, inheritHandle, processId);
}

JNIEXPORT jstring JNICALL Java_org_krakenapps_winapi_Process_GetModuleBaseName
(JNIEnv *env, jobject obj, jint hProcess, jint maxLength)
{
	jstring name = NULL;
	HMODULE hMod = NULL;
	DWORD cbNeeded = 0;
	TCHAR szProcessName[MAX_PATH];

	if(EnumProcessModules(hProcess, &hMod, sizeof(hMod), &cbNeeded)) {
		DWORD length = GetModuleBaseName(hProcess, hMod, szProcessName, maxLength);
		if(length > 0)
			name = (*env)->NewString(env, szProcessName, length);
	}

	return name;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_Process_CloseHandle
(JNIEnv *env, jobject obj, jint handle)
{
	return CloseHandle(handle);
}
