package com.bnrc.busapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONObject;

import u.aly.bu;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.domob.android.ads.AdEventListener;
import cn.domob.android.ads.AdView;
import cn.domob.android.ads.AdManager.ErrorCode;

import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.MostSimilarString;
import com.bnrc.util.MyPoiOverlay;
import com.bnrc.util.collectwifi.BaseActivity;
import com.ab.activity.AbActivity;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.activity.AboutActivity;
import com.bnrc.busapp.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public class SearchSomethingView extends BaseActivity {

	public LocationClient mLocationClient = null;
	public MapView mMapView;
	public LocationManager locationer = null;
	public DataBaseHelper dabase = null;
	public BaiduMap mBaiduMap = null;
	public LatLng mpoint = null;
	public LocationUtil mApplication = null;
	private PoiSearch mPoiSearch = null;
	private String keywordString = null;

	RelativeLayout mAdContainer;
	AdView mAdview;

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setAbContentView(R.layout.search_something_view);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent
				.setOnlineConfigureListener(new UmengOnlineConfigureListener() {
					@Override
					public void onDataReceived(JSONObject data) {
					}
				});
		String value = MobclickAgent.getConfigParams(SearchSomethingView.this,
				"open_ad");
		Log.i("开启广告", value);
		if (value.equals("1")) {
			mAdContainer = (RelativeLayout) findViewById(R.id.adcontainer);
			// Create ad view
			mAdview = new AdView(this, "56OJzfwIuN7tr9LoSs",
					"16TLmHWoAp8diNUdpuAEMYfi");
			SharedPreferences mySharedPreferences = getSharedPreferences(
					"setting", UserSettingView.MODE_PRIVATE);
			String agString = mySharedPreferences.getString("userAge", "20");
			String sexString = mySharedPreferences.getString("userSex", "女");

			if (sexString.equals("女")) {
				mAdview.setUserGender("female");
			} else {
				mAdview.setUserGender("male");
			}
			// mAdview.setKeyword("game");

			Calendar mycalendar = Calendar.getInstance();// 获取现在时间
			String curYearString = String
					.valueOf(mycalendar.get(Calendar.YEAR));// 获取年份
			int age = Integer.parseInt(agString);
			int birth = Integer.parseInt(curYearString) - age;
			mAdview.setUserBirthdayStr(birth + "-08-08");
			mAdview.setUserPostcode("123456");
			mAdview.setAdEventListener(new AdEventListener() {
				@Override
				public void onAdOverlayPresented(AdView adView) {
					Log.i("DomobSDKDemo", "overlayPresented");
				}

				@Override
				public void onAdOverlayDismissed(AdView adView) {
					Log.i("DomobSDKDemo", "Overrided be dismissed");
				}

				@Override
				public void onAdClicked(AdView arg0) {
					Log.i("DomobSDKDemo", "onDomobAdClicked");
				}

				@Override
				public void onLeaveApplication(AdView arg0) {
					Log.i("DomobSDKDemo", "onDomobLeaveApplication");
				}

				@Override
				public Context onAdRequiresCurrentContext() {
					return SearchSomethingView.this;
				}

				@Override
				public void onAdFailed(AdView arg0, ErrorCode arg1) {
					Log.i("DomobSDKDemo", "onDomobAdFailed");
				}

				@Override
				public void onEventAdReturned(AdView arg0) {
					Log.i("DomobSDKDemo", "onDomobAdReturned");
				}
			});
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
			mAdview.setLayoutParams(layout);
			mAdContainer.addView(mAdview);
		}

		// initTitleRightLayout();

		mApplication = LocationUtil.getInstance(SearchSomethingView.this);

		// 加载地图和定位
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		dabase = DataBaseHelper.getInstance(SearchSomethingView.this);

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onMarkerClick(final Marker arg0) {
				// TODO Auto-generated method stub
				// 创建InfoWindow展示的view
				final LatLng pt = arg0.getPosition();

				double distance = DistanceUtil.getDistance(mpoint,
						arg0.getPosition());
				// button.setBackgroundResource(R.drawable.popup);
				if (arg0.getTitle().equalsIgnoreCase("我的位置")) {
					// Button button = new Button(getApplicationContext());
					// button.setTextSize(17);
					// button.setEnabled(true);
					// button.setText(arg0.getTitle() + "\n"
					// + mApplication.addressString);
					// // 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
					// InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);
					// // 显示InfoWindow
					// mBaiduMap.showInfoWindow(mInfoWindow);
				}

				return false;
			}
		});
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
			}
		});

		getAroundSomething();
	}

	public void getAroundSomething() {

		mBaiduMap.clear();

		BDLocation location = mApplication.mLocation;
		mpoint = new LatLng(location.getLatitude(), location.getLongitude());
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.nearby_station);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(mpoint)
				.icon(bitmap).zIndex(9) // 设置marker所在层级
				.draggable(true).title("我的位置"); // 设置手势拖拽;
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
		// 调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听

		// 定义文字所显示的坐标点
		LatLng llText = new LatLng(mpoint.latitude, mpoint.longitude);
		// 构建文字Option对象，用于在地图上添加文字
		OverlayOptions textOption = new TextOptions().bgColor(0xAAFFFF00)
				.fontSize(36).fontColor(0xFFFF00FF)
				.text(mApplication.addressString).rotate(0).position(llText);
		// 在地图上添加该文字对象并显示

		mBaiduMap.addOverlay(textOption);

		mPoiSearch = PoiSearch.newInstance();
		OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
			public void onGetPoiResult(PoiResult result) {
				// 获取POI检索结果
				if (result == null
						|| result.error != PoiResult.ERRORNO.NO_ERROR) {
					Toast.makeText(SearchSomethingView.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				}
				if (result.error == PoiResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == PoiResult.ERRORNO.NO_ERROR) {

					PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(overlay);

					overlay.setData(result);
					overlay.addToMap();
				}
			}

			public void onGetPoiDetailResult(PoiDetailResult result) {
				// 获取Place详情页检索结果
				if (result.error != PoiDetailResult.ERRORNO.NO_ERROR) {
					Toast.makeText(SearchSomethingView.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SearchSomethingView.this,
							result.getName() + ": " + result.getAddress(),
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		Intent intent = getIntent();
		keywordString = intent.getStringExtra("keyword");
		if (keywordString.length() == 0) {
			keywordString = "公交";
		}
		this.setTitleText("附近的" + keywordString);
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		PoiNearbySearchOption option2 = (new PoiNearbySearchOption())
				.keyword(keywordString).location(mpoint).radius(1200)
				.pageCapacity(20).pageNum(1);
		mPoiSearch.searchNearby(option2);

		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(15.8f);
		mBaiduMap.animateMapStatus(u);
		u = MapStatusUpdateFactory.newLatLng(mpoint);
		mBaiduMap.animateMapStatus(u);

	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			// if (poi.hasCaterDetails) {
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
					.poiUid(poi.uid));
			// }
			return true;
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // 统计页面
		MobclickAgent.onResume(this); // 统计时长
		mMapView.onResume();
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); // 保证 onPageEnd 在onPause
													// 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPause(this);
		mMapView.onResume();
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

}
