package org.namelessrom.devicecontrol.utils;

/**
 * A wrapper class for communicating with our shared library
 */
public class NativeWrapper {

    // load our shared libraries
    static {
        System.loadLibrary("jni_devicecontrol");
    }

    /**
     * @return Hello from C++ JNI !
     */
    public static native String stringFromJNI();
}
