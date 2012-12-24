package com.android.lee.View;

import java.util.ArrayList;

public interface IViewInfo {
//	public void needUpdateData(int pos,int offset);a
	
	public interface IPageDateInfo{
		public void setCurPagePos(int start,int end,int edge,int fileLen);
		
		public int getCurPageStartPos();
		
		public int getCurPageEndPos();
		public float getCurProgress();
		
		/**
		 * -1 到达文件开头处
		 * 0  文件中间
		 * 1  文件开头
		 * 2  文件结束
		 * 3  是开头又是结尾
		 * */
//		private int IsFileEdge();
		
		public boolean IsFileStart();
		
		public boolean IsFileEnd();
		
		public ArrayList<Integer> getRecordList();
		
		public String getDataStr();
		
		public void setDataStr(String dataString);
		
		public void clear();
	}
	
	public class PageDataInfo implements IPageDateInfo{
		private 	int 				fileStartPos;
		private 	int 				fileEndPos;
		private 	String 				data = "";
		private 	ArrayList<Integer>	record;
		
		//begin-1 mid0 end1,first and end 2
		private 	int					atFileEdge;
		private 	float				atFileProgress;
		public  PageDataInfo(){
			record = new ArrayList<Integer>();
		}
		
		public void setCurPagePos(int start,int end,int edge,int fileLen){
			fileEndPos = end;
			fileStartPos = start;
			atFileEdge = edge;
			atFileProgress = (float)((float)start / (float)fileLen);
		}
		
		public float getCurProgress(){
			return atFileProgress;
		}
		
		public int getCurPageStartPos(){
			return fileStartPos;
		}
		
		public int getCurPageEndPos(){
			return fileEndPos;
		}
		
		/**
		 * -1 到达文件开头处
		 * 0  文件中间
		 * 1  文件开头
		 * 2  文件结束
		 * 3  是开头又是结尾
		 * */
		private int IsFileEdge(){
			return atFileEdge;
		}
		
		public boolean IsFileStart(){
			int i = atFileEdge & 0x01;
			return i > 0;
		}
		
		public boolean IsFileEnd(){
			int i = atFileEdge & 0x02;
			return i > 0;
		}
		
		public ArrayList<Integer> getRecordList(){
			return record;
		}
		
		public String getDataStr(){
			return data;
		}
		
		public void setDataStr(String dataString){
			data = dataString;
		}
		
		public void clear(){
			record.clear();
			record = null;
			data = null;
		}
	}
	
	public ArrayList<Integer> 	getViewRecord();
	public String 				getViewDataStr();
	public IPageDateInfo 		getAnalyseData();
}

