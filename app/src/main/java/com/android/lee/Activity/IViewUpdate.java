package com.android.lee.Activity;

import com.android.lee.View.IViewInfo.IPageDateInfo;

public interface IViewUpdate {
	//-=====FileManager
	public void needUpdateData(IPageDateInfo info,int offset,boolean isNext);
	public void updateDataSuccess();

	
	
	//===FileListView
	/**
	 * 文件路径改变
	 * @param path
	 */
	void updatePath(String path);
	//导入文件
	void importFile(String name);
//	void updateReadableData();

	void selectFileForImport();
	//打开文件,文件开始位置
	void openFileForReading(String name,int startPos);
	
	/**
	 * 给listview设置要显示的数据，并且更新到导入文件界面
	 * */
	void setImportDataToDisplay();
}

