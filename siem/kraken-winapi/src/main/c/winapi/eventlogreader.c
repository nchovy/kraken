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
#include <strsafe.h>
#include "org_krakenapps_winapi_EventLogReader.h"

#define MAX_RECORD_BUFFER_SIZE 0x10000

JNIEXPORT jobject JNICALL Java_org_krakenapps_winapi_EventLogReader_readAllEventLogs(JNIEnv *env, jobject obj, jstring jLogName, jint begin) {
	jclass clzList = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID listInit = (*env)->GetMethodID(env, clzList, "<init>", "()V");
	jmethodID listAdd = (*env)->GetMethodID(env, clzList, "add", "(Ljava/lang/Object;)Z");
	jobject list = (*env)->NewObject(env, clzList, listInit);
	
	DWORD dwReadFlags = 0;
	LPVOID lpBuffer = (LPVOID)malloc(MAX_RECORD_BUFFER_SIZE);
	DWORD nNumberOfBytesToRead = MAX_RECORD_BUFFER_SIZE;
	DWORD pnBytesRead = 0;
	DWORD pnMinNumberOfBytesNeeded = 0;	

	LPTSTR lpLogName;
	HANDLE hEventLog;

	if ( jLogName == (jstring)NULL )
	{
		jclass exceptionClass = (*env)->FindClass(env, "java/lang/NullPointerException");
		(*env)->ThrowNew(env, exceptionClass, "Should not input \"NULL\"");
		return 0;
	}

	lpLogName = (LPTSTR)(*env)->GetStringChars(env, jLogName, JNI_FALSE);
	hEventLog = OpenEventLog(NULL, lpLogName);

	if(hEventLog == NULL) {
		fwprintf(stderr, L"Error in OpenEventLog: 0x%x\n", GetLastError());
		return 0;
	}

	dwReadFlags = EVENTLOG_SEQUENTIAL_READ | EVENTLOG_FORWARDS_READ;

	for(;;) {
		if(!ReadEventLog(hEventLog, dwReadFlags, begin, lpBuffer, nNumberOfBytesToRead, &pnBytesRead, &pnMinNumberOfBytesNeeded)) {
			DWORD error = GetLastError();
			LPVOID tempBuffer = NULL;

			if(error == ERROR_HANDLE_EOF || nNumberOfBytesToRead == 0)
				break;

			if(error == ERROR_INSUFFICIENT_BUFFER) {
				fwprintf(stderr, L"Error in ReadEventLog: 0x%x\n", error);
				return 0;
			}

			tempBuffer = (LPVOID)realloc(lpBuffer, nNumberOfBytesToRead);
			if(tempBuffer == NULL) {
				fwprintf(stderr, L"Failed to reallocate the memory for the record buffer (%d bytes)\n", pnMinNumberOfBytesNeeded);
				return 0;
			}
			lpBuffer = tempBuffer;
			nNumberOfBytesToRead = pnMinNumberOfBytesNeeded;
		} else {
			PBYTE pRead = lpBuffer;
			PBYTE pBufferEnd = pRead + pnBytesRead;

			while(pRead < pBufferEnd) {
				PEVENTLOGRECORD record = (PEVENTLOGRECORD)pRead;

				pRead += record->Length;
				if(record->RecordNumber < begin)
					continue;

				(*env)->CallVoidMethod(env, list, listAdd, getEventLogObject(env, lpLogName, record));
				begin = record->RecordNumber + 1;
			}
		}
	}
	(*env)->ReleaseStringChars(env, jLogName, lpLogName);

	if(lpBuffer)
		free(lpBuffer);

	if(!CloseEventLog(hEventLog))
		fwprintf(stderr, L"Error in CloseEventLog: 0x%x\n", GetLastError());

	return list;
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_winapi_EventLogReader_readEventLog(JNIEnv *env, jobject obj, jstring jLogName, jint begin) {
	jobject record = NULL;		
	DWORD dwReadFlags = EVENTLOG_SEEK_READ | EVENTLOG_FORWARDS_READ;
	LPVOID lpBuffer = (LPVOID)malloc(MAX_RECORD_BUFFER_SIZE);
	DWORD nNumberOfBytesToRead = MAX_RECORD_BUFFER_SIZE;
	DWORD pnBytesRead = 0;
	DWORD pnMinNumberOfBytesNeeded = 0;	
	LPTSTR lpLogName;
	HANDLE hEventLog;	


	if ( jLogName == (jstring)NULL )
	{
		jclass exceptionClass = (*env)->FindClass(env, "java/lang/NullPointerException");
		(*env)->ThrowNew(env, exceptionClass, "Should not input \"NULL\"");
		return 0;
	}

	lpLogName = (LPTSTR)(*env)->GetStringChars(env, jLogName, JNI_FALSE);
	hEventLog = OpenEventLog(NULL, lpLogName);

	if(lpLogName == NULL)
	{
		fwprintf(stderr, L"Error in OpenEventLog: 0x%x\n", GetLastError());		
		return 0;
	}

	if(hEventLog == NULL) {
		fwprintf(stderr, L"Error in OpenEventLog: 0x%x\n", GetLastError());
		return 0;
	}

	if(!ReadEventLog(hEventLog, dwReadFlags, begin, lpBuffer, nNumberOfBytesToRead, &pnBytesRead, &pnMinNumberOfBytesNeeded)) {
		DWORD error = GetLastError();
		LPVOID tempBuffer = NULL;

		if(error == ERROR_HANDLE_EOF)
			return NULL;

		if(error == ERROR_INSUFFICIENT_BUFFER) {
			fwprintf(stderr, L"Error in ReadEventLog: 0x%x\n", error);
			return NULL;
		}

		tempBuffer = (LPVOID)realloc(lpBuffer, nNumberOfBytesToRead);
		if(tempBuffer == NULL) {
			fwprintf(stderr, L"Failed to reallocate the memory for the record buffer (%d bytes)\n", pnMinNumberOfBytesNeeded);
			return NULL;
		}
		lpBuffer = tempBuffer;
	}

	if(pnBytesRead == 0) {
		dwReadFlags = EVENTLOG_SEQUENTIAL_READ | EVENTLOG_FORWARDS_READ;

		if(!ReadEventLog(hEventLog, dwReadFlags, begin, lpBuffer, nNumberOfBytesToRead, &pnBytesRead, &pnMinNumberOfBytesNeeded)) {
			DWORD error = GetLastError();
			LPVOID tempBuffer = NULL;

			if(error == ERROR_HANDLE_EOF)
				return NULL;

			if(error == ERROR_INSUFFICIENT_BUFFER) {
				fwprintf(stderr, L"Error in ReadEventLog: 0x%x\n", error);
				return NULL;
			}

			tempBuffer = (LPVOID)realloc(lpBuffer, nNumberOfBytesToRead);
			if(tempBuffer == NULL) {
				fwprintf(stderr, L"Failed to reallocate the memory for the record buffer (%d bytes)\n", pnMinNumberOfBytesNeeded);
				return NULL;
			}
			lpBuffer = tempBuffer;
		}

		if(pnBytesRead == 0 || ((PEVENTLOGRECORD)lpBuffer)->RecordNumber < (DWORD)begin) { 
			(*env)->ReleaseStringChars(env, jLogName, lpLogName);
			if(lpBuffer)
				free(lpBuffer);

			return NULL;
		}
	}

	record = getEventLogObject(env, lpLogName, (PEVENTLOGRECORD)lpBuffer);
	(*env)->ReleaseStringChars(env, jLogName, lpLogName);

	if(lpBuffer)
		free(lpBuffer);

	if(!CloseEventLog(hEventLog))
		fwprintf(stderr, L"Error in CloseEventLog: 0x%x\n", GetLastError());

	return record;
}

jobject getEventLogObject(JNIEnv *env, LPTSTR lpLogName, PEVENTLOGRECORD record) {
	jclass clzString = (*env)->FindClass(env, "java/lang/String");
	jclass clzEventLog = (*env)->FindClass(env, "org/krakenapps/winapi/EventLog");
	jmethodID eventLogInit = (*env)->GetMethodID(env, clzEventLog, "<init>", "(IILorg/krakenapps/winapi/EventType;IILjava/lang/String;Ljava/lang/String;[BLjava/lang/String;Ljava/lang/String;[B)V");

	LPTSTR lpEventType = getEventType(record->EventType);
	LPTSTR lpSourceName = (LPTSTR)((PBYTE)&(record->DataOffset)+4);
	LPTSTR lpEventCategory = getMessageString(lpLogName, lpSourceName, L"CategoryMessageFile", record->EventCategory, 0, NULL);
	LPTSTR pStrings = (LPTSTR)((PBYTE)record+record->StringOffset);
	LPTSTR lpMessage = getMessageString(lpLogName, lpSourceName, L"EventMessageFile", record->EventID, record->NumStrings, pStrings);

	jint recordNumber = record->RecordNumber;
	jint eventId = record->EventID & 0xFFFF;
	jobject eventType = (*env)->NewString(env, lpEventType, (jsize)wcslen(lpEventType));
	jint generated = record->TimeGenerated;
	jint written = record->TimeWritten;
	jstring sourceName = (*env)->NewString(env, lpSourceName, (jsize)wcslen(lpSourceName));
	jstring eventCategory = lpEventCategory ? (*env)->NewString(env, lpEventCategory, (jsize)wcslen(lpEventCategory)) : NULL;
	jbyteArray sid = NULL;
	jstring user = NULL;
	jobject message = lpMessage ? (*env)->NewString(env, lpMessage, (jsize)wcslen(lpMessage)) : NULL;
	jbyteArray data = NULL;

	if(record->UserSidLength > 0) {
		LPTSTR lpName = NULL;
		LPTSTR lpDomain = NULL;
		DWORD dwNameSize = 0;
		DWORD dwDomainSize = 0;
		SID_NAME_USE nUse;

		sid = (*env)->NewByteArray(env, record->UserSidLength);
		(*env)->SetByteArrayRegion(env, sid, 0, record->UserSidLength, (PBYTE)record+record->UserSidOffset);

		LookupAccountSid(NULL, (PSID)((PBYTE)record+record->UserSidOffset), NULL, &dwNameSize, NULL, &dwDomainSize, &nUse);

		lpName = (LPTSTR)malloc(sizeof(TCHAR)*dwNameSize);
		lpDomain = (LPTSTR)malloc(sizeof(TCHAR)*dwDomainSize);
		if(!LookupAccountSid(NULL, (PSID)((PBYTE)record+record->UserSidOffset), lpName, &dwNameSize, lpDomain, &dwDomainSize, &nUse)) {
			fprintf(stderr, "Error in LookupAccountSid: 0x%x\n", GetLastError());
			free(lpName);
			free(lpDomain);
		} else {
			user = (*env)->NewString(env, lpName, (jsize)wcslen(lpName));
		}
	}

	if(record->DataLength > 0) {
		data = (*env)->NewByteArray(env, record->DataLength);
		(*env)->SetByteArrayRegion(env, data, 0, record->DataLength, (PBYTE)record+record->DataOffset);
	}

	return (*env)->NewObject(env, clzEventLog, eventLogInit, recordNumber, eventId, eventType, generated, written, sourceName, eventCategory, sid, user, message, data);
}

LPTSTR getEventType(WORD wEventType) {
	switch(wEventType) {
	case 0x01:
		return L"Error";
	case 0x02:
		return L"Warning";
	case 0x04:
		return L"Information";
	case 0x08:
		return L"AuditSuccess";
	case 0x10:
		return L"AuditFailure";
	}
	return L"Information";
}

LPTSTR getMessageString(LPTSTR lpLogName, LPTSTR lpSourceName, LPTSTR lpValueName, DWORD dwMessageId, WORD numStrings, LPTSTR pStrings) {
	DWORD dwFlags = FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_HMODULE | FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_MAX_WIDTH_MASK;// | FORMAT_MESSAGE_IGNORE_INSERTS;
	LPCVOID lpSource = NULL;
	DWORD dwLanguageId = MAKELANGID(LANG_NEUTRAL, SUBLANG_SYS_DEFAULT);
	LPTSTR lpBuffer = NULL;
	void **Arguments = NULL;
	WORD i;

	lpSource = getResource(lpLogName, lpSourceName, lpValueName);
	if(!lpSource)
		return NULL;

	if(numStrings > 0) {
		Arguments = (void**)malloc(sizeof(void*)*numStrings * 2);
		memset(Arguments, 0, sizeof(void*)*numStrings * 2);
		for(i=0; i<numStrings; i++) {
			*(Arguments + i) = (void*)pStrings;
			pStrings += wcslen(pStrings) + 1;
		}		
	}
	
	if(!FormatMessage(dwFlags, lpSource, dwMessageId, dwLanguageId, (LPTSTR)&lpBuffer, 0, (va_list*)Arguments)) {
		 //fwprintf(stderr, L"Error in FormatMessage: 0x%x, %s(%d)\n", GetLastError(), lpSourceName, dwMessageId);
		 //return NULL;
	}	


	FreeLibrary((HMODULE)lpSource);

	if(Arguments)
		free(Arguments);

	return lpBuffer;
}

LPCVOID getResource(LPTSTR lpLogName, LPTSTR lpSourceName, LPTSTR lpValueName) {
	HANDLE hResource = NULL;
	LPCTSTR lpFileName = NULL;
	LPBYTE lpSrc = NULL;
	DWORD nSize = 0;
	HKEY hKey;
	DWORD lpcbData = 0;
	LPTSTR lpSubKey = NULL;
	size_t nSubKeySize = 0;

	nSubKeySize = sizeof(TCHAR)*(45+wcslen(lpLogName)+wcslen(lpSourceName));
	lpSubKey = (LPTSTR)malloc(nSubKeySize);
	StringCbPrintf(lpSubKey, nSubKeySize, L"SYSTEM\\CurrentControlSet\\services\\eventlog\\%s\\%s", lpLogName, lpSourceName);

	if ( RegOpenKeyEx(HKEY_LOCAL_MACHINE, lpSubKey, 0, KEY_READ, &hKey) != 0 )
		return NULL;

	RegQueryValueEx(hKey, lpValueName, NULL, NULL, NULL, &lpcbData);
	if(lpcbData == 0)
		return NULL;
	lpSrc = (LPBYTE)malloc(lpcbData);
	RegQueryValueEx(hKey, lpValueName, NULL, NULL, lpSrc, &lpcbData);
	RegCloseKey(hKey);

	nSize = ExpandEnvironmentStrings((LPCTSTR)lpSrc, NULL, 0);
	lpFileName = (LPCTSTR)malloc(sizeof(TCHAR)*nSize);
	ExpandEnvironmentStrings((LPCTSTR)lpSrc, (LPTSTR)lpFileName, nSize);

	hResource = LoadLibrary(lpFileName);

	return (LPCVOID)hResource;
}
