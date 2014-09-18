package com.iflytek.tts.TtsService;

import android.util.Log;

public final class Tts {
	static {
		System.loadLibrary("Aisound");
	}

	/**
	 * 开始合成任务线程
	 */
	public synchronized static void startReadThread(String value) {

		final String text = new String(value);
		class TtsRunThread implements Runnable {
			public void run() {
				Log.e("JNI Version", "" + JniGetVersion());
				JniSpeak(text);
			}
		}
		Thread ttsRun = (new Thread(new TtsRunThread()));
		ttsRun.setPriority(Thread.MAX_PRIORITY);
		ttsRun.start();
	}

	public static native int JniGetVersion();

	public static native int JniCreate(String resFilename);

	public static native int JniDestory();

	public static native int JniStop();

	public static native int JniSpeak(String text);

	/**
	 * about param Id , see the const value which started with ivTTS_PARAM_XXX
	 * 
	 * @param paramId
	 * @param value
	 * @return
	 */
	public static native int JniSetParam(int paramId, int value);

	public static native int JniGetParam(int paramId);

	public static native int JniIsPlaying();

	public static native boolean JniIsCreated();

	/**
	 * STATUS CODE.
	 */

	/**
	 * this is playing value.
	 */
	public static final int ivTTS_ISPLAYING = 1;

	/*
	 * ERROR CODES
	 */

	/**
	 * success
	 */
	public static final int ivTTS_ERR_OK = 0x0000;
	/**
	 * failed
	 */
	public static final int ivTTS_ERR_FAILED = 0xFFFF;

	/**
	 * end of input stream
	 */
	public static final int ivTTS_ERR_END_OF_INPUT = 0x0001;
	/**
	 * exit TTS
	 */
	public static final int ivTTS_ERR_EXIT = 0x0002;
	/**
	 * state base
	 */
	public static final int ivTTS_STATE_BASE = 0x0100;
	/**
	 * invalid data
	 */
	public static final int ivTTS_STATE_INVALID_DATA = ivTTS_STATE_BASE + 2;
	/**
	 * TTS stop
	 */
	public static final int ivTTS_STATE_TTS_STOP = ivTTS_STATE_BASE + 3;

	/**
	 * error number base
	 */
	public static final int ivTTS_ERR_BASE = 0x8000;
	/**
	 * unimplemented function
	 */
	public static final int ivTTS_ERR_UNIMPEMENTED = ivTTS_ERR_BASE + 0;

	/**
	 * unsupported on this platform
	 */
	public static final int ivTTS_ERR_UNSUPPORTED = ivTTS_ERR_BASE + 1;
	/**
	 * invalid handle
	 */
	public static final int ivTTS_ERR_INVALID_HANDLE = ivTTS_ERR_BASE + 2;
	/**
	 * invalid parameter(s)
	 */
	public static final int ivTTS_ERR_INVALID_PARAMETER = ivTTS_ERR_BASE + 3;
	/**
	 * insufficient heap size
	 */
	public static final int ivTTS_ERR_INSUFFICIENT_HEAP = ivTTS_ERR_BASE + 4;

	/**
	 * refuse to do in current state
	 */
	public static final int ivTTS_ERR_STATE_REFUSE = ivTTS_ERR_BASE + 5;

	/**
	 * invalid parameter ID
	 */
	public static final int ivTTS_ERR_INVALID_PARAM_ID = ivTTS_ERR_BASE + 6;

	/**
	 * invalid parameter value
	 */
	public static final int ivTTS_ERR_INVALID_PARAM_VALUE = ivTTS_ERR_BASE + 7;
	/**
	 * Resource is error
	 */
	public static final int ivTTS_ERR_RESOURCE = ivTTS_ERR_BASE + 8;
	/**
	 * read resource error
	 */
	public static final int ivTTS_ERR_RESOURCE_READ = ivTTS_ERR_BASE + 9;

	/**
	 * the Endian of SDK is error
	 */
	public static final int ivTTS_ERR_LBENDIAN = ivTTS_ERR_BASE + 10;
	/**
	 * the HeadFile is different of the SDK
	 */
	public static final int ivTTS_ERR_HEADFILE = ivTTS_ERR_BASE + 11;
	/**
	 * get data size exceed the data buffer
	 */
	public static final int ivTTS_ERR_SIZE_EXCEED_BUFFER = ivTTS_ERR_BASE + 12;

	/*
	 * INSTANCE PARAMETERS
	 */

	/* constants for values of field nParamID */

	/**
	 * parameter change callback entry
	 */
	public static final int ivTTS_PARAM_PARAMCH_CALLBACK = 0x00000000;
	/**
	 * language, e.g. Chinese
	 */
	public static final int ivTTS_PARAM_LANGUAGE = 0x00000100;
	/**
	 * input code page, e.g. GBK
	 */
	public static final int ivTTS_PARAM_INPUT_CODEPAGE = 0x00000101;
	/**
	 * text mark, e.g. CSSML
	 */
	public static final int ivTTS_PARAM_TEXT_MARK = 0x00000102;
	/**
	 * whether use prompts
	 */
	public static final int ivTTS_PARAM_USE_PROMPTS = 0x00000104;
	/**
	 * how to recognize phoneme input
	 */
	public static final int ivTTS_PARAM_RECOGNIZE_PHONEME = 0x00000105;
	/**
	 * input mode, e.g. from fixed buffer, from callback
	 */
	public static final int ivTTS_PARAM_INPUT_MODE = 0x00000200;
	/**
	 * input text buffer
	 */
	public static final int ivTTS_PARAM_INPUT_TEXT_BUFFER = 0x00000201;
	/**
	 * input text size
	 */
	public static final int ivTTS_PARAM_INPUT_TEXT_SIZE = 0x00000202;
	/**
	 * input callback entry
	 */
	public static final int ivTTS_PARAM_INPUT_CALLBACK = 0x00000203;
	/**
	 * current processing position
	 */
	public static final int ivTTS_PARAM_PROGRESS_BEGIN = 0x00000204;
	/**
	 * current processing length
	 */
	public static final int ivTTS_PARAM_PROGRESS_LENGTH = 0x00000205;
	/**
	 * progress callback entry
	 */
	public static final int ivTTS_PARAM_PROGRESS_CALLBACK = 0x00000206;
	/**
	 * whether read as name
	 */
	public static final int ivTTS_PARAM_READ_AS_NAME = 0x00000301;
	/**
	 * how to read digit, e.g. read as number, read as value
	 */
	public static final int ivTTS_PARAM_READ_DIGIT = 0x00000302;
	/**
	 * how to read number "1" in Chinese
	 */
	public static final int ivTTS_PARAM_CHINESE_NUMBER_1 = 0x00000303;
	/**
	 * whether use manual prosody
	 */
	public static final int ivTTS_PARAM_MANUAL_PROSODY = 0x00000304;
	/**
	 * how to read number "0" in English
	 */
	public static final int ivTTS_PARAM_ENGLISH_NUMBER_0 = 0x00000305;
	/**
	 * how to read word in English, e.g. read by word, read as alpha
	 */
	public static final int ivTTS_PARAM_READ_WORD = 0x00000306;
	/**
	 * output callback entry
	 */
	public static final int ivTTS_PARAM_OUTPUT_CALLBACK = 0x00000401;

	/**
	 * speaker role
	 */
	public static final int ivTTS_PARAM_ROLE = 0x00000500;
	/**
	 * speak style
	 */
	public static final int ivTTS_PARAM_SPEAK_STYLE = 0x00000501;

	/**
	 * voice speed
	 */
	public static final int ivTTS_PARAM_VOICE_SPEED = 0x00000502;

	/**
	 * voice tone
	 */
	public static final int ivTTS_PARAM_VOICE_PITCH = 0x00000503;
	/**
	 * volume value
	 */
	public static final int ivTTS_PARAM_VOLUME = 0x00000504;
	/**
	 * Chinese speaker role
	 */
	public static final int ivTTS_PARAM_CHINESE_ROLE = 0x00000510;

	/**
	 * English speaker role
	 */
	public static final int ivTTS_PARAM_ENGLISH_ROLE = 0x00000511;
	/**
	 * voice effect - predefined mode
	 */
	public static final int ivTTS_PARAM_VEMODE = 0x00000600;
	/**
	 * user's mode
	 */
	public static final int ivTTS_PARAM_USERMODE = 0x00000701;
	/**
	 * Navigation Version
	 */
	public static final int ivTTS_PARAM_NAVIGATION_MODE = 0x00000701;

	/**
	 * sleep callback entry
	 */
	public static final int ivTTS_PARAM_EVENT_CALLBACK = 0x00001001;
	/**
	 * output buffer
	 */
	public static final int ivTTS_PARAM_OUTPUT_BUF = 0x00001002;
	/**
	 * output buffer size
	 */
	public static final int ivTTS_PARAM_OUTPUT_BUFSIZE = 0x00001003;
	/**
	 * delay time
	 */
	public static final int ivTTS_PARAM_DELAYTIME = 0x00001004;

	/**
	 * Detect language automatically
	 */
	public static final int ivTTS_LANGUAGE_AUTO = 0;
	/**
	 * Chinese (with English)
	 */
	public static final int ivTTS_LANGUAGE_CHINESE = 1;
	/**
	 * English
	 */
	public static final int ivTTS_LANGUAGE_ENGLISH = 2;

	/**
	 * Tianchang (female, Chinese)
	 */
	public static final int ivTTS_ROLE_TIANCHANG = 1;
	/**
	 * Wenjing (female, Chinese)
	 */
	public static final int ivTTS_ROLE_WENJING = 2;
	/**
	 * Xiaoyan (female, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOYAN = 3;
	/**
	 * Xiaoyan (female, Chinese)
	 */
	public static final int ivTTS_ROLE_YANPING = 3;
	/**
	 * Xiaofeng (male, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOFENG = 4;
	/**
	 * Xiaofeng (male, Chinese)
	 */
	public static final int ivTTS_ROLE_YUFENG = 4;
	/**
	 * Sherri (female, US English)
	 */
	public static final int ivTTS_ROLE_SHERRI = 5;
	/**
	 * Xiaojin (female, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOJIN = 6;
	/**
	 * Nannan (child, Chinese)
	 */
	public static final int ivTTS_ROLE_NANNAN = 7;
	/**
	 * Jinger (female, Chinese)
	 */
	public static final int ivTTS_ROLE_JINGER = 8;
	/**
	 * Jiajia (girl, Chinese)
	 */
	public static final int ivTTS_ROLE_JIAJIA = 9;
	/**
	 * Yuer (female, Chinese)
	 */
	public static final int ivTTS_ROLE_YUER = 10;
	/**
	 * Xiaoqian (female, Chinese Northeast)
	 */
	public static final int ivTTS_ROLE_XIAOQIAN = 11;
	/**
	 * Laoma (male, Chinese)
	 */
	public static final int ivTTS_ROLE_LAOMA = 12;
	/**
	 * Bush (male, US English)
	 */
	public static final int ivTTS_ROLE_BUSH = 13;
	/**
	 * Xiaorong (female, Chinese Szechwan)
	 */
	public static final int ivTTS_ROLE_XIAORONG = 14;
	/**
	 * Xiaomei (female, Cantonese)
	 */
	public static final int ivTTS_ROLE_XIAOMEI = 15;
	/**
	 * Anni (female, Chinese)
	 */
	public static final int ivTTS_ROLE_ANNI = 16;
	/**
	 * John (male, US English)
	 */
	public static final int ivTTS_ROLE_JOHN = 17;
	/**
	 * Anita (female, British English)
	 */
	public static final int ivTTS_ROLE_ANITA = 18;
	/**
	 * Terry (female, US English)
	 */
	public static final int ivTTS_ROLE_TERRY = 19;
	/**
	 * Catherine (female, US English)
	 */
	public static final int ivTTS_ROLE_CATHERINE = 20;
	/**
	 * Terry (female, US English Word)
	 */
	public static final int ivTTS_ROLE_TERRYW = 21;
	/**
	 * Xiaolin (female, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOLIN = 22;
	/**
	 * Xiaomeng (female, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOMENG = 23;
	/**
	 * Xiaoqiang (male, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOQIANG = 24;

	/**
	 * XiaoKun (male, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOKUN = 25;

	/**
	 * Jiu Xu (male, Chinese)
	 */
	public static final int ivTTS_ROLE_JIUXU = 51;

	/**
	 * Duo Xu (male, Chinese)
	 */
	public static final int ivTTS_ROLE_DUOXU = 52;
	/**
	 * Xiaoping (female, Chinese)
	 */
	public static final int ivTTS_ROLE_XIAOPING = 53;
	/**
	 * Donald Duck (male, Chinese)
	 */
	public static final int ivTTS_ROLE_DONALDDUCK = 54;
	/**
	 * Baby Xu (child, Chinese)
	 */
	public static final int ivTTS_ROLE_BABYXU = 55;

	/**
	 * Dalong (male, Cantonese)
	 */
	public static final int ivTTS_ROLE_DALONG = 56;
	/**
	 * user defined
	 */
	public static final int ivTTS_ROLE_USER = 99;

	/**
	 * -------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_SPEAK_STYLE
	 * -------------------------------------------------------------------
	 */

	/**
	 * plain speak style
	 */
	public static final int ivTTS_STYLE_PLAIN = 0;
	/**
	 * normal speak style (default)
	 */
	public static final int ivTTS_STYLE_NORMAL = 1;

	/**
	 * constants for values of parameter ivTTS_PARAM_VOICE_SPEED ,the range of
	 * voice speed value is from -32768 to +32767
	 */

	/**
	 * slowest voice speed
	 */
	public static final int ivTTS_SPEED_MIN = -32768;
	/**
	 * normal voice speed (default)
	 */
	public static final int ivTTS_SPEED_NORMAL = 0;
	/**
	 * fastest voice speed
	 */
	public static final int ivTTS_SPEED_MAX = +32767;

	/* constants for values of parameter ivTTS_PARAM_VOICE_PITCH */
	/* the range of voice tone value is from -32768 to +32767 */

	/**
	 * lowest voice tone
	 */
	public static final int ivTTS_PITCH_MIN = -32768;
	/**
	 * normal voice tone (default)
	 */
	public static final int ivTTS_PITCH_NORMAL = 0;
	/**
	 * highest voice tone
	 */
	public static final int ivTTS_PITCH_MAX = +32767;

	/* constants for values of parameter ivTTS_PARAM_VOLUME */
	/* the range of volume value is from -32768 to +32767 */

	/**
	 * minimized volume
	 */
	public static final int ivTTS_VOLUME_MIN = -32768;

	/**
	 * normal volume
	 */
	public static final int ivTTS_VOLUME_NORMAL = 0;
	/**
	 * maximized volume (default)
	 */
	public static final int ivTTS_VOLUME_MAX = +32767;

	/* constants for values of parameter ivTTS_PARAM_VEMODE */
	/**
	 * none
	 */
	public static final int ivTTS_VEMODE_NONE = 0;
	/**
	 * wander
	 */
	public static final int ivTTS_VEMODE_WANDER = 1;
	/**
	 * echo
	 */
	public static final int ivTTS_VEMODE_ECHO = 2;
	/**
	 * robert
	 */
	public static final int ivTTS_VEMODE_ROBERT = 3;
	/**
	 * chorus
	 */
	public static final int ivTTS_VEMODE_CHROUS = 4;

	/**
	 * underwater
	 */
	public static final int ivTTS_VEMODE_UNDERWATER = 5;
	/**
	 * reverb
	 */
	public static final int ivTTS_VEMODE_REVERB = 6;

	/**
	 * eccentric
	 */
	public static final int ivTTS_VEMODE_ECCENTRIC = 7;

	/*
	 * constants for values of parameter
	 * ivTTS_PARAM_USERMODE(ivTTS_PARAM_NAVIGATION_MODE)
	 */

	/**
	 * synthesize in the Mode of Normal
	 */
	public static final int ivTTS_USE_NORMAL = 0;
	/**
	 * synthesize in the Mode of Navigation
	 */
	public static final int ivTTS_USE_NAVIGATION = 1;
	/**
	 * synthesize in the Mode of Mobile
	 */
	public static final int ivTTS_USE_MOBILE = 2;
	/**
	 * synthesize in the Mode of Education
	 */
	public static final int ivTTS_USE_EDUCATION = 3;

	/* constants for values of parameter ivTTS_PARAM_READ_WORD */

	/**
	 * say words by the way of word
	 */
	public static final int ivTTS_READWORD_BY_WORD = 2;

	/**
	 * say words by the way of alpha
	 */
	public static final int ivTTS_READWORD_BY_ALPHA = 1;

	/**
	 * say words by the way of auto
	 */
	public static final int ivTTS_READWORD_BY_AUTO = 0;

	/* constants for values of parameter nEventID */

	/**
	 * sleep
	 */
	public static final int ivTTS_EVENT_SLEEP = 0x0100;
	/**
	 * start playing
	 */
	public static final int ivTTS_EVENT_PLAYSTART = 0x0101;
	/**
	 * context switch
	 */
	public static final int ivTTS_EVENT_SWITCHCONTEXT = 0x0102;

	/* constants for values of parameter wCode */

	/**
	 * PCM 8K 16bit
	 */
	public static final int ivTTS_CODE_PCM8K16B = 0x0208;
	/**
	 * PCM 11K 16bit
	 */
	public static final int ivTTS_CODE_PCM11K16B = 0x020B;
	/**
	 * PCM 16K 16bit
	 */
	public static final int ivTTS_CODE_PCM16K16B = 0x0210;

	/**
	 * ASCII
	 */
	public static final int ivTTS_CODEPAGE_ASCII = 437;
	/**
	 * GBK (default)
	 */
	public static final int ivTTS_CODEPAGE_GBK = 936;
	/**
	 * Big5
	 */
	public static final int ivTTS_CODEPAGE_BIG5 = 950;
	/**
	 * UTF-16 little-endian
	 */
	public static final int ivTTS_CODEPAGE_UTF16LE = 1200;
	/**
	 * UTF-16 big-endian
	 */
	public static final int ivTTS_CODEPAGE_UTF16BE = 1201;
	/**
	 * UTF-8
	 */
	public static final int ivTTS_CODEPAGE_UTF8 = 65001;

	public static final int ivTTS_CODEPAGE_GB2312 = ivTTS_CODEPAGE_GBK;

	public static final int ivTTS_CODEPAGE_GB18030 = ivTTS_CODEPAGE_GBK;

	/**
	 * if BIG_EN
	 */
	public static final int ivTTS_CODEPAGE_UNICODE_BIG_ENDIAN = 1201;
	public static final int ivTTS_CODEPAGE_UNICODE = 1200;
	public static final int ivTTS_CODEPAGE_PHONETIC_PLAIN = 23456;

	/**
	 * --------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_TEXT_MARK
	 * --------------------------------------------------------------------
	 */

	/**
	 * none
	 */
	public static final int ivTTS_TEXTMARK_NONE = 0;
	/**
	 * simple tags (default)
	 */
	public static final int ivTTS_TEXTMARK_SIMPLE_TAGS = 1;

	/**
	 * --------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_INPUT_MODE
	 * --------------------------------------------------------------------
	 */

	/**
	 * from fixed buffer
	 */
	public static final int ivTTS_INPUT_FIXED_BUFFER = 0;
	/**
	 * from callback
	 */
	public static final int ivTTS_INPUT_CALLBACK = 1;

	/**
	 * --------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_READ_DIGIT
	 * ------------------ --------------------------------------------------
	 */

	/**
	 * decide automatically (default)
	 */
	public static final int ivTTS_READDIGIT_AUTO = 0;
	/**
	 * say digit as number
	 */
	public static final int ivTTS_READDIGIT_AS_NUMBER = 1;

	/**
	 * say digit as value
	 */
	public static final int ivTTS_READDIGIT_AS_VALUE = 2;

	/**
	 * --------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_CHINESE_NUMBER_1
	 * --------------------------------------------------------------------
	 */

	/**
	 * read number "1" [yao1] in chinese (default)
	 */
	public static final int ivTTS_CHNUM1_READ_YAO = 0;

	/**
	 * read number "1" [yi1] in chinese
	 */
	public static final int ivTTS_CHNUM1_READ_YI = 1;

	/**
	 * --------------------------------------------------------------------
	 * constants for values of parameter ivTTS_PARAM_ENGLISH_NUMBER_0
	 * --------------------------------------------------------------------
	 */

	/**
	 * read number "0" [zero] in english (default)
	 */
	public static final int ivTTS_ENNUM0_READ_ZERO = 0;
	/**
	 * read number "0" [o] in englsih
	 */
	public static final int ivTTS_ENNUM0_READ_O = 1;

}
