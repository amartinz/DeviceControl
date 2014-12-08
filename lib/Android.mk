LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := nameless-proprietary
LOCAL_SDK_VERSION := 21

ifeq ($(NAMELESS_PROPRIETARY),true)
LOCAL_SRC_FILES := priv/src/main/java/org/namelessrom/proprietary/Configuration.java
else
LOCAL_SRC_FILES := src/main/java/org/namelessrom/proprietary/Configuration.java
endif

include $(BUILD_STATIC_JAVA_LIBRARY)
