package com.android.lee.utils;

import android.util.Log;

public class LogHelper {
	private static boolean DEBUG = true;
	
	public static void LOGD(String tag,String log){
		if(DEBUG){
			Log.d(tag, log);
		}
	}
	
	public static void LOGE(String tag,String log){
		if(DEBUG){
			Log.e(tag, log);
		}
	}
	
	public static void LOGW(String tag,String log){
		if(DEBUG){
			Log.w(tag, log);
		}
	}
	
	public static void LOGV(String tag,String log){
		if(DEBUG){
			Log.v(tag, log);
		}
	}
}
