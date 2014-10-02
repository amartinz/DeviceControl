LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES      := acra android-support-v13-r20 android-support-v7-recyclerview-r21 ion otto

LOCAL_ASSET_DIR    := $(LOCAL_PATH)/assets
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_SRC_FILES    := \
        $(call all-java-files-under,java) \
        aidl/org/namelessrom/devicecontrol/api/IRemoteService.aidl \

## Cardslib

library_src_files := ../../../../../../external/cardslib/library/src/main/java
LOCAL_SRC_FILES   += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../external/cardslib/library/src/main/res

## Holoaccent

library_src_files := ../../../../../../external/holoaccent/HoloAccent/src
LOCAL_SRC_FILES   += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../external/holoaccent/HoloAccent/res

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
    --extra-packages it.gmariotti.cardslib.library \
    --extra-packages com.negusoft.holoaccent \
    --extra-packages com.larswerkman.holocolorpicker \
    --extra-packages com.github.mikephil.charting \

######

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME      := DeviceControl
LOCAL_CERTIFICATE       := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS       := optional

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))

#===================================================================
include $(CLEAR_VARS)
LOCAL_SRC_FILES := ../../../libs/android-support-v7-recyclerview-r21.jar
LOCAL_MODULE := android-support-v7-recyclerview-r21
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_PATH := $(TARGET_OUT)/fake_packages/$(LOCAL_SRC_FILES)
include $(BUILD_PREBUILT)
#===================================================================
