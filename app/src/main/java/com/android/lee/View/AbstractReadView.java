package com.android.lee.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import com.android.lee.utils.LogHelper;
import com.iflytek.tts.R;

public abstract class AbstractReadView extends View /*implements IViewInfo*/{
    private 	String			TAG = "AbstractReadView";
    private		boolean			DEBUG = false;
    
    //背景图片，应该scrollto无法滚动背景..所以定义变量绘制进去
//    protected	Drawable 		background;
    protected int color;

    private 	Scroller 		mScroller;
    private		boolean			mStartAnimation = false;
    private 	DecelerateInterpolator interpolator;
    private		int				mAnimationDuration = 200;
    
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
//        color = mTheme.getColor();
//		background = getResources().getDrawable(mTheme.getThemeId());
//		background.setCallback(null);
//		background.setBounds(0, 0, mTheme.getScreenWidth(), mTheme.getScreenHeight());
        if(mTheme.getThemeId() == IDisplayTheme.NIGHT_THEME) {
            color = getResources().getColor(R.color.read_bg_night);
            mTheme.getPaint().setColor(getResources().getColor(R.color.read_textColor_night));
        }else if(mTheme.getThemeId() == IDisplayTheme.DAY_THEME){
            color = getResources().getColor(R.color.read_bg_day);
            mTheme.getPaint().setColor(getResources().getColor(R.color.read_textColor_day));
        }

		if(DEBUG)
			LogHelper.LOGD(TAG, "---------------setTheme---------mTheme.getThemeId()=" + mTheme.getThemeId()
             + ",color=" + color);
	}
	
	public void updateSize(){
//		background.setBounds(0, 0, mTheme.getScreenWidth(), mTheme.getScreenHeight());
        if(mTheme.getThemeId() == IDisplayTheme.NIGHT_THEME) {
            color = getResources().getColor(R.color.read_bg_night);
        }else if(mTheme.getThemeId() == IDisplayTheme.DAY_THEME){
            color = getResources().getColor(R.color.read_bg_day);
        }
		if(DEBUG)
			LogHelper.LOGD(TAG, "---------------setTheme---------");
	}
	
	protected void drawProgress(Canvas canvas,String progress){
		mTheme.drawProgress(canvas, progress);
	}

    protected void drawTime(Canvas canvas){
        mTheme.drawTime(canvas);
    }
	
	public void moveToLeft() {
		if(DEBUG)LogHelper.LOGD(TAG, "moveToLeft");
//		synchronized (DisplayThemeInfo.animationLock) {
			if(!mStartAnimation){
				if(DEBUG)
					LogHelper.LOGD(TAG, "moveLeftIn----mScroller.getCurrX()="+mScroller.getCurrX());
				mStartAnimation = true;
				isLeft = true;
				if(!mScroller.isFinished()){
					mScroller.abortAnimation();
				}
				/**
				 * dx是移动距离。。。
				 * */
				mScroller.startScroll(-mTheme.getScreenWidth(), 0, mTheme.getScreenWidth(), 0, mAnimationDuration);
				invalidate();
			}
//		}
	}
	
	public void leftmoveIn() {
//		synchronized (DisplayThemeInfo.animationLock) {
			if(!mStartAnimation) {
				if(DEBUG)
					LogHelper.LOGD(TAG, "moveLeftIn" + "mScroller.getCurrX()="+mScroller.getCurrX());
				mStartAnimation = true;
				isLeft  = false;
				if(!mScroller.isFinished()){
					mScroller.abortAnimation();
				}
				mScroller.startScroll(mTheme.getScreenWidth(), 0, -mTheme.getScreenWidth(), 0, mAnimationDuration);
				invalidate();
			}
//		}
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
			mStartAnimation = false;
			if(DEBUG)
				LogHelper.LOGD(TAG, "mStartAnimation && mScroller.isFinished()="+mScroller.getCurrX() );
			
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
