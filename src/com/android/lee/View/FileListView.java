package com.android.lee.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lee.Activity.IViewUpdate;
import com.android.lee.Activity.PageState;
import com.android.lee.Activity.PageState.OnPageStateChanged;
import com.android.lee.Database.DBUtils;
import com.android.lee.FileInfo.FileInfo;
import com.android.lee.utils.LogHelper;
import com.android.lee.utils.Utils;
import com.android.lee.utils.LinuxCommand.LinuxFileCommand;
import com.iflytek.tts.R;

public class FileListView extends LinearLayout implements OnItemClickListener,OnPageStateChanged {
	private 	ListView 				listView;
	private 	LinuxFileCommand  		linuxCommand;
	private 	ArrayList<FileInfo>  	adapterList ;
	private		FileListAdapter			adapter;
	
	private		static 		String 		TAG = "FileListView";
	private		static 		boolean 	DEBUG = true;
	
	private 	String[] 				dTypeName;
	private 	Drawable[] 				dTypeCover = new Drawable[Utils.UNKNOW+1];
	
	private 	Drawable[] 				dTxtCover = new Drawable[Utils.UNKNOW+1];
	
	private		DateFormat 				mDateFormat;
//	private 	String 					mCurpath = "";
	
	private 	Object					clickLock = new Object();
	//文件类型
	private 	int[] 	dTypeResId = {
						R.drawable.format_01_dir,
						R.drawable.format_02_txt,
						R.drawable.format_03_html,
						R.drawable.format_04_pic,
						R.drawable.format_05_zip,
						R.drawable.format_06_comic};
	//文件封面
	private 	int[] 	dCoverResId = {
						R.drawable.bs_grid_cover_1,
						R.drawable.bs_grid_cover_2,
						R.drawable.bs_grid_cover_3,
						R.drawable.bs_grid_cover_4,
						R.drawable.bs_grid_cover_5,
						R.drawable.bs_grid_cover_6};

	private		IViewUpdate 	mNeedUpdate;
	private		PageState 		pageState;
	//显示已经导入的书籍的时候要加入一个导入书籍的界面
//	private		View 			mHeadView;
//	private 	int				mCurState = PageState.STATE_FM;
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(DEBUG)LogHelper.LOGD(TAG, "onKeyUp");
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			if(pageState.getState() == PageState.STATE_FM){
				if(!Utils.CurPath.equals("/")){
					synchronized (clickLock) {
						openNewDir(new File(Utils.CurPath).getParent());
					}
					return true;
				}
			}else if(pageState.getState() == PageState.STATE_FL){
				
			}else if(pageState.getState() == PageState.STATE_RD){//返回的时候需要更新最后阅读位置
				
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	public FileListView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		mNeedUpdate = (IViewUpdate)context;
		if(linuxCommand == null){
			linuxCommand = new LinuxFileCommand(Runtime.getRuntime());
		}
		dTypeName = getResources().getStringArray(R.array.FileTypeName);
		if(dTypeCover[0] == null){
			for(int i=0; i<dTypeResId.length; i++){
				dTypeCover[i] = getResources().getDrawable(dTypeResId[i]);
				dTypeCover[i].setCallback(null);
				dTxtCover[i] = getResources().getDrawable(dCoverResId[i]);
				dTxtCover[i].setCallback(null);
			}
		}
		
		mDateFormat = new SimpleDateFormat("yyyy.MM.dd  HH:mm");//:ss
		//这样设置之后才可以截获onkeyup键
		this.setFocusable(true);
		setFocusableInTouchMode(true);
	}
	
	protected void setFilePath(){
		if(DEBUG)LogHelper.LOGD(TAG, "setFilePath");
		File file = new File(Utils.CurPath);
		if(!file.exists()){
			try {
				linuxCommand.createDirectory(Utils.CurPath);
			} catch (IOException e) {
//				exitByException(Utils.MAIN_PATH + " 目录创建失败");
				Toast.makeText(getContext(), Utils.CurPath + " 目录创建失败", Toast.LENGTH_SHORT).show();
			}
		}
		openNewDir(Utils.CurPath);
	}
	
	public void setInterface(IViewUpdate isNeedUpdate,PageState pageState){
		if(isNeedUpdate != null){
			mNeedUpdate = isNeedUpdate;
		}
		this.pageState = pageState;
	}

	@Override
	protected void onFinishInflate() {
		if(DEBUG)LogHelper.LOGD(TAG, "onFinishInflate");
		super.onFinishInflate();
		setWillNotDraw(false);
		
		//view init
		initView();
		setData();
		
		stateChange(PageState.DEFAULT_STATE);
	}
	
	private void initView(){
		listView = (ListView)findViewById(R.id.bl_list);
		listView.setOnItemClickListener(this);
	}
	
	private void setData(){
		adapter = new FileListAdapter();
		
		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		synchronized (clickLock) {
			if(pageState.getState() == PageState.STATE_FM){
				switch (adapterList.get(position).getType()) {
					case Utils.DIRECTORY:
						String path = adapterList.get(position).getAbsolutePath();
						openNewDir(path);
						break;
					case Utils.TXT://导入
						if(mNeedUpdate != null)
							mNeedUpdate.importFile(adapterList.get(position).getAbsolutePath());
						break;
					default:
						break;
				}
			}else if(pageState.getState() == PageState.STATE_FL){
				if(position == 0){
					pageState.updateState(PageState.STATE_FM);
				}else{
					if(mNeedUpdate != null){
						mNeedUpdate.openFileForReading(adapterList.get(position).getAbsolutePath(),adapterList.get(position).getLastReadingPos());
						selectedPos = position;
					}
				}
			}
		}
	}
	
	//用于保存进入阅读界面对应于这个界面的list位置
	private int 	selectedPos = -1;
	
	public void updateLastReadingPos(int pos,String content){
		if(adapterList.get(selectedPos).getLastReadingPos() != pos){
			adapterList.get(selectedPos).setLastReadingPos(pos);
			//于界面不相干
//			adapter.notifyDataSetChanged();
		}
		if(content != null && !content.equals(adapterList.get(selectedPos).getSizeOrContent())){
			adapterList.get(selectedPos).setSizeOrContent(content);
			adapter.notifyDataSetChanged();
		}
	}
	
	/*private void exitByException(String log){
		Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
//		getContext().finish();
	}*/
	
	/*private void analyzeFileList(File file){
		if(adapterList == null){
			adapterList = new ArrayList<FileInfo>();
		}else{
			adapterList.clear();
		}
		
		File[] fileArrFiles = file.listFiles();
		if(fileArrFiles != null){
			for(File tmp : fileArrFiles){
				final FileInfo fileInfo = new FileInfo(tmp,mDateFormat);//tmp.getAbsolutePath(),tmp.getName(),Utils.GetFileIcon(tmp),tmp.length(),mDateFormat.format(new Date(tmp.lastModified())));
				adapterList.add(fileInfo);
			}
		}
		if(DEBUG)
			LogHelper.LOGE(TAG, "listSize=" + adapterList.size());
	}*/
	
	/**
	 * 显示导入的书籍界面，默认第一个是打开书籍,外部提供更新，匹配数据库
	 * */
	public  void updateList(Cursor cursor){
		if(adapterList == null){//clear fm状态下的数据
			adapterList = new ArrayList<FileInfo>();
		}else{
			adapterList.clear();
		}
		
		adapterList.add(new FileInfo(getResources().getString(R.string.TBI_OpenFile),getResources().getString(R.string.TBI_ImportFile)));
		
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			do{
				try{
					adapterList.add(new FileInfo(cursor.getString(cursor.getColumnIndexOrThrow(DBUtils.COLUMN_PATH)),
							cursor.getString(cursor.getColumnIndexOrThrow(DBUtils.COLUMN_DATE)),
							cursor.getString(cursor.getColumnIndexOrThrow(DBUtils.COLUMN_CONTENT)),
							cursor.getInt(cursor.getColumnIndexOrThrow(DBUtils.COLUMN_POS))));
				}catch (IllegalArgumentException e) {
					LogHelper.LOGE(TAG, "sqlite date get error");
				}
			}while(cursor.moveToNext());
		}
		
		adapter.notifyDataSetChanged();
		if(DEBUG)
			LogHelper.LOGE(TAG, "listSize=" + adapterList.size());
	}
	
	public String getCurTime(){
		return mDateFormat.format( new Date(System.currentTimeMillis()));
	}
	
	private void openNewDir(String path){
		if(DEBUG)LogHelper.LOGD(TAG, "setFilePath path="+path);
		if(analyzeFileList(path)){
			if(mNeedUpdate != null)
				mNeedUpdate.updatePath(path);
			Utils.CurPath = path;
		}else{
			if(DEBUG)
				LogHelper.LOGE(TAG, "open dir fail");
			Utils.CurPath = Utils.MAIN_PATH;//路径还原
			analyzeFileList(Utils.CurPath); 
		}
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	private boolean analyzeFileList(String dir){
		boolean ret = true;
		try {
			if(adapterList == null){
				adapterList = new ArrayList<FileInfo>();
			}else{
				adapterList.clear();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(linuxCommand.ls_hd(dir).getInputStream()));
			String name = null;  
			while ((name = in.readLine()) != null) {
				final File temp = new File(dir + "/" + name);
				final FileInfo fileInfo = new FileInfo(temp,mDateFormat);//dir + "/" + name,name,Utils.GetFileIcon(dir + "/" + name),temp.length(),mDateFormat.format(new Date(temp.lastModified())));
				adapterList.add(fileInfo);
            }  
            in.close();
		} catch (IOException e) {
			ret = false;
			e.printStackTrace();
		}
		if(DEBUG)
			LogHelper.LOGE(TAG, "listSize=" + adapterList.size());
		return ret;
	}
	
	private class ViewHolder{
		private TextView 	mName;
		private ImageView 	mCover;
		private TextView 	mDetail;
		private TextView 	mDate;
	}
	
	private class FileListAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			if(adapterList != null)
				return adapterList.size();
			else {
				return 0;
			}
		}

		@Override
		public Object getItem(int arg0) {
			if(adapterList != null){
				return adapterList.get(arg0).getName();
			}else{
				return null;
			}
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ViewHolder vHolder = null;
			if(arg1 == null){
				arg1 = View.inflate(getContext(), R.layout.ctrl_book_list_item, null);
				vHolder = new ViewHolder();
				vHolder.mCover = (ImageView)arg1.findViewById(R.id.cover);
				vHolder.mName = (TextView)arg1.findViewById(R.id.title);
				vHolder.mDetail = (TextView)arg1.findViewById(R.id.detail);
				vHolder.mDate = (TextView)arg1.findViewById(R.id.date);
				arg1.setTag(vHolder);
			}else{
				vHolder = (ViewHolder) arg1.getTag();
			}
			
			vHolder.mName.setText(adapterList.get(arg0).getName());
			if(pageState.getState() == PageState.STATE_FM){
				vHolder.mCover.setImageDrawable(dTypeCover[adapterList.get(arg0).getType()]);
				vHolder.mDetail.setText(dTypeName[adapterList.get(arg0).getType()] + "  " + adapterList.get(arg0).getSizeOrContent());
				vHolder.mDate.setText(adapterList.get(arg0).getDate());
			}else  if(pageState.getState() == PageState.STATE_FL){
				if(arg0 == 0){
					vHolder.mCover.setImageDrawable(getResources().getDrawable(R.drawable.bs_list_item_bkg));
					vHolder.mDetail.setText(getResources().getString(R.string.TBI_ImportFile));
					vHolder.mDate.setText("");
				}else{
					vHolder.mCover.setImageDrawable(dTxtCover[arg0%dTxtCover.length]);
					//content
					vHolder.mDetail.setText(adapterList.get(arg0).getSizeOrContent());
					vHolder.mDate.setText(adapterList.get(arg0).getDate());
				}
			}
//			vHolder.mDate.setText(adapterList.get(arg0).getDate());
			return arg1;
		}
	}
	
	public void exit(){
		if(adapterList != null){
			adapterList.clear();
			adapterList = null;
		}
		dTypeCover = null;
		dTxtCover = null;
	}

	@Override
	public void stateChange(int state) {
		if(DEBUG)LogHelper.LOGD(TAG, "stateChange");
		if(state == PageState.STATE_FL){
			//view初始化的时候设置这里为null，因为那时候该view还在activity里面为null，所以改回调无法调用改view里面的函数
			if(mNeedUpdate != null)
				mNeedUpdate.setImportDataToDisplay();
		}else if(state == PageState.STATE_FM){
//			openNewDir(Utils.MAIN_PATH);
			setFilePath();
//			setFilePath(Utils.MAIN_PATH);
		}
	}
}
