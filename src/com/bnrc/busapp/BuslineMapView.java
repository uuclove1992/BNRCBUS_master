package com.bnrc.busapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bnrc.network.MyVolley;
import com.bnrc.network.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineResult.BusStep;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;

import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;

import com.ab.activity.AbActivity;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.bnrc.busapp.R;
import com.bnrc.network.toolbox.Response;
import com.bnrc.network.toolbox.VolleyError;
import com.bnrc.util.BuslineDBHelper;
import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.MostSimilarString;
import com.bnrc.util.StationOverlay;
import com.bnrc.util.UserDataDBHelper;
import com.bnrc.util.collectwifi.BaseActivity;
import com.google.gson.JsonArray;
import com.umeng.analytics.MobclickAgent;

public class BuslineMapView extends BaseActivity implements
		OnGetPoiSearchResultListener, OnGetBusLineSearchResultListener,
		BaiduMap.OnMapClickListener {

	private String buslineName = null;
	private String buslineTotalName = null;
	private String buslineID = null;
	private BusLineResult route = null;
	private List<String> busLineIDList = null;
	public ArrayList<ArrayList<String>> busline = null;
	public ArrayList<ArrayList<String>> busesArr = null;
	public ArrayList<Object> stationItems = null;
	private int busLineIndex = 0;
	public LatLng mpoint = null;
	public DataBaseHelper dabase = null;
	public UserDataDBHelper userdabase = null;
	public LocationUtil mApplication = null;
	public BuslineDBHelper buslineDBHelper = null;
	public HorizontalScrollView mScrollView = null;
	// 搜索相关
	private PoiSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private BusLineSearch mBusLineSearch = null;
	private BaiduMap mBaiduMap = null;
	public MapView mMapView;
	private ArrayList<ArrayList<String>> stations = null;
	private ArrayList<View> busArrayList = null;
	private LinearLayout stationContainer;
	private FrameLayout busContainer;
	private int stationItemWidth = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setAbContentView(R.layout.busline_map_view);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleTextMargin(0, 0, 0, 0);
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();

		Intent intent = getIntent();

		buslineName = intent.getStringExtra("title");
		buslineTotalName = intent.getStringExtra("buslineName");
		this.setTitleText(buslineTotalName);
		buslineID = intent.getStringExtra("buslineID");
		mApplication = LocationUtil.getInstance(BuslineMapView.this);
		;
		dabase = DataBaseHelper.getInstance(BuslineMapView.this);
		userdabase = UserDataDBHelper.getInstance(BuslineMapView.this);
		busline = dabase.getStationsWithBuslineId(buslineID);
		// 加载地图和定位
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启交通图
		// mBaiduMap.setTrafficEnabled(true);

		mBaiduMap.setOnMapClickListener(this);
		mSearch = PoiSearch.newInstance();
		mSearch.setOnGetPoiSearchResultListener(this);
		mScrollView = (HorizontalScrollView) findViewById(R.id.mScrollView);
		mBusLineSearch = BusLineSearch.newInstance();
		mBusLineSearch.setOnGetBusLineSearchResultListener(this);
		busLineIDList = new ArrayList<String>();
		busLineIDList.clear();
		busLineIndex = 0;
		stationContainer = (LinearLayout) findViewById(R.id.stationList);
		busContainer = (FrameLayout) findViewById(R.id.busList);

		stations = dabase.getStationsWithBuslineId(buslineID);

		busArrayList = new ArrayList<View>();
		ArrayList<String> buslineArrayList = BuslineDBHelper.getInstance(
				BuslineMapView.this).getBuslineInfoWithBuslineName(
				buslineTotalName);
		if (buslineArrayList.size() > 0) {
			loadBuslineData();
		} else {
			mScrollView.setVisibility(View.GONE);
		}
		mScrollView.setVisibility(View.GONE);

		try {
			loadBuslineMap();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimerTask task = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							ArrayList<String> buslineArrayList = BuslineDBHelper
									.getInstance(BuslineMapView.this)
									.getBuslineInfoWithBuslineName(
											buslineTotalName);
							if (buslineArrayList.size() > 0) {
								try {
									get_realtime_data(buslineTotalName, 1);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								// Toast toast =
								// Toast.makeText(BuslineMapView.this,
								// "暂不支持该线路的实时信息！",
								// Toast.LENGTH_LONG);
								// toast.show();
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				// curlocation.setText("当前位置："+mApplication.addressString);
			}
		};

		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		Timer timer = new Timer(true);
		String timeString = mySharedPreferences.getString("refreshMode", "30秒");
		timeString = timeString.substring(0, timeString.length() - 1);
		timer.schedule(task, 10000, Integer.parseInt(timeString) * 1000); // 延时1000ms后执行，1000ms执行一次
		// timer.cancel(); //退出计时器
	}

	public void SearchNextBusline(View v) {
		if (busLineIndex >= busLineIDList.size()) {
			busLineIndex = 0;
		}
		if (busLineIndex >= 0 && busLineIndex < busLineIDList.size()
				&& busLineIDList.size() > 0) {
			mBusLineSearch.searchBusLine((new BusLineSearchOption().city("北京")
					.uid(busLineIDList.get(busLineIndex))));
			busLineIndex++;
		}
	}

	public void loadBuslineMap() throws JSONException {
		// 发起poi检索，从得到所有poi中找到公交线路类型的poi，再使用该poi的uid进行公交详情搜索
		mSearch.searchInCity((new PoiCitySearchOption()).city("北京").keyword(
				buslineName));
		// 如下代码为发起检索代码，定义监听者和设置监听器的方法与POI中的类似
		mBusLineSearch.searchBusLine((new BusLineSearchOption().city("北京")
				.uid(buslineName)));
	}

	public void loadBuslineData() {

		int j = stations.size();
		stationItems = new ArrayList<Object>();
		for (int i = 0; i < j; i++) {
			View stationItem = View.inflate(BuslineMapView.this,
					R.layout.station_item_view, null);
			if (i == 0) {
				int w = View.MeasureSpec.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED);
				int h = View.MeasureSpec.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED);
				stationItem.measure(w, h);
				stationItemWidth = stationItem.getMeasuredWidth();
			}
			TextView title = (TextView) stationItem.findViewById(R.id.tv_title);
			Button stationImg = (Button) stationItem
					.findViewById(R.id.stationImg);
			ArrayList<String> station = new ArrayList<String>();
			station = stations.get(i);
			title.setText(station.get(0));
			stationItems.add(stationItem);
			stationContainer.addView(stationItem);
			stationImg.setTag((i + 1024) + "");

			stationImg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					int tag = Integer.parseInt(arg0.getTag().toString()) - 1024;
					// Intent intent = null;
					Intent intent = new Intent(BuslineMapView.this,
							StationInformationView.class);
					// 在意图中传递数据
					ArrayList<String> station = new ArrayList<String>();
					station = stations.get(tag);
					intent.putExtra("title", station.get(0));
					intent.putExtra("latitude", station.get(1));
					intent.putExtra("longitude", station.get(2));
					// 启动意图
					startActivity(intent);
				}
			});
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	@Override
	protected void onDestroy() {
		mSearch.destroy();
		mBusLineSearch.destroy();
		super.onDestroy();
	}

	@Override
	public void onGetBusLineResult(BusLineResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

			return;
		}
		mBaiduMap.clear();

		route = result;

		StationOverlay overlay = new StationOverlay(mBaiduMap,
				BuslineMapView.this);
		mBaiduMap.setOnMarkerClickListener(overlay);
		overlay.setData(result);
		overlay.addToMap();
		overlay.zoomToSpan();
		Toast.makeText(BuslineMapView.this, result.getBusLineName(),
				Toast.LENGTH_SHORT).show();

		ArrayList<String> buslineArrayList = BuslineDBHelper.getInstance(
				BuslineMapView.this).getBuslineInfoWithBuslineName(
				buslineTotalName);
		if (buslineArrayList.size() > 0) {
			try {
				get_realtime_data(buslineTotalName, stations.size() / 2);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Toast toast = Toast.makeText(BuslineMapView.this,
			// "暂不支持该线路的实时信息！",
			// Toast.LENGTH_LONG);
			// toast.show();
		}

	}

	@Override
	public void onGetPoiResult(PoiResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			// Toast.makeText(BuslineMapView.this, "抱歉，未找到结果",
			// Toast.LENGTH_LONG)
			// .show();
			return;
		}
		// 遍历所有poi，找到类型为公交线路的poi
		busLineIDList.clear();
		for (PoiInfo poi : result.getAllPoi()) {
			if (poi.type == PoiInfo.POITYPE.BUS_LINE
					|| poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
				busLineIDList.add(poi.uid);
			}
		}
		SearchNextBusline(null);
		route = null;
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {

	}

	@Override
	public void onMapClick(LatLng point) {
		mBaiduMap.hideInfoWindow();
	}

	@Override
	public boolean onMapPoiClick(MapPoi poi) {
		return false;
	}

	public void showStationView(final int index) throws JSONException {
		// TODO Auto-generated method stub
		if (route != null) {
			if (index > 0 && index < route.getStations().size()) {
				Toast toast = Toast
						.makeText(BuslineMapView.this,
								route.getStations().get(index).getTitle(),
								Toast.LENGTH_LONG);
				toast.show();
			}
		}

		// TextView popupText = new TextView(this);
		// popupText.setBackgroundResource(R.drawable.bus_img2);
		// popupText.setTextColor(0xff000000);
		//
		// if (index >= 0) {
		// // 移动到指定索引的坐标
		// int x = (int) ((index - 0.65) * stationItemWidth);
		// int y = 0;
		// mScrollView.smoothScrollTo(x, y);
		//
		// LocationUtil mApplication = LocationUtil
		// .getInstance(BuslineMapView.this);
		// ;
		// BDLocation location = mApplication.mLocation;
		// // 定义Maker坐标点
		// mpoint = new LatLng(location.getLatitude(), location.getLongitude());
		// double distance = DistanceUtil.getDistance(mpoint, route
		// .getStations().get(index).getLocation());
		// View popup = View.inflate(BuslineMapView.this,
		// R.layout.station_pop, null);
		// TextView title = (TextView) popup.findViewById(R.id.tv_title);
		// TextView content = (TextView) popup.findViewById(R.id.tv_content);
		// title.setText(route.getStations().get(index).getTitle());
		// final Button alertBtn = (Button) popup.findViewById(R.id.alertbt);
		// final String alertString = userdabase
		// .checkAlertStationWithStationID(route.getStations()
		// .get(index).getTitle()) ? "删除提醒" : "添加提醒";
		// alertBtn.setText(alertString);
		// final Button favBtn = (Button) popup.findViewById(R.id.routbt);
		// final String favString = userdabase
		// .checkFavStationWithStationID(route.getStations()
		// .get(index).getTitle()) ? "取消收藏" : "添加收藏";
		// favBtn.setText(favString);
		// Button infoBtn = (Button) popup.findViewById(R.id.moreinfobt);
		// alertBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg1) {
		// ArrayList<String> station = new ArrayList<String>();
		// station.add(route.getStations().get(index).getTitle());
		// station.add(route.getStations().get(index).getLocation().latitude
		// + "");
		// station.add(route.getStations().get(index).getLocation().longitude
		// + "");
		// if (userdabase.checkAlertStationWithStationID(route
		// .getStations().get(index).getTitle())) {
		// userdabase.deleteAlertStationWithStation(station);
		// Toast toast = Toast.makeText(BuslineMapView.this,
		// "您已删除\""
		// + route.getStations().get(index)
		// .getTitle() + "\"下车提醒站点",
		// Toast.LENGTH_LONG);
		// alertBtn.setText("添加提醒");
		// toast.show();
		// } else {
		// userdabase.addAlertStationWithStation(station);
		//
		// Toast toast = Toast.makeText(BuslineMapView.this,
		// "您已添加\""
		// + route.getStations().get(index)
		// .getTitle() + "\"为下车提醒站点",
		// Toast.LENGTH_LONG);
		// alertBtn.setText("删除提醒");
		// toast.show();
		// }
		// }
		// });
		//
		// favBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg1) {
		// // TODO Auto-generated method stub
		// ArrayList<String> station = new ArrayList<String>();
		// station.add(route.getStations().get(index).getTitle());
		// station.add(route.getStations().get(index).getLocation().latitude
		// + "");
		// station.add(route.getStations().get(index).getLocation().longitude
		// + "");
		// if (userdabase.checkFavStationWithStationID(route
		// .getStations().get(index).getTitle())) {
		// userdabase.deleteFavStationWithStation(station);
		// Toast toast = Toast.makeText(BuslineMapView.this,
		// "您已删除\""
		// + route.getStations().get(index)
		// .getTitle() + "\"收藏站点",
		// Toast.LENGTH_LONG);
		// favBtn.setText("添加收藏");
		// toast.show();
		// } else {
		// userdabase.addFavStationWithStation(station);
		//
		// Toast toast = Toast.makeText(BuslineMapView.this,
		// "您已添加\""
		// + route.getStations().get(index)
		// .getTitle() + "\"为收藏站点",
		// Toast.LENGTH_LONG);
		// favBtn.setText("取消收藏");
		// toast.show();
		// }
		// }
		// });
		//
		// infoBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg1) {
		// Intent intent = new Intent(BuslineMapView.this,
		// StationInformationView.class);
		// // 在意图中传递数据
		// intent.putExtra("title", route.getStations().get(index)
		// .getTitle());
		// intent.putExtra("latitude", route.getStations().get(index)
		// .getLocation().latitude
		// + "");
		// intent.putExtra("longitude", route.getStations().get(index)
		// .getLocation().longitude
		// + "");
		// // 启动意图
		// startActivity(intent);
		// }
		// });
		// content.setText("距离我" + (int) (Math.floor(distance + 0.5)) + "米");
		// InfoWindow mInfoWindow = new InfoWindow(popup, route.getStations()
		// .get(index).getLocation(), -47);
		// // 显示InfoWindow
		// mBaiduMap.showInfoWindow(mInfoWindow);
		// }
	}

	public void get_realtime_data(String lineName, int station_num)
			throws JSONException, UnsupportedEncodingException {

		mBaiduMap.clear();
		List<BusStation> allStations = new ArrayList<BusLineResult.BusStation>();
		if (route != null) {
			List<BusStep> steps = route.getSteps();
			if (steps.size() > 0) {
				allStations = route.getStations();
				List<LatLng> stepPts = steps.get(0).getWayPoints();
				// 构建用户绘制多边形的Option对象
				OverlayOptions polygonOption = new PolylineOptions().points(
						stepPts).color(0x9900FF00);
				// 在地图上添加多边形Option，用于显示
				mBaiduMap.addOverlay(polygonOption);
			}
		}

		int j = allStations.size();
		for (int i = 0; i < j; i++) {
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.busline_station);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions()
					.position(allStations.get(i).getLocation()).icon(bitmap)
					.zIndex(-1)
					// 设置marker所在层级
					.draggable(true).title(allStations.get(i).getTitle())
					.anchor(0.5f, 0.5f); // 设置手势拖拽;
			// 在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
		}

		ArrayList<String> buslineArrayList = BuslineDBHelper.getInstance(
				BuslineMapView.this).getBuslineInfoWithBuslineName(lineName);
		String url = "http://bjgj.aibang.com:8899/bus.php?city="
				+ URLEncoder.encode("北京", "utf-8") + "&id="
				+ buslineArrayList.get(3) + "&no=" + station_num
				+ "&type=0&encrypt=1&versionid=2";
		String value = MobclickAgent.getConfigParams(BuslineMapView.this,
				"rtbusurl");
		if (value.toString().length() > 0) {
			url = value + "&" + URLEncoder.encode("北京", "utf-8") + "&id="
					+ buslineArrayList.get(3) + "&no=" + station_num;
		}
		StringRequest request = new StringRequest(url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {

						JSONObject jsonObj = null;
						try {
							jsonObj = XML.toJSONObject(response);

							JSONObject busJsonObject = ((JSONObject) (jsonObj
									.getJSONObject("root")))
									.getJSONObject("data");
							JSONArray busesArray = busJsonObject
									.getJSONArray("bus");
							InputMethodManager imm = (InputMethodManager) getSystemService(BuslineMapView.this.INPUT_METHOD_SERVICE);
							BDLocation location = mApplication.mLocation;
							// 定义Maker坐标点
							mpoint = new LatLng(location.getLatitude(),
									location.getLongitude());
							// 构建Marker图标
							BitmapDescriptor bitmap = BitmapDescriptorFactory
									.fromResource(R.drawable.nearby_station);
							// 构建MarkerOption，用于在地图上添加Marker
							OverlayOptions option = new MarkerOptions()
									.position(mpoint).icon(bitmap).zIndex(-1) // 设置marker所在层级
									.draggable(true).title("我的位置"); // 设置手势拖拽;
							// 在地图上添加Marker，并显示
							mBaiduMap.addOverlay(option);
							// 调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听

							try {
								int m = busArrayList.size();
								for (int i = 0; i < m; i++) {
									View bus = busArrayList.get(i);
									bus.setVisibility(View.GONE);
									busArrayList.remove(i);
									i--;
									m--;
								}
								int j = busesArray.length();
								for (int i = 0; i < j; i++) {
									JSONObject jsonObject = (JSONObject) busesArray
											.get(i);

									LatLng point = new LatLng(
											Float.parseFloat(new MyCipher(
													"aibang"
															+ jsonObject
																	.getString("gt"))
													.decrypt(jsonObject
															.getString("y"))),
											Float.parseFloat(new MyCipher(
													"aibang"
															+ jsonObject
																	.getString("gt"))
													.decrypt(jsonObject
															.getString("x"))));
									// 将google地图、soso地图、aliyun地图、mapabc地图和amap地图//
									// 所用坐标转换成百度坐标
									CoordinateConverter converter = new CoordinateConverter();
									converter.from(CoordType.COMMON);
									// sourceLatLng待转换坐标
									converter.coord(point);
									point = converter.convert();

									// 构建Marker图标
									BitmapDescriptor bitmap2 = BitmapDescriptorFactory
											.fromResource(R.drawable.br_clr_ptrs);
									// 构建MarkerOption，用于在地图上添加Marker
									OverlayOptions option2 = new MarkerOptions()
											.position(point)
											.icon(bitmap2)
											.zIndex(9)
											// 设置marker所在层级
											.draggable(true)
											.title(new MyCipher("aibang"
													+ jsonObject
															.getString("gt"))
													.decrypt(jsonObject
															.getString("ns"))); // 设置手势拖拽;
									// 在地图上添加Marker，并显示
									mBaiduMap.addOverlay(option2);

									int k = stations.size();
									ArrayList<String> station = new ArrayList<String>();

									for (int l = 0; l < k; l++) {
										station = stations.get(l);
										if (station
												.get(0)
												.equalsIgnoreCase(
														new MyCipher(
																"aibang"
																		+ jsonObject
																				.getString("gt"))
																.decrypt(jsonObject
																		.getString("ns")))) {
											Button bus1 = new Button(
													BuslineMapView.this);
											bus1.setBackgroundResource(R.drawable.ic_5_car_target);
											bus1.setGravity(Gravity.BOTTOM);

											FrameLayout.LayoutParams layoutParamsTextInfo = new FrameLayout.LayoutParams(
													120, 70);

											layoutParamsTextInfo.gravity = Gravity.BOTTOM;
											layoutParamsTextInfo.leftMargin = (int) ((l + 0.5) * stationItemWidth);
											busContainer.addView(bus1,
													layoutParamsTextInfo);
											bus1.setMaxHeight(38);
											bus1.setMaxWidth(64);
											busArrayList.add(bus1);
											break;
										}
									}

								}
							} catch (SQLException sqle) {
								throw sqle;
							}
						} catch (JSONException e) {
							Log.e("JSON exception", e.getMessage());
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});
		MyVolley.sharedVolley(BuslineMapView.this).getRequestQueue()
				.add(request);

	}

}
