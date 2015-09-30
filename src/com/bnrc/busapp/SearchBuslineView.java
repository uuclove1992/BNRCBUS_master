package com.bnrc.busapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import cn.domob.android.ads.AdEventListener;
import cn.domob.android.ads.AdView;
import cn.domob.android.ads.AdManager.ErrorCode;

import com.ab.activity.AbActivity;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.bnrc.adapter.MyListViewAdapter;
import com.bnrc.busapp.R;
import com.bnrc.util.DataBaseHelper;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.UserDataDBHelper;
import com.bnrc.util.collectwifi.BaseActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public class SearchBuslineView extends BaseActivity {

	private ImageView ivDeleteText;
	private EditText etSearch;
	private Button searchaBtn;
	private ListView mListView;
	// private TextView mTitleTextView;
	public DataBaseHelper dabase = null;
	public UserDataDBHelper userdabase = null;
	public List<Map<String, Object>> listData;
	private MyListViewAdapter myListViewAdapter;
	private ArrayList<ArrayList<String>> buslines;
	private ArrayList<View> searchArrayList = null;
	private LinearLayout buslineContainer;

	RelativeLayout mAdContainer;
	AdView mAdview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.search_busline_view);
		this.setTitleText("线路搜索");
		this.setLogo(R.drawable.button_selector_back);
		this.setTitleLayoutBackground(R.drawable.top_bg);
		this.setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		initbase();

		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent
				.setOnlineConfigureListener(new UmengOnlineConfigureListener() {
					@Override
					public void onDataReceived(JSONObject data) {
					}
				});
		String value = MobclickAgent.getConfigParams(SearchBuslineView.this,
				"open_ad");

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

			Calendar mycalendar = Calendar.getInstance();// ��ȡ����ʱ��
			String curYearString = String
					.valueOf(mycalendar.get(Calendar.YEAR));// ��ȡ���
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
					return SearchBuslineView.this;
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

		ivDeleteText = (ImageView) findViewById(R.id.ivDeleteText);
		etSearch = (EditText) findViewById(R.id.etSearch);
		searchaBtn = (Button) findViewById(R.id.btnSearch);
		// mTitleTextView = (TextView)findViewById(R.id.mTitleTextView);
		ivDeleteText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				etSearch.setText("");

			}
		});

		etSearch.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s.length() > 1) {
					getBuslineWithKeyword(etSearch.getText().toString());
				}

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				// loadHistoryData();
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
				InputMethodManager imm = (InputMethodManager) getSystemService(SearchBuslineView.this.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
				if (etSearch.getText().length() < 1) {
					Toast toast = Toast.makeText(getApplicationContext(),
							"线路名称不能为空哦~", Toast.LENGTH_LONG);
					toast.show();
				} else {
					getBuslineWithKeyword(etSearch.getText().toString());
				}
			}
		});
		dabase = DataBaseHelper.getInstance(SearchBuslineView.this);
		userdabase = UserDataDBHelper.getInstance(SearchBuslineView.this);
		mListView = (ListView) this.findViewById(R.id.mBuslineListView);
		listData = new ArrayList<Map<String, Object>>();
		myListViewAdapter = new MyListViewAdapter(this, listData,
				R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
						"itemsText" }, new int[] { R.id.itemsIcon,
						R.id.itemsTitle, R.id.itemsText });
		mListView.setAdapter(myListViewAdapter);

		// item������¼�
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Intent intent = null;
				Intent intent = new Intent(SearchBuslineView.this,
						BuslineListView.class);
				// ����ͼ�д�������
				ArrayList<String> busline = new ArrayList<String>();
				busline = buslines.get(position);
				intent.putExtra("title", busline.get(1));
				intent.putExtra("buslineID", busline.get(0));
				intent.putExtra("buslineName", busline.get(2));
				intent.putExtra("stationName", "");
				// ������ͼ
				startActivity(intent);

				ArrayList<String> line = new ArrayList<String>();
				line.add(busline.get(0));
				line.add(busline.get(1));
				line.add(busline.get(2));
				userdabase.addSearchBuslineWithBusline(line);

			}
		});

		getSearchList();
		TimerTask task = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (etSearch.getText().toString().length() > 0) {
							getBuslineWithKeyword(etSearch.getText().toString());
						}
					}
				});
			}
		};

		Intent intent = getIntent();
		String keywordString = intent.getStringExtra("keyword");
		if (keywordString.length() > 0) {
			etSearch.setText(keywordString);
		} else {
			getNearbyBusline();
		}

		Timer timer = new Timer(true);
		timer.schedule(task, 200); // 延时1000ms后执行，1000ms执行一次
		// timer.cancel(); //退出计时器
		// 默认软键盘不弹出
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

	}

	public void loadHistoryData() {
		listData.clear();
		buslines = userdabase.getLatestSearchBuslines();
		int j = buslines.size();
		// mTitleTextView.setText("以下是最近搜索历史记录");
		Map<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < j; i++) {
			ArrayList<String> busline = new ArrayList<String>();
			busline = buslines.get(i);
			map = new HashMap<String, Object>();
			map.put("itemsIcon", R.drawable.bus_img);
			map.put("itemsTitle", busline.get(1));
			map.put("itemsText", busline.get(2));
			listData.add(map);
		}

		myListViewAdapter.notifyDataSetChanged();
	}

	public void getNearbyBusline() {
		listData.clear();
		BDLocation location = LocationUtil.getInstance(SearchBuslineView.this).mLocation;
		buslines = dabase.getNearbyBuslineWithLocation(new LatLng(location
				.getLatitude(), location.getLongitude()));
		int j = buslines.size();
		// mTitleTextView.setText("您的附近有 "+j+" 条公交线路...");
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < j; i++) {
			ArrayList<String> busline = new ArrayList<String>();
			busline = buslines.get(i);
			map = new HashMap<String, Object>();
			map.put("itemsIcon", R.drawable.bus_img);
			map.put("itemsTitle", busline.get(1));
			map.put("itemsText", busline.get(2));
			listData.add(map);
		}
		myListViewAdapter.notifyDataSetChanged();
	}

	public void getBuslineWithKeyword(String keyword) {
		listData.clear();
		buslines = dabase.searchBusLinesWithKeyword(etSearch.getText()
				.toString());
		int j = buslines.size();
		// mTitleTextView.setText("共搜索到"+j+"条关于\""+etSearch.getText().toString()+"\"的公交线路·");
		Map<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < j; i++) {
			ArrayList<String> busline = new ArrayList<String>();
			busline = buslines.get(i);
			map = new HashMap<String, Object>();
			map.put("itemsIcon", R.drawable.bus_img);
			map.put("itemsTitle", busline.get(1));
			map.put("itemsText", busline.get(2));
			listData.add(map);
		}

		myListViewAdapter.notifyDataSetChanged();
	}

	public void getSearchList() {
		buslineContainer = (LinearLayout) findViewById(R.id.searchList);

		if (searchArrayList == null) {
			searchArrayList = new ArrayList<View>();
		} else {
			int m = searchArrayList.size();
			for (int i = 0; i < m; i++) {
				searchArrayList.get(i).setVisibility(View.GONE);
				searchArrayList.remove(i);
				i--;
				m--;
			}
		}
		userdabase = UserDataDBHelper.getInstance(SearchBuslineView.this);
		buslines = userdabase.getLatestSearchBuslines();
		int j = buslines.size();
		ArrayList<String> busline;
		for (int i = 0; i < j; i++) {
			busline = buslines.get(i);
			View stationItem = View.inflate(SearchBuslineView.this,
					R.layout.search_item_view, null);
			if (i == 0) {
				int w = View.MeasureSpec.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED);
				int h = View.MeasureSpec.makeMeasureSpec(0,
						View.MeasureSpec.UNSPECIFIED);
				stationItem.measure(w, h);
			}
			TextView title = (TextView) stationItem.findViewById(R.id.tv_title);
			title.setText(busline.get(1));
			title.setTextColor(Color.BLACK);
			searchArrayList.add(stationItem);
			buslineContainer.addView(stationItem);
			title.setTag((i + 1024) + "");

			title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					int tag = Integer.parseInt(arg0.getTag().toString()) - 1024;
					// Intent intent = null;
					Intent intent = new Intent(SearchBuslineView.this,
							BuslineListView.class);
					// 在意图中传递数据
					ArrayList<String> busline = new ArrayList<String>();
					busline = buslines.get(tag);
					intent.putExtra("title", busline.get(1));
					intent.putExtra("buslineID", busline.get(0));
					intent.putExtra("buslineName", busline.get(2));
					intent.putExtra("stationName", "");
					startActivity(intent);

					ArrayList<String> line = new ArrayList<String>();
					line.add(busline.get(0));
					line.add(busline.get(1));
					line.add(busline.get(2));
					userdabase.addSearchBuslineWithBusline(line);
				}
			});
		}
		if (j == 0) {
			View stationItem = View.inflate(SearchBuslineView.this,
					R.layout.search_item_view, null);
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			stationItem.measure(w, h);
			TextView title = (TextView) stationItem.findViewById(R.id.tv_title);
			title.setText("您还没有搜索过任何线路");
			searchArrayList.add(stationItem);
			buslineContainer.addView(stationItem);
		} else {
			View stationItem = View.inflate(SearchBuslineView.this,
					R.layout.search_item_view, null);
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			stationItem.measure(w, h);
			TextView title = (TextView) stationItem.findViewById(R.id.tv_title);
			title.setText("清空记录");
			searchArrayList.add(stationItem);
			buslineContainer.addView(stationItem);

			title.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					userdabase.deleteAllBuslineSearchHistory();
					getSearchList();
				}
			});
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
		MobclickAgent.onResume(this); // ͳ��ʱ��
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	public void onRestart() {
		super.onRestart();
		loadHistoryData();
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); // ��֤ onPageEnd ��onPaus //
													// ֮ǰ����,��Ϊ onPause
													// �лᱣ����Ϣ
		MobclickAgent.onPause(this);
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}

}
