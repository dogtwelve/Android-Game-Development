LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NDKtest
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := NDKtest.cpp
LOCAL_LDLIBS    := -llog -lGLESv2

include $(BUILD_SHARED_LIBRARY)