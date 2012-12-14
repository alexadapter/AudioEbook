package com.android.lee.Database;

public class DBUtils {
	public static int	 DATAVERSION = 1;
	public static String DATABASENAME = "LeeReader";
	
	public static String TABLE_BOOKS = "BOOKS";
	public static String COLUMN_URLID = "LASTURLID";
	
	public static String TABLE_URLS = "URLS";
	public static String COLUMN_BOOKID = "BOOKID";
	public static String COLUMN_PATH = "FULLPATH";
	public static String COLUMN_DATE = "LASTREADDATE";
	public static String COLUMN_POS = "LASTREADPOS";
	public static String COLUMN_CONTENT = "LASTREADCONTENT";
}
