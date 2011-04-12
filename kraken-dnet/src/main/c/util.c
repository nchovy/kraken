#include "util.h"
#include <dnet.h>
#include <memory.h>

struct addr make_addr(JNIEnv *env, jobject obj) {
	struct addr entry;
	jclass cAddress = (*env)->FindClass(env, "org/krakenapps/dnet/Address");
	jmethodID getType = (*env)->GetMethodID(env, cAddress, "getType", "()Lorg/krakenapps/dnet/Address$Type");
	jmethodID getBits = (*env)->GetMethodID(env, cAddress, "getBits", "()I");
	jmethodID getData = (*env)->GetMethodID(env, cAddress, "getData", "()[B");
	jclass cType = (*env)->FindClass(env, "org/krakenapps/dnet/Address$Type");
	jmethodID ordinal = (*env)->GetMethodID(env, cType, "ordinal", "()I");

	jobject enumType = (*env)->CallObjectMethod(env, obj, getType);
	jint type = (*env)->CallIntMethod(env, enumType, ordinal);
	jint bits = (*env)->CallIntMethod(env, obj, getBits);
	jbyteArray jdata = (jbyteArray) (*env)->CallObjectMethod(env, obj, getData);
	jbyte *data = (*env)->GetByteArrayElements(env, jdata, NULL);

	entry.addr_type = (uint16_t) type;
	entry.addr_bits = (uint16_t) bits;
	memcpy(&entry.__addr_u, data, 16);

	return entry;
}

jobject make_java_address(JNIEnv *env, struct addr addr) {
	jclass clzAddress = (*env)->FindClass(env, "org/krakenapps/dnet/Address");
	jmethodID addressInit = (*env)->GetMethodID(env, clzAddress, "<init>", "(Ljava/lang/String;I[B)V");
	jstring type = NULL;
	jint bits = addr.addr_bits;
	jbyteArray data = (*env)->NewByteArray(env, 16);

	switch(addr.addr_type) {
	case ADDR_TYPE_ETH:
		type = (*env)->NewStringUTF(env, "Ethernet");
		break;
	case ADDR_TYPE_IP:
		type = (*env)->NewStringUTF(env, "IP");
		break;
	case ADDR_TYPE_IP6:
		type = (*env)->NewStringUTF(env, "IPv6");
		break;
	default:
		type = (*env)->NewStringUTF(env, "None");
	}

	(*env)->SetByteArrayRegion(env, data, 0, 16, (jbyte *) addr.addr_data8);

	return (*env)->NewObject(env, clzAddress, addressInit, type, bits, data);
}
