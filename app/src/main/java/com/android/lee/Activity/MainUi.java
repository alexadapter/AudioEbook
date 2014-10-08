package com.android.lee.Activity;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.android.lee.View.ReadingLayout;
import com.android.lee.utils.LDialog;
import com.android.lee.utils.LogHelper;
import com.android.lee.utils.Utils;
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
	private		float 			delayTime = 0;
	private		LDialog 			settingDialog;

    private GestureDetector mGestureDetector;
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
        mGestureDetector = new GestureDetector(listener);
//		createMenuView();
		
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        registerReceiver(mIntentReceiver, filter);
//        mCalendar = Calendar.getInstance(TimeZone.getDefault());
//        changeStateToFL();
        
        if(!openwithIntent(getIntent())){//默认的
        	mPageState.updateState(PageState.DEFAULT_STATE);
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if(!openwithIntent(intent)){//默认的
        }
		super.onNewIntent(intent);
	}
	
	private boolean openwithIntent(Intent intent){
		boolean ret = false;
		Uri uri=(Uri)intent.getData();
		if(uri != null){
			String path=uri.getPath();
			LogHelper.LOGD(TAG, "path=" + path);
			if(path != null){
				File file = new File(path);
				if(file != null && file.exists() ){
					if(Utils.TXT == Utils.GetFileIcon(file)){
						if(mDbAccess != null){
							mDbAccess.openDB();
							int lastReadPos = 0;
							Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + "  like ?  ", new String[]{path});
							if(cursor == null || cursor.getCount() == 0){//如果是没有的书，就添加
								ContentValues contentValue = new ContentValues();
					            contentValue.put(DBUtils.COLUMN_PATH, path);
					            contentValue.put(DBUtils.COLUMN_BOOKID, 1);
					            contentValue.put(DBUtils.COLUMN_DATE, mFileListView.getCurTime());
								mDbAccess.insert(DBUtils.TABLE_URLS, null, contentValue);
							}else{
								try{//已经存在的书就直接打开，从之前的阅读位置
									cursor.moveToFirst();
									lastReadPos = cursor.getInt(cursor.getColumnIndexOrThrow(DBUtils.COLUMN_POS));
								}catch (IllegalArgumentException e) {
									LogHelper.LOGE(TAG, "sqlite date get error");
								}
							}
							if(cursor != null && !cursor.isClosed()){
								cursor.close();
								cursor = null;
							}
							if(DEBUG)
								LogHelper.LOGE(TAG, "importFile= send stateChanged");
							setImportDataToDisplay();
							openFileForReading(path,lastReadPos);
//							changeStateToFL();
							//这里的状态通知不一定能通知出去...
							//mPageState.updateState(PageState.STATE_FL);
//							setImportDataToDisplay();
						}
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
            if(DEBUG)	LogHelper.LOGW(TAG, "mCalendar.getTime().getHours()=" );
            if(delayTime-- > 0){
            	if(delayTime <= 0){
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
		public void stateChange(int state,boolean isChanged) {
			if(DEBUG)	LogHelper.LOGW(TAG, "send to stateChanged ");
			if(mFileListView!= null){
				mFileListView.stateChange(state,isChanged);
			}
			if(mFileListLayout != null){
				mFileListLayout.stateChange(state,isChanged);
			}
			if(mContentLayout != null){
				mContentLayout.stateChange(state,isChanged);
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
		
		try {//主题更新后读取新的文件信息...
			initFileInfo(mContentLayout.getDisplayData().getCurPageStartPos());
		} catch (IOException e) {
			e.printStackTrace();
			exitByException("File error");
		}
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

	private void showDialog_Layout(Context context,String title) { 
		if(settingDialog == null){
	        /*LayoutInflater inflater = LayoutInflater.from(this);  
	        final View textEntryView = inflater.inflate(R.layout.dialog, null);  
	        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput);  
	        settingDialog = new Dialog(context,R.style.trsdialog);
	        settingDialog.setContentView(textEntryView);*/
//			LayoutInflater inflater = LayoutInflater.from(this);  
	        /*final View textEntryView = inflater.inflate(R.layout.settingsdialog, null);  
	        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput);  */
	        settingDialog = new LDialog(context);
	        View textEntryView = settingDialog.addView(R.layout.settingsdialog);
	        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput); 
			
	        //该对话框设置文件进度和播放延时时间
	        textEntryView.findViewById(R.id.sure).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    setTitle(edtInput.getText());
                    float var = 0;
                    if(edtInput.getText() != null)
                        var = Float.valueOf(edtInput.getText().toString());
                    if(clickMenuId == MENU_ITEM_SEEK){
	                    float startPos = ((float)mFileFactroy.getFileSize() * ((float)(var %101/100.0f)));
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
                    }else{
                        delayTime = var;
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
		
		if(!mContentLayout.getAnalyseData(0).IsFileStart())
			mFileFactroy.readLastPageData(mContentLayout.getAnalyseData(0).getCurPageStartPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(1), mTheme.getPaint());
		
		if(!mContentLayout.getAnalyseData(0).IsFileEnd())
			mFileFactroy.readPageData(mContentLayout.getAnalyseData(0).getCurPageEndPos(), 
					mTheme.getRowSum(), mContentLayout.getAnalyseData(2), mTheme.getPaint());
		
		//readline readbuff 慢2倍
		if(DEBUG) LogHelper.LOGD(TAG, "analyse end time" + (System.currentTimeMillis() - time));
	}
	
	@Override
	protected void onStop() {
//		onClickStop();
		super.onStop();
	}

    GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
	@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absX = Math.abs(velocityX);
            float absY = Math.abs(velocityY);
            if(absX > absY) {
                if (absX > 100) {
                    if (velocityX > 0) {
                        toPriPage();
                    } else if (velocityX < 0) {
                        toNextPage();
                    }
                    return true;
                }
            }else if (absY > 100)  {
                /*if(menuDialog != null && !menuDialog.isShowing()) {
                    menuDialog.show();
                    return true;
                }*/
                if(menuView != null && menuView.getParent() != null){
                    dismissMenuView();
                }else {
                    showMenuView();
                }
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            /*int y = (int) e.getY();
            if(y < DisplayThemeInfo.getDefaultTheme().getScreenHeight() /3){
                toPriPage();
            }else if(y > 2 * DisplayThemeInfo.getDefaultTheme().getScreenHeight() /3){
                toNextPage();
            }*/
            int x = (int) e.getX();
            if(x < DisplayThemeInfo.getDefaultTheme().getScreenWidth() /3){
                toPriPage();
            }else if(x > 2 * DisplayThemeInfo.getDefaultTheme().getScreenWidth() /3){
                toNextPage();
            }
			return true;
        }
    };
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if(mPageState.getState() == PageState.STATE_RD){
			return mGestureDetector.onTouchEvent(event);
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
        if(mPageState.getState() == PageState.STATE_RD){//其他两种状态在view里面处理onkeyup
            if(DEBUG)LogHelper.LOGD(TAG, "activity KEYCODE_BACK");
            if(mContentLayout != null){
                String firstLine = mContentLayout.getFirstLine();
                int pos = mContentLayout.getDisplayData().getCurPageStartPos();
                //this data save
                saveFileReadingPos( mFileFactroy.getFileName(),firstLine, pos);
//					mFileListView.updateLastReadingPos(pos,firstLine);
                //不需要更新数据，只是界面更新
//                mPageState.updateState(PageState.STATE_FL);
            }
        }
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
		if(DEBUG ){
			LogHelper.LOGW(TAG, "activity-- keyCode=" + keyCode);
//			return super.onKeyUp(keyCode, event);
		}
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mPageState.getState() == PageState.STATE_RD){//其他两种状态在view里面处理onkeyup
				if(DEBUG)LogHelper.LOGD(TAG, "activity KEYCODE_BACK");
				if(menuView != null && wm != null && menuView.getParent() != null){
					wm.removeView(menuView);
					return true;
				}
                onClickStop();
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
		}else if(keyCode == KeyEvent.KEYCODE_MENU){
			if (mPageState.getState() == PageState.STATE_RD) {
				showMenuView();
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
			exitByException("File Error");
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
			LogHelper.LOGE(TAG, "importFile="+name);
		
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
				
			}
			if(cursor != null && !cursor.isClosed()){
				cursor.close();
				cursor = null;
			}
			if(DEBUG)
				LogHelper.LOGE(TAG, "importFile= send stateChanged");
//			changeStateToFL();
			//这里的状态通知不一定能通知出去...
			mPageState.updateState(PageState.STATE_FL);
//			setImportDataToDisplay();
		}
	}
	
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
	
	@Override
	public void delDatabaseItem(String path) {
		if(mDbAccess != null){
			mDbAccess.openDB();
		}
//		if(DEBUG)LogHelper.LOGE(TAG, "cursor=" + cursor.getCount());
		mDbAccess.delete(DBUtils.TABLE_URLS, DBUtils.COLUMN_PATH  + "  like ?  ", new String[]{path});
		
		Cursor cursor = mDbAccess.rawQuery("select * from " + DBUtils.TABLE_URLS + " where " + DBUtils.COLUMN_PATH + " != '' " , null);
		mFileListView.updateList(cursor);
		if(cursor != null && !cursor.isClosed())
			cursor.close();
		cursor = null;
	}
	
	//==========菜单=====================
//	Dialog menuDialog;// menu菜单Dialog
	GridView 					menuGrid;
	View 						menuView;
	MenuAdapter					menuAdapter;
	WindowManager.LayoutParams 	menuLp;
	private WindowManager 		wm;
	
	private final int MENU_ITEM_READ = 0;// 播放
	private final int MENU_ITEM_STOP = 1;// 停止
	private final int MENU_ITEM_TIME = 2;// 定时
	private final int MENU_ITEM_SEEK = 3;// 跳转
	private final int MENU_ITEM_BOOKMARK = 4;// 添加书签
	private final int MENU_ITEM_NIGHT = 5;// 晚上
	private final int MENU_ITEM_DAY = 6;// 白天
	private final int MENU_ITEM_TEXT_ADD = 7;// 删除当前
	private final int MENU_ITEM_TEXT_DES = 8;// 删除当前
	private int		clickMenuId = -1;
	private final String 	TIME_TITLE = "设置延时时间(分)";
	private final String 	PROGRESS_TITLE = "设置进度百分比";
	
	private void showMenuView(){
		if(menuView == null){
			createMenuView();
		}else{
			if(menuView != null && wm != null && menuView.getParent() == null){
//				menuLp.flags = menuLp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				menuLp.flags = 0;//menuLp.flags & 0xf7;// WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				menuLp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						/*| WindowManager.LayoutParams.FLAG_DIM_BEHIND*/
                   | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                   | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                   | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                   | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
				wm.addView(menuView, menuLp);
			}
		}
	}
	
	private void dismissMenuView(){
		if(menuView != null && wm != null && menuView.getParent() != null){
			wm.removeView(menuView);

		}
	}
	
	/**
	 * 之所以用windowmanager只是为了测试一下其他应用的功能...
	 * */
	private void createMenuView(){
		menuView = View.inflate(this, R.layout.menu_gridview, null);
		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuAdapter = new MenuAdapter();
		menuGrid.setAdapter(menuAdapter);
		menuLp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                //add this and menuLp.dimAmount will dim behind 0.5 alpha
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            	| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,//仅在第一次down事件时可以收到 ACTION_OUTSIDE
                PixelFormat.TRANSLUCENT);
		//不设置这个之后焦点就转换到windowmanager上了,activity将不能接收事件，windowmanager里面的setOnItemClickListener之类有效果
//		| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		
		//不设置FLAG_NOT_FOCUSALBE的同时设置下面这个属性,activity将能接收事件，但是activity里面的没发处理返回,home这些键值，windowmanager里面的setOnItemClickListener之类也有效果
//		WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		
		//设置这个之后焦点就转换到windowmanager下面了只有0,1两个键值,activity将能接收事件，windowmanager里面的setOnItemClickListener之类没有效果
		//..setOnItemClickListener之类将没有效果，
		//如果不设置，下面的点击将没效果
		menuLp.dimAmount = 0.5f;
		menuLp.gravity = Gravity.BOTTOM | Gravity.CENTER;
		menuLp.setTitle("MenuPanel");
		menuLp.windowAnimations = R.style.anim_view;
		menuLp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.addView(menuView, menuLp);
        /*
        menuView.setFocusable(true);
        如果没法接收keyevent，就加上setFocusableInTouchMode为true,点击的时候焦点切换过去
        menuView.setFocusableInTouchMode(true);*/
        
        menuView.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 final int action = event.getAction();
				 if(DEBUG) LogHelper.LOGW(TAG, "menu setOnTouchListener" + action);
				 if (action == MotionEvent.ACTION_OUTSIDE){
					 /*if(menuLp.flags != (menuLp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)){
						 menuLp.flags = menuLp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
						 wm.updateViewLayout(menuView, menuLp);
						 return true;
					 }*/
					 dismissMenuView();
					 //这里return 没啥效果
//					 return false;
				 }else{
					 if(menuLp.flags == (menuLp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)){
						 menuLp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
		                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
		                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
		                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
						 wm.updateViewLayout(menuView, menuLp);
					 }
				 }
				 //是否返回给子类/外部(包括外部activity)处理..
				return false;
			}
        });
        
       menuView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(DEBUG) LogHelper.LOGW(TAG, "menu  onKey" + event.getAction() + "keyCode == " + keyCode);
				if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
					dismissMenuView();
					return true;
				}
				return false;
			}
		});
        
        /** 监听menu选项 **/
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				if(DEBUG) LogHelper.LOGW(TAG, "menu  onItemClick");
				switch (arg2) {
				case MENU_ITEM_READ:// 播放
					synchronized (readLock) {
						onClickStart();
					}
					break;
				case MENU_ITEM_STOP://停止
					synchronized (readLock) {
						onClickStop();
						playing = false;
					}
					break;
				case MENU_ITEM_TIME://定时
					showDialog_Layout(MainUi.this,TIME_TITLE);
					break;
				case MENU_ITEM_SEEK:
					showDialog_Layout(MainUi.this,PROGRESS_TITLE);
					break;
				case MENU_ITEM_BOOKMARK:
					break;
                case MENU_ITEM_NIGHT:
                    mTheme.setThemeId(IDisplayTheme.NIGHT_THEME);
                    break;
                case MENU_ITEM_DAY:
                    mTheme.setThemeId(IDisplayTheme.DAY_THEME);
                    break;
                case MENU_ITEM_TEXT_ADD:
                    mTheme.addTextSize();
                    break;
                case MENU_ITEM_TEXT_DES:
                    mTheme.desTextSize();
                    break;
				}
				clickMenuId = arg2;
				dismissMenuView();
			}
		});
	}
	
	private class MenuAdapter extends BaseAdapter{
		/** 菜单图片 and 菜单文字 **/
		private		Object[][]	Objects = {
				{R.drawable.menu_status_play,"播放"},
				{R.drawable.menu_status_pause,"停止"},
				{R.drawable.menu_status_settime,"定时"},
				{R.drawable.menu_seek,"跳转"},
				{R.drawable.menu_bookmark,"书签"},
				{R.drawable.menu_bookmark,"夜间模式"},
				{R.drawable.menu_bookmark,"白天模式"},
				{R.drawable.menu_bookmark,"字体+"},
				{R.drawable.menu_bookmark,"字体-"},
//				{R.drawable.menu_status_play,"播放"},
						};
		
		public 	MenuAdapter(){
			
		}
		
		@Override
		public int getCount() {
			if(DEBUG)LogHelper.LOGD(TAG, "getCount"+Objects.length);
			return Objects.length;
		}

		@Override
		public Object getItem(int arg0) {
				return Objects[arg0][0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View contentView, ViewGroup arg2) {
			MenuViewHolder holder;
			if(contentView == null){
				contentView = View.inflate(MainUi.this, R.layout.menu_grid_item, null);
				holder = new MenuViewHolder();
				holder.mImage = (ImageView)contentView.findViewById(R.id.item_image);
				holder.mTitle = (TextView)contentView.findViewById(R.id.item_text);
				contentView.setTag(holder);
			}else{
				holder = (MenuViewHolder) contentView.getTag();
			}
			String value = (String) Objects[arg0][1];
			if(DEBUG)LogHelper.LOGD(TAG, "arg0=" + arg0  + "getCount"+value);
			holder.mTitle.setText(value);
			holder.mImage.setImageDrawable(getResources().getDrawable((Integer) Objects[arg0][0]));
			return contentView;
		}
	}
	
	class MenuViewHolder {
		TextView 	mTitle;
		ImageView 	mImage;
	}

}
