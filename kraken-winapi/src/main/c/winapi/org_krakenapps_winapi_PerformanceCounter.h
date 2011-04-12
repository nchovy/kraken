/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_krakenapps_winapi_PerformanceCounter */

#ifndef _Included_org_krakenapps_winapi_PerformanceCounter
#define _Included_org_krakenapps_winapi_PerformanceCounter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    getMachine
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getMachines
  (JNIEnv *, jobject);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    getCategories
 * Signature: (Ljava/lang/String;I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getCategories
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    getCounters
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Ljava/util/HashMap;
 */
JNIEXPORT jobject JNICALL Java_org_krakenapps_winapi_PerformanceCounter_getCounters
  (JNIEnv *, jobject, jstring, jstring, jint);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_PerformanceCounter_open
  (JNIEnv *, jobject);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    addCounter
 * Signature: (ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_krakenapps_winapi_PerformanceCounter_addCounter
  (JNIEnv *, jobject, jint, jstring, jstring, jstring, jstring);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    nextValue
 * Signature: (II)D
 */
JNIEXPORT jdouble JNICALL Java_org_krakenapps_winapi_PerformanceCounter_nextValue
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     org_krakenapps_winapi_PerformanceCounter
 * Method:    close
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_krakenapps_winapi_PerformanceCounter_close
  (JNIEnv *, jobject, jint, jint);

jobjectArray convertStringArray(JNIEnv *, LPTSTR, DWORD);

#ifdef __cplusplus
}
#endif
#endif
