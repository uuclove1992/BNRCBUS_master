package com.bnrc.busapp;

import java.io.IOException;
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

import u.aly.ap;

import android.R.integer;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View.OnClickListener;

import com.ab.activity.AbActivity;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.activity.AboutActivity;
import com.bnrc.adapter.MyBusListViewAdapter;
import com.bnrc.adapter.MyListViewAdapter;
import com.bnrc.busapp.R;
import com.bnrc.network.MyVolley;
import com.bnrc.network.StringRequest;
import com.bnrc.network.toolbox.Response;
import com.bnrc.network.toolbox.VolleyError;
import com.bnrc.util.AbBase64;
import com.bnrc.util.BuslineDBHelper;
import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.StationOverlay;
import com.bnrc.util.UserDataDBHelper;
import com.bnrc.util.collectwifi.BaseActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.net.v;
import android.widget.AdapterView.OnItemLongClickListener;

public class BuslineListView extends BaseActivity {

	private ListView mListView;
	public DataBaseHelper dabase = null;
	public UserDataDBHelper userdabase = null;
	public List<Map<String, Object>> listData;
	private MyBusListViewAdapter myListViewAdapter;
	private String buslineID;
	private String buslineName;
	private String stationName;
	private int stationPosition = 0;
	private boolean shouldScrollTostation = true;
	private ArrayList<ArrayList<String>> stations = null;
	private ArrayList<ArrayList<String>> buslines = null;
	private ArrayList<String> rtStations = null;
	private String title;
	private TimerTask task;
	private Timer timer;

	private Button isfavButton = null;
	private Button changeDirButton = null;
	private Button vieInMapButton = null;

	private ProgressDialog pd;
	// 定义Handler对象
	private Handler handler = new Handler() {
		@Override
		// 当有消息发送出来的时候就执行Handler的这个方法
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 只要执行到这里就关闭对话框
			pd.dismiss();
			mListView.setVisibility(View.VISIBLE);
			initData();

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setAbContentView(R.layout.busline_listview);
		this.setTitleText("线路详情");
		this.setTitleTextMargin(20, 0, 0, 0);
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();

		Intent intent = getIntent();
		this.setTitleText(intent.getStringExtra("buslineName"));
		title = intent.getStringExtra("title");
		buslineID = intent.getStringExtra("buslineID"); // ��ȡ��ͼ��������
		buslineName = intent.getStringExtra("buslineName");// ��ȡ��ͼ��������
		stationName = intent.getStringExtra("stationName");

		userdabase = UserDataDBHelper.getInstance(BuslineListView.this);
		ArrayList<String> line = new ArrayList<String>();
		line.add(buslineID);
		line.add(title);
		line.add(buslineName);
		userdabase.addSearchBuslineWithBusline(line);
		mListView = (ListView) this.findViewById(R.id.mBuslineListView);
		mListView.setVisibility(View.INVISIBLE);

		// 构建一个下载进度条
		pd = ProgressDialog.show(BuslineListView.this, "数据加载中…", "请等待");
		new Thread() {
			public void run() {
				// 在这里执行长耗时方法
				dabase = DataBaseHelper.getInstance(BuslineListView.this);
				buslines = dabase
						.getBothsideBusLinesWithBuslineName(buslineName);
				// 执行完毕后给handler发送一个消息
				handler.sendEmptyMessage(0);
			}
		}.start();

	}

	public void initData() {
		userdabase = UserDataDBHelper.getInstance(BuslineListView.this);
		String favString = userdabase.checkFavBuslineWithBuslineID(buslineID) ? "取消收藏"
				: "添加收藏";
		isfavButton = (Button) this.findViewById(R.id.isFavbtn);
		isfavButton.setText(favString);
		isfavButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ArrayList<String> line = new ArrayList<String>();
				line.add(buslineID);
				line.add(title);
				line.add(buslineName);
				if (isfavButton.getText().toString().equalsIgnoreCase("取消收藏")) {
					userdabase.deleteFavBuslineWithBusline(line);
					Toast toast = Toast.makeText(getApplicationContext(),
							"您已取消收藏\"" + buslineName + "\"线路",
							Toast.LENGTH_LONG);
					isfavButton.setText("添加收藏");
				} else {
					userdabase.addFavBuslineWithBusline(line);
					Toast toast = Toast.makeText(getApplicationContext(),
							"您已成功收藏\"" + buslineName + "\"线路",
							Toast.LENGTH_LONG);
					toast.show();
					isfavButton.setText("取消收藏");
				}
			}
		});

		changeDirButton = (Button) this.findViewById(R.id.changedrictbtn);
		vieInMapButton = (Button) this.findViewById(R.id.viewinmapbtn);

		mListView.setDividerHeight(0);
		listData = new ArrayList<Map<String, Object>>();
		myListViewAdapter = new MyBusListViewAdapter(this, listData,
				R.layout.bus_list_items, new String[] { "itemsIcon",
						"itemsTitle", "itemsText", "busIcon", "isalert",
						"buslineIcon", "isFav", "isCurStation" }, new int[] {
						R.id.itemsContainer, R.id.itemsTitle, R.id.itemsText,
						R.id.onRoadbusIcon, R.id.img_name });
		mListView.setAdapter(myListViewAdapter);

		// item
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				// TODO Auto-generated method stub
				final ArrayList<String> station = stations.get(arg2);

				final String alertString = userdabase
						.checkAlertStationWithStation(station) ? "删除下车提醒"
						: "添加到下车提醒";
				final String favString = userdabase
						.checkFavStationWithStationID(station.get(0)) ? "取消收藏站点"
						: "添加收藏站点";
				final String[] arrayChoice = new String[] { "查看站点详情",
						alertString, favString };
				new AlertDialog.Builder(BuslineListView.this)
						// build AlertDialog
						.setTitle("您可进行的操作")
						// title
						.setItems(arrayChoice,
								new DialogInterface.OnClickListener() { // content
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (which == 0) {
											// Intent intent = null;
											Intent intent = new Intent(
													BuslineListView.this,
													StationInformationView.class);
											// ����ͼ�д�������
											ArrayList<String> station = new ArrayList<String>();
											station = stations.get(which);
											intent.putExtra("title",
													station.get(0));
											intent.putExtra("latitude",
													station.get(1));
											intent.putExtra("longitude",
													station.get(2));
											startActivity(intent);
										} else if (which == 1) {
											if (alertString
													.equalsIgnoreCase("删除下车提醒")) {
												userdabase
														.deleteAlertStationWithStation(station);
												loadBuslineData();
												try {
													try {
														get_realtime_data(
																buslineName, 1);
													} catch (UnsupportedEncodingException e) {
														// TODO
														// Auto-generated
														// catch block
														e.printStackTrace();
													}
												} catch (JSONException e) {
													// TODO Auto-generated
													// catch block
													e.printStackTrace();
												}
												Toast toast = Toast
														.makeText(
																getApplicationContext(),
																"您已删除\""
																		+ station
																				.get(0)
																		+ "\"下车提醒站点",
																Toast.LENGTH_LONG);
												toast.show();
											} else {
												userdabase
														.addAlertStationWithStation(station);
												loadBuslineData();
												try {
													try {
														get_realtime_data(
																buslineName, 1);
													} catch (UnsupportedEncodingException e) {
														// TODO
														// Auto-generated
														// catch block
														e.printStackTrace();
													}
												} catch (JSONException e) {
													// TODO Auto-generated
													// catch block
													e.printStackTrace();
												}
												Toast toast = Toast
														.makeText(
																getApplicationContext(),
																"您已添加\""
																		+ station
																				.get(0)
																		+ "\"为下车提醒站点",
																Toast.LENGTH_LONG);
												toast.show();
											}

										} else if (which == 2) {
											if (favString
													.equalsIgnoreCase("取消收藏站点")) {
												ArrayList<String> line = new ArrayList<String>();
												line.add(buslineID);
												line.add(title);
												line.add(buslineName);
												userdabase
														.deleteFavStationWithStationAndBusline(
																station, line);
												loadBuslineData();

												Toast toast = Toast
														.makeText(
																getApplicationContext(),
																"您已取消收藏\""
																		+ station
																				.get(0)
																		+ "\"站点",
																Toast.LENGTH_LONG);
												toast.show();
											} else {
												userdabase
														.addFavStationWithStation(station);

												Toast toast = Toast
														.makeText(
																getApplicationContext(),
																"您已成功收藏\""
																		+ buslineName
																		+ "路"
																		+ station
																				.get(0)
																		+ "\"站点",
																Toast.LENGTH_LONG);
												toast.show();
												ArrayList<String> line = new ArrayList<String>();
												line.add(buslineID);
												line.add(title);
												line.add(buslineName);
												userdabase
														.addFavBuslineWithBusline(line);
												userdabase
														.addRTBusWithBuslineAndStation(
																line, station);
												isfavButton.setText("取消收藏");
												loadBuslineData();
											}
										}
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss(); // �ر�alertDialog
									}
								}).show();
				return true;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Intent intent = null;
				Intent intent = new Intent(BuslineListView.this,
						StationInformationView.class);
				ArrayList<String> station = new ArrayList<String>();
				station = stations.get(position);
				intent.putExtra("title", station.get(0));
				intent.putExtra("latitude", station.get(1));
				intent.putExtra("longitude", station.get(2));
				startActivity(intent);
			}
		});

		changeDirButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (buslines.size() < 2) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"未找到其它线路~", Toast.LENGTH_LONG);
					toast.show();
				} else {
					getChangeBuslines();
				}
			}
		});

		vieInMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (isNetworkConnected(BuslineListView.this)) {
					Intent intent = new Intent(BuslineListView.this,
							BuslineMapView.class);
					// ����ͼ�д�������
					ArrayList<String> busline = new ArrayList<String>();
					intent.putExtra("title", title);
					intent.putExtra("buslineName", buslineName);
					intent.putExtra("buslineID", buslineID);
					// ������ͼ
					startActivity(intent);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"您的网络好像有问题哦~", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});

		loadBuslineData();

		task = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							try {
								get_realtime_data(buslineName, 1);
							} catch (UnsupportedEncodingException e) {
								// TODO
								// Auto-generated
								// catch block
								e.printStackTrace();
							}
						} catch (JSONException e) {
							// TODO Auto-generated
							// catch block
							e.printStackTrace();
						}
					}
				});
			}
		};

		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		timer = new Timer(true);
		String timeString = mySharedPreferences.getString("refreshMode", "30秒");
		timeString = timeString.substring(0, timeString.length() - 1);
		timer.schedule(task, 1200, Integer.parseInt(timeString) * 1000); // 延时1000ms后执行，1000ms执行一次
		// timer.cancel(); //退出计时器
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

	public void get_realtime_data(String lineName, int station_num)
			throws JSONException, UnsupportedEncodingException {

		Log.i("lineName", "lineName:" + lineName.toString());
		ArrayList<String> buslineArrayList = BuslineDBHelper.getInstance(
				BuslineListView.this).getBuslineInfoWithBuslineName(lineName);
		if (buslineArrayList.size() < 1
				&& (isNetworkConnected(BuslineListView.this))) {
			Log.i("lineName", "lineName:" + lineName.toString() + "没有实时公交");
			changeDirButton.setClickable(true);
			return;
		}
		Log.i("lineName", "lineName:" + lineName.toString() + "有实时公交");
		String url = "http://bjgj.aibang.com:8899/bus.php?city="
				+ URLEncoder.encode("北京", "utf-8") + "&id="
				+ buslineArrayList.get(3) + "&no=" + station_num
				+ "&type=0&encrypt=1&versionid=2";
		String value = MobclickAgent.getConfigParams(BuslineListView.this,
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
							Log.i("jsonObj", jsonObj.toString());

							JSONObject busJsonObject = ((JSONObject) (jsonObj
									.getJSONObject("root")))
									.getJSONObject("data");
							JSONArray busesArray = busJsonObject
									.getJSONArray("bus");
							try {
								int j = busesArray.length();
								rtStations = new ArrayList<String>();
								String stationNameString;
								for (int i = 0; i < j; i++) {
									JSONObject jsonObject = (JSONObject) busesArray
											.get(i);
									stationNameString = new MyCipher("aibang"
											+ jsonObject.getString("gt"))
											.decrypt(jsonObject.getString("ns"));
									if (stationNameString != null) {
										stationNameString = stationNameString
												.trim();
										int k = 0;
										for (String station : rtStations) {
											if (station.trim()
													.equalsIgnoreCase(
															stationNameString)) {
												break;
											}
											k++;
										}
										if (k == rtStations.size()) {
											rtStations.add(stationNameString);
										}
									}
								}

								j = stations.size();
								Map<String, Object> map = new HashMap<String, Object>();
								LocationUtil mApplication = LocationUtil
										.getInstance(BuslineListView.this);
								BDLocation location = mApplication.mLocation;
								final LatLng mypoint = new LatLng(location
										.getLatitude(), location.getLongitude());
								listData.clear();

								int m = 1000;
								for (int i = 0; i < j; i++) {
									ArrayList<String> station = new ArrayList<String>();
									station = stations.get(i);
									map = new HashMap<String, Object>();
									if (station
											.get(0)
											.trim()
											.equalsIgnoreCase(
													stationName.trim())) {
										stationPosition = i > 3 ? (i - 3) : i;
										map.put("isCurStation", "Yes");
									} else {
										map.put("isCurStation", "NO");
									}
									if (userdabase
											.checkAlertStationWithStation(station)) {
										map.put("itemsIcon",
												R.drawable.alert_img);

										LatLng stationPoint = new LatLng(
												Float.parseFloat(station.get(1)),
												Float.parseFloat(station.get(2)));
										double distance = DistanceUtil
												.getDistance(mypoint,
														stationPoint);
										map.put("itemsText",
												"目前距离我 "
														+ (int) (Math
																.floor(distance + 0.5))
														+ " 米");
										map.put("isalert", "Yes");
									} else {
										map.put("itemsIcon", R.drawable.station);
										map.put("itemsText", " ");
										map.put("isalert", "NO");
									}
									if (userdabase
											.checkFavStationWithStationID(station
													.get(0))) {
										map.put("itemsTitle", station.get(0));
										map.put("isFav", "Yes");
									} else {
										map.put("itemsTitle", station.get(0));
										map.put("isFav", "NO");
									}

									int k = rtStations.size();
									int k2;
									String curStation;
									for (k2 = 0; k2 < k; k2++) {
										curStation = rtStations.get(k2);
										if (station.get(0).toString()
												.equalsIgnoreCase(curStation)) {
											map.put("busIcon",
													R.drawable.hasbusimg);
											map.put("buslineIcon",
													R.drawable.buslineiconnormal);
											m = i;
											break;
										}
									}
									if (k == k2) {
										map.put("busIcon",
												R.drawable.havenobusimg);
										map.put("buslineIcon",
												R.drawable.buslineiconnormal);

									}
									if (m == 1000) {
										map.put("itemsText", "暂无实时公交信息");
									} else {
										if ((i - m) > 0) {
											map.put("itemsText", "车辆将在 "
													+ (i - m) + " 站后到达");
										} else {
											map.put("itemsText", "车辆即将到站");
										}

									}
									listData.add(map);
								}
								changeDirButton.setClickable(true);
								myListViewAdapter.notifyDataSetChanged();
								if (shouldScrollTostation) {
									mListView.setSelection(stationPosition);
									shouldScrollTostation = false;
								}
							} catch (SQLException sqle) {
								changeDirButton.setClickable(true);
								throw sqle;
							}
						} catch (JSONException e) {
							changeDirButton.setClickable(true);
							Log.e("JSON exception", e.getMessage());
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});
		MyVolley.sharedVolley(BuslineListView.this).getRequestQueue()
				.add(request);

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
		MobclickAgent.onResume(this); // ͳ��ʱ��
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); // ��֤ onPageEnd ��onPause
													// ֮ǰ����,��Ϊ onPause
													// �лᱣ����Ϣ
		MobclickAgent.onPause(this);
		task.cancel();
		timer.cancel();
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}

	@Override
	public void onRestart() {
		super.onRestart();
		myListViewAdapter.notifyDataSetChanged();
		if (shouldScrollTostation) {
			mListView.setSelection(stationPosition);
			shouldScrollTostation = false;
		}
		task = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							ArrayList<String> buslineArrayList = BuslineDBHelper
									.getInstance(BuslineListView.this)
									.getBuslineInfoWithBuslineName(buslineName);
							if (buslineArrayList.size() > 0) {
								try {
									if (isNetworkConnected(BuslineListView.this)) {
										get_realtime_data(buslineName, 1);
									} else {

									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		};
		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		timer = new Timer(true);
		String timeString = mySharedPreferences.getString("refreshMode", "30秒");
		timeString = timeString.substring(0, timeString.length() - 1);
		timer.schedule(task, 1200, Integer.parseInt(timeString) * 1000); // 延时1000ms后执行，1000ms执行一次
	}

	static int num = 1;

	public void getChangeBuslines() {

		if (isNetworkConnected(BuslineListView.this)) {
			changeDirButton.setClickable(false);
		}

		loadBuslineData();

		if (isNetworkConnected(BuslineListView.this)) {
			try {
				get_realtime_data(buslineName, 1);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		shouldScrollTostation = true;
		num++;
	}

	public void loadBuslineData() {
		Log.i("buslines", buslines.toString());
		ArrayList<String> busline = buslines.get(num % (buslines.size()));
		this.setTitleText(busline.get(2));
		title = busline.get(1);
		buslineID = busline.get(0);
		buslineName = busline.get(2);
		listData.clear();
		String favString = userdabase.checkFavBuslineWithBuslineID(buslineID) ? "取消收藏"
				: "添加收藏";
		isfavButton.setText(favString);
		stations = dabase.getStationsWithBuslineId(busline.get(0));
		int j = stations.size();
		Map<String, Object> map = new HashMap<String, Object>();
		LocationUtil mApplication = LocationUtil
				.getInstance(BuslineListView.this);
		BDLocation location = mApplication.mLocation;
		final LatLng mypoint = new LatLng(location.getLatitude(),
				location.getLongitude());

		for (int i = 0; i < j; i++) {
			ArrayList<String> station = new ArrayList<String>();
			station = stations.get(i);

			map = new HashMap<String, Object>();
			if (station.get(0).trim().equalsIgnoreCase(stationName.trim())) {
				stationPosition = i > 3 ? (i - 3) : i;
				map.put("isCurStation", "Yes");
			} else {
				map.put("isCurStation", "NO");
			}
			if (userdabase.checkFavStationWithStationID(station.get(0))) {
				map.put("itemsTitle", station.get(0));
				map.put("isFav", "Yes");
			} else {
				map.put("itemsTitle", station.get(0));
				map.put("isFav", "NO");
			}

			if (userdabase.checkAlertStationWithStation(station)) {
				map.put("itemsIcon", R.drawable.alert_img);

				LatLng stationPoint = new LatLng(Float.parseFloat(station
						.get(1)), Float.parseFloat(station.get(2)));
				double distance = DistanceUtil.getDistance(mypoint,
						stationPoint);
				map.put("itemsText",
						"目前距离我 " + (int) (Math.floor(distance + 0.5)) + " 米");
				map.put("isalert", "Yes");
			} else {
				map.put("itemsIcon", R.drawable.station);
				map.put("itemsText", "暂无实时公交信息");
				map.put("isalert", "NO");
			}

			map.put("busIcon", R.drawable.havenobusimg);
			map.put("buslineIcon", R.drawable.buslineiconnormal);

			listData.add(map);
		}
		myListViewAdapter.notifyDataSetChanged();
		if (shouldScrollTostation) {
			mListView.setSelection(stationPosition);
			shouldScrollTostation = false;
		}
	}
}
