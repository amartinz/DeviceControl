#include <string.h>
#include <jni.h>

// always use the NativeWrapper: Java_org_namelessrom_devicecontrol_utils_NativeWrapper

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_org_namelessrom_devicecontrol_utils_NativeWrapper_stringFromJNI(JNIEnv *env, jobject thiz)
    {
        return env->NewStringUTF("Hello from C++ JNI !");
    }
}
