package com.android.lee.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

public class Utils {
	//由于系统基于字体的底部来绘制文本，所有需要加上字体的高度。
	public  static 	int		StartDrawH;
//	public  static 	int		View_Theme;
//	/ReadEbook
	public 	static  final  String  MAIN_PATH = "/mnt/sdcard";
	public  static  String  CurPath = MAIN_PATH;
	
	private static 	String  TAG = "Utils";
	private static 	boolean	DEBUG = true;
	
	public final static int DIRECTORY = 0;
	public final static int TXT = 1;
	public final static int HTM = 2;
//	public final static int MOVIE = 3;
//	public final static int MUSIC = 4;
	public final static int PHOTO = 3;
//	public final static int APK = 6;
	public final static int ZIP = 4;
	public final static int UNKNOW = 5;
	public final static String UNKNOWTYPE = "Unknown";
	
	// 对应文件后缀的图标序号
	public static final int GetFileIcon(File file){
		if (file.isDirectory())
			return DIRECTORY;
		String name = file.getName();
		if (name == null){
			return UNKNOW;
		}
		name = name.toLowerCase();
		int i = name.lastIndexOf(".");
		if(i+1 < name.length() && i > -1)
			name = name.substring(i+1, name.length());
		else{
			return UNKNOW;
		}
		if(DEBUG)LogHelper.LOGD(TAG, name);
		if (name.equals("txt") || name.equals("doc") || name.equals("pdf")) {
			return  TXT;
		} else if (name.equals("html") || name.equals("htm") ||
				name.equals("chm") || name.equals("xml")){
			return  HTM;
		} else if (name.equals("jpeg") || name.equals("jpg") ||
				name.equals("bmp") || name.equals("gif") || name.equals("png")){
			return  PHOTO;
		} /*else if (name.equals("rmvb") || name.equals("rmb") || 
				name.equals("avi") || name.equals("wmv") || name.equals("mp4")
				|| name.equals("3gp") || name.equals("flv")){
			return MOVIE;
		} else if (name.equals("mp3") || name.equals("wav") || name.equals("wma")){
			return MUSIC;
		}else if (name.equals("apk")){
			return APK;
		}*/ else if (name.equals("zip") || name.equals("tar") ||
				name.equals("bar") || name.equals("bz2") || name.equals("bz")
				|| name.equals("gz") || name.equals("rar")) {
			return ZIP;
		}
		return UNKNOW;
	}
	
	public static final int GetFileIcon(String  name){
		if (new File(name).isDirectory())
			return DIRECTORY;
		if (name == null){
			return UNKNOW;
		}
		name = name.toLowerCase();
		int i = name.lastIndexOf(".");
		if(i+1 < name.length() && i > -1)
			name = name.substring(i+1, name.length());
		else{
			return UNKNOW;
		}
		if(DEBUG)LogHelper.LOGD(TAG, name);
		if (name.equals("txt") || name.equals("doc") || name.equals("pdf")) {
			return  TXT;
		} else if (name.equals("html") || name.equals("htm") ||
				name.equals("chm") || name.equals("xml")){
			return  HTM;
		} else if (name.equals("jpeg") || name.equals("jpg") ||
				name.equals("bmp") || name.equals("gif") || name.equals("png")){
			return  PHOTO;
		} /*else if (name.equals("rmvb") || name.equals("rmb") || 
				name.equals("avi") || name.equals("wmv") || name.equals("mp4")
				|| name.equals("3gp") || name.equals("flv")){
			return MOVIE;
		} else if (name.equals("mp3") || name.equals("wav") || name.equals("wma")){
			return MUSIC;
		}else if (name.equals("apk")){
			return APK;
		}*/ else if (name.equals("zip") || name.equals("tar") ||
				name.equals("bar") || name.equals("bz2") || name.equals("bz")
				|| name.equals("gz") || name.equals("rar")) {
			return ZIP;
		}
		return UNKNOW;
	}
	
	
	public   static String GetFileEncode(String fileName){
		String ret = UNKNOWTYPE;
		/*------------------------------------------------------------------------ 
		  detector是探测器，它把探测任务交给具体的探测实现类的实例完成。 
		  cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 
		  加进来，如ParsingDetector、 JChardetFacade、ASCIIDetector、UnicodeDetector。   
		  detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的 
		  字符集编码。 
		--------------------------------------------------------------------------*/  
		info.monitorenter.cpdetector.io.CodepageDetectorProxy detector =  
				info.monitorenter.cpdetector.io.CodepageDetectorProxy.getInstance();  
		/*------------------------------------------------------------------------- 
		  ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于 
		  指示是否显示探测过程的详细信息，为false不显示。 
		---------------------------------------------------------------------------*/  
//		detector.add(new info.monitorenter.cpdetector.io.ParsingDetector(false));   
		/*-------------------------------------------------------------------------- 
		  JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码 
		  测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以 
		  再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。 
		 ---------------------------------------------------------------------------*/   
		detector.add(info.monitorenter.cpdetector.io.JChardetFacade.getInstance());  
		//ASCIIDetector用于ASCII编码测定  
//		detector.add(info.monitorenter.cpdetector.io.ASCIIDetector.getInstance());  
		//UnicodeDetector用于Unicode家族编码的测定  
//		detector.add(info.monitorenter.cpdetector.io.UnicodeDetector.getInstance());  
		java.nio.charset.Charset charset = null;  
		File f=new File(fileName);  
		try {  
		      charset = detector.detectCodepage(f.toURL());  
		} catch (Exception ex) {
			ex.printStackTrace();
		}  
		
		if(charset!=null){  
		     System.out.println(f.getName()+"编码是："+charset.name());  
		     ret = charset.name();
		}else  {
		    System.out.println(f.getName()+"未知");
		}
		return ret;
	}
	
	public static String GetFileEncode(String fileName ,boolean flag) throws IOException{
		InputStream inputStream = new FileInputStream(fileName);  
        byte[] head = new byte[3];  
        inputStream.read(head);   
        String code = "";  
        code = "gb2312";  
        if (head[0] == -1 && head[1] == -2 )  
            code = "UTF-16";  
        if (head[0] == -2 && head[1] == -1 )  
            code = "Unicode";  
        if(head[0]==-17 && head[1]==-69 && head[2] ==-65)  
            code = "UTF-8";
        System.out.println(code);
		return code; 
	}
	
	public static void AnalyseString(List<Integer> record,byte[] content,int length, Paint p, int width,int rowCount){
		int enterPos=-1,tabPos=-1, breakPos,start = 0; 
		boolean needSearchEnter = true,needSearchTab = true;
		int line = 0;
		while(start < length && line < rowCount){
			if(enterPos == -1 && needSearchEnter){
				for(int i=start; i<content.length; i++){
					if(content[i] == 0x0d){
						enterPos = i;
						break;
					}
				}
//				enterPos = content.indexOf(0x0d, start);
				if(enterPos < 0){
					needSearchEnter = false;
				}
			}
			if(tabPos == -1 && needSearchTab){
//				tabPos = content.indexOf(0x0a, start);
				for(int i=start; i<content.length; i++){
					if(content[i] == 0x0a){
						tabPos = i;
						break;
					}
				}
				if(tabPos < 0){//找不到之后就不查找了..
					needSearchTab = false;
				}
			}
			/*try {
				int tt = content.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}*/
			
			breakPos = 0;//p.breakText(content, start, length, true, width, null);
			record.add(start);
			line++;
//			Utils.DEBUG(TAG, "breakPos="+breakPos + "enterPos=" + enterPos + "tabPos" + tabPos + "start=" + start);
			if(needSearchEnter && needSearchTab){//\r\n
				if(enterPos < tabPos){
					if(start+breakPos > enterPos){
						if(enterPos + 1 == tabPos){
							start = enterPos + 2;
							enterPos = -1;
							tabPos = -1;
						}else{
							start = enterPos + 1;
							enterPos = -1;
						}
					}else{
						start += breakPos;
					}
				}else{
					if(start+breakPos > tabPos){
						start = tabPos + 1;
						tabPos = -1;
					}else{
						start += breakPos;
					}
				}
			}else if(needSearchEnter && !needSearchTab){
				if(start+breakPos > enterPos){
					start = enterPos + 1;
					enterPos = -1;
				}else{
					start += breakPos;
				}
			}else if(!needSearchEnter && needSearchTab){
				if(start+breakPos > tabPos){
					start = tabPos + 1;
					tabPos = -1;
				}else{
					start += breakPos;
				}
			}else{
				start += breakPos;
			}
		}
		if(start >= length)//if read end to exit will add last line
			record.add(length);
	}
	
	/**
	 * @return left over 剩下来的字符串
	 * */
	public static int AnalyseOneLine(List<Integer> record,String content,int start, Paint p, int width,int rowCount){
		int  breakPos,ret = -1; 
		int line = 0,length = content.length() + start;
		record.add(start);
		while(start < length && line < rowCount){
			breakPos = p.breakText(content, start, content.length(), true, width, null);
			start = start + breakPos;
			record.add(start);
			line++;
//			Utils.DEBUG(TAG, "breakPos="+breakPos + "enterPos=" + enterPos + "tabPos" + tabPos + "start=" + start);
		}
		if(start >= length)//if read end to exit will add last line
			record.add(length);
		else{
			ret = length - start;
		}
		return -1;
	}
	
	public static void AnalyseString(List<Integer> record,String content,Paint p, int width,int rowCount){
		int enterPos=-1,tabPos=-1, breakPos,start = 0,length = content.length(); 
		boolean needSearchEnter = true,needSearchTab = true;
		int line = 0;
//		int []lineInfo = new int[rowCount];

//		if(DEBUG)
//			LogHelper.LOGD(TAG, "content=" + content);
		record.add(start);
		while(start < length /*&& line < rowCount*/){
			if(enterPos == -1 && needSearchEnter){
				enterPos = content.indexOf(0x0d, start);
				if(enterPos < 0){
					needSearchEnter = false;
				}
			}
			if(tabPos == -1 && needSearchTab){
				tabPos = content.indexOf(0x0a, start);
				if(tabPos < 0){//找不到之后就不查找了..
					needSearchTab = false;
				}
			}
//			if(DEBUG)
//				LogHelper.LOGD(TAG, "enterPos=" + enterPos + "tabPos" + tabPos + "start=" + start + "length="+length);
			breakPos = p.breakText(content, start, length, true, width, null);
//			lineInfo[line] = length-start;
			line++;
			if(needSearchEnter && needSearchTab){//\r\n
				if(enterPos < tabPos){
					if(start+breakPos > enterPos){
						if(enterPos + 1 == tabPos){
							start = enterPos + 2;
							enterPos = -1;
							tabPos = -1;
						}else{
							start = enterPos + 1;
							enterPos = -1;
						}
					}else{
						start += breakPos;
					}
				}else{
					if(start+breakPos > tabPos){
						start = tabPos + 1;
						tabPos = -1;
					}else{
						start += breakPos;
					}
				}
			}else if(needSearchEnter && !needSearchTab){
				if(start+breakPos > enterPos){
					start = enterPos + 1;
					enterPos = -1;
				}else{
					start += breakPos;
				}
			}else if(!needSearchEnter && needSearchTab){
				if(start+breakPos > tabPos){
					start = tabPos + 1;
					tabPos = -1;
				}else{
					start += breakPos;
				}
			}else{
				start += breakPos;
			}
			record.add(start);
//			record.add(length-start);
		}
		/*if(start >= length)//if read end to exit will add last line
			record.add(length);*/
		/*逆向取数据
		 * while(record.size() > rowCount){
			record.remove((Integer)0);
		}*/
	}
	
	public static void AnalyseString(List<Integer> record,String content,int length, Paint p, int width,int rowCount){
		int enterPos=-1,tabPos=-1, breakPos,start = 0; 
		boolean needSearchEnter = true,needSearchTab = true;
		int line = 0;
		record.add(start);
		while(start < length && line < rowCount){
			if(enterPos == -1 && needSearchEnter){
				enterPos = content.indexOf(0x0d, start);
				if(enterPos < 0){
					needSearchEnter = false;
				}
			}
			if(tabPos == -1 && needSearchTab){
				tabPos = content.indexOf(0x0a, start);
				if(tabPos < 0){//找不到之后就不查找了..
					needSearchTab = false;
				}
			}
			breakPos = p.breakText(content, start, length, true, width, null);
			line++;
//			if(DEBUG)
//				LogHelper.LOGD(TAG, "breakPos="+breakPos + "enterPos=" + enterPos + "tabPos" + tabPos + "start=" + start);
			if(needSearchEnter && needSearchTab){//\r\n
				if(enterPos < tabPos){
					if(start+breakPos > enterPos){
						if(enterPos + 1 == tabPos){
							start = enterPos + 2;
							enterPos = -1;
							tabPos = -1;
						}else{
							start = enterPos + 1;
							enterPos = -1;
						}
					}else{
						start += breakPos;
					}
				}else{
					if(start+breakPos > tabPos){
						start = tabPos + 1;
						tabPos = -1;
					}else{
						start += breakPos;
					}
				}
			}else if(needSearchEnter && !needSearchTab){
				if(start+breakPos > enterPos){
					start = enterPos + 1;
					enterPos = -1;
				}else{
					start += breakPos;
				}
			}else if(!needSearchEnter && needSearchTab){
				if(start+breakPos > tabPos){
					start = tabPos + 1;
					tabPos = -1;
				}else{
					start += breakPos;
				}
			}else{
				start += breakPos;
			}
			record.add(start);
		}
		/*if(start >= length)//if read end to exit will add last line
			record.add(length);
		else{
			record.add(start);
		}*/
	}
	
	/**
	 * 自动分割文本
	 * @param content 需要分割的文本
	 * @param p  画笔，用来根据字体测量文本的宽度
	 * @param width 指定的宽度
	 * @return 一个字符串数组，保存每行的文本
	 */
	private String[] autoSplit(String content, Paint p, float width) {
		int length = content.length();
		float textWidth = p.measureText(content);
		if(textWidth <= width) {
			return new String[]{content};
		}
		
		int start = 0, end = 1, i = 0;
		int lines = (int) Math.ceil(textWidth / width);//计算行数
		String[] lineTexts = new String[lines];
		//单词不拆分
		/*if( (data[i]>='a' && data[i]<='z') || (data[i]>='A' && data[i]<='Z')
			|| (data[i]>='0' && data[i]<='9') || data[i]=='.'){
			if(!bWordStart){
				bWordStart = true;
				iWordStart = i;
			}
		}else if(bWordStart)
			bWordStart = false;*/
		
		int wordSum = p.breakText(content, true, width, null);
		
		while(start < length) {
			if(p.measureText(content, start, end) > width) { //文本宽度超出控件宽度时
				lineTexts[i++] = (String) content.subSequence(start, end);
				start = end;
			}
			if(end == length) { //不足一行的文本
				lineTexts[i] = (String) content.subSequence(start, end);
				break;
			}
			end += 1;
		}
		return lineTexts;
	}
	
	/**
	 * textView的换行处理...
	 * */
	private void textSplit(){
		TextPaint textPaint = new TextPaint();

		textPaint.setARGB(0xFF, 0xFF, 0, 0);
		textPaint.setTextSize(20.0F);
		String aboutTheGame = "关于本游戏：本游戏是做测试用的，这些文字也是，都不是瞎写的！ ";

		/**
		* aboutTheGame ：要 绘制 的 字符串   ,textPaint(TextPaint 类型)设置了字符串格式及属性 的画笔,240为设置 画多宽后 换行，后面的参数是对齐方式...
		*/
		Canvas can = new Canvas();
		StaticLayout layout = new StaticLayout(aboutTheGame,textPaint,240,Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
		//从 (20,80)的位置开始绘制
		can.translate(20,80);
		layout.draw(can);
	}
	
	/**
	 * 获取指定单位对应的原始大小（根据设备信息）
	 * px,dip,sp -> px
	 * 
	 * Paint.setTextSize()单位为px
	 * 
	 * 代码摘自：TextView.setTextSize()
	 * 
	 * @param unit  TypedValue.COMPLEX_UNIT_*
	 * @param size
	 * @return
	 */
	/*public float getRawSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();
        
        return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }*/
	
	/**
	 * 转换文件大小
	 * */
	public static String ConvertFileSize(long filesize) { 
		String strUnit="Bytes"; 
		String strAfterComma=""; 
		int intDivisor=1; 
		if(filesize>=1024*1024) { 
			strUnit = "MB"; 
			intDivisor=1024*1024; 
		}else if(filesize>=1024) { 
			strUnit = "KB"; 
			intDivisor=1024; 
		} 
		if(intDivisor==1) 
			return filesize + " " + strUnit; 
		strAfterComma = "" + 100 * (filesize % intDivisor) / intDivisor ; 
		if(strAfterComma=="") 
			strAfterComma=".0"; 
		return filesize / intDivisor + "." + strAfterComma + " " + strUnit; 

	} 
}
