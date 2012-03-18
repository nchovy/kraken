#include <windows.h>
#include "org_krakenapps_winapi_SystemTime.h"

JNIEXPORT jlongArray JNICALL Java_org_krakenapps_winapi_SystemTime_getSystemTimes(JNIEnv *env, jobject obj) {
	jlongArray times = (*env)->NewLongArray(env, 3);
	jlong idle = 0;
	jlong kernel = 0;
	jlong user = 0;
	FILETIME lpIdle;
	FILETIME lpKernel;
	FILETIME lpUser;

	GetSystemTimes(&lpIdle, &lpKernel, &lpUser);
	idle = ((__int64)lpIdle.dwHighDateTime << 32) + lpIdle.dwLowDateTime;
	kernel = ((__int64)lpKernel.dwHighDateTime << 32) + lpKernel.dwLowDateTime;
	user = ((__int64)lpUser.dwHighDateTime << 32) + lpUser.dwLowDateTime;

	(*env)->SetLongArrayRegion(env, times, 0, 1, &idle);
	(*env)->SetLongArrayRegion(env, times, 1, 1, &kernel);
	(*env)->SetLongArrayRegion(env, times, 2, 1, &user);

	return times;
}