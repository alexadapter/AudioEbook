package com.android.lee.View;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

import com.android.lee.utils.LogHelper;
import com.android.lee.utils.Utils;
import com.iflytek.tts.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public  class DisplayThemeInfo implements IDisplayTheme/*,OnThemeChangedListener*/{
	
	private   	int				mTopPadding,mBottomPadding,mLeftPadding=10,mRightPadding=10;
	//defy 尺寸
	private   	int				mViewWidth = 480;
	private   	int				mViewHeight = 854;
	private   	int				mScreenHeight = 854;
	private   	int  			mDisplayWidth = mViewWidth - mLeftPadding - mRightPadding;
	//private		int				mDisplayHeight;
		
	private		int				mThemeid = 3;
	private  	int				mRowCount = 0;
	
	private 	int				mDefaultTextSize = 42;
	//ReadView的通用信息
	private    	int				mTextSize ;
  	private    	int				mTextHeight;
  	private 	Paint			mPaint;
  	
  	private    	int				mProgressTextSize = 32;
  	private 	int				mProgressTextHeight = 42;
  	
  	private static	String			TAG = "ReadAbstractView";
    private static	boolean			DEBUG = true;
    
    private static DisplayThemeInfo themeInfo;
    
	public static DisplayThemeInfo getDefaultTheme(){
		if(themeInfo == null){
			themeInfo = new DisplayThemeInfo();
			if(DEBUG)	LogHelper.LOGW(TAG, "DisplayThemeInfo new");
		}
		return themeInfo;
	}
	
  	public DisplayThemeInfo(){
  	//init paint
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.BLACK);
  			
  		setTextSize(mDefaultTextSize);
  	}
  	
  	public interface UpdateThemeListener{
  		void updateThemeBg();
  		void updateSize();
  	}
  	
  	private UpdateThemeListener updateThemeListener;
  	public void setUpdateThemeListener(UpdateThemeListener update){
  		updateThemeListener = update;
  	}
  	
	@Override
	public void setThemeId(int id) {
		mThemeid = id;
		if(updateThemeListener != null)
			updateThemeListener.updateThemeBg();
	}

	/**
	 * 显示进度
	 * */
	public void drawProgress(Canvas canvas,String progress){
		mPaint.setTextSize(mProgressTextSize);
		canvas.drawText(progress, mLeftPadding, mViewHeight + mProgressTextSize/2 - 6, mPaint);
		mPaint.setTextSize(mTextSize);
	}

    public void drawTime(Canvas canvas){
        mPaint.setTextSize(mProgressTextSize);
        String time = DateFormat.getDateTimeInstance().format(new Date());
        LogHelper.LOGD(TAG,"time=" + time);
        int textWidth = (int)mPaint.measureText(time);
        LogHelper.LOGD(TAG,"mViewWidth=" + mViewWidth + ",textWidth=" + textWidth);

        canvas.drawText(time, mViewWidth - textWidth - mRightPadding, mViewHeight  + mProgressTextSize/2 - 6, mPaint);
        mPaint.setTextSize(mTextSize);
    }
	
	@Override
	public void setTextSize(int size) {
		mTextSize = size;
		mPaint.setTextSize(mTextSize);
		mTextHeight = mTextSize + 10;
		
		mRowCount = mViewHeight / mTextHeight ;
		int last = mViewHeight % mTextHeight;
		//由于系统基于字体的底部来绘制文本，所有需要加上字体的高度。
		//draw text
		FontMetrics fm = mPaint.getFontMetrics();
		Utils.StartDrawH = (int) (fm.descent - fm.ascent)+1; 
				
		mBottomPadding = last / 2;
		mTopPadding = last - mBottomPadding;
		
//		mProgressTextSize = 16;
	}

    @Override
    public void addTextSize() {
        mTextSize = mTextSize + 2;
        mPaint.setTextSize(mTextSize);
    }

    @Override
    public void desTextSize() {
        mTextSize = mTextSize > 2 ? mTextSize - 2 : mTextSize;
        mPaint.setTextSize(mTextSize);
    }

    public int 	getDisplayWidth(){
		return mDisplayWidth;
	}
	
	public int 	getScreenWidth(){
		return mViewWidth;
	}
	
	public int 	getScreenHeight(){
		return mScreenHeight;
	}
	
	public int getLeftPadding(){
		return mLeftPadding;
	}
	
	public int getTopPadding(){
		return mTopPadding;
	}
	
	public int getBottomPadding(){
		return mBottomPadding;
	}
	
	public int getRightPadding(){
		return mRightPadding;
	}
	
	/**
	 * first set 
	 * */
	public void	setScreenInfo(int width, int height){
		mScreenHeight = height;
		mViewWidth = width;
		
		//default TextSize set
		mViewHeight = mScreenHeight - mProgressTextHeight;
		
		mDisplayWidth = mViewWidth - mLeftPadding - mRightPadding;
		mRowCount = mViewHeight / mTextHeight ;
		int last = mViewHeight % mTextHeight;
		mBottomPadding = last / 2;
		mTopPadding = last - mBottomPadding;
		
		if(updateThemeListener != null)
			updateThemeListener.updateSize();
	}
	
	public void setPadding(int left,int right){
		mLeftPadding = left;
		mRightPadding = right;
		mDisplayWidth = mViewWidth - mLeftPadding - mRightPadding;
	}
	
	@Override
	public void setTextColor(int color) {
		mPaint.setColor(color);
	}
	
	@Override
	public int getRowSum() {
		return mRowCount;
	}

	@Override
	public int getThemeId() {
		return mThemeid + R.drawable.theme_1;
	}

	@Override
	public int getTextHeight() {
		return mTextHeight;
	}

	@Override
	public Paint getPaint() {
		return mPaint;
	}


}
