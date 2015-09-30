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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.domob.android.ads.AdEventListener;
import cn.domob.android.ads.AdView;
import cn.domob.android.ads.AdManager.ErrorCode;

import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.MostSimilarString;
import com.bnrc.util.UserDataDBHelper;
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
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.activity.AboutActivity;
import com.bnrc.busapp.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public class SearchStationView extends BaseActivity {

	private ImageView ivDeleteText;
	private EditText etSearch;
	private Button searchaBtn;
	public LocationClient mLocationClient = null;
	public MapView mMapView;
	public LocationManager locationer = null;
	public DataBaseHelper dabase = null;
	public UserDataDBHelper userdabase = null;
	public BaiduMap mBaiduMap = null;
	public LatLng mpoint = null;
	public LocationUtil mApplication = null;

	RelativeLayout mAdContainer;
	AdView mAdview;

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setAbContentView(R.layout.search_station_view);
		this.setTitleText("站点查询");
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();
		String value = MobclickAgent.getConfigParams(SearchStationView.this,
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
					return SearchStationView.this;
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

		mApplication = LocationUtil.getInstance(SearchStationView.this);
		// mainLayout = (FrameLayout)findViewById(R.id.mainLayout);
		// mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		// mSurfaceView.getHolder().setFixedSize(800, 480);
		// mSurfaceView.setVisibility(View.INVISIBLE);

		ivDeleteText = (ImageView) findViewById(R.id.ivDeleteText);
		etSearch = (EditText) findViewById(R.id.etSearch);
		searchaBtn = (Button) findViewById(R.id.btnSearch);

		Intent intent = getIntent();
		etSearch.setText(intent.getStringExtra("stationName"));

		// 加载地图和定位
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启交通图
		mBaiduMap.setTrafficEnabled(true);
		dabase = DataBaseHelper.getInstance(SearchStationView.this);
		userdabase = UserDataDBHelper.getInstance(SearchStationView.this);
		getAroundStation();
		ivDeleteText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				etSearch.setText("");
			}
		});

		etSearch.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					ivDeleteText.setVisibility(View.GONE);
				} else {
					ivDeleteText.setVisibility(View.VISIBLE);
				}
			}
		});

		searchaBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				searchStations();

			}
		});

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onMarkerClick(final Marker arg0) {
				// TODO Auto-generated method stub
				// 创建InfoWindow展示的view
				final LatLng pt = arg0.getPosition();
				ArrayList<String> station = new ArrayList<String>();
				station.add(arg0.getTitle());
				station.add(arg0.getPosition().latitude + "");
				station.add(arg0.getPosition().longitude + "");
				// MapStatusUpdate u = MapStatusUpdateFactory
				// .zoomTo(20.0f);
				// mBaiduMap.animateMapStatus(u);
				// u = MapStatusUpdateFactory.newLatLng(pt);
				// mBaiduMap.animateMapStatus(u);

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
				} else {
					//  创建InfoWindow展示的view     
					View popup = View.inflate(SearchStationView.this,
							R.layout.station_pop, null);
					TextView title = (TextView) popup
							.findViewById(R.id.tv_title);
					TextView content = (TextView) popup
							.findViewById(R.id.tv_content);
					title.setText(arg0.getTitle());
					final Button alertBtn = (Button) popup
							.findViewById(R.id.alertbt);
					final String alertString = userdabase
							.checkAlertStationWithStation(station) ? "删除提醒"
							: "添加提醒";
					alertBtn.setText(alertString);
					final Button favBtn = (Button) popup
							.findViewById(R.id.routbt);
					final String favString = userdabase
							.checkFavStationWithStationID(arg0.getTitle()) ? "取消收藏"
							: "添加收藏";
					favBtn.setText(favString);
					Button infoBtn = (Button) popup
							.findViewById(R.id.moreinfobt);
					alertBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg1) {
							// TODO Auto-generated method stub
							ArrayList<String> station = new ArrayList<String>();
							station.add(arg0.getTitle());
							station.add(pt.latitude + "");
							station.add(pt.longitude + "");
							if (userdabase
									.checkAlertStationWithStation(station)) {
								userdabase
										.deleteAlertStationWithStation(station);
								Toast toast = Toast.makeText(
										SearchStationView.this,
										"您已删除\"" + arg0.getTitle() + "\"下车提醒站点",
										Toast.LENGTH_LONG);
								alertBtn.setText("添加提醒");
								toast.show();
							} else {
								userdabase.addAlertStationWithStation(station);

								Toast toast = Toast.makeText(
										SearchStationView.this,
										"您已添加\"" + arg0.getTitle()
												+ "\"为下车提醒站点",
										Toast.LENGTH_LONG);
								alertBtn.setText("删除提醒");
								toast.show();
							}
						}
					});

					favBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg1) {
							// TODO Auto-generated method stub
							ArrayList<String> station = new ArrayList<String>();
							station.add(arg0.getTitle());
							station.add(pt.latitude + "");
							station.add(pt.longitude + "");
							if (userdabase.checkFavStationWithStationID(arg0
									.getTitle())) {
								userdabase.deleteFavStationWithStation(station);
								Toast toast = Toast.makeText(
										SearchStationView.this,
										"您已删除\"" + arg0.getTitle() + "\"收藏站点",
										Toast.LENGTH_LONG);
								favBtn.setText("添加收藏");
								toast.show();
							} else {
								userdabase.addFavStationWithStation(station);

								Toast toast = Toast.makeText(
										SearchStationView.this,
										"您已添加\"" + arg0.getTitle() + "\"为收藏站点",
										Toast.LENGTH_LONG);
								favBtn.setText("取消收藏");
								toast.show();
							}
						}
					});

					infoBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg1) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(SearchStationView.this,
									StationInformationView.class);
							// 在意图中传递数据
							intent.putExtra("title", arg0.getTitle());
							intent.putExtra("latitude",
									arg0.getPosition().latitude + "");
							intent.putExtra("longitude",
									arg0.getPosition().longitude + "");
							// 启动意图
							startActivity(intent);
						}
					});
					content.setText("距离我" + (int) (Math.floor(distance + 0.5))
							+ "米");
					InfoWindow mInfoWindow = new InfoWindow(popup, pt, -47);
					// 显示InfoWindow
					mBaiduMap.showInfoWindow(mInfoWindow);

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

		searchStations();
		// 默认软键盘不弹出
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	public void searchStations() {
		mBaiduMap.clear();
		InputMethodManager imm = (InputMethodManager) getSystemService(SearchStationView.this.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

		if (etSearch.getText().toString().equalsIgnoreCase("我的位置")
				|| etSearch.getText().toString().length() < 1) {
			getAroundStation();
		} else {
			BDLocation location = mApplication.mLocation;
			// 定义Maker坐标点
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

			try {
				ArrayList<ArrayList<String>> stations = dabase
						.getStationsWithStationKeyword(etSearch.getText()
								.toString());
				Toast toast = Toast.makeText(getApplicationContext(),
						"查找到关于\"" + etSearch.getText().toString() + "\"的"
								+ stations.size() + "个站点", Toast.LENGTH_LONG);
				// toast.setGravity(Gravity.TOP, 0, 0);
				if (stations.size() > 0) {
					String mostSimilarStationString = (new MostSimilarString())
							.getMostSimilarStringWithArray(stations, etSearch
									.getText().toString(), 1);
					toast.show();
					for (int i = 0; i < stations.size(); i++) {
						ArrayList<String> station = new ArrayList<String>();
						station = stations.get(i);
						// 定义Maker坐标点
						LatLng point = new LatLng(Float.parseFloat(station
								.get(2)), Float.parseFloat(station.get(3)));
						// 构建Marker图标
						BitmapDescriptor bitmap2 = BitmapDescriptorFactory
								.fromResource(R.drawable.bus_img2);
						// 构建MarkerOption，用于在地图上添加Marker
						OverlayOptions option2 = new MarkerOptions()
								.position(point).icon(bitmap2).zIndex(9) // 设置marker所在层级
								.draggable(true).title(station.get(1)); // 设置手势拖拽;
						// 在地图上添加Marker，并显示
						mBaiduMap.addOverlay(option2);

						if (station.get(1).equalsIgnoreCase(
								mostSimilarStationString)) {
							MapStatusUpdate u = MapStatusUpdateFactory
									.zoomTo(20.0f);
							mBaiduMap.animateMapStatus(u);
							u = MapStatusUpdateFactory.newLatLng(point);
							mBaiduMap.animateMapStatus(u);
						}
					}
				}

			} catch (SQLException sqle) {
				throw sqle;
			}
		}
	}

	public void getAroundStation() {

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
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.animateMapStatus(u);
		u = MapStatusUpdateFactory.newLatLng(mpoint);
		mBaiduMap.animateMapStatus(u);

		// 定义文字所显示的坐标点
		LatLng llText = new LatLng(mpoint.latitude, mpoint.longitude);
		// 构建文字Option对象，用于在地图上添加文字
		OverlayOptions textOption = new TextOptions().bgColor(0xAAFFFF00)
				.fontSize(36).fontColor(0xFFFF00FF)
				.text(mApplication.addressString).rotate(0).position(llText);
		// 在地图上添加该文字对象并显示
		mBaiduMap.addOverlay(textOption);

		try {
			ArrayList<ArrayList<String>> stations = dabase
					.getAroundStationsWithLocation(mpoint);
			Toast toast = Toast.makeText(getApplicationContext(), "在附近查找到"
					+ stations.size() + "个站点", Toast.LENGTH_LONG);
			toast.show();
			for (int i = 0; i < stations.size(); i++) {
				ArrayList<String> station = new ArrayList<String>();
				station = stations.get(i);
				// 定义Maker坐标点
				LatLng point = new LatLng(Float.parseFloat(station.get(2)),
						Float.parseFloat(station.get(3)));
				// 构建Marker图标
				BitmapDescriptor bitmap2 = BitmapDescriptorFactory
						.fromResource(R.drawable.bus_img2);
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option2 = new MarkerOptions().position(point)
						.icon(bitmap2).zIndex(9) // 设置marker所在层级
						.draggable(true).title(station.get(1)); // 设置手势拖拽;
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option2);

			}
		} catch (SQLException sqle) {
			throw sqle;
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // 统计页面
		MobclickAgent.onResume(this); // 统计时长
		mMapView.onResume();
		registerReceiver(mWifiReceiver,wifiFilter);
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
