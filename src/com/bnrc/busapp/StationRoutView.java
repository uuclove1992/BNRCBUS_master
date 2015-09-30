package com.bnrc.busapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.ab.activity.AbActivity;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.adapter.MyListViewAdapter;
import com.bnrc.network.JsonObjectRequest;
import com.bnrc.network.MyVolley;
import com.bnrc.network.StringRequest;
import com.bnrc.network.toolbox.Request;
import com.bnrc.network.toolbox.RequestQueue;
import com.bnrc.network.toolbox.Response;
import com.bnrc.network.toolbox.Volley;
import com.bnrc.network.toolbox.VolleyError;
import com.bnrc.util.BuslineDBHelper;
import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.UserDataDBHelper;
import com.bnrc.util.collectwifi.BaseActivity;
import com.google.gson.JsonArray;
import com.umeng.analytics.MobclickAgent;

public class StationRoutView extends BaseActivity {

	public DataBaseHelper dabase = null;
	private String title;
	private LatLng stationPoint = null;

	public MapView mMapView;
	private TextView mTitleTextView;
	public BaiduMap mBaiduMap = null;
	public LatLng mpoint = null;
	public LocationUtil mApplication = null;

	// 步行
	public Button walkButton = null;
	private ListView walkListView = null;
	private MyListViewAdapter walkListViewAdapter;
	public List<Map<String, Object>> walkListData;

	// 公交
	public Button busButton = null;
	private ListView busListView = null;
	private MyListViewAdapter busListViewAdapter;
	public List<Map<String, Object>> busListData;

	// 驾车
	public Button driveButton = null;
	private ListView driveListView = null;
	private MyListViewAdapter driveListViewAdapter;
	public List<Map<String, Object>> driveListData;

	public RoutePlanSearch search;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setAbContentView(R.layout.station_rout_view);
		this.setTitleTextMargin(20, 0, 0, 0);
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();
		Intent intent = getIntent();
		this.setTitleText(intent.getStringExtra("title"));
		title = intent.getStringExtra("title");
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		stationPoint = new LatLng(Float.parseFloat(intent
				.getStringExtra("latitude")), Float.parseFloat(intent
				.getStringExtra("longitude")));
		ArrayList<String> station = new ArrayList<String>();
		station.add(title);
		station.add(intent.getStringExtra("latitude"));
		station.add(intent.getStringExtra("longitude"));
		UserDataDBHelper.getInstance(StationRoutView.this)
				.addSearchStaitonWithStation(station);
		mTitleTextView = (TextView) findViewById(R.id.mTitleTextView);
		mTitleTextView.setText("以下是到达该站点的步行方案...");
		busButton = (Button) findViewById(R.id.busbtn);
		walkButton = (Button) findViewById(R.id.walkbtn);
		driveButton = (Button) findViewById(R.id.drivebtn);

		walkListView = (ListView) this.findViewById(R.id.mBuslineListView);
		walkListData = new ArrayList<Map<String, Object>>();
		walkListViewAdapter = new MyListViewAdapter(this, walkListData,
				R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
						"itemsText" }, new int[] { R.id.itemsIcon,
						R.id.itemsTitle, R.id.itemsText });
		walkListView.setAdapter(walkListViewAdapter);

		busListView = (ListView) findViewById(R.id.mRoutListView);
		busListData = new ArrayList<Map<String, Object>>();
		busListViewAdapter = new MyListViewAdapter(this, busListData,
				R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
						"itemsText" }, new int[] { R.id.itemsIcon,
						R.id.itemsTitle, R.id.itemsText });
		busListView.setAdapter(busListViewAdapter);

		driveListView = (ListView) findViewById(R.id.mRTBusListView);
		driveListData = new ArrayList<Map<String, Object>>();
		driveListViewAdapter = new MyListViewAdapter(this, driveListData,
				R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
						"itemsText" }, new int[] { R.id.itemsIcon,
						R.id.itemsTitle, R.id.itemsText });
		driveListView.setAdapter(driveListViewAdapter);

		dabase = DataBaseHelper.getInstance(StationRoutView.this);

		mApplication = LocationUtil.getInstance(StationRoutView.this);
		BDLocation location = mApplication.mLocation;
		mpoint = new LatLng(location.getLatitude(), location.getLongitude());

		search = RoutePlanSearch.newInstance();
		search.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {

			@Override
			public void onGetWalkingRouteResult(WalkingRouteResult result) {
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					Toast.makeText(StationRoutView.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				}
				if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// ���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == SearchResult.ERRORNO.NO_ERROR) {
					// route = result.getRouteLines().get(0);
					WalkingRouteOverlay overlay = new WalkingRouteOverlay(
							mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(overlay);
					mBaiduMap.clear();
					if (result.getRouteLines() != null) {
						if (result.getRouteLines().size() > 0) {
							overlay.setData(result.getRouteLines().get(0));
							walkListData.clear();
							WalkingRouteLine liendata = result.getRouteLines()
									.get(0);
							List<WalkingRouteLine.WalkingStep> allstepList = liendata
									.getAllStep();
							int j = allstepList.size();
							Map<String, Object> map = new HashMap<String, Object>();
							String duration = null;
							for (int i = 0; i < j; i++) {
								WalkingRouteLine.WalkingStep step = allstepList
										.get(i);

								int d = step.getDuration();

								if (d > 3600) {
									duration = "预计消耗时间 " + d / 3600 + " 小时 "
											+ d % 3600 / 60
											+ (d % 60 > 30 ? 1 : 0) + " 分钟";
								} else if (d > 60) {
									duration = "预计消耗时间 "
											+ (d / 60 + (d % 60 > 30 ? 1 : 0))
											+ " 分钟";
								} else {
									duration = "预计消耗时间 " + d + " 秒";
								}

								map = new HashMap<String, Object>();
								map.put("itemsIcon", R.drawable.walk_img);
								map.put("itemsTitle", step.getInstructions());
								map.put("itemsText", duration);
								walkListData.add(map);
							}
							walkListViewAdapter.notifyDataSetChanged();

							overlay.addToMap();
							overlay.zoomToSpan();
							MapStatusUpdate u = MapStatusUpdateFactory
									.zoomTo(17.0f);
							mBaiduMap.animateMapStatus(u);

							LatLng center = new LatLng(
									(mpoint.latitude + stationPoint.latitude) / 2,
									(mpoint.longitude + stationPoint.longitude) / 2);
							u = MapStatusUpdateFactory.newLatLng(center);
							mBaiduMap.animateMapStatus(u);
							mTitleTextView.setText("以下是到达该站点的步行方案...");
						}
					}

				}
			}

			@Override
			public void onGetTransitRouteResult(TransitRouteResult result) {
				// ��ȡ��������·���滮���
				// ��ȡ������·�滮���
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					Toast.makeText(StationRoutView.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				}
				if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// ���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == SearchResult.ERRORNO.NO_ERROR) {
					// route = result.getRouteLines().get(0);
					TransitRouteOverlay overlay = new TransitRouteOverlay(
							mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(overlay);
					mBaiduMap.clear();
					if (result.getRouteLines() != null) {
						if (result.getRouteLines().size() > 0) {
							overlay.setData(result.getRouteLines().get(0));

							TransitRouteLine liendata = result.getRouteLines()
									.get(0);
							List<TransitRouteLine.TransitStep> allstepList = liendata
									.getAllStep();
							int j = allstepList.size();
							Map<String, Object> map = new HashMap<String, Object>();
							String duration = null;

							busListData.clear();
							for (int i = 0; i < j; i++) {
								TransitRouteLine.TransitStep step = allstepList
										.get(i);
								int d = step.getDuration();

								if (d > 3600) {
									duration = "预计消耗时间 " + d / 3600 + " 小时 "
											+ d % 3600 / 60
											+ (d % 60 > 30 ? 1 : 0) + " 分钟";
								} else if (d > 60) {
									duration = "预计消耗时间 "
											+ (d / 60 + (d % 60 > 30 ? 1 : 0))
											+ " 分钟";
								} else {
									duration = "预计消耗时间 " + d + " 秒";
								}

								map = new HashMap<String, Object>();
								if (step.getInstructions().indexOf("步行") >= 0)
									map.put("itemsIcon", R.drawable.walk_img);
								else {
									map.put("itemsIcon", R.drawable.bus_img);
								}
								map.put("itemsTitle", step.getInstructions());
								map.put("itemsText", duration);
								busListData.add(map);
							}
							busListViewAdapter.notifyDataSetChanged();

							overlay.addToMap();
							overlay.zoomToSpan();
							MapStatusUpdate u = MapStatusUpdateFactory
									.zoomTo(14.0f);
							mBaiduMap.animateMapStatus(u);
							LatLng center = new LatLng(
									(mpoint.latitude + stationPoint.latitude) / 2,
									(mpoint.longitude + stationPoint.longitude) / 2);
							u = MapStatusUpdateFactory.newLatLng(center);
							mBaiduMap.animateMapStatus(u);
							mTitleTextView.setText("以下是到达该站点的公交方案");

							BitmapDescriptor bitmap = BitmapDescriptorFactory
									.fromResource(R.drawable.umeng_socialize_location_on);
							// ����MarkerOption�������ڵ�ͼ�����Marker
							OverlayOptions option = new MarkerOptions()
									.position(stationPoint).icon(bitmap)
									.zIndex(1) // ����marker���ڲ㼶
									.draggable(true).title("我的位置"); // ����������ק;
							// �ڵ�ͼ�����Marker������ʾ
							mBaiduMap.addOverlay(option);

						}
					}
				}
			}

			@Override
			public void onGetDrivingRouteResult(DrivingRouteResult result) {
				// ��ȡ�ݳ���·�滮���
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					Toast.makeText(StationRoutView.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				}
				if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// ���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == SearchResult.ERRORNO.NO_ERROR) {
					// route = result.getRouteLines().get(0);
					DrivingRouteOverlay overlay = new DrivingRouteOverlay(
							mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(overlay);
					mBaiduMap.clear();
					if (result.getRouteLines() != null) {
						if (result.getRouteLines().size() > 0) {
							overlay.setData(result.getRouteLines().get(0));

							DrivingRouteLine liendata = result.getRouteLines()
									.get(0);
							List<DrivingRouteLine.DrivingStep> allstepList = liendata
									.getAllStep();
							int j = allstepList.size();
							Map<String, Object> map = new HashMap<String, Object>();
							String duration = null;

							driveListData.clear();
							for (int i = 0; i < j; i++) {
								DrivingRouteLine.DrivingStep step = allstepList
										.get(i);
								int d = step.getDuration();

								if (d > 3600) {
									duration = "预计消耗时间 " + d / 3600 + " 小时 "
											+ d % 3600 / 60
											+ (d % 60 > 30 ? 1 : 0) + " 分钟";
								} else if (d > 60) {
									duration = "预计消耗时间 "
											+ (d / 60 + (d % 60 > 30 ? 1 : 0))
											+ " 分钟";
								} else {
									duration = "预计消耗时间 " + d + " 秒";
								}

								map = new HashMap<String, Object>();
								if (step.getInstructions().indexOf("步行") >= 0)
									map.put("itemsIcon", R.drawable.walk_img);
								else {
									map.put("itemsIcon", R.drawable.bus_img);
								}
								map.put("itemsTitle", step.getInstructions());
								map.put("itemsText", duration);
								driveListData.add(map);
							}
							driveListViewAdapter.notifyDataSetChanged();

							overlay.addToMap();
							overlay.zoomToSpan();
							MapStatusUpdate u = MapStatusUpdateFactory
									.zoomTo(14.0f);
							mBaiduMap.animateMapStatus(u);
							LatLng center = new LatLng(
									(mpoint.latitude + stationPoint.latitude) / 2,
									(mpoint.longitude + stationPoint.longitude) / 2);
							u = MapStatusUpdateFactory.newLatLng(center);
							mBaiduMap.animateMapStatus(u);
							mTitleTextView.setText("以下是到达该站点的驾车方案");

							BitmapDescriptor bitmap = BitmapDescriptorFactory
									.fromResource(R.drawable.umeng_socialize_location_on);
							// ����MarkerOption�������ڵ�ͼ�����Marker
							OverlayOptions option = new MarkerOptions()
									.position(stationPoint).icon(bitmap)
									.zIndex(1) // ����marker���ڲ㼶
									.draggable(true).title("我的位置"); // ����������ק;
							// �ڵ�ͼ�����Marker������ʾ
							mBaiduMap.addOverlay(option);
						}
					}
				}
			}
		});

		// ��Ҫ�滮������
		PlanNode st = PlanNode.withLocation(mpoint);
		PlanNode ed = PlanNode.withLocation(stationPoint);

		double distance = DistanceUtil.getDistance(mpoint, stationPoint);
		// ʵ����SharedPreferences���󣨵�һ����
		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		String distanceString = mySharedPreferences.getString("searchRMode",
				"1000米");
		Integer.parseInt(distanceString.substring(0,
				distanceString.length() - 1));
		if (distance < 1000) {
			search.walkingSearch(new WalkingRoutePlanOption().from(st).to(ed));
			walkListView.setVisibility(View.VISIBLE);
			busListView.setVisibility(View.INVISIBLE);
			driveListView.setVisibility(View.INVISIBLE);
			busButton.setTextColor(Color.rgb(255, 255, 255));
			walkButton.setTextColor(Color.rgb(255, 255, 255));
			driveButton.setTextColor(Color.rgb(255, 255, 255));
			busButton.setBackgroundColor(Color.rgb(200, 200, 200));
			driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
			walkButton.setBackgroundColor(Color.rgb(44, 167, 204));
		} else {
			search.transitSearch(new TransitRoutePlanOption().from(st).to(ed)
					.city("北京"));
			driveListView.setVisibility(View.INVISIBLE);
			busListView.setVisibility(View.VISIBLE);
			walkListView.setVisibility(View.INVISIBLE);
			busButton.setTextColor(Color.rgb(255, 255, 255));
			walkButton.setTextColor(Color.rgb(255, 255, 255));
			driveButton.setTextColor(Color.rgb(255, 255, 255));
			busButton.setBackgroundColor(Color.rgb(44, 167, 204));
			driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
			walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
		}

		busButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = null;
				PlanNode st = PlanNode.withLocation(mpoint);
				PlanNode ed = PlanNode.withLocation(stationPoint);
				search.transitSearch(new TransitRoutePlanOption().from(st)
						.to(ed).city("北京"));
				walkListView.setVisibility(View.INVISIBLE);
				driveListView.setVisibility(View.INVISIBLE);
				busListView.setVisibility(View.VISIBLE);
				busButton.setTextColor(Color.rgb(255, 255, 255));
				walkButton.setTextColor(Color.rgb(255, 255, 255));
				driveButton.setTextColor(Color.rgb(255, 255, 255));
				busButton.setBackgroundColor(Color.rgb(44, 167, 204));
				walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
				driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
			}

		});

		walkButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = null;
				PlanNode st = PlanNode.withLocation(mpoint);
				PlanNode ed = PlanNode.withLocation(stationPoint);
				search.walkingSearch(new WalkingRoutePlanOption().from(st).to(
						ed));
				walkListView.setVisibility(View.VISIBLE);
				busListView.setVisibility(View.INVISIBLE);
				driveListView.setVisibility(View.INVISIBLE);
				busButton.setTextColor(Color.rgb(255, 255, 255));
				walkButton.setTextColor(Color.rgb(255, 255, 255));
				driveButton.setTextColor(Color.rgb(255, 255, 255));
				walkButton.setBackgroundColor(Color.rgb(44, 167, 204));
				busButton.setBackgroundColor(Color.rgb(200, 200, 200));
				driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
			}

		});

		driveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = null;
				PlanNode st = PlanNode.withLocation(mpoint);
				PlanNode ed = PlanNode.withLocation(stationPoint);
				search.drivingSearch(new DrivingRoutePlanOption().from(st).to(
						ed));
				driveListView.setVisibility(View.VISIBLE);
				busListView.setVisibility(View.INVISIBLE);
				walkListView.setVisibility(View.INVISIBLE);
				busButton.setTextColor(Color.rgb(255, 255, 255));
				walkButton.setTextColor(Color.rgb(255, 255, 255));
				driveButton.setTextColor(Color.rgb(255, 255, 255));
				driveButton.setBackgroundColor(Color.rgb(44, 167, 204));
				busButton.setBackgroundColor(Color.rgb(200, 200, 200));
				walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
			}

		});

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
		MobclickAgent.onResume(this); // ͳ��ʱ��
		registerReceiver(mWifiReceiver,wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); // ��֤ onPageEnd ��onPause
													// ֮ǰ����,��Ϊ onPause
													// �лᱣ����Ϣ
		MobclickAgent.onPause(this);
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}
}