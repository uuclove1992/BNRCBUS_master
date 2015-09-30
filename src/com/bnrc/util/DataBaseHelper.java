package com.bnrc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.MyAlertStationView;
import com.bnrc.busapp.R;
import com.bnrc.busapp.RootView;
import com.bnrc.busapp.SettingView;
import com.umeng.analytics.MobclickAgent;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class DataBaseHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static DataBaseHelper instance;
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "businfo.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	private int FileLength;
	private int DownedFileLength = 0;
	private InputStream inputStream;
	private URLConnection connection;
	private OutputStream outputStream;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					// progressBar.setMax(FileLength);
					Log.i("文件长度----------->", FileLength + "");
					break;
				case 1:
					// progressBar.setProgress(DownedFileLength);
					int x = DownedFileLength * 100 / FileLength;
					Log.i("文件长度----------->", DownedFileLength + "");
					// textView.setText(x+"%");
					break;
				case 2:
					openDataBase();
					MobclickAgent.updateOnlineConfig(myContext);
					String value = MobclickAgent.getConfigParams(myContext,
							"bus_data_version");
					Log.i("bus_data_version", value);
					JSONObject jsonObj = null;
					try {
						jsonObj = new JSONObject(value);
						String version = jsonObj.getString("version");
						SharedPreferences mySharedPreferences = myContext
								.getSharedPreferences("setting",
										SettingView.MODE_PRIVATE);
						SharedPreferences.Editor editor = mySharedPreferences
								.edit();
						editor.putString("bus_data_version", version);
						editor.commit();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Toast.makeText(myContext, "公交数据更新完成", Toast.LENGTH_LONG)
							.show();
					break;

				default:
					break;
				}
			}
		}

	};

	public static DataBaseHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");

				instance = new DataBaseHelper(context);
				// Runtime.getRuntime().exec("chmod 666" +
				// "/data/data/test.txt");
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
	public DataBaseHelper(Context context) throws IOException {
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
						R.raw.businfo);
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

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.

	public ArrayList<ArrayList<String>> getAroundStationsWithLocation(
			final LatLng location) {
		// openDataBase();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		float latRadius = 0.0f;
		float lngRadius = 0.0f;

		SharedPreferences mySharedPreferences = myContext.getSharedPreferences(
				"setting", Context.MODE_PRIVATE);
		String searchradius = mySharedPreferences.getString("searchRMode",
				"800米");
		int radius = Integer.parseInt(searchradius.subSequence(0,
				searchradius.length() - 1).toString());
		switch (radius) {
		case 600:
			latRadius = 0.004f;
			lngRadius = 0.005f;
			break;
		case 700:
			latRadius = 0.005f;
			lngRadius = 0.0065f;
			break;
		case 800:
			latRadius = 0.006f;
			lngRadius = 0.008f;
			break;
		case 900:
			latRadius = 0.007f;
			lngRadius = 0.009f;
			break;
		case 1000:
			latRadius = 0.008f;
			lngRadius = 0.010f;
			break;
		case 1100:
			latRadius = 0.009f;
			lngRadius = 0.011f;
			break;
		case 1200:
			latRadius = 0.009f;
			lngRadius = 0.012f;
			break;
		case 1300:
			latRadius = 0.010f;
			lngRadius = 0.013f;
			break;
		case 1400:
			latRadius = 0.010f;
			lngRadius = 0.014f;
			break;
		case 1500:
			latRadius = 0.011f;
			lngRadius = 0.015f;
			break;
		default:
			break;
		}

		double lat = location.latitude;
		double lng = location.longitude;
		double smallLat = lat - latRadius;
		double smallLng = lng - lngRadius;
		double bigLat = lat + latRadius;
		double bigLng = lng + lngRadius;
		// ���Ҹ�����վ
		String sql = "select AZ,NAME,GY,GX,LNs from CSTATIONS where  GY > "
				+ smallLat + " and GY < " + bigLat + " and GX > " + smallLng
				+ " and GX < " + bigLng;
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			LatLng stationPoint = new LatLng(cursor.getFloat(2),
					cursor.getFloat(3));
			double distance = DistanceUtil.getDistance(location, stationPoint);
			arrayList.add(cursor.getInt(0) + "");
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getFloat(2) + "");
			arrayList.add(cursor.getFloat(3) + "");
			arrayList.add(cursor.getString(4));
			arrayList.add(distance + "");
			stations.add(arrayList);
		}

		Comparator<ArrayList<String>> comparator = new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> s1, ArrayList<String> s2) {

				double distance1 = Double.parseDouble(s1.get(5));

				double distance2 = Double.parseDouble(s2.get(5));
				if (distance1 > distance2) {
					return 1;
				} else if (distance1 < distance2) {
					return -1;
				}
				return 0;
			}
		};

		Collections.sort(stations, comparator);

		cursor.close();

		// close();
		return stations;
	}

	public ArrayList<ArrayList<String>> getNearbyBuslineWithLocation(
			LatLng location) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = getAroundStationsWithLocation(location);
		ArrayList<ArrayList<String>> lineStop = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < stations.size(); i++) {
			String lineString = stations.get(i).get(4);
			String[] l = lineString.split(";");
			for (int k = 0; k < l.length; k++) {
				int j = 0;
				for (j = 0; j < lineStop.size(); j++) {
					if (lineStop.get(j).get(0)
							.equalsIgnoreCase(l[k].substring(0, 7))) {
						break;
					}
				}
				if (j == lineStop.size()) {
					ArrayList<String> item = new ArrayList<String>();
					item.add(l[k].substring(0, 7));
					item.add(stations.get(i).get(1));
					lineStop.add(item);
				}
			}

		}
		// 根据线路id查找线路名，需要显示线路名，线路id需要纪录，传到下一页面，查看线路详情。
		for (int i = 0; i < lineStop.size(); i++) {
			String sql = "select NAME, S_START, S_END from LINES where LN = "
					+ lineStop.get(i).get(0);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				ArrayList<String> arrayList;
				arrayList = new ArrayList<String>();
				arrayList.add(lineStop.get(i).get(0));
				arrayList.add(cursor.getString(0));
				arrayList.add(cursor.getString(0) + "(" + cursor.getString(1)
						+ "-" + cursor.getString(2) + ")");
				arrayList.add(lineStop.get(i).get(1));
				buslines.add(arrayList);

			}
			cursor.close();
		}

		return buslines;
	}

	public ArrayList<Map<String, String>> getNearBusInfo(LatLng location) {
		ArrayList<Map<String, String>> buslines = new ArrayList<Map<String, String>>();
		ArrayList<ArrayList<String>> stations = getAroundStationsWithLocation(location);
		ArrayList<ArrayList<String>> lineStop = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < stations.size(); i++) {
			String lineString = stations.get(i).get(4);
			String stationLat = stations.get(i).get(3);
			String[] l = lineString.split(";");
			for (int k = 0; k < l.length; k++) {
				int j = 0;
				for (j = 0; j < lineStop.size(); j++) {
					if (lineStop.get(j).get(0)
							.equalsIgnoreCase(l[k].substring(0, 7))) {
						break;
					}
				}
				if (j == lineStop.size()) {
					ArrayList<String> item = new ArrayList<String>();
					item.add(l[k].substring(0, 7));
					item.add(stations.get(i).get(1));
					item.add(stations.get(i).get(5));
					lineStop.add(item);
				}
			}

		}
		// 根据线路id查找线路名，需要显示线路名，线路id需要纪录，传到下一页面，查看线路详情。
		for (int i = 0; i < lineStop.size(); i++) {
			String sql = "select NAME, S_START, S_END from LINES where LN = "
					+ lineStop.get(i).get(0);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				Map<String, String> summary = new HashMap<String, String>();
				summary.put("距离",
						(int) Double.parseDouble(lineStop.get(i).get(2)) + "");
				summary.put("车站", lineStop.get(i).get(1));
				summary.put("方向", cursor.getString(2));
				summary.put("线路", cursor.getString(0));
				buslines.add(summary);
			}
			cursor.close();
		}

		return buslines;
	}

	public Cursor FindBusByKeyname(String keyWord) {
		Cursor cursor = getMyDataBase()
				.rawQuery(
						"select * from LINES where NAME like ? group by NAME order by NAME",
						new String[] { keyWord + "%" });
		return cursor;
	}

	public ArrayList<ArrayList<String>> getStationSWithBuslineName(
			String busline) {
		busline = busline.replace("路(", "(");
		ArrayList<ArrayList<String>> buslines = getBusLinesWithKeyword(busline
				.substring(0, busline.indexOf("-")));
		if (buslines.size() > 0) {
			return getStationsWithBuslineId(buslines.get(0).get(0).toString());
		}
		return null;
	}

	public String getBuslineIdWithBuslineName(String busline) {
		busline = busline.replace("路(", "(");
		ArrayList<ArrayList<String>> buslines = getBusLinesWithKeyword(busline
				.substring(0, busline.indexOf("-")));
		if (buslines.size() > 0) {
			return buslines.get(0).get(0).toString();
		}
		return null;
	}

	public ArrayList<ArrayList<String>> getStationsWithStationKeyword(
			String keyword) {
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		String sql = "select AZ,NAME,GY,GX,LNs from CSTATIONS where  NAME  like \'%"
				+ keyword + "%\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getInt(0) + "");
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getFloat(2) + "");
			arrayList.add(cursor.getFloat(3) + "");
			arrayList.add(cursor.getString(4));
			stations.add(arrayList);
		}
		cursor.close();
		// close();
		return stations;
	}

	public ArrayList<ArrayList<String>> searchBusLinesWithKeyword(String keyword) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select LN, NAME, S_START, S_END from LINES  where name like \'%"
				+ keyword + "%\' order by NAME asc ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(1) + "(" + cursor.getString(2) + "-"
					+ cursor.getString(3) + ")");

			int i = 0;
			for (i = 0; i < buslines.size(); i++) {
				ArrayList<String> array = buslines.get(i);
				if (getNumWithStr(cursor.getString(1)) < getNumWithStr(array
						.get(1))) {
					buslines.add(i, arrayList);
					break;
				}
			}
			if (i == buslines.size()) {
				buslines.add(arrayList);
			}
		}
		cursor.close();

		// close();
		return buslines;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithKeyword(String keyword) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		// openDataBase();

		Log.i("keyword", keyword);
		String sql = "select LN, NAME, S_START, S_END from LINES  where NAME like \'%"
				+ keyword.substring(0, keyword.indexOf("("))
				+ "%\' and S_START like  \'%"
				+ keyword.substring(keyword.indexOf("(") + 1,
						keyword.indexOf("(") + 2) + "%\' order by NAME asc ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(1) + "(" + cursor.getString(2) + "-"
					+ cursor.getString(3) + ")");

			int i = 0;
			for (i = 0; i < buslines.size(); i++) {
				ArrayList<String> array = buslines.get(i);
				if (getNumWithStr(cursor.getString(1)) < getNumWithStr(array
						.get(1))) {
					buslines.add(i, arrayList);
					break;
				}
			}
			if (i == buslines.size()) {
				buslines.add(arrayList);
			}
		}
		cursor.close();
		// close();
		return buslines;
	}

	public ArrayList<ArrayList<String>> getBothsideBusLinesWithBuslineName(
			String keyword) {
		Log.i("keyword", keyword);
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		if (keyword.indexOf("内(") > 0) {
			String sql = "select LN, NAME, S_START, S_END from LINES  where NAME like \'"
					+ keyword.substring(0, keyword.indexOf("内"))
					+ "\' "
					+ "order by NAME asc ";
			Log.i("sql", sql);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add(cursor.getString(0));
				arrayList.add(cursor.getString(1));
				arrayList.add(cursor.getString(1) + "(" + cursor.getString(2)
						+ "-" + cursor.getString(3) + ")");
				buslines.add(arrayList);
			}
			cursor.close();
		} else if (keyword.indexOf("外(") > 0) {
			String sql = "select LN, NAME, S_START, S_END from LINES  where NAME  like \'"
					+ keyword.substring(0, keyword.indexOf("外"))
					+ "\'"
					+ "order by NAME asc ";
			Log.i("sql", sql);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add(cursor.getString(0));
				arrayList.add(cursor.getString(1));
				arrayList.add(cursor.getString(1) + "(" + cursor.getString(2)
						+ "-" + cursor.getString(3) + ")");
				buslines.add(arrayList);
			}
			cursor.close();
		} else {
			Log.i("keyword", keyword);
			String sql = "select LN, NAME, S_START, S_END from LINES  where NAME like \'"
					+ keyword.substring(0, keyword.indexOf("("))
					+ "\' order by NAME asc ";
			Log.i("sql", sql);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add(cursor.getString(0));
				arrayList.add(cursor.getString(1));
				arrayList.add(cursor.getString(1) + "(" + cursor.getString(2)
						+ "-" + cursor.getString(3) + ")");
				buslines.add(arrayList);

			}
			cursor.close();
		}

		return buslines;
	}

	public int getNumWithStr(String str) {
		str = str.trim();
		String str2 = "";
		if (str != null && !"".equals(str)) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
					str2 += str.charAt(i);
				}
			}
		}
		return Integer.parseInt(str2);
	}

	public ArrayList<ArrayList<String>> getStationsWithBuslineId(
			String buslineId) {
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select NAME,GY,GX ,SN from STATIONS where SN like \'"
				+ buslineId + "%\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getFloat(1) + "");
			arrayList.add(cursor.getFloat(2) + "");
			arrayList.add(cursor.getString(3));
			stations.add(arrayList);
		}
		cursor.close();
		// close();
		return stations;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithStation(
			ArrayList<String> station) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();

		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station.get(0) + "\'";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		MostSimilarString mostSimilarString = new MostSimilarString();
		String buslineIds = null;
		String[] buslineidArr = null;
		float maxSimilar = 0;
		int maxsimilarIndex = 0;
		float curSimilar;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
			curSimilar = mostSimilarString.getSimilarityRatio(
					cursor.getString(1), station.get(1))
					+ mostSimilarString.getSimilarityRatio(cursor.getString(2),
							station.get(2));
			if (curSimilar > maxSimilar) {
				maxSimilar = curSimilar;
				maxsimilarIndex = stations.size() - 1;
			}
		}
		if (stations.size() == 0) {
			buslineIds = null;
		} else {
			buslineIds = stations.get(maxsimilarIndex).get(0);
		}
		Log.i("buslineIds", "buslineIds = " + buslineIds);
		cursor.close();
		if (buslineIds == null) {
			return buslines;
		} else {
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				if (stemp.length() > 7) {
					String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
							+ stemp.substring(0, 7);
					Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
					while (cursor2.moveToNext()) {
						ArrayList<String> arrayList = new ArrayList<String>();
						arrayList.add(cursor2.getString(0));
						arrayList.add(cursor2.getString(1));
						arrayList.add(cursor2.getString(1) + "("
								+ cursor2.getString(2) + "-"
								+ cursor2.getString(3) + ")");
						buslines.add(arrayList);
						break;
					}
					cursor2.close();
				}

			}
		}
		// close();
		return buslines;
	}

	public ArrayList<ArrayList<ArrayList<String>>> getBothsideBusLinesWithStation(
			ArrayList<String> station) {
		ArrayList<ArrayList<ArrayList<String>>> bothsideBusLines = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station.get(0) + "\'";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		String buslineIds = null;
		String[] buslineidArr = null;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			Log.i("buslineIds", "buslineIds = " + cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
		}
		cursor.close();
		int j = stations.size();
		if (j == 0) {
			return null;
		}
		ArrayList<ArrayList<String>> buslines;
		for (int i = 0; i < j; i++) {
			buslines = new ArrayList<ArrayList<String>>();
			buslineIds = stations.get(i).get(0);
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				if (stemp.length() > 7) {
					String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
							+ stemp.substring(0, 7);
					Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
					while (cursor2.moveToNext()) {
						ArrayList<String> arrayList = new ArrayList<String>();
						arrayList.add(cursor2.getString(0));
						arrayList.add(cursor2.getString(1));
						arrayList.add(cursor2.getString(1) + "("
								+ cursor2.getString(2) + "-"
								+ cursor2.getString(3) + ")");
						buslines.add(arrayList);
						break;
					}
					cursor2.close();
				}

			}
			stations.get(i).add(station.get(0));
			buslines.add(stations.get(i));
			bothsideBusLines.add(buslines);
		}
		return bothsideBusLines;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithStationName(
			String station) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		MostSimilarString mostSimilarString = new MostSimilarString();
		String buslineIds = null;
		String[] buslineidArr = null;
		float maxSimilar = 0;
		int maxsimilarIndex = 0;
		float curSimilar;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
			curSimilar = mostSimilarString.getSimilarityRatio(
					cursor.getString(1), station)
					+ mostSimilarString.getSimilarityRatio(cursor.getString(2),
							station);
			if (curSimilar > maxSimilar) {
				maxSimilar = curSimilar;
				maxsimilarIndex = stations.size() - 1;
			}
		}
		if (stations.size() == 0) {
			buslineIds = null;
		} else {
			buslineIds = stations.get(maxsimilarIndex).get(0);
		}
		cursor.close();
		if (buslineIds == null) {
			return buslines;
		} else {
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
						+ stemp;
				Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
				while (cursor2.moveToNext()) {
					ArrayList<String> arrayList = new ArrayList<String>();
					arrayList.add(cursor2.getString(0));
					arrayList.add(cursor2.getString(1));
					arrayList.add(cursor2.getString(1) + "("
							+ cursor.getString(2) + "-" + cursor.getString(3)
							+ ")");
					buslines.add(arrayList);
					break;
				}
				cursor2.close();
			}
		}
		// close();
		return buslines;
	}

	public void DownFileWithUrl(final String urlString) {
		DownedFileLength = 0;
		// TODO Auto-generated method stub
		Thread thread = new Thread() {
			public void run() {
				try {
					DownFile(urlString);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
		thread.start();
	}

	private void DownFile(String urlString) {

		/*
		 * 连接到服务器
		 */

		try {
			URL url = new URL(urlString);
			Log.i("urlString", urlString);
			connection = url.openConnection();
			if (connection.getReadTimeout() == 5) {
				Log.i("---------->", "当前网络有问题");
				// return;
			}
			inputStream = connection.getInputStream();

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * 文件的保存路径和和文件名其中Nobody.mp3是在手机SD卡上要保存的路径，如果不存在则新建
		 */

		File file1 = new File(DB_PATH + "update" + DB_NAME);
		if (!file1.exists()) {
			close();
			file1.delete();
		}
		File file = new File(DB_PATH + "update" + DB_NAME);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * 向SD卡中写入文件,用Handle传递线程
		 */
		Message message = new Message();
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024 * 4];
			FileLength = connection.getContentLength();
			message.what = 0;
			handler.sendMessage(message);
			while (DownedFileLength < FileLength) {
				outputStream.write(buffer);
				DownedFileLength += inputStream.read(buffer);
				Log.i("-------->", DownedFileLength + "");
				Message message1 = new Message();
				message1.what = 1;
				handler.sendMessage(message1);
			}
			Message message2 = new Message();
			message2.what = 2;
			handler.sendMessage(message2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
