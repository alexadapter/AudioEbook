package com.android.lee.FileInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.graphics.Paint;

import com.android.lee.View.DisplayThemeInfo;
import com.android.lee.View.IViewInfo.PageDataInfo;
import com.android.lee.utils.LogHelper;
import com.android.lee.utils.Utils;

public class FileManager implements IDataFactory{
	//一次读取数...4 * 1024-- 没有用到》。。
	private		int				mBufferSize = 4 * 1024;
	private		byte[] 			mBuffer = new byte[mBufferSize];
	
	private		int				mFileSize = -1; 
	private		int				mFileHandler = -1;
	
//	private		BufferedReader	mFileStream;
	
	private 	String			mFileType;
	private 	String 			TAG = "FileManager";
	private		boolean			DEBUG = true;
	
	private 	Object 			lock = new Object();
//	private		boolean 		running = false;
//	private		boolean 		pause = false;

	private 	Object 			readFileLock = new Object();
	
	//全部数据
	private		String 			dataStr="";
	private 	String 			mFileNameString;

	//分解后的数据
//	private		String[]		dataString = new String[3];
	//数据按行切割位置
//	private 	ArrayList<Integer> []record = new ArrayList[3];
	
	private		int 			offset = 0;
	
	private		int 			mFileLength = -1;
	
	public FileManager(){
//		clearRecord();
	}
	
	/*private boolean openFile(String fileName) throws FileNotFoundException{
		boolean ret = false;
		File file = new File(fileName);
		if(file.exists()){
			mFileType = Utils.GetFileEncode(fileName);
			if(!mFileType.equals(Utils.UNKNOW)){
				mFileSize = (int) file.length();
				mFileHandler = open(fileName,0);
				mFileLength = getLength(mFileHandler);
				ret = true;
			}
		}
		
		mFileType = Utils.GetFileEncode(fileName);
		if(!mFileType.equals(Utils.UNKNOW)){
			mFileStream = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
			if(mFileStream.markSupported()){
				if(DEBUG)
					LogHelper.LOGW(TAG, "mFileStream.markSupported()");
			}
			ret = true;
		}
		try {
			mFileStream.mark(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}*/
	
	public void exit() throws IOException{
		if(mFileHandler >= 0){
			close(mFileHandler);
			/*if(mFileStream!= null){
				mFileStream.close();
				mFileStream = null;
			}*/
		}
		mBuffer = null;
	}
	
	public String getFileType(){
		return mFileType;
	}
	
	public boolean openFile(String fileName) throws IOException, FileNotFoundException{
		boolean ret = false;
		if(mFileNameString == null || !mFileNameString.equals(fileName)){
			if(mFileHandler >= 0){
				close(mFileHandler);
				/*if(mFileStream!= null){
					mFileStream.close();
					mFileStream = null;
				}*/
			}
			File file = new File(fileName);
			if(file.exists()){
				mFileType = Utils.GetFileEncode(fileName);
				if(!mFileType.equals(Utils.UNKNOW)){
					mFileSize = (int) file.length();
					mFileHandler = open(fileName,0);
					mFileLength = getLength(mFileHandler);
					ret = true;
				}
			}
			mFileNameString = fileName;
		}else{
			ret = true;
		}
		/*mFileType = Utils.GetFileEncode(fileName);
		if(!mFileType.equals(Utils.UNKNOW)){
			mFileStream = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
			if(mFileStream.markSupported()){
				if(DEBUG)
					LogHelper.LOGW(TAG, "mFileStream.markSupported()");
			}
			ret = true;
		}*/
		/*try {
			mFileStream.mark(0);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return ret;
	}
	
	/**
	 * @author lee
	 * @param paint
	 * @param file seek offset
	 * */
/*	public boolean readFile(int offset){
		boolean ret = false;
		int readSize;
		synchronized (readFileLock) {
			readSize = read(mFileHandler,mBuffer,mBufferSize,offset);
		}
		//无法重新定位....
//		BufferedReader
		if(readSize == mBufferSize){
		}else{//读到文件结束位置了..
		}
		try {
			if(readSize > 0){
				ret = true;
				dataStr = new String(mBuffer,0,readSize,mFileType);
				if(DEBUG)
					//如果是中文，在utf-8的编码中，一个字被分割了的话...dataStr.getBytes(mFileType).length的长度将是加上了改完整的字的长度的..
					//这样的话，那只能按行读取比较靠谱了，不会出现分割的情况
					LogHelper.LOGW(TAG, "getLength = " + dataStr.getBytes(mFileType).length 
							+ "readSize=" + readSize + "fileseek=" + ftell(mFileHandler) + "dataStrgelength =" + dataStr.length());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			ret = false;
		} 
		if(DEBUG)
			LogHelper.LOGW(TAG, "dataStr=" + dataStr);
		if(readSize > 0){
			ret = true;
		}
		return ret;
	}*/
	
/*	private String catString(String src1,String src2){
		StringBuilder sb = new StringBuilder(src1.length() + src2.length()); // well estimated buffer  
		sb.append(src1);
		sb.append(src2);
		return sb.toString();
	}*/
	
	/**
	 * @author lee
	 * @param paint 测试代码，可以已读取一行文本的方式读取..c代码已经实现
	 * 转到转去还是用java处理好了，因为要根据paint来划分，最总还是得在java中切割》。其他的在c里面也没多大意义
	 * @param file seek offset
	 * */
/*	public boolean readFile(int offset1,int rowCount) throws IOException{
		boolean ret = true;
		int readSize = 0;
		this.offset = offset1;
		for(int i=0; i<rowCount; i++){
			synchronized (readFileLock) {
				readSize = readLine(mFileHandler,mBuffer,mBufferSize,offset);
			}
			dataStr += new String(mBuffer,0,readSize,mFileType);
			offset += readSize;
		}
		//设置本次读取到的位置
//		seek(mFileHandler, offset + , 0);
		return ret;
	}*/
	
	/*public boolean readFile(int offset1,int rowCount,int pageCount) throws IOException{
		boolean ret = true;
		int readSize = 0;
		offset = offset1;
		for(int i=0; i<pageCount * rowCount; i++){
			synchronized (readFileLock) {
				readSize = readLine(mFileHandler,mBuffer,mBufferSize,offset);
			}
			dataStr += new String(mBuffer,0,readSize,mFileType);
			offset += readSize;
		}
		//设置本次读取到的位置
//		seek(mFileHandler, offset + , 0);
		if(DEBUG)
			LogHelper.LOGW(TAG, "getLength = " + dataStr.getBytes(mFileType).length 
					+  "fileseek=" + (offset)ftell(mFileHandler) + "dataStrgelength =" + dataStr.length());
		return ret;
	}*/
	
	/*public void analyseData(PageDataInfo analyseData,Paint paint,int rowCount){
		String dataString = "";
		analyseData.getRecordList().clear();
		int end,start = offset;
		Utils.AnalyseString(analyseData.getRecordList(), dataStr, dataStr.length(), paint, DisplayThemeInfo.getDefaultTheme().getDisplayWidth(),rowCount);
		int min = rowCount < analyseData.getRecordList().size()-1 ? rowCount : analyseData.getRecordList().size()-1;
		String str = dataStr.substring(analyseData.getRecordList().get(0), analyseData.getRecordList().get(min));
		analyseData.setDataStr(str);
		try {
			end = str.getBytes(mFileType).length + start;
			analyseData.setCurPagePos(offset, end,getFileEdgeValue(start,end));
			if(dataStr.length() > 0 && dataString.length() < dataStr.length())
				dataStr = dataStr.substring(dataString.length()+1, dataStr.length());
		} catch (IndexOutOfBoundsException e) {
		}catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}*/
	
	private int getFileEdgeValue(int start ,int end){
		int isFileEdge = 0;
		if(start == 0){
			isFileEdge = isFileEdge | 0x01;
		}
		if(end == mFileLength){
			isFileEdge = isFileEdge | 0x02;
		}
		if(DEBUG)
			LogHelper.LOGW(TAG, "isFileEdge=" + isFileEdge + "start=" + start + "end=" + end + "mFileLength" + mFileLength);
		return isFileEdge;
	}
	
	//编码问题还没有处理,如果是非正常编码的情况
	public boolean readPageData(int offset1,int rowCount,PageDataInfo analyseData,Paint paint)  throws IOException {
		boolean ret = true;
		int readSize = 0;
		int start = offset1;
		offset = offset1;
		
		String data = "";
		for(int i=0; i<rowCount; i++){
			synchronized (readFileLock) {
				readSize = readLine(mFileHandler,mBuffer,mBufferSize,offset);
			}
			data += new String(mBuffer,0,readSize,mFileType);
			offset += readSize;
		}
		analyseData(analyseData,rowCount,paint,start,data);
		return ret;
	}
	
	public int ignoreOneLine(int offset){
		synchronized (readFileLock) {
			offset += readLine(mFileHandler,mBuffer,mBufferSize,offset);
		}
		return offset;
	}
	
	public boolean readLastPageData(int offset1,int rowCount,PageDataInfo analyseData,Paint paint)  throws IOException {
		boolean ret = true;
		int readSize = 0;
		int end = offset1;
		offset = offset1;
		
		String data = "";
		for(int i=0; i<rowCount; i++){
			synchronized (readFileLock) {
				readSize = readLastLine(mFileHandler,mBuffer,offset);
			}
			data = new String(mBuffer,0,readSize,mFileType) + data;
			
//			if(DEBUG) LogHelper.LOGW(TAG, "subdata=" + data + "readSize=" + readSize );
			offset -= readSize;
		}
		analyseData(analyseData,rowCount,paint,end,data,true);
		return ret;
	}
	
	private int analyseData(PageDataInfo analyseData,int rowCount,Paint paint,int end,String data,boolean last) throws UnsupportedEncodingException{
		analyseData.getRecordList().clear();
		Utils.AnalyseString(analyseData.getRecordList(), data,  paint, DisplayThemeInfo.getDefaultTheme().getDisplayWidth(),rowCount);
		//int min = rowCount < analyseData.getRecordList().size()-1 ? analyseData.getRecordList().size()-1 - rowCount : 0;
		while((rowCount+1) < analyseData.getRecordList().size()){
			analyseData.getRecordList().remove(0);
		}
		
		String subdata = data.substring(analyseData.getRecordList().get(0), analyseData.getRecordList().get(analyseData.getRecordList().size()-1));
		analyseData.setDataStr(subdata);
		int start = end - subdata.getBytes(mFileType).length ;
		
		analyseData.setCurPagePos(start, end,getFileEdgeValue(start,end));
		return end;
	}
	
	/*private boolean readSumPageData(int offset1,int rowCount,int pageCount,PageDataInfo[] analyseData,Paint paint)  throws IOException,IndexOutOfBoundsException {
		boolean ret = true;
		int readSize = 0;
		int start = offset1;
		offset = offset1;
		String data = "";
		for(int i=0; i<pageCount*rowCount; i++){
			synchronized (readFileLock) {
				readSize = readLine(mFileHandler,mBuffer,mBufferSize,offset);
			}
			data += new String(mBuffer,0,readSize,mFileType);
			offset += readSize;
		}
		for(int i=0; i<pageCount; i++){
			start = analyseData(analyseData[0],rowCount,paint,start,data);
			
			if(analyseData[0].getDataStr().length() > 0 && data.length() < analyseData[0].getDataStr().length())
				data = data.substring(analyseData[0].getDataStr().length()+1, data.length());
		}
		
		return ret;
	}*/
	
	private int analyseData(PageDataInfo analyseData,int rowCount,Paint paint,int start,String data) throws UnsupportedEncodingException{
		analyseData.getRecordList().clear();
		Utils.AnalyseString(analyseData.getRecordList(), data, data.length(), paint, DisplayThemeInfo.getDefaultTheme().getDisplayWidth(),rowCount);
		
		int max = rowCount < analyseData.getRecordList().size()-2 ? rowCount + 1: analyseData.getRecordList().size()-1;
		
		if(DEBUG) LogHelper.LOGW(TAG, "rowCount=" + rowCount + "max=" + max +"--max " + (analyseData.getRecordList().size()-1) );
		String subdata = data.substring(analyseData.getRecordList().get(0), analyseData.getRecordList().get(max));
		
		analyseData.setDataStr(subdata);
//		try {
		int end = subdata.getBytes(mFileType).length + start;
		
		analyseData.setCurPagePos(start, end,getFileEdgeValue(start,end));
		
//		if(DEBUG) LogHelper.LOGW(TAG, "subdata=" + subdata + "subzie" + subdata.getBytes(mFileType).length +  "start= " + start + "--end=" + end);
		return end;
	}
	
	/**
	 * @author lee 加载runnable
	 * */
	/*private class LodingRunnable implements Runnable {
		int startPos = 0;
		public void setSeekOffset(int offset){
			startPos = offset;
		}
		
		@Override
		public void run() {
			while (running) {
				synchronized (lock) {
					int readSize = read(mFileHandler,mBuffer,mBufferSize,startPos);
					if(readSize == mBufferSize){
						
					}else{//读到文件结束位置了..
						
					}
//					Utils.AnalyseString(mBuffer,readSize);
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (lock) {
				lock.notify();
			}
		}
	}*/
	
	static {
		System.loadLibrary("FileJni");
	}
	
	native int open(String filename,int mode);
	native int close(int handler);
	native int getLength(int handler);
	native int seek(int handler,int startPos, int seekPos);
	native int ftell(int handler);
	native int readInt(int handler,int startPos);
	native int read(int handler,byte[] buf, int size,int seekOffset);
	native int readLine(int handler,byte[] buf, int size,int seekOffset);
	native int readLastLine(int handler,byte[] buf, int seekOffset);

	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return mFileNameString;
	}

	@Override
	public int getFileSize() {
		// TODO Auto-generated method stub
		return mFileLength;
	}

}
