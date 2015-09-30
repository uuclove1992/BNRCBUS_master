package com.bnrc.util.collectwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class CollectWifiDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "CollectWifiDBHelper";
	private static CollectWifiDBHelper instance = null;
	private static SQLiteDatabase mDatabase;
	private static final int dbVersion = 1;
	private static final String DB_NAME = "collectdata.db"; // 保存的数据库文件名
	private static final String COLLECT_TABLE = "CollectInfo";// 一直采集Wifi的表
	private static final String SURE_TABLE = "SureInfo";// 一直采集Wifi的表

	private static final String PACKAGE_NAME = "com.example.copybnrcbus";
	private static final String DATABASE_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME; // 在手机里存放数据库的位置
	private Context mContext;
	private static final String[] COLUMNS = { "Time", "lat", "lon", "SSID",
			"MAC", "Level", "LocType", "LocRadius", "LocSpeed" };

	public static CollectWifiDBHelper getInstance(Context context) {
		if (instance == null) {
			try {
				Log.i(TAG, "instance fisrt");
				instance = new CollectWifiDBHelper(
						context.getApplicationContext());
				instance.openDatabase();
				mDatabase = instance.getWritableDatabase();
			} catch (Exception e) {
				Log.i(TAG, "open database error");
				// TODO: handle exception
			}

		}
		// mDatabase = instance.getWritableDatabase();
		return instance;

	}

	public CollectWifiDBHelper(Context context) {
		super(context, DB_NAME, null, dbVersion);
		this.mContext = context;
	}

	// 打开数据库
	private void openDatabase() {
		Log.i(TAG, "opendatabase ");
		try {
			String databaseFilename = DATABASE_PATH + "/databases/" + DB_NAME;
			File dir = new File(DATABASE_PATH + "/databases/");
			if (!dir.exists())
				dir.mkdir();
			if (!(new File(databaseFilename)).exists()) {
				Log.i(TAG, "copy bj");
				InputStream is = mContext.getResources().openRawResource(
						R.raw.collectdata);
				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[8192];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}

				fos.close();
				is.close();
				// }
				// SQLiteDatabase.openOrCreateDatabase(databaseFilename, null);
			}
		} catch (Exception e) {
		}

	}

	public SQLiteDatabase getMyDatabase() {
		return mDatabase;
	}

	// public void closeDB() {
	// instance.close();
	// // if (mDatabase != null)
	// // mDatabase.close();
	//
	// }

	public void onOpen(SQLiteDatabase db) {

	}

	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "create ");
		String createCollectSql = "create table IF NOT EXISTS " + COLLECT_TABLE
				+ " (" + COLUMNS[0] + " varchar(30) primary key ," + COLUMNS[1]
				+ " varchar(30)," + COLUMNS[2] + " varchar(20)," + COLUMNS[3]
				+ " varchar(10)," + COLUMNS[4] + " varchar(20)," + COLUMNS[5]
				+ " varchar(20)," + COLUMNS[6] + " varchar(20)," + COLUMNS[7]
				+ " varchar(20)," + COLUMNS[8] + " varchar(20));";
		db.execSQL(createCollectSql);
		String createSureSql = "create table IF NOT EXISTS "
				+ SURE_TABLE
				+ " (id Integer primary key ,SSID varchar(30), MAC varchar(20),ROUTE varchar(10));";
		db.execSQL(createSureSql);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "upgrade");
		db.execSQL("drop table if exists " + COLLECT_TABLE);
		db.execSQL("drop table if exists " + SURE_TABLE);
		onCreate(db);
	}

	// 插入扫描到的wifi信息
	public void InsertScanData(Map<String, Object> collectInfo) {
		Log.i(TAG, "InsertScanData: " + collectInfo.toString());
		ContentValues values = new ContentValues();
		values.put(COLUMNS[0], String.valueOf(collectInfo.get("时间")));
		values.put(COLUMNS[1], String.valueOf(collectInfo.get("纬度")));
		values.put(COLUMNS[2], String.valueOf(collectInfo.get("经度")));
		values.put(COLUMNS[3], String.valueOf(collectInfo.get("SSID")));
		values.put(COLUMNS[4], String.valueOf(collectInfo.get("MAC")));
		values.put(COLUMNS[5], String.valueOf(collectInfo.get("Level")));
		values.put(COLUMNS[6], String.valueOf(collectInfo.get("LocType")));
		values.put(COLUMNS[7], String.valueOf(collectInfo.get("LocRadius")));
		values.put(COLUMNS[8], String.valueOf(collectInfo.get("LocSpeed")));
		getMyDatabase().insert(COLLECT_TABLE, null, values);
	}

	List<Map<String, String>> list;

	// 查询扫描到的wifi信息
	public List<Map<String, String>> FindScanData() {
		list = new ArrayList<Map<String, String>>();
		Map<String, String> map;
		Cursor cursor = getMyDatabase().query(COLLECT_TABLE, COLUMNS, null,
				null, null, null, null);
		list.clear();
		while (cursor.moveToNext()) {
			map = new HashMap<String, String>();
			map.put("时间", cursor.getString(0));
			map.put("纬度", cursor.getString(1));
			map.put("经度", cursor.getString(2));
			map.put("SSID", cursor.getString(3));
			map.put("MAC", cursor.getString(4));
			map.put("Level", cursor.getString(5));
			map.put("LocType", cursor.getString(6));
			map.put("LocRadius", cursor.getString(7));
			map.put("LocSpeed", cursor.getString(8));
			list.add(map);
		}
		cursor.close();
		Log.i(TAG, "FindScanData: " + list.toString());

		return list;
	}

	// 插入用户已经确定的公交--Wifi信息
	public void InsertSureData(Map<String, String> sureData) {
		Log.i(TAG, "InsertSureData: " + sureData.toString());
		getMyDatabase().execSQL(
				"insert into " + SURE_TABLE + " values(null,?,?,?)",
				new String[] { sureData.get("SSID"), sureData.get("MAC"),
						sureData.get("线路") });
		Log.i(TAG,
				"InsertSureData :  " + sureData.get("线路") + " "
						+ sureData.get("SSID") + " " + sureData.get("MAC"));
		// getMyDatabase().execSQL("insert into subinfo values(null,?,?,?)",
		// new String[] { inputBus.getText().toString().trim(),
		// "BNRC-AIR", bssid });
	}

	public boolean HasSureData(String mac) {// 查看确定的信息表中是否有要查询的mac
		Cursor cursor = getMyDatabase().rawQuery(
				"select * from " + SURE_TABLE + " where MAC=\'" + mac + "\'",
				null);
		if (cursor.getCount() != 0) {
			Log.i(TAG, cursor.getCount() + "");
			return true;
		} else
			return false;
	}

	// 查询用户已经确定的公交--Wifi信息
	public List<Map<String, String>> FindSureData() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Cursor cursor = getMyDatabase().rawQuery("select * from " + SURE_TABLE,
				null);
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getInt(0) + "");
			map.put("SSID", cursor.getString(1));
			map.put("MAC", cursor.getString(2));
			map.put("ROUTE", cursor.getString(3));

			list.add(map);
		}
		cursor.close();
		Log.i(TAG, "FindSureData: " + list.toString());

		return list;
	}

	public void deleteCollectWifiTables() {
		Log.i(TAG, "create ");
		getMyDatabase().execSQL("drop table if exists " + COLLECT_TABLE);
		onCreate(getMyDatabase());
	}

	public void deleteSureWifiTables() {
		Log.i(TAG, "create ");
		getMyDatabase().execSQL("drop table if exists " + SURE_TABLE);
		onCreate(getMyDatabase());
	}

	public void deleteCollectWifiTablesByTimestamp(String timeStamp) {
		Log.i(TAG, "deleteCollectWifiTablesByTimestamp " + "timeStamp: "
				+ timeStamp);
		if (timeStamp.equalsIgnoreCase("0"))
			return;
		String sql = "delete from " + COLLECT_TABLE + " where Time<	"
				+ timeStamp;
		getMyDatabase().execSQL(sql);
	}
}
