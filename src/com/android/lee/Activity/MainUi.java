package com.android.lee.Activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.lee.Activity.PageState.OnPageStateChanged;
import com.android.lee.Database.DBUtils;
import com.android.lee.Database.DbAccess;
import com.android.lee.FileInfo.FileManager;
import com.android.lee.FileInfo.IDataFactory;
import com.android.lee.View.DisplayThemeInfo;
import com.android.lee.View.FileListLayout;
import com.android.lee.View.FileListView;
import com.android.lee.View.IDisplayTheme;
import com.android.lee.View.IViewInfo.IPageDateInfo;
import com.android.lee.View.IViewInfo.PageDataInfo;
import com.android.lee.View.ReadingLayout;
import com.android.lee.utils.LogHelper;
import com.iflytek.tts.R;
import com.iflytek.tts.TTSUtils;
import com.iflytek.tts.TTSUtils.ITTSUTilsPlayState;

public class MainUi extends Activity implements IViewUpdate{
	private 	IDataFactory 	mFileFactroy;
	private 	ReadingLayout	mContentLayout;

	private		static 	String 	TAG = "MainUi";
	static 		boolean 		DEBUG = true;
	
	private		IDisplayTheme 	mTheme;
	//语音
	private 	TTSUtils 		ttsUtils ;
	//tts lock
	private 	Object 			readLock = new Object();
	private 	boolean  		playing = false;
	
	private		DbAccess		mDbAccess;
	private		FileListLayout  mFileListLayout;
	private		FileListView  	mFileListView;
	private 	PageState		mPageState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTheme = DisplayThemeInfo.getDefaultTheme();
		setScreenMetrics();
		//init..显示的时候要用到数据库..所以要先初始化
		mDbAccess = new DbAccess(this);
				
		//在setView前设置好宽高的主题
		setContentView(R.layout.main_activity);
		
		if(DEBUG)	LogHelper.LOGW(TAG, "onCreate");
		
		mFileListLayout = (FileListLayout)findViewById(R.id.root);
		mFileListView = (FileListView)findViewById(R.id.bl_container);
		mContentLayout = (ReadingLayout)findViewById(R.id.reading);
		
		mPageState = new PageState();
		mPageState.setStateChangedListener(stateChanged);

		mFileListView.setInterface(this,mPageState);
		mFileListLayout.setPageState(mPageState);
		
		ttsUtils = new TTSUtils();
		ttsUtils.setIstate(ttsListener);
		createMenuView();
		
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        registerReceiver(mIntentReceiver, filter);
//        mCalendar = Calendar.getInstance(TimeZone.getDefault());
//        changeStateToFL();
//        mPageState.updateState(PageState.STATE_FL);
        setImportDataToDisplay();
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
            /*if(DEBUG)	LogHelper.LOGW(TAG, "mCalendar.getTime().getHours()=" +mCalendar.get(Calendar.HOUR_OF_DAY)
            		+":"+mCalendar.get(Calendar.MINUTE));*/
            if(delayTime-- > 0){
            	if(delayTime == 0){
            		synchronized (readLock) {
	            		if(playing){
		            		onClickStop();
							playing = false;
	            		}
            		}
            	}
            }
        }
    };
    
	OnPageStateChanged stateChanged = new OnPageStateChanged() {
		@Override
		public void stateChange(int state) {
			if(DEBUG)	LogHelper.LOGW(TAG, "send to stateChanged ");
			if(mFileListView!= null){
				mFileListView.stateChange(state);
			}
			if(mFileListLayout != null){
				mFileListLayout.stateChange(state);
			}
			if(mContentLayout != null){
				mContentLayout.stateChange(state);
			}
		}
	};
	
	ITTSUTilsPlayState ttsListener = new ITTSUTilsPlayState() {
		@Override
		public void playInterrupted() {
			playing = false;
		}
		
		@Override
		public void playFinished() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					playing = true;
					if(DEBUG) LogHelper.LOGD(TAG, "playFinished");
					toNextPage();
				}
			});
		}
	};
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){   
			System.out.println("435435435456454");
		}else if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			System.out.println("kjkjhugggtvg");
		}
		 
		setScreenMetrics();
		super.onConfigurationChanged(newConfig);
	}
	
	private void setScreenMetrics(){
		// 获得屏幕大小1
		/*WindowManager manager = getWindowManager();
		int width = getWindowManager.getDefaultDisplay().getWidth();
		int height = manager.getDefaultDisplay().getHeight();
		// 获得屏幕大小2
		DisplayMetrics dMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
		int screenWidth = dMetrics.widthPixels;
	  	int screenHeight = dMetrics.heightPixels;*/
	  	mTheme.setScreenInfo(getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight());
	  	if(DEBUG)LogHelper.LOGD(TAG, "width="+getWindowManager().getDefaultDisplay().getWidth()
	  			+"-height="+getWindowManager().getDefaultDisplay().getHeight());
	}

	int delayTime = 0;
	Dialog settingDialog;
	private void showDialog_Layout(Context context,String title) { 
		if(settingDialog == null){
	        LayoutInflater inflater = LayoutInflater.from(this);  
	        final View textEntryView = inflater.inflate(R.layout.dialog, null);  
	        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput);  
	        settingDialog = new Dialog(context,R.style.trsdialog);
	        settingDialog.setContentView(textEntryView);
	        //该对话框设置文件进度和播放延时时间
	        textEntryView.findViewById(R.id.sure).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    setTitle(edtInput.getText());  
                    if(edtInput.getText() != null)
                    	delayTime = Integer.valueOf(edtInput.getText().toString());
                    if(clickMenuId == MENU_ITEM_SEEK){
	                    float startPos = ((float)mFileFactroy.getFileSize() * ((float)(delayTime %101/100.0f)));
	                    if(DEBUG)LogHelper.LOGD(TAG, "startPos="+startPos + "mFileFactroy.getFileSize()=" + mFileFactroy.getFileSize());
	                    try {
	                    	if(startPos > 0)
	                    		//快进快退的时候要考虑当前位置..直接忽略掉当前这行..文件开头就不用过滤了
	                    		startPos = mFileFactroy.ignoreOneLine((int)startPos);
	                    	initFileInfo((int)startPos);
						} catch (IOException e) {
							exitByException("seek file fail");
							e.printStackTrace();
						}
	                    mContentLayout.invalidate();
                    }
                    settingDialog.dismiss();
                    edtInput.setText("");
				}
			} );
	        
	        textEntryView.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    setTitle("");
                    settingDialog.dismiss();
                    edtInput.setText("");
                    /*if(menuDialog.isShowing())
                    	menuDialog.dismiss();*/
				}
			} );
		}
//		settingDialog.setTitle(title);
		settingDialog.show();  
    }  
	
	private void onClickStop() {
    	//停止并退出
//        Tts.JniStop();
//        Tts.JniDestory();
		delayTime = 0;
		if(ttsUtils != null && ttsUtils.isPlaying()){
			ttsUtils.stopRead();
			if(DEBUG)LogHelper.LOGD(TAG, "stop read");
		}
    }

	/**
	 * tts player
	 * */
    private void onClickStart() {
    	//开启TTS播报线程
//    	Tts.startReadThread(this);
    	if(mContentLayout != null){
    		String temp = mContentLayout.getDisplayData().getDataStr();
    		if(temp != null){
    			if(DEBUG)LogHelper.LOGD(TAG, "start read");
    			playing = true;
    			ttsUtils.playSentence(temp);
    		}
    	}
    }

    /**
     * 解析文件信息
     */
    long time = System.currentTimeMillis();
	private void initFileInfo(String fileName,int startPos) throws IOException{
		if(mFileFactroy == null)
			mFileFactroy = new FileManager();
		//open file
		if(!mFileFactroy.openFile(fileName)){
			exitByException("file open fail");
		}
		initFileInfo(startPos);
		//数据库出错
		/*if(startPos > mFileFactroy.getFileSize()){
			startPos = 0;
			ContentValues contentValue = new ContentValues();
            contentValue.put(DBUtils.COLUMN_POS, 0);
			mDbAccess.update(DBUtils.TABLE_URLS, contentValue, DBUtils.COLUMN_PATH + "  like ? ", new String[]{fileName});
		}
		if(DEBUG)
			LogHelper.LOGD(TAG, "analyse start");
		time = System.currentTimeMillis();
		
		//第一次只需要两张就可以了
		mFileFactroy.readPageData(startPos,mTheme.getRowSum(), mContentLayout.getAnalyseData(0), mTheme.getPaint());
		
		if(!mContentLayout.getAnalyseData(0).IsFileEnd())
			mFileFactroy.readPageData(mContentLayout.getAnalyseData(0).getCurPageEndPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(2), mTheme.getPaint());
		if(!mContentLayout.getAnalyseData(0).IsFileStart())
			mFileFactroy.readLastPageData(mContentLayout.getAnalyseData(0).getCurPageStartPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(1), mTheme.getPaint());*/
		//readline readbuff 慢2倍
		if(DEBUG) LogHelper.LOGD(TAG, "analyse end time" + (System.currentTimeMillis() - time));
	}
	
	private void initFileInfo(int startPos) throws IOException{
		//数据库出错
		if(startPos > mFileFactroy.getFileSize()){
			startPos = 0;
			ContentValues contentValue = new ContentValues();
            contentValue.put(DBUtils.COLUMN_POS, 0);
			mDbAccess.update(DBUtils.TABLE_URLS, contentValue, DBUtils.COLUMN_PATH + "  like ? ", new String[]{mFileFactroy.getFileName()});
		}
		time = System.currentTimeMillis();
		if(DEBUG) LogHelper.LOGD(TAG, "analyse startPos=" + startPos + "mFileFactroy.getFileSize()" + mFileFactroy.getFileSize());
		
		if(startPos >= mFileFactroy.getFileSize())
			mFileFactroy.readLastPageData(startPos,mTheme.getRowSum(), mContentLayout.getAnalyseData(0), mTheme.getPaint());
		else
			mFileFactroy.readPageData(startPos,mTheme.getRowSum(), mContentLayout.getAnalyseData(0), mTheme.getPaint());
		
		if(!mContentLayout.getAnalyseData(0).IsFileEnd())
			mFileFactroy.readPageData(mContentLayout.getAnalyseData(0).getCurPageEndPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(2), mTheme.getPaint());
		if(!mContentLayout.getAnalyseData(0).IsFileStart())
			mFileFactroy.readLastPageData(mContentLayout.getAnalyseData(0).getCurPageStartPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(1), mTheme.getPaint());
		//readline readbuff 慢2倍
		if(DEBUG) LogHelper.LOGD(TAG, "analyse end time" + (System.currentTimeMillis() - time));
	}
	
	@Override
	protected void onStop() {
		onClickStop();
		super.onStop();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if(mPageState.getState() == PageState.STATE_RD){
			switch(action){
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					int y = (int) event.getY();
					if(y < DisplayThemeInfo.getDefaultTheme().getScreenHeight() /3){
						toPriPage();
					}else if(y > 2 * DisplayThemeInfo.getDefaultTheme().getScreenHeight() /3){
						toNextPage();
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					break;
			}
			return true;
		}else{
			return super.onTouchEvent(event);
		}
	}
	
	private void toPriPage(){
		if(!mContentLayout.toPri()){
			Toast.makeText(this, "is File Begin", Toast.LENGTH_LONG).show();
		}
	}
	
	private void toNextPage(){
		if(!mContentLayout.toNext()){
			Toast.makeText(this, "is File End", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		if(DEBUG){
			LogHelper.LOGW(TAG, "onDestroy");
		}
		if(mDbAccess != null){
			mDbAccess.closeDB();
		}
		if(mIntentReceiver != null)
			unregisterReceiver(mIntentReceiver);
		
		if(mContentLayout != null)
			mContentLayout.exit();
		if(mFileListView != null)
			mFileListView.exit();
		
		if(mFileFactroy != null){
			try {
				mFileFactroy.exit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if(DEBUG){
			LogHelper.LOGW(TAG, "onBackPressed");
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(DEBUG){
			LogHelper.LOGW(TAG, "onKeyUp");
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			if(mPageState.getState() == PageState.STATE_RD){//其他两种状态在view里面处理onkeyup
				/*if(settingDialog.isShowing()){
					settingDialog.dismiss();
				}else if(menuDialog.isShowing()){
					menuDialog.dismiss();
				}else */{
					if(mContentLayout != null){
						String firstLine = mContentLayout.getFirstLine();
						int pos = mContentLayout.getDisplayData().getCurPageStartPos();
						//this data save
						saveFileReadingPos( mFileFactroy.getFileName(),firstLine, pos);
	//					mFileListView.updateLastReadingPos(pos,firstLine);
						//不需要更新数据，只是界面更新
						mPageState.updateState(PageState.STATE_FL);
						return true;
					}
				}
			}
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
			if (mPageState.getState() == PageState.STATE_RD) {
				/*if (menuDialog == null) {
					menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
				} else {*/
				menuDialog.show();
//				}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void saveFileReadingPos(String name,String content,int pos){
		if(DEBUG)LogHelper.LOGD(TAG, "saveFileReadingPos --startPos = " + pos);
		if(mDbAccess != null){
			mDbAccess.openDB();
			Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + "  like ?  ", new String[]{name});
			if(cursor == null || cursor.getCount() == 0){
				ContentValues contentValue = new ContentValues();
	            contentValue.put(DBUtils.COLUMN_PATH, name);
	            contentValue.put(DBUtils.COLUMN_BOOKID, 1);
	            contentValue.put(DBUtils.COLUMN_DATE, mFileListView.getCurTime());
				mDbAccess.insert(DBUtils.TABLE_URLS, null, contentValue);
			}else{
				ContentValues contentValue = new ContentValues();
	            contentValue.put(DBUtils.COLUMN_POS, pos);
	            if(content != null)
	            	contentValue.put(DBUtils.COLUMN_CONTENT, content);
				mDbAccess.update(DBUtils.TABLE_URLS, contentValue, DBUtils.COLUMN_PATH + "  like ? ", new String[]{name});
			}
			
			if(cursor != null && !cursor.isClosed()){
				cursor.close();
				cursor = null;
			}
		}
	}
	
	private void exitByException(String log){
		Toast.makeText(this, log, Toast.LENGTH_SHORT).show();
		finish();
	}
	
	@Override
	public void needUpdateData(IPageDateInfo info,int offset,boolean isNext) {
		try {
			if(isNext){
				mFileFactroy.readPageData(offset, mTheme.getRowSum(), info, mTheme.getPaint());
			}else{
				mFileFactroy.readLastPageData(offset, mTheme.getRowSum(), info, mTheme.getPaint());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		synchronized (readLock) {
			if(playing){
				if(DEBUG)LogHelper.LOGD(TAG, "auto read");
				//数据更新成功了才重新播放...
				new Thread(){
					@Override
					public void run() {
						if(ttsUtils!= null && ttsUtils.isPlaying()){
							onClickStop();
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						onClickStart();
					}
				}.start();
			}
		}
	}

	@Override
	public void updateDataSuccess() {
	}

	@Override
	public void updatePath(String path) {
		//当前打开的文件路径
		mFileListLayout.updatePath(path);
	}

	@Override
	public void importFile(String name) {
		if(DEBUG)
			LogHelper.LOGE(TAG, "importFile=");
		
		if(mDbAccess != null){
			mDbAccess.openDB();

			Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + "  like ?  ", new String[]{name});
			
			if(cursor == null || cursor.getCount() == 0){
				ContentValues contentValue = new ContentValues();
	            contentValue.put(DBUtils.COLUMN_PATH, name);
	            contentValue.put(DBUtils.COLUMN_BOOKID, 1);
	            contentValue.put(DBUtils.COLUMN_DATE, mFileListView.getCurTime());
				mDbAccess.insert(DBUtils.TABLE_URLS, null, contentValue);
			}
			if(cursor != null && !cursor.isClosed()){
				cursor.close();
				cursor = null;
			}
//			changeStateToFL();
			//这里的状态通知不一定能通知出去...
			mPageState.updateState(PageState.STATE_FL);
//			setImportDataToDisplay();
		}
	}
	
	/*private void changeStateToFL(){
		if(mDbAccess != null){
			mDbAccess.openDB();
		}
		//这里的状态通知不一定能通知出去...
		mPageState.updateState(PageState.STATE_FL);
		Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + " != '' " , null);
		if(DEBUG)LogHelper.LOGE(TAG, "cursor=" + cursor.getCount());
//		mFileListView.updateList(cursor);
		setImportDataToDisplay(cursor);
		if(cursor != null && !cursor.isClosed())
			cursor.close();
		cursor = null;
	}*/

	@Override
	public void selectFileForImport() {
		mPageState.updateState(PageState.STATE_FM);
	}

	@Override
	public void setImportDataToDisplay() {
		if(mDbAccess != null){
			mDbAccess.openDB();
		}
		Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + " != '' " , null);
		if(DEBUG)LogHelper.LOGE(TAG, "cursor=" + cursor.getCount());
		mFileListView.updateList(cursor);
		if(cursor != null && !cursor.isClosed())
			cursor.close();
		cursor = null;
	}
	
	@Override
	public void openFileForReading(String name,int startPos) {
		try {
			mPageState.updateState(PageState.STATE_RD);
			if(DEBUG)LogHelper.LOGD(TAG, "openFile startPos = " + startPos);
			initFileInfo(name,startPos);
			mContentLayout.invalidate();
		} catch (IOException e) {
			exitByException("file init error");
			e.printStackTrace();
		}
	}
	
	//==========菜单=====================
	AlertDialog menuDialog;// menu菜单Dialog
	GridView menuGrid;
	View menuView;
	
	private final int MENU_ITEM_READ = 0;// 播放
	private final int MENU_ITEM_STOP = 1;// 停止
	private final int MENU_ITEM_TIME = 2;// 定时
	private final int MENU_ITEM_SEEK = 3;// 跳转
	private final int MENU_ITEM_BOOKMARK = 4;// 添加书签
//	private final int MENU_ITEM_DELETEALL = 5;// 删除所有
//	private final int MENU_ITEM_DELETECUR = 6;// 删除当前
	private int		clickMenuId = -1;
	private final String 	TIME_TITLE = "设置延时时间(分)";
	private final String 	PROGRESS_TITLE = "设置进度百分比";

	/** 菜单图片 **/
	int[] menu_image_array = { R.drawable.menu_status_play,
								R.drawable.menu_status_pause,
								R.drawable.menu_status_settime,
								R.drawable.menu_seek,
								R.drawable.menu_bookmark,
								};
	/*R.drawable.menu_delete_all,
	R.drawable.menu_delete_cover*/
	/** 菜单文字 **/
	String[] menu_name_array = { "播放", "停止", "定时","跳转","书签"};
	private void createMenuView(){
		menuView = View.inflate(this, R.layout.menu_gridview, null);
		menuDialog = new AlertDialog.Builder(this).create();
		menuDialog.setView(menuView);
		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		/** 监听menu选项 **/
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				switch (arg2) {
				case MENU_ITEM_READ:// 播放
					synchronized (readLock) {
						onClickStart();
					}
					menuDialog.dismiss();
					break;
				case MENU_ITEM_STOP://停止
					synchronized (readLock) {
						onClickStop();
						playing = false;
					}
					menuDialog.dismiss();
					break;
				case MENU_ITEM_TIME://定时
					showDialog_Layout(MainUi.this,TIME_TITLE);
					break;
				case MENU_ITEM_SEEK:
					showDialog_Layout(MainUi.this,PROGRESS_TITLE);
					break;
				case MENU_ITEM_BOOKMARK:
					break;
				}
				clickMenuId = arg2;
			}
		});
	}
	
	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.menu_grid_item, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;
	}
}
