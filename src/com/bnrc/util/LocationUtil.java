package com.bnrc.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.UserSettingView;
import com.bnrc.util.collectwifi.Constants;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

public class LocationUtil extends Application {
	private static LocationUtil mInstance = null;
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	public DataBaseHelper dabase = null;

	public String mLocationResult, logMsg;
	public BDLocation mLocation = null;
	public String addressString = null;
	Context mContext;
	String mProvider;
	private SharedPreferences preferences;

	public static LocationUtil getInstance(Context context) {
		if (mInstance == null) {
			SDKInitializer.initialize(context.getApplicationContext());
			mInstance = new LocationUtil(context);
			mInstance.mLocation = new BDLocation();
			SharedPreferences mySharedPreferences = context
					.getSharedPreferences("setting",
							UserSettingView.MODE_PRIVATE);
			String defaultLatitude = mySharedPreferences.getString(
					"defaultLatitude", "39.963175");
			String defaultLongitude = mySharedPreferences.getString(
					"defaultLongitude", "116.400244");
			mInstance.mLocation
					.setLatitude(Double.parseDouble(defaultLatitude));
			mInstance.mLocation.setLongitude(Double
					.parseDouble(defaultLongitude));
			mInstance.addressString = mySharedPreferences.getString(
					"defaultAddressString", "北京");
			mInstance.mContext = context;
		}

		if (!mInstance.mLocationClient.isStarted()) {
			mInstance.mLocationClient.start();
		}
		if (mInstance.mLocationClient != null
				&& mInstance.mLocationClient.isStarted()) {
			mInstance.mLocationClient.requestLocation();
		}

		return mInstance;
	}

	public LocationUtil() {

	}

	public LocationUtil(Context context) {

		mContext = context;
		mLocationClient = new LocationClient(context.getApplicationContext());
		preferences = mContext.getSharedPreferences(Constants.SETTING,
				mContext.MODE_PRIVATE);

		startLocation();
		dabase = DataBaseHelper.getInstance(context.getApplicationContext());
	}

	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * ʵ��ʵλ�ص�����
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// Receive Location
			mLocation = location;
			SharedPreferences mySharedPreferences = mContext
					.getSharedPreferences("setting",
							UserSettingView.MODE_PRIVATE);
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putString("defaultLatitude", mLocation.getLatitude() + "");
			editor.putString("defaultLongitude", mLocation.getLongitude() + "");
			if (isNetworkConnected(mContext)) {
				addressString = location.getAddrStr();
			} else {
				addressString = "网络好像有问题哦~";
			}

			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
				addressString = location.getAddrStr();
				editor.putString("defaultAddressString", addressString);
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				addressString = location.getAddrStr();
				editor.putString("defaultAddressString", addressString);
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			editor.commit();
			logMsg(sb.toString());
		}
	}

	/**
	 * ��ʾ�����ַ���
	 * 
	 * @param str
	 */
	public void logMsg(String str) {
		try {
			if (mLocationResult != null)
				mLocationResult = str;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();

		// ���ö�λģʽ
		SharedPreferences mySharedPreferences = mContext.getSharedPreferences(
				"setting", Context.MODE_PRIVATE);
		String batteryMode = mySharedPreferences.getString("batteryMode",
				"2级(推荐)");
		option.setLocationMode(LocationMode.Device_Sensors);
		if (batteryMode.equalsIgnoreCase("1级(比较损耗)")) {
			option.setLocationMode(LocationMode.Hight_Accuracy);
		} else if (batteryMode.equalsIgnoreCase("2级(推荐)")) {
			option.setLocationMode(LocationMode.Device_Sensors);
		} else if (batteryMode.equalsIgnoreCase("3级(损耗很少)")) {
			option.setLocationMode(LocationMode.Battery_Saving);
		}

		String precision = preferences.getString(Constants.SETTING_PRECISION,
				"GPS和网络同时定位");

		if (precision.trim().equalsIgnoreCase("仅GPS定位")) {
			option.setLocationMode(LocationMode.Device_Sensors);// 设置GPS+网络同时定位模式
			option.setOpenGps(true);
		} else if (precision.trim().equalsIgnoreCase("仅网络定位")) {
			option.setLocationMode(LocationMode.Battery_Saving);// 设置GPS+网络同时定位模式
			option.setOpenGps(false);
		} else {
			option.setLocationMode(LocationMode.Hight_Accuracy);// 设置GPS+网络同时定位模式
			option.setOpenGps(true);
		}

		Log.i("batteryMode", batteryMode);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);
		// option.setOpenGps(true);
		mLocationClient.setLocOption(option);
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
	}

	public void startLocation() {
		InitLocation();
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	public double getDistanceWithLocation(LatLng location) {
		return DistanceUtil.getDistance(new LatLng(mLocation.getLatitude(),
				mLocation.getLongitude()), location);
	}

	public double getDistanceWithLocations(LatLng locationa, LatLng locationb) {
		return DistanceUtil.getDistance(locationa, locationb);
	}

	public void stopLocation() {
		InitLocation();
		mLocationClient.stop();
	}
}
