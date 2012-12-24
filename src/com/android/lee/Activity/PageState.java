package com.android.lee.Activity;


public class PageState {
	//文件管理状态-选择文件
	public  	static final int STATE_FM = 0;
	//文件列表状态-已经载入的文件列表
	public 		static final int STATE_FL = 1;
	//阅读界面
	public 		static final int STATE_RD = 2;

	public 		static final int DEFAULT_STATE = STATE_FL;
	
	private		 int	mState = -1;
	
	private		static Object lock = new Object();
	
	public 	 int 	getState(){
		synchronized (lock) {
			return mState;
		}
	}
	
	/*private static PageState pageState;
	public static PageState getDefault(){
		if(pageState == null){
			pageState = new PageState();
		}
		return pageState;
	}*/
	
	public 	void updateState(int state){
		synchronized (lock) {
			if(mState != state){
				mState = state;//自行判断是否一致
				if(listener != null){
					listener.stateChange(mState,true);
				}
			}else{
				if(listener != null){
					listener.stateChange(mState,false);
				}
			}
		}
	}
	
	public interface OnPageStateChanged{
		void stateChange(int state,boolean isChanged);
	}
	
	OnPageStateChanged listener;
	public void setStateChangedListener(OnPageStateChanged stateChanged){
		if(stateChanged != null){
			listener = stateChanged;
		}
	}
}
