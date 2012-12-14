package com.android.lee.Database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAccess {
	/**
	 * 数据库操作
	 * */
	protected 	MyDatabaseHelper 	dbHelper;
	protected	SQLiteDatabase 		db;
	private		Context				mContext;
	
	protected	static Object		lock;
	public DbAccess(Context context){
		mContext = context;
		lock = new Object();
	}
	
	public void execSQL(String sql) throws SQLiteException,SQLException{
		synchronized (lock) {
			try{
				openDB();
				db.execSQL(sql);
			}finally{
				closeDB();
			}
		}
	}
	
	public void execSQL(String sql,String[] params) throws SQLiteException,SQLException{
		synchronized (lock) {
			try{
				openDB();
				db.execSQL(sql,params);
			}finally{
				closeDB();
			}
		}
	}
	
	public void insert(String table, String colume,ArrayList<ContentValues> array) throws SQLiteException,SQLException{
		synchronized (lock) {
			try{
				openDB();
				for(int i=0; i<array.size(); i++){
					db.insert(table, colume, array.get(i));
				}
			}finally{
				closeDB();
			}
		}
	}
	
	public void insert(String table, String colume,ArrayList<ContentValues> array,boolean flag) throws SQLiteException,SQLException{
		synchronized (lock) {
			for(int i=0; i<array.size(); i++){
				db.insert(table, colume, array.get(i));
			}
		}
	}
	
	public Cursor rawQuery(String sql,String[] params) throws SQLiteException,SQLException{
		Cursor cursor = null;
		/*"select * from " + Utils.TABLE_NAME2 + " where " + Utils.COLUMN_STARTADR + " = " + startAdr +" AND " +  Utils.COLUMN_PATH + " like ? ", new String[]{
				path*/
		synchronized (lock) {
			cursor = db.rawQuery(sql,params);
		}
		return cursor;
	}
	
	
	public void insert(String table, String nullColumnHack, ContentValues values){
		synchronized (lock) {
			db.insert(table, nullColumnHack, values);
		}
	}
	
	public Cursor query(boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit){
		synchronized (lock) {
			Cursor cursor;
			cursor = db.query(distinct,table,columns,selection,selectionArgs, groupBy, having, orderBy,limit);
			return cursor;
		}
	}
	
	public void delete(String table, String whereClause, String[] whereArgs){
		synchronized (lock) {
			db.delete(table, whereClause, whereArgs);
		}
	}
	
	public void update(String table, ContentValues values, String whereClause, String[] whereArgs){
		synchronized (lock) {
			db.update(table, values, whereClause, whereArgs);
		}
	}
	
	 /**
     * 打开数据库
     */
    public void openDB(){
    	synchronized (lock) {
	    	if(dbHelper == null)
	    		dbHelper = new MyDatabaseHelper(mContext, DBUtils.DATABASENAME,DBUtils.DATAVERSION);
	        if(db == null || !db.isOpen())
	        	db = dbHelper.getWritableDatabase();
	    }
    }
    
    /**
     * 关闭数据库
     */
    public void closeDB(){
    	synchronized (lock) {
	    	if(db != null && db.isOpen()){
	    		db.close();
	    	}
	    	if(dbHelper != null){
	    		dbHelper.close();
	    	}
    	}
    }
    
    public class MyDatabaseHelper extends SQLiteOpenHelper{
    	//默认是设置了不允许为空的，调试的时候先打开，允许为空先
    	String CREATE_TABLE_SQL =
    			"create table if not exists " + DBUtils.TABLE_URLS +
				"(_id integer primary key autoincrement,"+
					DBUtils.COLUMN_BOOKID + " integer not null," + 
					DBUtils.COLUMN_PATH + " text not null,"+
					DBUtils.COLUMN_DATE + " integer," +
					DBUtils.COLUMN_POS + " integer," + 
					DBUtils.COLUMN_CONTENT + " text)";
    	
    	String CREATE_TABLE_SQL1 =
    			"create table if not exists " + DBUtils.TABLE_BOOKS +
				"(_id integer primary key autoincrement,"+
					DBUtils.COLUMN_PATH + " text not null,"+
					DBUtils.COLUMN_URLID + " integer not null)";
    	
    	/**
    	 * @param context
    	 * @param name
    	 */
    	public MyDatabaseHelper(Context context, String name,int version){
    		super(context, name, null, version);
    	}

    	@Override
    	public void onCreate(SQLiteDatabase db){
    		// 第一个使用数据库时自动建表
    		db.execSQL(CREATE_TABLE_SQL);
    		db.execSQL(CREATE_TABLE_SQL1);
    	}

    	@Override
    	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    		System.out.println("--------onUpdate Called--------" + oldVersion + "--->" + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + DBUtils.TABLE_URLS);
            db.execSQL("DROP TABLE IF EXISTS " + DBUtils.TABLE_BOOKS);
    		onCreate(db);
    	}
    }
}
