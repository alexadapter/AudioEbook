package com.android.lee.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Paint;

import com.android.lee.View.IViewInfo.PageDataInfo;

public interface IDataFactory {
	public boolean 				openFile(String fileName) throws IOException, FileNotFoundException;
	
	public String 				getFileName();
	public int 					getFileSize();
	
	public void 				exit() throws IOException;
	public String 				getFileType();
//	public void 				changFile(String fileName) throws FileNotFoundException, IOException;
//	public boolean 				readFile(int offset);
//	public boolean 				readFile(/*ArrayList<Integer> record,Paint paint,*/int offset,int rowCount) throws IOException;
//	public boolean 				readFile(int offset,int rowCount,int pageCount) throws IOException;
//	public void 				analyseData(PageDataInfo data,Paint paint,int rowCount);
	
	public boolean 				readPageData(int offset1,int rowCount,PageDataInfo analyseData,Paint paint)  throws IOException;
	public boolean 				readLastPageData(int offset1,int rowCount,PageDataInfo analyseData,Paint paint)  throws IOException;
	//忽略掉第一行，因为跳转的时候不知道这位置是否是中文还是英文，也不知道是什么编码》。。
	public int 					ignoreOneLine(int offset);
}
