LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_GRADLE_SUBPROJECT := :app

LOCAL_MODULE       := DeviceControl
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS  := optional

LOCAL_PACKAGE_NAME      := DeviceControl
LOCAL_CERTIFICATE       := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.pro

include $(BUILD_GRADLE)

include $(call all-makefiles-under,$(LOCAL_PATH))
