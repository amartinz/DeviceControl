LOCAL_PATH:= $(call my-dir)

#===================================================================
include $(CLEAR_VARS)
LOCAL_SRC_FILES := pollfish_4.0.2.jar
LOCAL_MODULE := pollfish-sdk
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_PATH := $(TARGET_OUT)/fake_packages/$(LOCAL_SRC_FILES)
include $(BUILD_PREBUILT)
#===================================================================

#===================================================================
include $(CLEAR_VARS)
LOCAL_SRC_FILES := nameless-proprietary.jar
LOCAL_MODULE := nameless-proprietary
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_PATH := $(TARGET_OUT)/fake_packages/$(LOCAL_SRC_FILES)
include $(BUILD_PREBUILT)
#===================================================================
