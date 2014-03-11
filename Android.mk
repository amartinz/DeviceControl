LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-opt-cards android-support-v13 acra

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_ASSET_DIR += packages/apps/DeviceControl/assets

cards_dir := ../../../frameworks/opt/cards/res
res_dirs := res $(cards_dir)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_PACKAGE_NAME := DeviceControl
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.cards

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
