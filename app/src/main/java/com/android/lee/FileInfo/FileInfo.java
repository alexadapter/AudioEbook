package com.android.lee.FileInfo;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import com.android.lee.utils.Utils;

public class FileInfo {
	/**
	 * 文件类型 区分非书籍文件
	 * */
	private 	int		type = 9;
	
	/**
	 * 文件名
	 * */
	private		String  name;
	private 	String	sizeAndContent;
	private 	String  absolutePath;
	private 	String 	lastModifyDate;
	private 	int		lastReadingPos = 0;
	
	public FileInfo(File file,DateFormat format){
		this.absolutePath = file.getAbsolutePath();
		this.type = Utils.GetFileIcon(file);
		this.name = file.getName();
		this.sizeAndContent = Utils.ConvertFileSize(file.length());
		lastModifyDate = format.format( new Date(file.lastModified()));
	}
	
	/**
	 * 和导入书籍共用
	 * @param name 书籍名
	 * @param date 最后阅读时间
	 * @param content 最后阅读位置的文件
	 * @param uiid 给该书陪的封面 ..随机给好了
	 * */
	public FileInfo(String absolutePath,String date, String content,int lastReadPos){
		this.absolutePath = absolutePath;
		this.name = new File(absolutePath).getName();
		this.sizeAndContent = content;
		lastModifyDate = date;
		lastReadingPos = lastReadPos;
	}
	
	public FileInfo(String name,String date){
		this.name = name;
		lastModifyDate = date;
	}
	
	/*
	public FileInfo(String path,String name,int type,long size,String date){
		this.absolutePath = path;
		this.type = type;
		this.name = name;
		this.size = convertFileSize(size);
		lastModifyDate = date;
	}*/
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	public void setLastReadingPos(int pos){
		lastReadingPos = pos;
	}
	
	public int getLastReadingPos(){
		return lastReadingPos;
	}
	
	public String getName(){
		if(name != null)
			return name;
		else
			return "";
	}
	
	public String getAbsolutePath(){
		if(absolutePath != null)
			return absolutePath;
		else
			return "";
	}
	
	public String getDate(){
		if(lastModifyDate != null)
			return lastModifyDate;
		else 
			return "";
	}
	
	/*public void setBookmark(String bookmark){
		this.bookmark = bookmark;
	}
	
	public String getBookmark(){
		return bookmark;
	}*/
	
	public String getSizeOrContent(){
		if(sizeAndContent != null)
			return sizeAndContent;
		else 
			return "";
	}
	
	public void setSizeOrContent(String sizeorContent){
		sizeAndContent = sizeorContent;
	}
	
//	long filesize=objFile.getLength(); 

}
