package com.bnrc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.impl.cookie.BrowserCompatSpecFactory;

import u.aly.br;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.R;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.util.Log;

@SuppressLint("SdCardPath")
public class UserDataDBHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static UserDataDBHelper instance;
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "userdata.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	public ArrayList<ArrayList<String>> alertStations = null;
	public ArrayList<ArrayList<String>> favStations = null;
	public ArrayList<ArrayList<String>> favBuslines = null;

	public static UserDataDBHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");

				instance = new UserDataDBHelper(context);
				instance.myContext = context;
				instance.openDataBase();

			} catch (IOException ioe) {
				throw new Error("Unable to create database");
			}
		}
		return instance;
	}

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 * @throws IOException
	 */
	public UserDataDBHelper(Context context) throws IOException {
		super(context, DB_NAME, null, 1);

	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DB_NAME;
		try {
			// ����ļ��ľ���·��
			String databaseFilename = myPath;
			File dir = new File(DB_PATH);

			if (!dir.exists()) {
				dir.mkdir();
			}
			;

			if (!(new File(databaseFilename)).exists()) {
				InputStream is = myContext.getResources().openRawResource(
						R.raw.userdata);
				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[8192];
				int count = 0;
				// ��ʼ�����ļ�
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}

			myDataBase = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);
			getAlertStations();
			getFavStations();
			getAllFavBuslines();
		} catch (Exception e) {
			Log.i("open error", e.getMessage());
		}
	}

	@Override
	public synchronized void close() {

		if (getMyDataBase() != null)
			getMyDataBase().close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public SQLiteDatabase getMyDataBase() {
		return myDataBase;
	}

	public void setMyDataBase(SQLiteDatabase myDataBase) {
		this.myDataBase = myDataBase;
	}

	public void addAlertStationWithStation(ArrayList<String> station) {
		// openDataBase();
		String sql = "delete from  station_alert where latitude = \'"
				+ station.get(1) + "\' and longtitude = \'" + station.get(2)
				+ "\'";
		myDataBase.execSQL(sql);
		sql = "insert into station_alert (name,latitude,longtitude) values (\'"
				+ station.get(0) + "\',\'" + station.get(1) + "\',\'"
				+ station.get(2) + "\')";
		myDataBase.execSQL(sql);
		// close();
		getAlertStations();
	}

	public boolean checkAlertStationWithStation(ArrayList<String> station) {
		// openDataBase();
		int j = alertStations.size();
		for (int i = 0; i < j; i++) {
			if (alertStations.get(i).get(0).equalsIgnoreCase(station.get(0))) {
				// 距离小于50m
				return true;
			}
		}
		return false;
		// close();
	}

	public void deleteAlertStationWithStation(ArrayList<String> station) {
		// openDataBase();
		String sql = "delete from  station_alert where latitude = \'"
				+ station.get(1) + "\' and longtitude = \'" + station.get(2)
				+ "\'";
		myDataBase.execSQL(sql);
		// close();
		getAlertStations();

	}

	public void getAlertStations() {
		alertStations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select name,latitude,longtitude from station_alert";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			alertStations.add(arrayList);
		}
		cursor.close();
		// close();
	}

	public void addFavStationWithStation(ArrayList<String> station) {
		// openDataBase();
		String sql = "delete from  station_fav where name = \'"
				+ station.get(0) + "\'";
		myDataBase.execSQL(sql);
		sql = "delete from  rtbus_fav where  stationName = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2) + "\'";
		myDataBase.execSQL(sql);
		sql = "insert into station_fav (name,latitude,longtitude) values (\'"
				+ station.get(0) + "\',\'" + station.get(1) + "\',\'"
				+ station.get(2) + "\')";
		myDataBase.execSQL(sql);
		// close();
		getFavStations();
	}

	public boolean checkFavStationWithStationID(String stationid) {
		// openDataBase();
		int j = favStations.size();
		for (int i = 0; i < j; i++) {
			if (favStations.get(i).get(0).equalsIgnoreCase(stationid)) {
				return true;
			}
		}
		return false;
		// close();
	}

	public void deleteFavStationWithStation(ArrayList<String> station) {
		// openDataBase();
		String sql = "delete from  station_fav where name = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2) + "\'";
		myDataBase.execSQL(sql);
		sql = "delete from  rtbus_fav where stationName = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2) + "\'";
		myDataBase.execSQL(sql);
		getFavStations();
	}

	public void deleteFavStationWithStationAndBusline(
			ArrayList<String> station, ArrayList<String> busline) {
		// openDataBase();
		deleteFavStationWithStation(station);
		String sql = "delete from  rtbus_fav where  stationName = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2)
				+ "\' and buslineId = \'" + busline.get(0) + "\'";
		myDataBase.execSQL(sql);

		ArrayList<ArrayList<String>> stations = DataBaseHelper.getInstance(
				myContext).getStationsWithBuslineId(busline.get(0));
		int i = 0;
		for (i = 0; i < stations.size(); i++) {
			if (checkFavStationWithStationID(stations.get(i).get(0))) {
				break;
			}
		}
		if (i == stations.size()) {
			deleteFavBuslineWithBusline(busline);
		}
	}

	public void getFavStations() {
		favStations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select name,latitude,longtitude from station_fav";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			favStations.add(arrayList);
		}
		cursor.close();
		// close();
	}

	public ArrayList<ArrayList<String>> getLatestSearchBuslines() {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select id,keyName,name from latest_search order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext() && buslines.size() <= 20) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			buslines.add(arrayList);
		}
		cursor.close();
		return buslines;
	}

	public void addSearchBuslineWithBusline(ArrayList<String> busline) {
		// openDataBase();
		String sql = "delete from  latest_search where id = \'"
				+ busline.get(0) + "\';";
		myDataBase.execSQL(sql);
		sql = "insert into latest_search (id,keyName,name) values (\'"
				+ busline.get(0) + "\',\'" + busline.get(1) + "\',\'"
				+ busline.get(2) + "\')";
		myDataBase.execSQL(sql);
		// close();
	}

	public void addFavBuslineWithBusline(ArrayList<String> busline) {
		// openDataBase();
		String sql = "delete from  busline_fav where id = \'" + busline.get(0)
				+ "\';";
		myDataBase.execSQL(sql);
		sql = "insert into busline_fav (id,keyName,name) values (\'"
				+ busline.get(0) + "\',\'" + busline.get(1) + "\',\'"
				+ busline.get(2) + "\')";
		myDataBase.execSQL(sql);

		getAllFavBuslines();
		// close();
	}

	public boolean checkFavBuslineWithBuslineID(String busline) {
		// openDataBase();
		String sql = "select * from busline_fav  where id = \'" + busline
				+ "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
		// close();
	}

	public void deleteFavBuslineWithBusline(ArrayList<String> busline) {
		// openDataBase();
		String sql = "delete from  busline_fav where id = \'" + busline.get(0)
				+ "\'";
		myDataBase.execSQL(sql);
		sql = "delete from  rtbus_fav where  buslineId = \'" + busline.get(0) + "\'";
		myDataBase.execSQL(sql);
		getAllFavBuslines();
	}

	public void deleteFavBuslineWithBuslineAndStation(
			ArrayList<String> busline, ArrayList<String> station) {
		// openDataBase();
		deleteFavBuslineWithBusline(busline);
		String sql = "delete from  rtbus_fav where  stationName = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2)
				+ "\' and buslineId = \'" + busline.get(0) + "\'";
		myDataBase.execSQL(sql);
		ArrayList<ArrayList<String>> busliens = DataBaseHelper.getInstance(
				myContext).getBusLinesWithStation(station);
		int i = 0;
		for (i = 0; i < busliens.size(); i++) {
			if (checkFavBuslineWithBuslineID(busliens.get(i).get(0))) {
				break;
			}
		}
		if (i == busliens.size()) {
			deleteFavStationWithStation(station);
		}
	}

	public void getAllFavBuslines() {
		favBuslines = new ArrayList<ArrayList<String>>();

		String sql = "select id,keyName,name from busline_fav order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			favBuslines.add(arrayList);
		}
		cursor.close();
	}

	public ArrayList<ArrayList<String>> getLatestSearchStations() {
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select name,latitude,longtitude from station_search  order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext() && stations.size() <= 20) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
		}
		cursor.close();
		// close();
		return stations;
	}

	public void addSearchStaitonWithStation(ArrayList<String> station) {
		// openDataBase();

		String sql = "delete from  station_search where name = \'"
				+ station.get(0) + "\'";
		myDataBase.execSQL(sql);
		sql = "insert into station_search (name,latitude,longtitude) values (\'"
				+ station.get(0)
				+ "\',\'"
				+ station.get(1)
				+ "\',\'"
				+ station.get(2) + "\')";
		myDataBase.execSQL(sql);
		// close();
	}

	public void deleteAllSearchHistory() {
		String sql = "delete from station_search";
		myDataBase.execSQL(sql);
		sql = "delete from  latest_search ";
		myDataBase.execSQL(sql);
	}

	public void deleteAllStationSearchHistory() {
		String sql = "delete from station_search";
		myDataBase.execSQL(sql);
	}

	public void deleteAllBuslineSearchHistory() {
		String sql = "delete from  latest_search ";
		myDataBase.execSQL(sql);
	}

	public void addRTBusWithBuslineAndStation(ArrayList<String> busline,
			ArrayList<String> station) {
		String sql = "delete from  rtbus_fav where  stationName = \'"
				+ station.get(0) + "\' and latitude = \'" + station.get(1)
				+ "\' and longtitude = \'" + station.get(2)
				+ "\' and buslineId = \'" + busline.get(0) + "\'";
		myDataBase.execSQL(sql);
		sql = "insert into rtbus_fav (buslineId,buslineName,stationName,latitude,longtitude) values (\'"
				+ busline.get(0)
				+ "\',\'"
				+ busline.get(2)
				+ "\',\'"
				+ station.get(0)
				+ "\',\'"
				+ station.get(1)
				+ "\',\'"
				+ station.get(2) + "\')";
		myDataBase.execSQL(sql);
	}

	public ArrayList<ArrayList<String>> getMyAllFavRTBus() {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		String sql = "select buslineId,buslineName,stationName from rtbus_fav ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {

			ArrayList<String> arrayList;
			arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			if (cursor.getString(1).indexOf("(") >= 0) {
				arrayList.add(cursor.getString(1).substring(0,
						cursor.getString(1).indexOf("(")));
			} else {
				arrayList.add(cursor.getString(1));
			}
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			
            int k = 0;
		    for (ArrayList<String> bus : buslines) {
				if (bus.get(0).trim().equalsIgnoreCase(arrayList.get(0).trim()) &&
						bus.get(2).trim().equalsIgnoreCase(arrayList.get(2).trim())) {
					k = 1;
					break;
				}
			}
		    if(k == 0){
				buslines.add(arrayList);
		    }
		}
		cursor.close();

		return buslines;
	}
}
