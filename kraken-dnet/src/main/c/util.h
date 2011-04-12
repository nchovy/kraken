#include <jni.h>

struct addr make_addr(JNIEnv *, jobject);

jobject make_java_address(JNIEnv *, struct addr);
