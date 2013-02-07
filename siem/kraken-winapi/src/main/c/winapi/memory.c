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
#include "org_krakenapps_winapi_MemoryStatus.h"

JNIEXPORT void JNICALL Java_org_krakenapps_winapi_MemoryStatus_getMemoryStatus(JNIEnv *env, jobject obj, jobject stat) {
	jclass clzStat = (*env)->FindClass(env, "org/krakenapps/winapi/MemoryStatus");
	jfieldID totalPhys = (*env)->GetFieldID(env, clzStat, "totalPhysical", "J");
	jfieldID availPhys = (*env)->GetFieldID(env, clzStat, "availablePhysical", "J");
	jfieldID totalPF = (*env)->GetFieldID(env, clzStat, "totalPageFile", "J");
	jfieldID availPF = (*env)->GetFieldID(env, clzStat, "availablePageFile", "J");
	jfieldID totalVirt = (*env)->GetFieldID(env, clzStat, "totalVirtual", "J");
	jfieldID availVirt = (*env)->GetFieldID(env, clzStat, "availableVirtual", "J");
	MEMORYSTATUS mem;

	GlobalMemoryStatus(&mem);

	(*env)->SetLongField(env, stat, totalPhys, mem.dwTotalPhys);
	(*env)->SetLongField(env, stat, availPhys, mem.dwAvailPhys);
	(*env)->SetLongField(env, stat, totalPF, mem.dwTotalPageFile);
	(*env)->SetLongField(env, stat, availPF, mem.dwAvailPageFile);
	(*env)->SetLongField(env, stat, totalVirt, mem.dwTotalVirtual);
	(*env)->SetLongField(env, stat, availVirt, mem.dwAvailVirtual);
}