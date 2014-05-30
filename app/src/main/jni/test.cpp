#include <string.h>
#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_org_namelessrom_devicecontrol_Application_stringFromJNI(JNIEnv *env, jobject thiz)
    {
        return env->NewStringUTF("Hello from C++ JNI !");
    }
}
