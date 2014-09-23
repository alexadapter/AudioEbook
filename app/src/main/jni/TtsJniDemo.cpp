#include <TtsJniDemo.h>

jint FileOpen(JNIEnv *env, jobject clazz, jstring pbyFileName,jint openMode) {
	int ret = -1;
	char flag[5] = { 0 };
	FILE *fp = NULL;
	int i = 0;

	char *pFileName = (char *) env->GetStringUTFChars( pbyFileName, 0);
	if (g_nOpenFiles >= MAXOPENFILES) {
		return ERR_FILEOPEN_MUCH;
	}

	memset(flag, 0, 5);

	switch (openMode & FILEMODE_MASK) {
	case FILEMODE_READONLY:
		strcpy(flag, "rb");
		break;
	case FILEMODE_WRITE:
		strcpy(flag, "wb");
		break;
	case FILEMODE_READWRITE:
		strcpy(flag, "r+b");
		break;
	case FILEMODE_CREATEWRITE:
		strcpy(flag, "w+b");
		break;
	case FILEMODE_APPEND:
		strcpy(flag, "ab");
		break;
	case FILEMODE_NOSHAREWRITE:
		strcpy(flag, "rb");
		break;
	default:
		return ret;
	}

	fp = fopen(pFileName, flag);

	if (fp == NULL) {
		env->ReleaseStringUTFChars(pbyFileName, pFileName);
		ret = ERR_FILEOPEN_FAIL;
	} else {
		for (i = 0; i < MAXOPENFILES; i++) {
			if (NULL == fileopened[i].fp) {
				break;
			}
		}

		fileopened[i].fp = fp;
		strcpy(fileopened[i].filename, pFileName);
		g_nOpenFiles++;
		env->ReleaseStringUTFChars(pbyFileName, pFileName);
		ret =  (INT) fp;
	}
	return ret;
}

/* 获得文件长度 */
 jint FileLength(JNIEnv *env, jobject clazz, INT fd) {
	long p = 0;
	long temp = 0;

	temp = ftell((FILE*) fd);

	fseek((FILE*) fd, 0, SEEK_END);
	p = ftell((FILE*) fd);

	fseek((FILE*) fd, temp, SEEK_SET);

	return (int)p;
}

/* 移动文件指针 */
 jint FileSeek(JNIEnv *env, jobject clazz, INT fd, int offset, int origin) {
	return fseek((FILE*) fd, offset, SEEK_SET);
//	return ftell((FILE*)fd);
}

 jint FileTell(JNIEnv *env, jobject clazz, INT fd) {
 	//return fseek((FILE*) fd, offset, SEEK_SET);
 	return ftell((FILE*)fd);
 }

 jint readInt(JNIEnv *env, jobject clazz,int offset, int fd) {
	UINT8 byte[4];
	int a;
	fseek((FILE*) fd, offset, SEEK_SET);
	fread(byte, 1, 4, (FILE*) fd);
	a = (byte[0] & 0xff) | ((byte[1] << 8) & 0xff00)
			| ((byte[2] << 16) & 0xff0000) | (byte[3] << 24);
	return a;
}

jint FileClose(JNIEnv *env, jobject clazz, INT fd) {
	int i = 0;
	int ns = 0;

	ns = fd;
	if (fd != 0) {
		if (fclose((FILE*) fd) == 0) {
			for (i = 0; i < 20; i++) {
				if (fileopened[i].fp == (FILE *) ns) {
					break;
				}
			}
			memset(&fileopened[i], 0, sizeof(FILEOPENED));
			g_nOpenFiles--;
			return FILE_SUCCESS;
		} else {
			return ERR_FILECLOSE_FAIL;
		}
	} else {
		return FILE_FAIL;
	}
}

jint readFile(JNIEnv *env, jobject clazz, int fd, jbyteArray buf, int count, int seekOffset) {
	int iRet;
	jbyte *pjb = (jbyte *) env->GetByteArrayElements( buf, 0);
	if (pjb == NULL) {
		return -1;
	}
	//jsize len = env->GetArrayLength( buf);
	UINT8 *byBuf = (UINT8 *) pjb;

	int seekLen = fseek((FILE*) fd, seekOffset, 0);
	iRet = fread(byBuf, 1, count, (FILE*) fd);

	env->ReleaseByteArrayElements(buf, pjb, JNI_ABORT);
	return iRet;
}

jint readLine(JNIEnv *env, jobject clazz, int fd, jbyteArray buf, int count, int seekOffset) {
	int iRet;
	jbyte *pjb = (jbyte *) env->GetByteArrayElements( buf, 0);
	if (pjb == NULL) {
		return -1;
	}
	//jsize len = env->GetArrayLength( buf);
	UINT8 *byBuf = (UINT8 *) pjb;
	//LOGE("readline---%d,readCount = %d",seekOffset,count);
	fseek((FILE*) fd, seekOffset, SEEK_SET);
	//LOGE("ftell = %d",ftell((FILE*)fd));
	//iRet = fread(byBuf, 1, count, (FILE*) fd);
	iRet = Readline(fd,byBuf,count);
	//read end may seek to readpos and on java may get this pos
	//fseek((FILE*) fd, seekOffset + iRet, 0);
	//LOGE("readline end---iRet=%d",iRet);
	env->ReleaseByteArrayElements(buf, pjb, JNI_ABORT);
	return iRet;
}

jint readLastLine(JNIEnv *env, jobject clazz, int fd, jbyteArray buf,int seekOffset) {
	int iRet;
	jbyte *pjb = (jbyte *) env->GetByteArrayElements( buf, 0);
	if (pjb == NULL) {
		return -1;
	}
	//jsize len = env->GetArrayLength( buf);
	UINT8 *byBuf = (UINT8 *) pjb;
	//LOGE("readline---%d,readCount = %",seekOffset,count);
	int offset = seekOffset - MAXLINE;
	int length = offset > 0 ? MAXLINE : seekOffset;
	fseek((FILE*) fd, offset > 0 ? offset : 0, SEEK_SET);
	//LOGE("ftell = %d",ftell((FILE*)fd));
	//iRet = fread(byBuf, 1, count, (FILE*) fd);
	iRet = ReadLastline(fd,(char*)byBuf,length);
	//LOGE("readline end---iRet=%d",iRet);
	env->ReleaseByteArrayElements(buf, pjb, JNI_ABORT);
	return iRet;
}

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = env->FindClass( className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives( clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

#define JNIREG_CLASS "com/android/lee/FileInfo/FileManager"//指定要注册的类
<<<<<<< HEAD
=======

>>>>>>> save convert project to android-studio, add some,remove build dir
/**
 * Table of methods associated with a single class.
 */
static JNINativeMethod gMethods[] = {
	{ "open", "(Ljava/lang/String;I)I", (void*) FileOpen }, //绑定
	{ "getLength", "(I)I", (void*) FileLength },
	{ "seek", "(III)I", (void*) FileSeek },
	{ "ftell", "(I)I", (void*) FileTell },
	{ "readInt", "(II)I", (void*) readInt },
	{ "close", "(I)I",(void*) FileClose },
	{ "read", "(I[BII)I", (void*) readFile },
	{ "readLine", "(I[BII)I", (void*) readLine },
	{ "readLastLine", "(I[BI)I", (void*) readLastLine },
};

/*
 * Register native methods for all classes we know about.
 */
static int registerNatives(JNIEnv* env) {
	if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0])))
		return JNI_FALSE;

	return JNI_TRUE;
}

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
 jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		goto bail;
	}
	assert(env != NULL);

	if (!registerNatives(env)) { //注册
		return -1;
	}
	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	bail:
	    return result;
}
