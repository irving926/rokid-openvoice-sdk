include $(CLEAR_VARS)

LOCAL_MODULE := libspeech
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_TAGS := optional
LOCAL_CPP_EXTENSION := .cc

# protobuf source generate
SPEECH_PROTO_FILE := $(LOCAL_PATH)/proto/speech.proto
PROTOC_OUT_DIR := $(call local-intermediates-dir)/gen
RPROTOC := $(HOST_OUT_EXECUTABLES)/aprotoc
PROTOC_GEN_SRC := \
	$(PROTOC_OUT_DIR)/speech.pb.cc
$(PROTOC_GEN_SRC): PRIVATE_CUSTOM_TOOL := $(RPROTOC) -I$(LOCAL_PATH)/proto --cpp_out=$(PROTOC_OUT_DIR) $(SPEECH_PROTO_FILE)
$(PROTOC_GEN_SRC): $(SPEECH_PROTO_FILE)
	$(transform-generated-source)
LOCAL_GENERATED_SOURCES := $(PROTOC_GEN_SRC)

LOCAL_C_INCLUDES := \
	$(PROTOC_OUT_DIR) \
	$(LOCAL_PATH)/include \
	$(LOCAL_PATH)/src/common

COMMON_SRC := \
	src/common/speech_config.cc \
	src/common/speech_config.h \
	src/common/log.cc \
	src/common/log.h \
	src/common/speech_connection.cc \
	src/common/speech_connection.h

TTS_SRC := \
	src/tts/tts_impl.cc \
	src/tts/tts_impl.h \
	src/tts/types.h

ASR_SRC := \
	src/asr/asr_impl.cc \
	src/asr/asr_impl.h \
	src/asr/types.h

SPEECH_SRC := \
	src/speech/speech_impl.cc \
	src/speech/speech_impl.h \
	src/speech/types.h

LOCAL_SRC_FILES := \
	$(COMMON_SRC) \
	$(TTS_SRC) \
	$(ASR_SRC) \
	$(SPEECH_SRC)

LOCAL_CFLAGS := $(COMMON_CFLAGS) \
	-std=c++11 -frtti -fexceptions
LOCAL_SHARED_LIBRARIES := liblog libpoco libcrypto
LOCAL_EXPORT_C_INCLUDE_DIRS := $(LOCAL_PATH)/include

ifeq ($(PLATFORM_SDK_VERSION), 23)
LOCAL_SHARED_LIBRARIES += libprotobuf-rokid-cpp-full
LOCAL_CXX_STL := libc++
else ifeq ($(PLATFORM_SDK_VERSION), 22)
LOCAL_SHARED_LIBRARIES += libc++ libdl libprotobuf-rokid-cpp-full
LOCAL_C_INCLUDES += external/libcxx/include
LOCAL_CPPFLAGS := -DLOW_PB_VERSION
else
LOCAL_STATIC_LIBRARIES := libprotobuf-cpp-2.3.0-full-gnustl-rtti
LOCAL_SDK_VERSION := 14
LOCAL_NDK_STL_VARIANT := gnustl_static
LOCAL_CPPFLAGS := -D__STDC_FORMAT_MACROS -DLOW_PB_VERSION
LOCAL_C_INCLUDES += external/protobuf/src
endif

include $(BUILD_SHARED_LIBRARY)

# module speech_demo
include $(CLEAR_VARS)
LOCAL_MODULE := speech_demo
LOCAL_MODULE_TAGS := optional
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := \
	demo/demo.cc \
	demo/tts_demo.cc \
	demo/asr_demo.cc \
	demo/speech_demo.cc
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/include \
	$(LOCAL_PATH)/src/common \
	$(PROTOC_OUT_DIR) \
	external/protobuf/src \
	external/boringssl/include
LOCAL_SHARED_LIBRARIES := libpoco libspeech
LOCAL_CPPFLAGS := $(COMMON_CFLAGS) \
	-std=c++11 -frtti -fexceptions
ifeq ($(PLATFORM_SDK_VERSION), 23)
LOCAL_CXX_STL := libc++
else ifeq ($(PLATFORM_SDK_VERSION), 22)
LOCAL_SHARED_LIBRARIES += libc++ libdl
else
LOCAL_SDK_VERSION := 14
LOCAL_NDK_STL_VARIANT := gnustl_static
endif
include $(BUILD_EXECUTABLE)
