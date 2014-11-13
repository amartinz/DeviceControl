LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview \
    ion \

LOCAL_ASSET_DIR    := $(LOCAL_PATH)/assets
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_SRC_FILES    := \
    $(call all-java-files-under,java) \
    aidl/org/namelessrom/devicecontrol/api/IRemoteService.aidl \

## android-support-v7-appcompat

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../frameworks/support/v7/appcompat/res

## Holocolorpicker

library_src_files := ../../../../../../external/holocolorpicker/src
LOCAL_SRC_FILES   += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../external/holocolorpicker/res

## MPAndroidChart

library_src_files := ../../../../../../external/mpandroidchart/MPChartLib/src
LOCAL_SRC_FILES   += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../external/mpandroidchart/MPChartLib/res

######

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages com.larswerkman.holocolorpicker \
    --extra-packages com.github.mikephil.charting \

######

LOCAL_PROGUARD_FLAG_FILES := proguard.pro

LOCAL_PACKAGE_NAME      := DeviceControl
LOCAL_CERTIFICATE       := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS       := optional

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
