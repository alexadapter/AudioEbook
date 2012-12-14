package com.android.lee.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.lee.Activity.PageState;
import com.android.lee.Activity.PageState.OnPageStateChanged;
import com.android.lee.utils.LogHelper;
import com.android.lee.utils.Utils;
import com.iflytek.tts.R;

public class FileListLayout extends FrameLayout implements OnClickListener,OnPageStateChanged {
	private 	Button		back;
	private 	Button		menu;
	private		TextView	title;
	private		TextView	path;
	private		static 	String 	TAG = "FileListLayout";
	
	static 		boolean 	DEBUG = true;
	public FileListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		if(DEBUG)LogHelper.LOGD(TAG, "onFinishInflate");
		super.onFinishInflate();
		
		setWillNotDraw(false);
		//view init
		initView();
	}
	
	private void initView(){
		back = (Button)findViewById(R.id.back);
		menu = (Button)findViewById(R.id.menu);
		title = (TextView)findViewById(R.id.title);
		path = (TextView)findViewById(R.id.path);
		//default state 为已加载文件列表
		stateChange(PageState.DEFAULT_STATE);
		
		back.setOnClickListener(this);
		menu.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.back:
			if(pageState != null){
				pageState.updateState(PageState.STATE_FL);
			}
			break;
		case R.id.menu:
			break;
		}
	}
	
	PageState pageState;
	public void setPageState(PageState pageState){
		if(pageState != null)
			this.pageState = pageState;
	}

	public void updatePath(String path) {
		this.path.setText(path);
	}

	@Override
	public void stateChange(int state) {
		if(DEBUG)LogHelper.LOGD(TAG, "stateChange");
		if(state == PageState.STATE_FM){
			setVisibility(View.VISIBLE);
			path.setVisibility(View.VISIBLE);
			title.setText(R.string.TBI_OpenFile);
			path.setText(Utils.CurPath);
//			((FileListView)findViewById(R.id.bl_container)).setFilePath(Utils.CurPath);
		}else if(state == PageState.STATE_FL){
			setVisibility(View.VISIBLE);
			path.setVisibility(View.GONE);
			title.setText(R.string.TBI_BookShelf);
		}else if(state == PageState.STATE_RD){
			setVisibility(View.GONE);
		}
	}
}
