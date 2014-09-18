LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := FileJni
### Add all source file names to be included in lib separated by a whitespace
LOCAL_SRC_FILES := TtsJniDemo.cpp \
					readline.cpp

LOCAL_C_INCLUDES :=  $(JNI_H_INCLUDE)    #包含相应的头文件
LOCAL_LDLIBS :=  -llog                   #包含打印log需要的库文件
LOCAL_PRELINK_MODULE := false
   
include $(BUILD_SHARED_LIBRARY)
