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