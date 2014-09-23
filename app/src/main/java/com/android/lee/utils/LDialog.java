package com.android.lee.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.iflytek.tts.R;

/**
 * 用于listview弹出删除之类菜单的，菜单可以动态加载和delete
 * */
public class LDialog extends Dialog{
	RelativeLayout viewGroup;
	Context		context;
	public LDialog(Context context) {
		super(context,R.style.trsdialog);
		this.context = context;
//		LayoutInflater inflater = LayoutInflater.from(context);  
//		viewGroup = (ViewGroup)inflater.inflate(R.layout.dialog, null); 
		viewGroup = new RelativeLayout(context);
		viewGroup.setLayoutParams(new RelativeLayout.LayoutParams(200,RelativeLayout.LayoutParams.WRAP_CONTENT));
		viewGroup.setBackgroundResource(R.drawable.book_shadow);
		setContentView(viewGroup);
		viewGroup.removeAllViews();
	}
	
	public void addView(View view){
		viewGroup.addView(view);
	}
	
	/**
	 * 给一般对话框用的...
	 * */
	public View addView(int res){
		//使用merge的layout，必须这样使用attchtoroot设置为true
		View view = LayoutInflater.from(context).inflate(res, viewGroup, true);
		return view;
	}
}
