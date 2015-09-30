package com.bnrc.busapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.domob.android.ads.AdEventListener;
import cn.domob.android.ads.AdView;
import cn.domob.android.ads.AdManager.ErrorCode;

import com.ab.activity.AbActivity;
import com.ab.global.AbConstant;

import com.ab.view.wheel.AbStringWheelAdapter;
import com.ab.view.wheel.AbWheelAdapter;
import com.ab.view.wheel.AbWheelView;
import com.bnrc.activity.AboutActivity;
import com.bnrc.busapp.R;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.collectwifi.BaseActivity;
import com.bnrc.util.collectwifi.Constants;
import com.bnrc.util.collectwifi.ScanService;
import com.bnrc.util.collectwifi.ServiceUtils;
import com.bnrc.util.collectwifi.WifiReceiver;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

public class SettingView extends BaseActivity {
	private View selectSearchRView = null;
	private View selectAlertRView = null;
	private View selectBatteryModeView = null;
	private View refreshModeView = null;

	private TextView searchRTextView = null;
	private TextView alertRTextView = null;
	private TextView batteryModeTextView = null;
	// private TextView acceptAlertTextView = null;
	private TextView userSetView = null;
	private TextView shareAppTextView = null;
	private TextView feedbackTextView = null;
	private TextView aboutTextView = null;
	private TextView refreshTextView = null;
	// 收集wifi的相关设置
	private TextView tv_scanmethod, tv_scanap, tv_scantime;
	private View mDataViewRadius = null, mDataViewAlertDistance = null,
			mDataViewBattery = null;
	private View mDataViewScanMethod = null, mDataViewScanAp = null,
			mDataViewScanTime = null, mDataViewScanPrecision = null;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	private String searchRArr[] = { "600米", "700米", "800米", "900米", "1000米",
			"1100米", "1200米", "1300米", "1400米", "1500米" };
	private String alertRArr[] = { "100米", "200米", "400米", "600米", "800米",
			"1000米" };
	private String refreshTArr[] = { "5秒", "10秒", "15秒", "20秒", "30秒", "45秒",
			"60秒" };
	private String batteryModeArr[] = { "1级(比较损耗)", "2级(推荐)", "3级(损耗很少)" };

	RelativeLayout mAdContainer;
	AdView mAdview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.setting_view);
		SettingView.this.setTitleText("系统设置");
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
		String value = MobclickAgent.getConfigParams(SettingView.this,
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
					return SettingView.this;
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

		logoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// �����뾶ѡ����ѡ��Ľ���
		selectSearchRView = mInflater.inflate(R.layout.choose_one, null);
		// ���Ѿ���ѡ����ѡ��Ľ���
		selectAlertRView = mInflater.inflate(R.layout.choose_one, null);
		// ����ģʽѡ����ѡ��Ľ���
		selectBatteryModeView = mInflater.inflate(R.layout.choose_one, null);
		refreshModeView = mInflater.inflate(R.layout.choose_one, null);

		// ʵ����SharedPreferences���󣨵�һ����
		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		searchRTextView = (TextView) findViewById(R.id.searchRTv);
		searchRTextView.setText(mySharedPreferences.getString("searchRMode",
				"800米"));

		alertRTextView = (TextView) findViewById(R.id.alertRTv);
		alertRTextView.setText(mySharedPreferences.getString("alertRMode",
				"200米"));

		batteryModeTextView = (TextView) findViewById(R.id.batteryModeTv);
		batteryModeTextView.setText(mySharedPreferences.getString(
				"batteryMode", "2级(推荐)"));

		refreshTextView = (TextView) findViewById(R.id.refreshModeTv);
		refreshTextView.setText(mySharedPreferences.getString("refreshMode",
				"30秒"));

		// acceptAlertTextView = (TextView) findViewById(R.id.acceptAlertTv);
		// acceptAlertTextView.setText(mySharedPreferences.getString("acceptAlertMode",
		// "����"));

		userSetView = (TextView) findViewById(R.id.userSetTv);
		shareAppTextView = (TextView) findViewById(R.id.shareAppTv);
		feedbackTextView = (TextView) findViewById(R.id.feedbackTv);
		aboutTextView = (TextView) findViewById(R.id.aboutTv);

		userSetView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = null;
				Intent intent = new Intent(SettingView.this,
						UserSettingView.class);
				// ������ͼ
				startActivity(intent);
			}
		});

		searchRTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(AbConstant.DIALOGBOTTOM, selectSearchRView, 40);
			}
		});

		alertRTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(AbConstant.DIALOGBOTTOM, selectAlertRView, 40);
			}
		});

		batteryModeTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(AbConstant.DIALOGBOTTOM, selectBatteryModeView, 40);
			}

		});

		refreshTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(AbConstant.DIALOGBOTTOM, refreshModeView, 40);
			}

		});

		// acceptAlertTextView.setOnClickListener(new OnClickListener() {

		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// showDialog(1, mAvatarView, 40);
		// }
		// });

		feedbackTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				FeedbackAgent agent = new FeedbackAgent(SettingView.this);
				agent.startFeedbackActivity();
				agent.sync();
			}
		});

		aboutTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingView.this,
						AboutActivity.class);
				startActivity(intent);
			}
		});

		initWheelDataOfSearchRadius(selectSearchRView, searchRTextView);
		initWheelDataOfAlertDistance(selectAlertRView, alertRTextView);
		initWheelDataOfBatteryMode(selectBatteryModeView, batteryModeTextView);
		initWheelDataOfRefreshFrequency(refreshModeView, refreshTextView);

		// ����������Activity��������³�Ա����
		final UMSocialService mShareController = UMServiceFactory
				.getUMSocialService("com.umeng.share");
		// ���÷�������
		mShareController
				.setShareContent("我正在使用\"北京实时公交助手\"查公交，太方便啦，还可以看到站信息，你也试试吧？");
		// ���÷���ͼƬ������2Ϊ����ͼƬ����Դ����
		// mController.setShareMedia(new UMImage(getActivity(),
		// R.drawable.icon));

		// ���΢��ƽ̨
		UMWXHandler wxHandler = new UMWXHandler(this, "wx967daebe835fbeac",
				"5fa9e68ca3970e87a1f83e563c8dcbce");
		wxHandler.addToSocialSDK();
		// ���΢������Ȧ
		UMWXHandler wxCircleHandler = new UMWXHandler(this,
				"wx967daebe835fbeac", "5fa9e68ca3970e87a1f83e563c8dcbce");
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

		// ����1Ϊ��ǰActivity������2Ϊ��������QQ���������APP
		// ID������3Ϊ��������QQ���������APP kEY.
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, "100424468",
				"c7394704798a158208a74ab60104f0ba");
		qqSsoHandler.addToSocialSDK();

		// ����1Ϊ��ǰActivity������2Ϊ��������QQ���������APP
		// ID������3Ϊ��������QQ���������APP kEY.
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this,
				"100424468", "c7394704798a158208a74ab60104f0ba");
		qZoneSsoHandler.addToSocialSDK();

		mShareController.getConfig().removePlatform(SHARE_MEDIA.TENCENT,
				SHARE_MEDIA.WEIXIN_CIRCLE);
		shareAppTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ
				mShareController.openShare(SettingView.this, false);

			}
		});

		// ��������SSO handler
		mShareController.getConfig().setSsoHandler(new SinaSsoHandler());
		preferences = getSharedPreferences(Constants.SETTING, MODE_PRIVATE);
		editor = preferences.edit();
		findViewById();
		register();
		initbase();
	}

	// ��λ��ʾ����
	public void initWheelDataOfSearchRadius(View mDataView,
			final TextView textView) {
		final AbWheelView mWheelView1 = (AbWheelView) mDataView
				.findViewById(R.id.wheelView1);
		// mWheelView1.setAdapter(new AbNumericWheelAdapter(40, 190));
		mWheelView1.setAdapter(new AbWheelAdapter() {
			@Override
			public int getMaximumLength() {
				// TODO Auto-generated method stub
				return searchRArr.length;
			}

			@Override
			public int getItemsCount() {
				// TODO Auto-generated method stub
				return searchRArr.length;
			}

			@Override
			public String getItem(int arg0) {
				// TODO Auto-generated method stub
				return searchRArr[arg0];
			}
		});
		// ��ѭ������
		mWheelView1.setCyclic(false);
		// �������
		mWheelView1.setLabel(null);
		// ��ʼ��ʱ��ʾ������
		mWheelView1.setCurrentItem(40);
		mWheelView1.setValueTextSize(35);
		mWheelView1.setLabelTextSize(35);
		mWheelView1.setLabelTextColor(0x80000000);
		mWheelView1.setCenterSelectDrawable(this.getResources().getDrawable(
				R.drawable.wheel_select));

		Button okBtn = (Button) mDataView.findViewById(R.id.okBtn);
		Button cancelBtn = (Button) mDataView.findViewById(R.id.cancelBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
				int index = mWheelView1.getCurrentItem();
				String val = mWheelView1.getAdapter().getItem(index);
				textView.setText(val);
				// ʵ����SharedPreferences���󣨵�һ����
				SharedPreferences mySharedPreferences = getSharedPreferences(
						"setting", SettingView.MODE_PRIVATE);
				// ʵ����SharedPreferences.Editor���󣨵ڶ�����
				SharedPreferences.Editor editor = mySharedPreferences.edit();
				// ��putString�ķ�����������
				editor.putString("searchRMode", val);
				// �ύ��ǰ����
				editor.commit();
			}

		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
			}

		});
	}

	// ��λ��ʾ����
	public void initWheelDataOfAlertDistance(View mDataView,
			final TextView textView) {
		final AbWheelView mWheelView1 = (AbWheelView) mDataView
				.findViewById(R.id.wheelView1);
		// mWheelView1.setAdapter(new AbNumericWheelAdapter(40, 190));
		mWheelView1.setAdapter(new AbWheelAdapter() {
			@Override
			public int getMaximumLength() {
				// TODO Auto-generated method stub
				return alertRArr.length;
			}

			@Override
			public int getItemsCount() {
				// TODO Auto-generated method stub
				return alertRArr.length;
			}

			@Override
			public String getItem(int arg0) {
				// TODO Auto-generated method stub
				return alertRArr[arg0];
			}
		});
		// ��ѭ������
		mWheelView1.setCyclic(false);
		// �������
		mWheelView1.setLabel(null);
		// ��ʼ��ʱ��ʾ������
		mWheelView1.setCurrentItem(40);
		mWheelView1.setValueTextSize(35);
		mWheelView1.setLabelTextSize(35);
		mWheelView1.setLabelTextColor(0x80000000);
		mWheelView1.setCenterSelectDrawable(this.getResources().getDrawable(
				R.drawable.wheel_select));

		Button okBtn = (Button) mDataView.findViewById(R.id.okBtn);
		Button cancelBtn = (Button) mDataView.findViewById(R.id.cancelBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
				int index = mWheelView1.getCurrentItem();
				String val = mWheelView1.getAdapter().getItem(index);
				textView.setText(val);
				// ʵ����SharedPreferences���󣨵�һ����
				SharedPreferences mySharedPreferences = getSharedPreferences(
						"setting", SettingView.MODE_PRIVATE);
				// ʵ����SharedPreferences.Editor���󣨵ڶ�����
				SharedPreferences.Editor editor = mySharedPreferences.edit();
				// ��putString�ķ�����������
				editor.putString("alertRMode", val);
				// �ύ��ǰ����
				editor.commit();
			}

		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
			}

		});
	}

	// ��λ��ʾ����
	public void initWheelDataOfBatteryMode(View mDataView,
			final TextView textView) {
		final AbWheelView mWheelView1 = (AbWheelView) mDataView
				.findViewById(R.id.wheelView1);
		// mWheelView1.setAdapter(new AbNumericWheelAdapter(40, 190));
		mWheelView1.setAdapter(new AbWheelAdapter() {
			@Override
			public int getMaximumLength() {
				// TODO Auto-generated method stub
				return batteryModeArr.length;
			}

			@Override
			public int getItemsCount() {
				// TODO Auto-generated method stub
				return batteryModeArr.length;
			}

			@Override
			public String getItem(int arg0) {
				// TODO Auto-generated method stub
				return batteryModeArr[arg0];
			}
		});
		// ��ѭ������
		mWheelView1.setCyclic(false);
		// �������
		mWheelView1.setLabel(null);
		// ��ʼ��ʱ��ʾ������
		mWheelView1.setCurrentItem(40);
		mWheelView1.setValueTextSize(35);
		mWheelView1.setLabelTextSize(35);
		mWheelView1.setLabelTextColor(0x80000000);
		mWheelView1.setCenterSelectDrawable(this.getResources().getDrawable(
				R.drawable.wheel_select));

		Button okBtn = (Button) mDataView.findViewById(R.id.okBtn);
		Button cancelBtn = (Button) mDataView.findViewById(R.id.cancelBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
				int index = mWheelView1.getCurrentItem();
				String val = mWheelView1.getAdapter().getItem(index);
				textView.setText(val);
				// ʵ����SharedPreferences���󣨵�һ����
				SharedPreferences mySharedPreferences = getSharedPreferences(
						"setting", SettingView.MODE_PRIVATE);
				// ʵ����SharedPreferences.Editor���󣨵ڶ�����
				SharedPreferences.Editor editor = mySharedPreferences.edit();
				// ��putString�ķ�����������
				editor.putString("batteryMode", val);
				// �ύ��ǰ����
				editor.commit();
			}

		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
			}

		});
	}

	// ��λ��ʾ����
	public void initWheelDataOfRefreshFrequency(View mDataView,
			final TextView textView) {
		final AbWheelView mWheelView1 = (AbWheelView) mDataView
				.findViewById(R.id.wheelView1);
		// mWheelView1.setAdapter(new AbNumericWheelAdapter(40, 190));
		mWheelView1.setAdapter(new AbWheelAdapter() {
			@Override
			public int getMaximumLength() {
				// TODO Auto-generated method stub
				return refreshTArr.length;
			}

			@Override
			public int getItemsCount() {
				// TODO Auto-generated method stub
				return refreshTArr.length;
			}

			@Override
			public String getItem(int arg0) {
				// TODO Auto-generated method stub
				return refreshTArr[arg0];
			}
		});
		// ��ѭ������
		mWheelView1.setCyclic(false);
		// �������
		mWheelView1.setLabel(null);
		// ��ʼ��ʱ��ʾ������
		mWheelView1.setCurrentItem(40);
		mWheelView1.setValueTextSize(35);
		mWheelView1.setLabelTextSize(35);
		mWheelView1.setLabelTextColor(0x80000000);
		mWheelView1.setCenterSelectDrawable(this.getResources().getDrawable(
				R.drawable.wheel_select));

		Button okBtn = (Button) mDataView.findViewById(R.id.okBtn);
		Button cancelBtn = (Button) mDataView.findViewById(R.id.cancelBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
				int index = mWheelView1.getCurrentItem();
				String val = mWheelView1.getAdapter().getItem(index);
				textView.setText(val);
				// ʵ����SharedPreferences���󣨵�һ����
				SharedPreferences mySharedPreferences = getSharedPreferences(
						"setting", SettingView.MODE_PRIVATE);
				// ʵ����SharedPreferences.Editor���󣨵ڶ�����
				SharedPreferences.Editor editor = mySharedPreferences.edit();
				// ��putString�ķ�����������
				editor.putString("refreshMode", val);
				// �ύ��ǰ����
				editor.commit();
			}

		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
			}

		});
	}

	private void findViewById() {

//		tv_scanap = (TextView) findViewById(R.id.tv_setting_scanap);
//		tv_scanmethod = (TextView) findViewById(R.id.tv_setting_scanmethod);
//		tv_scantime = (TextView) findViewById(R.id.tv_setting_scanTime);
		String radius = preferences.getString(Constants.SETTING_RAD, "1000米");
		String alertDistance = preferences.getString(Constants.SETTING_ALE,
				"100米");
		String battery = preferences.getString(Constants.SETTING_BAT, "2级(推荐)");
		String ap = preferences.getString(Constants.SETTING_AP, "16wifi");
		String method = preferences
				.getString(Constants.SETTING_MET, "手动收集公交信息");
		String time = preferences.getString(Constants.SETTING_FRE, "10秒");
		String precision = preferences.getString(Constants.SETTING_PRECISION,
				"仅网络定位");
//
//		tv_scanap.setText(ap);
//		tv_scanmethod.setText(method);
//		tv_scantime.setText(time);
	}

	private void register() {
//		tv_scanmethod.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				Log.i(TAG, "tv_scanmethod is clicked");
//				mDataViewScanMethod = mInflater.inflate(
//						R.layout.setting_wheelview, null);
//				List<String> items = new ArrayList<String>();
//				items.add("自动收集公交信息");
//				items.add("手动收集公交信息");
//				Log.i(TAG, "items initial");
//				String title = Constants.SETTING_MET;
//				initWheelData(mDataViewScanMethod, items, tv_scanmethod, title);
//				showDialog(AbConstant.DIALOGBOTTOM, mDataViewScanMethod, 40);
//			}
//		});
//		tv_scanap.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				Log.i(TAG, "tv_scanap is clicked");
//				mDataViewScanAp = mInflater.inflate(R.layout.setting_wheelview,
//						null);
//				List<String> items = new ArrayList<String>();
//				items.add("BNRC-Air");
//				items.add("16wifi");
//				items.add("Bupt-1");
//				items.add("Bupt-2");
//				Log.i(TAG, "items initial");
//				String title = Constants.SETTING_AP;
//				initWheelData(mDataViewScanAp, items, tv_scanap, title);
//				showDialog(AbConstant.DIALOGBOTTOM, mDataViewScanAp, 40);
//			}
//		});
//		tv_scantime.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				Log.i(TAG, "tv_scantime is clicked");
//				mDataViewScanTime = mInflater.inflate(
//						R.layout.setting_wheelview, null);
//				List<String> items = new ArrayList<String>();
//				items.add("5秒");
//				items.add("10秒");
//				items.add("20秒");
//				items.add("30秒");
//				items.add("60秒");
//				Log.i(TAG, "items initial");
//				String title = Constants.SETTING_FRE;
//				initWheelData(mDataViewScanTime, items, tv_scantime, title);
//				showDialog(AbConstant.DIALOGBOTTOM, mDataViewScanTime, 40);
//
//			}
//		});

	}

	private void initWheelData(View mDataView, List<String> items,
			final TextView tv, final String title) {

		final AbWheelView mWheelView = (AbWheelView) mDataView
				.findViewById(R.id.wheelView1);
		mWheelView.setAdapter(new AbStringWheelAdapter(items));
		// 可循环滚动
		// mWheelView1.setCyclic(true);
		// //添加文字
		// mWheelView1.setLabel("lala");
		// 初始化时显示的数据
		mWheelView.setCurrentItem(1);
		mWheelView.setValueTextSize(35);
		// mWheelView1.setLabelTextSize(35);
		// mWheelView1.setLabelTextColor(0x80000000);
		mWheelView.setCenterSelectDrawable(this.getResources().getDrawable(
				R.drawable.wheel_select));

		Button okBtn = (Button) mDataView.findViewById(R.id.okBtn);
		Button cancelBtn = (Button) mDataView.findViewById(R.id.cancelBtn);
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
				int index = mWheelView.getCurrentItem();
				String val = mWheelView.getAdapter().getItem(index);
				tv.setText(val);
				Log.i(TAG, "val: " + val);
				Log.i(TAG, "title: " + title);
				editor.putString(title.trim(), val);
				editor.commit();
				if (title.trim().equalsIgnoreCase(Constants.SETTING_AP)
						|| title.trim().equalsIgnoreCase(Constants.SETTING_FRE)) {
					if (WifiReceiver.IsScan == 1)
						ServiceUtils.startPollingService(
								getApplicationContext(), ScanService.class,
								Constants.SERVICE_ACTION);
				} else if (title.trim().equalsIgnoreCase(
						Constants.SETTING_PRECISION))
					LocationUtil.getInstance(getApplicationContext())
							.startLocation();
			}

		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeDialog(1);
			}

		});
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
		MobclickAgent.onResume(this); // ͳ��ʱ��
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); // ��֤ onPageEnd ��onPause //
													// ֮ǰ����,��Ϊ onPause
													// �лᱣ����Ϣ
		MobclickAgent.onPause(this);
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);

	}
}
