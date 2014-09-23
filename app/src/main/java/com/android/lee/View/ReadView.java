package com.android.lee.View;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;

import com.android.lee.utils.Utils;

public class ReadView extends AbstractReadView implements IViewInfo{
	private 	String 			TAG = "ReadView"; 
	private		boolean			DEBUG = true;
	//-1begin 0 midd 1end 2end and begin
    private 	int				atFilePos = 0;
    private 	int				mRecordId;
    
//    private 	List<Integer> 	mDataRecord;
//    private		String 			mDataString;	
    private 	IPageDateInfo mDataInfo;
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
//			int start = getViewRecord().size()-2;
//			int end = start > mTheme.getRowSum() ? start-mTheme.getRowSum() : 0;
			int edge = 0;
			if(getViewRecord().size() > 0)
				edge = getViewRecord().get(0);
			/*if(DEBUG)
				LogHelper.LOGD(TAG, "record.size()=" + getViewRecord().size() + "edge=" + edge);*/
			for(int i = 0; i < getViewRecord().size()-1; i++){
				/*if(DEBUG)
					LogHelper.LOGD(TAG, "record.size()=" + getViewRecord().size() + "--edge=" + (getViewRecord().get(i)-edge)
							+"getViewDataStr" + getViewDataStr().length() + "getViewRecord().get(i)=" + getViewRecord().get(i)
							+"getViewRecord().get(i+1)" + (getViewRecord().get(i+1)-edge));*/
				canvas.drawText(getViewDataStr(), getViewRecord().get(i)-edge, getViewRecord().get(i+1)-edge, DisplayThemeInfo.getDefaultTheme().getLeftPadding(),
							Utils.StartDrawH + i * mTheme.getTextHeight(), mTheme.getPaint());
			}
//			canvas.drawText(getViewDataStr(), getViewRecord().get(i), getViewDataStr().length(), 0,Utils.StartDrawH + i * mTheme.getTextHeight(), mTheme.getPaint());
			canvas.restore();
		}
	}
	
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

	/*@Override
	public void setViewDataStr(String data) {
//		return mDataInfo.data;
		mDataInfo.data = data;
	}*/

	@Override
	public String getViewDataStr() {
		return mDataInfo.getDataStr();
	}

	/*@Override
	public void setAnalyseData(AnalyseDataInfo data) {
		// TODO Auto-generated method stub
//		mDataInfo.data = data.dataString;
		mDataInfo =  
//		int filePos = data.filePos;
	}*/

	@Override
	public IPageDateInfo getAnalyseData() {
		return mDataInfo;
	}
	
	/*public void exit(){
		mDataInfo.clear();
	}*/
	
	public void saveState(SharedPreferences pre,int id){
		pre.edit().putInt(id+"x1", mDataInfo.getCurPageStartPos())
				   .putInt(id+"x2", mDataInfo.getCurPageEndPos())
				   .putInt(id+"x3", atFilePos()).commit();
	}
	
	public void restoreState(SharedPreferences pre,int id){
		mDataInfo.setCurPagePos(pre.getInt(id+"x1", 0), pre.getInt(id+"x2", 0), pre.getInt(id+"x3", 0));
	}
}
