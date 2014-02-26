LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 acra

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_ASSET_DIR += packages/apps/DeviceControl/assets

LOCAL_PACKAGE_NAME := DeviceControl
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
