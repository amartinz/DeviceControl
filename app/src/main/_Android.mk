LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_STATIC_JAVA_LIBRARIES      := android-support-v13 acra butterknife ion

LOCAL_ASSET_DIR     := $(LOCAL_PATH)/assets
LOCAL_RESOURCE_DIR  := $(LOCAL_PATH)/res

LOCAL_SRC_FILES     := \
        $(call all-java-files-under,java) \
        $(call all-java-files-under,../../../proprietary/src/main/java) \
        aidl/com/android/vending/billing/IInAppBillingService.aidl \
        aidl/org/namelessrom/devicecontrol/api/IRemoteService.aidl

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME      := DeviceControl
LOCAL_CERTIFICATE       := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS       := optional

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
