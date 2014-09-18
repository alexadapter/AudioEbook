#ifndef TTSJNIDEMO_H_
#define TTSJNIDEMO_H_


#ifdef __cplusplus
extern "C" {
#endif

	#include "jni.h"
	#include <stdlib.h>
	#include <string.h>
	#include <stdio.h>
	#include <assert.h>
	#include <android/log.h>
	#include <string.h>

	typedef unsigned char   UINT8;
	typedef unsigned short  UINT16;
	typedef char            INT8;
	typedef short           INT16;
	typedef int             INT;
	typedef unsigned int    UINT;
	typedef long            INT32;
	typedef unsigned long   UINT32;
	typedef unsigned long   DWORD;
	//typedef int             BOOL;

	#define LOG_TAG "TestJniDemo"
	#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__) // 定义LOG类型
	#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__) // 定义LOG类型
	#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__) // 定义LOG类型
	#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__) // 定义LOG类型
	#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__) // 定义LOG类型

	#define FALSE       0
	#define TRUE        1

	#define MAX_PATH_LEN 256

	#define FILEMODE_MASK           0x07
	#define FILEMODE_READONLY       0x00           /*以只读方式打开*/
	#define FILEMODE_WRITE          0x01           /*以写方式打开*/
	#define FILEMODE_READWRITE      0x02           /*以读写方式打开*/
	#define FILEMODE_APPEND         0x03           /*附加数据方式打开*/
	#define FILEMODE_CREATEWRITE    0x04           /*若文件不存在则先创建再以读写方式打开*/
	#define FILEMODE_NOSHAREWRITE   0x05           /*不允许其他人共享写文件*/

	#define MAXOPENFILES            20


	const int DATA_HEAD_SIZE = 0x100;

	#define FILE_SUCCESS            0x00000000
	#define FILE_FAIL               0x0F000001
	#define ERR_FILEOPEN_MUCH       0x0F000002
	#define ERR_FILEOPEN_FAIL       0x0F000003
	#define ERR_FILECLOSE_FAIL      0x0F000004

	typedef struct{
		FILE  *fp;
		char filename[256];
	}FILEOPENED;

	static FILEOPENED fileopened[MAXOPENFILES] = { 0 };
	static INT g_nOpenFiles = 0;

	//one time read 4096 maxbyte
	#define MAXLINE 4096

	static ssize_t	my_read(int fd, char *ptr);
	ssize_t	readline(int fd, void *vptr, size_t maxlen);
	ssize_t readlinebuf(void **vptrptr);

	ssize_t Readline(int fd, void *ptr, size_t maxlen);


	static ssize_t	my_read_last(int fd, char *ptr, int maxlen);
	ssize_t	readlastline(int fd, void *vptr, size_t maxlen);
	ssize_t readlastlinebuf(void **vptrptr);

	ssize_t ReadLastline(int fd, char *ptr, size_t maxlen);
#ifdef __cplusplus
}
#endif

#endif /* TTSJNIDEMO_H_ */
