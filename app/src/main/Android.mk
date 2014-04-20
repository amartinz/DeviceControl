LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_STATIC_JAVA_LIBRARIES := \
        android-support-v7-appcompat \
        android-support-v7-mediarouter \
        android-support-v13 \
        acra \
        libGooglePlayServices

LOCAL_SRC_FILES     := \
        $(call all-java-files-under,java) \
        aidl/com/android/vending/billing/IInAppBillingService.aidl \
        $(call all-java-files-under,../../../proprietary/src/main/java)
LOCAL_ASSET_DIR     := $(LOCAL_PATH)/assets

google_play_dir := \
        ../../../../../../external/google/google_play_services/libproject/google-play-services_lib/res
appcompat_dir := \
        ../../../../../../frameworks/support/v7/appcompat/res
media_router_dir := \
        ../../../../../../frameworks/support/v7/mediarouter/res
res_dir := $(google_play_dir) $(appcompat_dir) $(media_router_dir) res

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.google.android.gms:android.support.v7.mediarouter:android.support.v7.appcompat

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME := DeviceControl
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
