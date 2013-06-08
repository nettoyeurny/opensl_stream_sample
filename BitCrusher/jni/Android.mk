LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := bitcrusher
LOCAL_C_INCLUDES := $(LOCAL_PATH)/jni
LOCAL_LDLIBS := -lOpenSLES -llog
LOCAL_SRC_FILES := bitcrusher.c opensl_stream/opensl_stream.c
include $(BUILD_SHARED_LIBRARY)
