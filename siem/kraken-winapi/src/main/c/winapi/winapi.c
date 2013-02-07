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
#include "org_krakenapps_winapi_RegistryKey.h"
#include "org_krakenapps_winapi_Process.h"

static void SetIntegerRef(JNIEnv *env, jobject obj, int value)
{
	jclass cls;
	jfieldID fid;
	cls = (*env)->GetObjectClass(env, obj);
	fid = (*env)->GetFieldID(env, cls, "value", "I");
	(*env)->SetIntField(env, obj, fid, (jint)value);
}

static void SetObjectRef(JNIEnv *env, jobject obj, jobject value)
{
	jclass cls;
	jfieldID fid;
	cls = (*env)->GetObjectClass(env, obj);
	fid = (*env)->GetFieldID(env, cls, "value", "Ljava/lang/Object;");
	(*env)->SetObjectField(env, obj, fid, value);
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegOpenKeyEx
  (JNIEnv *env, jobject obj, jint hkey, jstring valueName, jint type, jobject hkeyOut) 
{
	LONG ret;
	HKEY newKey;
	LPCWSTR pValueName = (LPCWSTR) (*env)->GetStringChars(env, valueName, JNI_FALSE);

	ret = RegOpenKeyEx((HKEY)hkey, pValueName, 0, KEY_READ, &newKey);
	SetIntegerRef(env, hkeyOut, (int)newKey);

	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegQueryValueEx
  (JNIEnv *env, jobject obj, jint hkey, jstring valueName, jobject typeOut, jobject bufferOut)
{
	LONG ret;
	jbyteArray arr;
	char *buffer = NULL;
	DWORD dwType, dwSize = 0;
	LPCWSTR pValueName = (LPCWSTR) (*env)->GetStringChars(env, valueName, JNI_FALSE);

	ret = RegQueryValueEx((HKEY)hkey, pValueName, NULL, &dwType, NULL, &dwSize); 

	buffer = (char *)malloc(dwSize);
	memset(buffer, 0, dwSize);

	ret = RegQueryValueEx((HKEY)hkey, pValueName, NULL, &dwType, buffer, &dwSize);

	SetIntegerRef(env, typeOut, dwType);

	arr = (*env)->NewByteArray(env, dwSize);
	(*env)->SetByteArrayRegion(env, arr, 0, dwSize, buffer);
	SetObjectRef(env, bufferOut, arr);

	free(buffer);
	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegCloseKey
  (JNIEnv *env, jobject obj, jint hkey)
{
	return RegCloseKey((HKEY)hkey);
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegQueryInfoKey
  (JNIEnv *env, jobject obj, jint hkey, jobject subKeyCount, jobject maxSubKeyLen, jobject maxClassLen, jobject valuesCount, jobject maxValueNameLen, jobject maxValueLen, jobject securityDescriptorSize, jobject lastWriteTime)
{
	LONG ret;
	DWORD    cSubKeys=0;               // number of subkeys 
    DWORD    cbMaxSubKey;              // longest subkey size 
    DWORD    cchMaxClass;              // longest class string 
    DWORD    cValues;              // number of values for key 
    DWORD    cchMaxValue;          // longest value name 
    DWORD    cbMaxValueData;       // longest value data 
    DWORD    cbSecurityDescriptor; // size of security descriptor 
    FILETIME ftLastWriteTime;      // last write time 
	
	ret = RegQueryInfoKey(
		(HKEY)hkey,
		NULL,
		NULL,
		NULL,
		&cSubKeys,               // number of subkeys 
        &cbMaxSubKey,            // longest subkey size 
        &cchMaxClass,            // longest class string 
        &cValues,                // number of values for this key 
        &cchMaxValue,            // longest value name 
        &cbMaxValueData,         // longest value data 
        &cbSecurityDescriptor,   // security descriptor 
        &ftLastWriteTime);       // last write time 

	SetIntegerRef(env, subKeyCount, cSubKeys);
	SetIntegerRef(env, maxSubKeyLen, cbMaxSubKey);
	SetIntegerRef(env, maxClassLen, cchMaxClass);
	SetIntegerRef(env, valuesCount, cValues);
	SetIntegerRef(env, maxValueNameLen, cchMaxValue);
	SetIntegerRef(env, maxValueLen, cbMaxValueData);
	SetIntegerRef(env, securityDescriptorSize, cbSecurityDescriptor);

	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegEnumKeyEx
  (JNIEnv *env, jobject obj, jint hkey, jint index, jobject name, jint maxNameLen, jobject lastWriteTime)
{
	LONG ret;
	DWORD cbName = maxNameLen + 1; // contains null-terminated
	TCHAR *achKey;   // buffer for subkey name
	FILETIME ftLastWriteTime;      // last write time 
	jstring newName;

	// allocate twice (unicode)
	achKey = malloc(cbName * 2);
	memset(achKey, 0, cbName * 2);

	ret = RegEnumKeyEx((HKEY)hkey, (DWORD)index, achKey, &cbName, NULL, NULL, NULL, &ftLastWriteTime);
	newName = (*env)->NewString(env, achKey, cbName);
	free(achKey);
	
	SetObjectRef(env, name, newName);
	return ret;
}

JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_RegistryKey_RegEnumValue
  (JNIEnv *env, jobject obj, jint hkey, jint index, jobject name, jint maxNameLen)
{
	LONG ret;
	DWORD cbName = maxNameLen + 1; // contains null-terminated
	TCHAR *achValue;   // buffer for subkey name
	jstring newName;

	// allocate twice (unicode)
	achValue = malloc(cbName * 2);
	memset(achValue, 0, cbName * 2);

	ret = RegEnumValue((HKEY)hkey, (DWORD)index, achValue, &cbName, NULL, NULL, NULL, NULL);
	newName = (*env)->NewString(env, achValue, cbName);
	free(achValue);
	
	SetObjectRef(env, name, newName);
	return ret;
}

