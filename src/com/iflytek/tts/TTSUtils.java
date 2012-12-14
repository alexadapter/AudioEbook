/**
 * @Title TTSUtils.java
 * @Description: TODO
 * @author Eden lee
 * @date 2012-7-10
 * @version
 */
package com.iflytek.tts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import com.iflytek.tts.TtsService.Tts;

/**
 * @ClassName TTSUtils
 * 
 */
public final class TTSUtils {

	public interface ITTSUTilsPlayState {
		/**
		 * play sentence finished.
		 */
		void playFinished();

		/**
		 * play sentence been interrupted.
		 */
		void playInterrupted();
	}

	private ITTSUTilsPlayState istate;

	private static final String TAG = "TTSUTILS";

	private boolean stop = false;

	ExecutorService executor = Executors.newCachedThreadPool();

	public TTSUtils() {
		initSpeak();
	}

	public void recycle() {
		try {
			executor.shutdownNow();
			executor = null;

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public ITTSUTilsPlayState getIstate() {
		return istate;
	}

	public void setIstate(ITTSUTilsPlayState istate) {
		this.istate = istate;
	}

	public boolean isPlaying() {
		if (Tts.JniIsCreated()) {
			return Tts.JniIsPlaying() == Tts.ivTTS_ISPLAYING;
		}
		return false;
	}

	/**
	 * start a new read will auto interrupt latest read.
	 * 
	 * @param sentence
	 */
	public void playSentence(String sentence) {
		try {
			stopRead();
			stop = false;
			Log.e(TAG, sentence);
			Tts.startReadThread(sentence);
			Thread ty = new Thread(new Runnable() {
				public void run() {
					try {
						boolean updated = false;
						do {
							Thread.sleep(50);
							if (stop)
								break;
							if (Tts.JniIsPlaying() == Tts.ivTTS_ISPLAYING) {
								continue;
							} else {
								if (istate != null && !updated) {
									updated = true;
									istate.playFinished();
								}
								break;
							}
						} while (true);
					} catch (Exception e) {
						Log.e(TAG, "play Sentence: " + e.toString());
					}
				}
			});
			ty.start();
		} catch (Exception e) {
			Log.e(TAG, "Play sentence: " + e.toString());
		}
	}

	private void initSpeak() {
		try {

			if (Tts.JniIsCreated()) {
				Log.i(TAG, "init speak called again and already inited.");
				return;
			}

			Tts.JniCreate("/mnt/sdcard/soundResource.irf");

			Tts.JniSetParam(Tts.ivTTS_PARAM_LANGUAGE, Tts.ivTTS_LANGUAGE_AUTO);
			Tts.JniSetParam(Tts.ivTTS_PARAM_ROLE, Tts.ivTTS_ROLE_JINGER);
			Tts.JniSetParam(Tts.ivTTS_PARAM_VOLUME, Tts.ivTTS_VOLUME_MAX);

			Tts.JniSetParam(Tts.ivTTS_PARAM_VOICE_SPEED, Tts.ivTTS_SPEED_NORMAL);
			Tts.JniSetParam(Tts.ivTTS_PARAM_VOICE_PITCH, Tts.ivTTS_PITCH_NORMAL);

			Log.i(TAG, "init speak finished.");
		} catch (Exception e) {
			Log.e(TAG, "TTSInit -> created: " + e.toString());
		}
	}

	/**
	 * when called this method, the callback will not be executed.
	 */
	public void stopRead() {
		stop = true;
		if (Tts.JniIsCreated()) {
			if (Tts.JniIsPlaying() == Tts.ivTTS_ISPLAYING) {
				try {
					Tts.JniStop();
					if (istate != null) {
						istate.playInterrupted();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据文件得到该文件中文本内容的编码
	 * 
	 * @param file 要分析的文件
	 */
	public static String getCharset(File file) {
	        String charset = "GBK"; // 默认编码
	        byte[] first3Bytes = new byte[3];
	        try {
	            boolean checked = false;
	            BufferedInputStream bis = new BufferedInputStream(
	                  new FileInputStream(file));
	            bis.mark(0);
	            int read = bis.read(first3Bytes, 0, 3);
	            if (read == -1)
	                return charset;
	            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
	                charset = "UTF-16LE";
	                checked = true;
	            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
	                == (byte) 0xFF) {
	                charset = "UTF-16BE";
	                checked = true;
	            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
	                    == (byte) 0xBB
	                    && first3Bytes[2] == (byte) 0xBF) {
	                charset = "UTF-8";
	                checked = true;
	            }
	            bis.reset();
	            if (!checked) {
	                int loc = 0;
	                while ((read = bis.read()) != -1) {
	                    loc++;
	                    if (read >= 0xF0)
	                        break;
	                    //单独出现BF以下的，也算是GBK
	                    if (0x80 <= read && read <= 0xBF)
	                        break;
	                    if (0xC0 <= read && read <= 0xDF) {
	                        read = bis.read();
	                        if (0x80 <= read && read <= 0xBF)// 双字节 (0xC0 - 0xDF)
	                            // (0x80 -
	                            // 0xBF),也可能在GB编码内
	                            continue;
	                        else
	                            break;
	                     // 也有可能出错，但是几率较小
	                    } else if (0xE0 <= read && read <= 0xEF) {
	                        read = bis.read();
	                        if (0x80 <= read && read <= 0xBF) {
	                            read = bis.read();
	                            if (0x80 <= read && read <= 0xBF) {
	                                charset = "UTF-8";
	                                break;
	                            } else
	                                break;
	                        } else
	                            break;
	                    }
	                }
	                System.out.println(loc + " " + Integer.toHexString(read));
	            }
	            bis.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return charset;
	}
}
