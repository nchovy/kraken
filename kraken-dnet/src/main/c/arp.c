#include "org_krakenapps_dnet_Arp.h"
#include "util.h"
#include <stdlib.h>

JNIEXPORT jlong JNICALL Java_org_krakenapps_dnet_Arp_nativeOpen(JNIEnv *env, jobject obj) {
	return (jlong) arp_open();
}

JNIEXPORT jint JNICALL Java_org_krakenapps_dnet_Arp_nativeAdd(JNIEnv *env, jobject obj, jlong id, jobject protocol, jobject hardware) {
	struct arp_entry entry = make_entry(env, protocol, hardware);
	return (jint) arp_add((arp_t *) id, &entry);
}

JNIEXPORT jint JNICALL Java_org_krakenapps_dnet_Arp_nativeDelete(JNIEnv *env, jobject obj, jlong id, jobject protocol, jobject hardware) {
        struct arp_entry entry = make_entry(env, protocol, hardware);
        return (jint) arp_add((arp_t *) id, &entry);
}

JNIEXPORT jint JNICALL Java_org_krakenapps_dnet_Arp_nativeGet(JNIEnv *env, jobject obj, jlong id, jobject protocol, jobject hardware) {
        struct arp_entry entry = make_entry(env, protocol, hardware);
        return (jint) arp_add((arp_t *) id, &entry);
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_dnet_Arp_nativeLoop(JNIEnv *env, jobject obj, jlong id) {
	jclass clzArrayList = (*env)->FindClass(env, "java/util/ArrayList");
	jmethodID arrayListInit = (*env)->GetMethodID(env, clzArrayList, "<init>", "()V");
	jobject entries = (*env)->NewObject(env, clzArrayList, arrayListInit);
	void *arg[3] = {env, clzArrayList, entries};

	arp_loop((arp_t *) id, arp_callback, arg);

	return entries;
}

JNIEXPORT jlong JNICALL Java_org_krakenapps_dnet_Arp_nativeClose(JNIEnv *env, jobject obj, jlong id) {
	if(id == -1) return 0;
	return (jlong) arp_close((arp_t *) id);
}

struct arp_entry make_entry(JNIEnv *env, jobject protocol, jobject hardware) {
	struct arp_entry entry;

	entry.arp_pa = make_addr(env, protocol);
	entry.arp_ha = make_addr(env, hardware);

	return entry;
}

int arp_callback(const struct arp_entry *entry, void *arg) {
	JNIEnv *env = ((void **) arg)[0];
	jclass clzArrayList = ((void **) arg)[1];
	jmethodID arrayListAdd = (*env)->GetMethodID(env, clzArrayList, "add", "(Ljava/lang/Object;)Z");
	jobject entries = ((void **) arg)[2];

	(*env)->CallVoidMethod(env, entries, arrayListAdd, make_java_arp_entry(env, entry));

	return 0;
}

jobject make_java_arp_entry(JNIEnv *env, const struct arp_entry *entry) {
        jclass clzArpEntry = (*env)->FindClass(env, "org/krakenapps/dnet/jni/NativeArpEntry");
        jmethodID arpEntryInit = (*env)->GetMethodID(env, clzArpEntry, "<init>", "(Lorg/krakenapps/dnet/Address;Lorg/krakenapps/dnet/Address;)V");
        jobject protocol = make_java_address(env, entry->arp_pa);
        jobject hardware = make_java_address(env, entry->arp_ha);

        return (*env)->NewObject(env, clzArpEntry, arpEntryInit, protocol, hardware);
}
