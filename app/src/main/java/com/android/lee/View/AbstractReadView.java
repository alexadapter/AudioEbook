package com.android.lee.View;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import com.android.lee.utils.LogHelper;

public abstract class AbstractReadView extends View /*implements IViewInfo*/{
    private 	String			TAG = "ReadAbstractView";
    private		boolean			DEBUG = true;
    
    //背景图片，应该scrollto无法滚动背景..所以定义变量绘制进去
    protected	Drawable 		background;
//    protected 	Bitmap			mBitmap;
//    protected	Canvas 			mCanvas;
    
    private 	Scroller 		mScroller;
    private		boolean			mStartAnimation = false;
    private 	DecelerateInterpolator interpolator;
    private		boolean			isLeft;
    private		AnimationEndListener	mFinishListener;
    
    protected 	IDisplayTheme 	mTheme;
    
	public AbstractReadView(Context context) {
		super(context, null);
		mTheme = DisplayThemeInfo.getDefaultTheme();
		updateTheme();

		//单独的背景，应该要移动...
		interpolator = new DecelerateInterpolator();
		mScroller = new Scroller(getContext(),interpolator);
	}
	
	public void updateTheme(){
		background = getResources().getDrawable(mTheme.getThemeId());
		background.setCallback(null);
		background.setBounds(0, 0, mTheme.getScreenWidth(), mTheme.getScreenHeight());
		if(DEBUG)
			LogHelper.LOGD(TAG, "---------------setTheme---------");
		/*if(first)
			updateTheme();*/
	}
	
	
	public void moveToLeft() {
		if(DEBUG)LogHelper.LOGD(TAG, "moveToLeft");
		if(!mStartAnimation){
			if(DEBUG)
				LogHelper.LOGD(TAG, "moveLeftIn----mScroller.getCurrX()="+mScroller.getCurrX());
			mStartAnimation = true;
			isLeft = true;
			/**
			 * dx是移动距离。。。
			 * */
			mScroller.startScroll(-mTheme.getScreenWidth(), 0, mTheme.getScreenWidth(), 0, 300);
			invalidate();
		}
	}
	
	public void leftmoveIn() {
		if(!mStartAnimation) {
			if(DEBUG)
				LogHelper.LOGD(TAG, "moveLeftIn" + "mScroller.getCurrX()="+mScroller.getCurrX());
			mStartAnimation = true;
			isLeft  = false;
			mScroller.startScroll(mTheme.getScreenWidth(), 0, -mTheme.getScreenWidth(), 0, 300);
			invalidate();
		}
	}
	
	public void setFinalxy(int x, int y){
		mScroller.setFinalX(x);
		mScroller.setFinalY(y);
//		scrollTo(x,y);
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(),0);
			if(DEBUG)
				LogHelper.LOGD(TAG, "mScroller.getCurrX()="+mScroller.getCurrX() );
			postInvalidate();
		}else if(mStartAnimation && mScroller.isFinished()){//结束动画
			if(mFinishListener != null){
				mFinishListener.finishAnimation(isLeft);
			}
			if(DEBUG)
				LogHelper.LOGD(TAG, "mStartAnimation && mScroller.isFinished()="+mScroller.getCurrX() );
			mStartAnimation = false;
		}
	}
	
	
	public void exit(){
		/*if(mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.recycle();
			mBitmap = null;
			mCanvas = null;
		}*/
	}
	
	public interface AnimationEndListener{
    	void finishAnimation(boolean isLeft);
    }
    
    public void setOnAnimFinishListener(AnimationEndListener lis){
    	if(lis != null)
    		mFinishListener = lis;
    }
    
    /*public int getRowSum(){
		return mTheme.getRowSum();
    }*/
  	
//	abstract void updateTheme();
}
