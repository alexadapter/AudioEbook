package com.android.lee.View;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;

import com.android.lee.utils.Utils;

public class ReadView extends AbstractReadView implements IViewInfo{
	private 	String 			TAG = "ReadView"; 
	private		boolean			DEBUG = true;
	//-1begin 0 midd 1end 2end and begin
    private 	int				atFilePos = 0;
    private 	int				mRecordId;
    
    private 	IPageDateInfo 	mDataInfo;
//    private		Rect			mReadProgressRect;
	public ReadView(Context context, int id) {
		super(context);
		setId(id);
		mDataInfo = new PageDataInfo();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(background != null)
			background.draw(canvas);
		/*if(mBitmap != null)
			canvas.drawBitmap(mBitmap, 0, 0, null);*/
		if(!getViewDataStr().isEmpty()){
			canvas.save();
			canvas.translate(0, DisplayThemeInfo.getDefaultTheme().getTopPadding());
			int edge = 0;
			if(getViewRecord().size() > 0)
				edge = getViewRecord().get(0);
			/*if(DEBUG)
				LogHelper.LOGD(TAG, "record.size()=" + getViewRecord().size() + "edge=" + edge);*/
			for(int i = 0; i < getViewRecord().size()-1; i++){
				canvas.drawText(getViewDataStr(), getViewRecord().get(i)-edge, getViewRecord().get(i+1)-edge, DisplayThemeInfo.getDefaultTheme().getLeftPadding(),
							Utils.StartDrawH + i * mTheme.getTextHeight(), mTheme.getPaint());
			}
//			canvas.drawText(getViewDataStr(), getViewRecord().get(i), getViewDataStr().length(), 0,Utils.StartDrawH + i * mTheme.getTextHeight(), mTheme.getPaint());
			
			drawProgress(canvas,String.valueOf(Math.round(mDataInfo.getCurProgress()*1000)/10.0) + "%");
            drawTime(canvas);
//			canvas.drawText(String.valueOf(mDataInfo.getCurProgress()),0,0,mTheme.getPaint());
			canvas.restore();
		}
	}

	/*public void updateReadingProgress(){
		this.invalidate();
	}*/
	
	public String getFirstLine(){
		int edge = getViewRecord().get(0);
		if( getViewRecord().size() > 1)
			return 	getViewDataStr().substring(0, getViewRecord().get(1)-edge);
		else 
			return "";
	}
	
	public void drawText(List<Integer> record,String string,int recordId){
		mRecordId = recordId;
		invalidate();
	}
	
	public int atFilePos(){
		return atFilePos;
	}
	
	public void setFilePos(int pos){
		atFilePos = pos;
	}
	
	public int getRecordId(){
		return mRecordId;
	}

	@Override
	public ArrayList<Integer> getViewRecord() {
		return (ArrayList<Integer>) mDataInfo.getRecordList();
	}

	@Override
	public String getViewDataStr() {
		return mDataInfo.getDataStr();
	}

	@Override
	public IPageDateInfo getAnalyseData() {
		return mDataInfo;
	}
	
	/*public void exit(){
		mDataInfo.clear();
	}*/
	
	/*public void saveState(SharedPreferences pre,int id){
		pre.edit().putInt(id+"x1", mDataInfo.getCurPageStartPos())
				   .putInt(id+"x2", mDataInfo.getCurPageEndPos())
				   .putInt(id+"x3", atFilePos()).commit();
	}
	
	public void restoreState(SharedPreferences pre,int id){
		mDataInfo.setCurPagePos(pre.getInt(id+"x1", 0), pre.getInt(id+"x2", 0), pre.getInt(id+"x3", 0));
	}*/
}
