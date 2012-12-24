package com.android.lee.View;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface IDisplayTheme{
	/**
	 * get word display width
	 * */
	public int 		getDisplayWidth();

	public void		setScreenInfo(int width, int height);
	public int 		getScreenWidth();
	public int 		getScreenHeight();
	
	public void 	setPadding(int left,int right);
	public int 		getLeftPadding();
	public int 		getTopPadding();
	public int 		getBottomPadding();
	public int 		getRightPadding();
		
	/**
	 * 单页显示的行数
	 * */
	public int 		getRowSum();
	
	public int 		getThemeId();
	public int 		getTextHeight();
	public Paint	getPaint();
	
	public void 	setThemeId(int id);
	
	public void 	setTextSize(int size);
	public void 	setTextColor(int color);
	
	public void 	drawProgress(Canvas canvas,String progress);
	
	/*public void 	sendSizeChanged(int width,int height);
	 
	public 	interface 	OnThemeChangedListener{
    	void onSizeChanged(int width,int height);
    }*/
}
