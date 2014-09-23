package com.android.lee.View;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.lee.Activity.IViewUpdate;
import com.android.lee.Activity.PageState;
import com.android.lee.Activity.PageState.OnPageStateChanged;
import com.android.lee.View.AbstractReadView.AnimationEndListener;
import com.android.lee.View.DisplayThemeInfo.UpdateThemeListener;
import com.android.lee.View.IViewInfo.IPageDateInfo;
import com.android.lee.utils.LogHelper;

public class ReadingLayout extends ViewGroup implements OnPageStateChanged{
	private 	int			childCount = 3;
	private 	String 		TAG = "MainLayout";
	private 	boolean		DEBUG = true;
	private 	Object 		lock = new Object();
	
	private 	ReadView		mReadView;
	private 	ReadView		mReadView1;
	private 	ReadView		mReadView2;
	private 	ViewGroup.LayoutParams vlp ;
	
	private		IDisplayTheme	mIDisplayTheme;
	private		IViewUpdate		mUpdateRequest;
	
	public ReadingLayout(Context context,  AttributeSet attrs) {
		super(context, attrs);
		mIDisplayTheme = DisplayThemeInfo.getDefaultTheme();
		vlp = new ViewGroup.LayoutParams(mIDisplayTheme.getScreenWidth(),mIDisplayTheme.getScreenHeight());
		mUpdateRequest = (IViewUpdate)context;
		mReadView = new ReadView(getContext(), 0);
		mReadView1 = new ReadView(getContext(), 1);
		mReadView2 = new ReadView(getContext(), 2);

		//最后的最先显示..
		addView(mReadView, vlp);
		addView(mReadView1,vlp);
		addView(mReadView2,vlp);
		
		//undisplay
//		mReadView.scrollTo(mIDisplayTheme.getScreenWidth(), 0);
		mReadView1.setFinalxy(mIDisplayTheme.getScreenWidth(), 0);
		mReadView2.setFinalxy(-mIDisplayTheme.getScreenWidth(), 0);
		
		mReadView.setOnAnimFinishListener(mFinishAnimLis);
		mReadView1.setOnAnimFinishListener(mFinishAnimLis);
		mReadView2.setOnAnimFinishListener(mFinishAnimLis);
		
		((DisplayThemeInfo)mIDisplayTheme).setUpdateThemeListener(updateTheme);
	}
	
	/**
	 * 内部重新排序
	 * */
	private	AnimationEndListener mFinishAnimLis = new AnimationEndListener(){
		@Override
		public void finishAnimation(boolean isLeft) {
			if(DEBUG)LogHelper.LOGD(TAG, "finishAnimation");
			reSortView(isLeft);
		}
	};
	
	/**
	 * 0--leftcached
	 * 1--rightcached
	 * 2--display
	 * */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(changed){
			int count = getChildCount();
			if(DEBUG)LogHelper.LOGD(TAG, "onLayout count="+count + "l, t, r, b + " + l + "--" + t + "---" +  r + "---" + b);
			if(count != childCount){
				return;
			}else{
				if(DEBUG)LogHelper.LOGD(TAG, "onLayout changed");
				getChildAt(2).layout(l, t, r, b);
				getChildAt(1).layout(l, t, r, b);
				getChildAt(0).layout(l, t, r, b);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//设置当前view的大小..如果直接设置为setContentView...那么不加这一段是没有显示的...不过可以在xml里面定义设置宽高也是可以显示
		//而且必须配合super.onMeasure(widthMeasureSpec, heightMeasureSpec)才能显示？？
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        
		final int count = getChildCount();
        for (int i = 0; i < count; i++){
        	View child = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mIDisplayTheme.getScreenWidth(), MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(mIDisplayTheme.getScreenHeight(), MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * 往前翻..看上一页...
	 * */
	private void moveToPri(){
		if(getChildCount() == childCount){
			ReadView view = (ReadView)getChildAt(0);
			synchronized (lock) {
				//sort child at first to last display
				detachViewFromParent(0);
				attachViewToParent(view, 2, getLayoutParams());
				
				((ReadView)getChildAt(1)).setFinalxy(mIDisplayTheme.getScreenWidth(), 0);
				((ReadView)getChildAt(2)).setFinalxy(-mIDisplayTheme.getScreenWidth(), 0);
				
				//往上查找
				mUpdateRequest.needUpdateData(getAnalyseData(1),getAnalyseData(0).getCurPageStartPos(),false);
			}
			invalidate();
		}
	}

	/**
	 * 往后翻..看下一页
	 * */
	private void moveToNext(){
		if(getChildCount() == childCount){
			ReadView view = (ReadView)getChildAt(0);
			ReadView view1 = (ReadView)getChildAt(1);
			synchronized (lock) {
				//sort child at last to first used for leftcached ---here must update content 
				detachViewFromParent(0);
				attachViewToParent(view, 2, getLayoutParams());
				detachViewFromParent(0);
				attachViewToParent(view1, 2, getLayoutParams());
				((ReadView)getChildAt(1)).setFinalxy(mIDisplayTheme.getScreenWidth(), 0);
				((ReadView)getChildAt(2)).setFinalxy(-mIDisplayTheme.getScreenWidth(), 0);
				
				Log.e("toNext", "moveToNext>>>id=" + ((ReadView)getChildAt(0)).getId() + ((ReadView)getChildAt(2)).getId());
				mUpdateRequest.needUpdateData(getAnalyseData(2),getAnalyseData(0).getCurPageEndPos(),true);
			}
			invalidate();
		}
	}
	
	private void reSortView(boolean isLeft){
		if(isLeft){
			moveToNext();
		}else{
			moveToPri();
		}
	}
	
	/**
	 * 往前翻..看上一页...根据动画来更新界面 开启动画
	 * */
	public boolean toPri(){
//		int edge = getDisplayData().IsFileEdge();
		boolean ret = false;
		if(!getDisplayData().IsFileStart()){
			((ReadView)getChildAt(1)).leftmoveIn();	
			ret = true;
		}
		return ret;
	}
	
	/**
	 * 往后翻..看下一页。。。根据动画来更新界面 开启动画
	 * */
	public boolean toNext(){
		boolean ret = false;
		if(!getDisplayData().IsFileEnd()){
			((ReadView)getChildAt(2)).moveToLeft();
			Log.e("toNext", ">>>id=" + ((ReadView)getChildAt(1)).getId());
			ret = true;
		}
		return ret;
	}
	
	/**
	 * 更新显示内容
	 * */
	public void updateData(List<Integer> record,String string, int id){
		((ReadView)getChildAt(id)).drawText(record,string,((ReadView)getChildAt(id)).getRecordId());
	}
	
	public void exit(){
		for(int i=getChildCount(); i>0; i--){
			getAnalyseData(i-1).clear();
		}
	}
	
	/*public void saveState(SharedPreferences pre){
		for(int i=getChildCount(); i>0; i--){
			((ReadView)getChildAt(i-1)).saveState(pre, i);
		}
	}
	
	public void restoreState(SharedPreferences pre){
		for(int i=getChildCount(); i>0; i--){
			((ReadView)getChildAt(i-1)).restoreState(pre, i);
		}
	}*/

	/**
	 * 更新主题
	 * */
	private	UpdateThemeListener updateTheme = new UpdateThemeListener(){
		@Override
		public void updateTheme() {
			for(int i=getChildCount(); i>0; i--){
				((AbstractReadView)getChildAt(i-1)).updateTheme();
			}
		}
	};

	public IPageDateInfo getDisplayData(){
		return ((IViewInfo)getChildAt(0)).getAnalyseData();
	}
	
	public String getFirstLine(){
		return ((ReadView)getChildAt(0)).getFirstLine();
	}
	
	public IPageDateInfo getAnalyseData(int id) {
		return ((IViewInfo)getChildAt(id)).getAnalyseData();
	}

	@Override
	public void stateChange(int state) {
		if(state == PageState.STATE_FM || state == PageState.STATE_FL){
			setVisibility(View.GONE);
		}else if(state == PageState.STATE_RD){
			setVisibility(View.VISIBLE);
		}
	} 
}
