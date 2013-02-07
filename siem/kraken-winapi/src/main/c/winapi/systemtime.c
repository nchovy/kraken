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