package com.bnrc.ui.rjz;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.bnrc.busapp.PollingService;
import com.bnrc.busapp.R;
import com.bnrc.busapp.SettingView;
import com.bnrc.ui.rjz.RTabHost.RTabHostListener;
import com.bnrc.util.LocationUtil;
import com.bnrc.util.collectwifi.ActivityReceiver;
import com.bnrc.util.collectwifi.BaseActivity;
import com.bnrc.util.collectwifi.CollectWifiDBHelper;
import com.bnrc.util.collectwifi.ConfigCheck;
import com.bnrc.util.collectwifi.Constants;
import com.bnrc.util.collectwifi.MyDialogReadSureWifi;
import com.bnrc.util.collectwifi.MyDialogReadWifi;
import com.bnrc.util.collectwifi.ScanService;
import com.bnrc.util.collectwifi.ServiceUtils;
import com.bnrc.util.collectwifi.Thread_send_json;
import com.bnrc.util.collectwifi.WifiAdmin;
import com.bnrc.util.collectwifi.WifiReceiver;
import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	protected static final String TAG = BaseActivity.class.getSimpleName();
	private RTabHost mTabHost;

	private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
	private BaseFragment mCurFragment;
	private int mLastIndex = 0;
	// 扫描wifi的变量
	protected ConfigCheck mConfigCheck;
	protected CollectWifiDBHelper mCollectWifiDBHelperInstance = null;
	protected WifiAdmin mWifiAdminInstance = null;
	protected List<Map<String, String>> mWifiData = null;
	protected ActivityReceiver mActivityReceiver = null;
	protected Builder wifiBuilderDialog = null;
	protected WifiReceiver mWifiReceiver = null;
	protected SharedPreferences preferences;
	protected IntentFilter activityFilter;
	protected IntentFilter wifiFilter;
	final int SWITCHSCAN = Menu.FIRST;
	final int READSQL = Menu.FIRST + 1;
	final int READSQLSURE = Menu.FIRST + 2;
	final int DELETESQL = Menu.FIRST + 3;
	final int DELETESURESQL = Menu.FIRST + 4;
	final int UPTOSERVER = Menu.FIRST + 5;
	final int SWITCHWIFI = Menu.FIRST + 6;
	final int EXIT = Menu.FIRST + 7;
	protected Handler mThreadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Log.d(TAG, "mThreadHandler。。。。。。");
			// 此处可以更新UI
			Bundle bundle = msg.getData();
			mCollectWifiDBHelperInstance
					.deleteCollectWifiTablesByTimestamp(bundle.getString(
							"responseMsg", "0"));

		}
	};

	private Timer timer3;
	private TimerTask task3;
	private Timer timer2;
	private TimerTask task2;
	int screenWidth;// 屏幕宽度
	public LocationUtil mApplication = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rjz_main);
		mApplication = LocationUtil.getInstance(this);
		mApplication.startLocation();
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		screenWidth = display.getWidth();

		initFragments();
		initTabHost();
		initbase();
		TimerTask task = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mCurFragment instanceof NearFragment) {
							((NearFragment) mCurFragment).curlocation
									.setText(mApplication.addressString);
						}
					}
				});
				// curlocation.setText("当前位置："+mApplication.addressString);
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(task, 2000, 10000);
		task2 = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {

						if (isNetworkConnected()) {
							if (mCurFragment instanceof NearFragment) {
								((NearFragment) mCurFragment).GetAllBuses();
							}
						} else {
							Toast toast = Toast.makeText(
									getApplicationContext(), "您的网络有问题哦~",
									Toast.LENGTH_LONG);
							toast.show();
						}
					}
				});
			}
		};

		timer2 = new Timer(true);
		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		String timeString = mySharedPreferences.getString("refreshMode", "30秒");
		timeString = timeString.substring(0, timeString.length() - 1);
		timer2.schedule(task2, 200, Integer.parseInt(timeString) * 1000);

		TimerTask task4 = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mCurFragment instanceof NearFragment) {
							((NearFragment) mCurFragment).firstTimeRequest++;
							((NearFragment) mCurFragment)
									.refreshRealtimeBuses();
						}
					}
				});
			}
		};

		task3 = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mCurFragment instanceof NearFragment) {
							((NearFragment) mCurFragment)
									.getNearbyStationsAndBuslines();
						}
					}
				});
			}
		};
		timer3 = new Timer(true);
		timer3.schedule(task3, 300, 5000 * 60);

		Timer timer4 = new Timer(true);
		timer4.schedule(task4, 0, Integer.parseInt(timeString) * 1000); // 延时1000ms后执行，1000ms执行一次
		Log.i(TAG, "onCreate()");
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (mCurFragment instanceof NearFragment) {
			((NearFragment) mCurFragment).getFavStationsAndBuslines();
		}

		task2 = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (isNetworkConnected()) {
							if (mCurFragment instanceof NearFragment) {
								((NearFragment) mCurFragment).GetAllBuses();
							}
						}

					}
				});
			}
		};
		timer2 = new Timer();

		task3 = new TimerTask() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mCurFragment instanceof NearFragment) {
							((NearFragment) mCurFragment)
									.getNearbyStationsAndBuslines();
						}
					}
				});
			}
		};

		timer3 = new Timer();
		timer3.schedule(task3, 5000 * 60, 5000 * 60);
		SharedPreferences mySharedPreferences = getSharedPreferences("setting",
				SettingView.MODE_PRIVATE);
		String timeString = mySharedPreferences.getString("refreshMode", "30秒");
		timeString = timeString.substring(0, timeString.length() - 1);
		timer2.schedule(task2, 1000, Integer.parseInt(timeString) * 1000);
		Log.i(TAG, "onRestart()");

	}

	@Override
	public void onPause() {
		super.onPause();

		MobclickAgent.onPageEnd("SplashScreen"); // ��֤ onPageEnd ��onPause
		MobclickAgent.onPause(this);
		task2.cancel();
		timer2.cancel();
		task3.cancel();
		timer3.cancel();
		unregisterReceiver(mWifiReceiver);
		unregisterReceiver(mActivityReceiver);
		Log.i(TAG, "onPause()");

	}

	@Override
	public void onResume() {
		super.onResume();

		PollingService.hasKnown = true;

		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
		MobclickAgent.onResume(this); // ͳ��ʱ��
		registerReceiver(mWifiReceiver, wifiFilter);
		registerReceiver(mActivityReceiver, activityFilter);
		Log.i(TAG, "onResume()");

	}

	public boolean isNetworkConnected() {
		if (MainActivity.this != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	protected void initbase() {
		try {

			// 扫描wifi的变量初始化
			mConfigCheck = new ConfigCheck(getApplicationContext());
			mCollectWifiDBHelperInstance = CollectWifiDBHelper
					.getInstance(getApplicationContext());
			mWifiAdminInstance = WifiAdmin.getInstance(getApplicationContext());
			mActivityReceiver = new ActivityReceiver();
			activityFilter = new IntentFilter();
			activityFilter.addAction(Constants.UPDATE_ACTION);
			activityFilter.addAction(Constants.SHAREPRF_ACTION);
			// 监听wifi广播
			mWifiReceiver = new WifiReceiver(mThreadHandler);
			wifiFilter = new IntentFilter();
			wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			// registerReceiver(mWifiReceiver, filter);
		} catch (NullPointerException e) {
			Log.i("NULLPOINTER", "initbase: " + e.toString());
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {

			menu.clear();
			if (!ServiceUtils.isPollingStart())
				menu.add(0, SWITCHSCAN, 0, "开始收集公交wifi").setIcon(
						android.R.drawable.ic_media_play);
			else
				menu.add(0, SWITCHSCAN, 0, "停止收集公交wifi").setIcon(
						android.R.drawable.ic_media_pause);

			if (!mConfigCheck.isWifiEnable()) {
				menu.add(0, SWITCHWIFI, 0, "打开Wifi").setIcon(
						android.R.drawable.btn_star_big_on);
			} else {
				menu.add(0, SWITCHWIFI, 0, "关闭Wifi").setIcon(
						android.R.drawable.btn_star_big_off);
			}

			SubMenu databaseMenu = menu.addSubMenu("数据库操作");
			databaseMenu.setIcon(android.R.drawable.ic_menu_search);
			databaseMenu.setHeaderIcon(android.R.drawable.ic_menu_search);
			databaseMenu.setHeaderTitle("选择操作方式");
			databaseMenu.add(0, READSQL, 0, "读取采集信息的数据库");
			databaseMenu.add(0, READSQLSURE, 0, "读取已确定的数据库");
			databaseMenu.add(0, DELETESQL, 0, "删除采集信息数据库");
			databaseMenu.add(0, DELETESURESQL, 0, "删除确定信息数据库");
			databaseMenu.add(0, UPTOSERVER, 0, "上传数据库到服务器");
			// menu.add(0, UPTOSERVER, 0, "上传到服务器").setIcon(
			// android.R.drawable.ic_menu_upload);
			menu.add(0, EXIT, 0, "退出程序").setIcon(
					android.R.drawable.ic_lock_power_off);
		} catch (NullPointerException e) {
			Log.i("NULLPOINTER", "onPrepareOptionsMenu: " + e.toString());
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		System.out.println("onOptionsItemSelected");

		try {

			switch (mi.getItemId()) {
			case SWITCHSCAN:
				if (ServiceUtils.isPollingStart()) {// 停止扫描
					WifiReceiver.IsScan = 2;// 0为初始值，1为开始扫描，2为停止扫描
					ServiceUtils.stopPollingService(getApplicationContext(),
							ScanService.class, Constants.SERVICE_ACTION);
				} else {// 开始扫描
					if (mConfigCheck.isWifiEnable()) {
						ServiceUtils.startPollingService(
								getApplicationContext(), ScanService.class,
								Constants.SERVICE_ACTION);
						WifiReceiver.IsScan = 1;// 0为初始值，1为开始扫描，2为停止扫描
					} else {
						WifiReceiver.IsScan = 1;// 0为初始值，1为开始扫描，2为停止扫描
						mWifiAdminInstance = WifiAdmin
								.getInstance(getApplicationContext());
						mWifiAdminInstance.openWifi();
					}

				}
				break;

			case SWITCHWIFI:
				if (mConfigCheck.isWifiEnable()) {
					mWifiAdminInstance.closeWifi();
				} else
					mWifiAdminInstance.openWifi();
				break;

			case EXIT:
				ServiceUtils.stopPollingService(getApplicationContext(),
						ScanService.class, Constants.SERVICE_ACTION);
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
				break;

			case READSQL:
				mWifiData = mCollectWifiDBHelperInstance.FindScanData();
				MyDialogReadWifi mReadWifiDialog = new MyDialogReadWifi(this,
						R.style.MyDBDialog, mWifiData);
				mReadWifiDialog.show();
				break;

			case READSQLSURE:
				mWifiData = mCollectWifiDBHelperInstance.FindSureData();
				MyDialogReadSureWifi readDialogSure = new MyDialogReadSureWifi(
						this, R.style.MyDBDialog, mWifiData);
				readDialogSure.show();
				break;
			case DELETESQL:
				mCollectWifiDBHelperInstance.deleteCollectWifiTables();
				break;

			case DELETESURESQL:
				mCollectWifiDBHelperInstance.deleteSureWifiTables();
				break;
			case UPTOSERVER:
				Toast.makeText(getApplicationContext(), "连接服务器线程开始", 1000)
						.show();
				new Thread_send_json(MainActivity.this, mThreadHandler).start();
				break;
			default:
				break;
			}
		} catch (NullPointerException e) {
			Log.i("NULLPOINTER", "onOptionsItemSelected: " + e.toString());// TODO:
																			// handle
																			// exception
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		System.out.println("onCreateOptionsMenu");

		getMenuInflater().inflate(R.menu.main, menu);
		Log.i(TAG, "onCreateOptionsMenu()");

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showExitDialog();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void initTabHost() {
		mTabHost = (RTabHost) findViewById(R.id.tab_host);

		mTabHost.setQQTabHostListener(new RTabHostListener() {

			@Override
			public void onTabSelected(int index) {
				selectTab(index);
			}
		});

		mTabHost.selectTab(mLastIndex);
	}

	private void initFragments() {
		mFragments.clear();

		List<Class<? extends BaseFragment>> classList = new ArrayList<Class<? extends BaseFragment>>();
		classList.add(NearFragment.class);
		classList.add(ConcernFragment.class);
		classList.add(SearchFragment.class);
		classList.add(SettingFragment.class);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transcation = getSupportFragmentManager()
				.beginTransaction();
		for (Class<? extends BaseFragment> fragClass : classList) {
			BaseFragment frag = (BaseFragment) fm.findFragmentByTag(fragClass
					.getName());
			if (null == frag) {
				frag = createFragmentByClass(fragClass);
				transcation
						.add(R.id.page_conatainer, frag, fragClass.getName());
			}

			mFragments.add(frag);
			transcation.hide(frag);
		}

		transcation.commitAllowingStateLoss();
	}

	private BaseFragment createFragmentByClass(
			Class<? extends BaseFragment> fragClass) {
		BaseFragment frag = null;
		try {
			try {
				Constructor<? extends BaseFragment> cons = null;
				cons = fragClass.getConstructor();
				frag = cons.newInstance();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			throw new RuntimeException("Can not create instance for class "
					+ fragClass.getName(), e);
		}
		return frag;
	}

	private void selectTab(int index) {
		mLastIndex = index;
		selectFragment(index);
	}

	private void selectFragment(int index) {
		if (mFragments.get(index).isVisible()) {
			return;
		}

		FragmentTransaction transcation = getSupportFragmentManager()
				.beginTransaction();

		if (null != mCurFragment) {

			transcation.hide(mCurFragment);
		}

		mCurFragment = mFragments.get(index);
		transcation.show(mCurFragment);

		transcation.commitAllowingStateLoss();
	}

	private long mLastBackTime = 0;

	private void showExitDialog() {

		long now = System.currentTimeMillis();
		if (now - mLastBackTime < 3000) {
			exitProcess();
		} else {
			mLastBackTime = now;
			// Toast.makeText(MainActivity.this, "再次点击退出程序",
			// Toast.LENGTH_SHORT).show();
			//
			Toast toast = new Toast(MainActivity.this);
			toast.setView(LayoutInflater.from(MainActivity.this).inflate(
					R.layout.toast_view, null));

			toast.setDuration(Toast.LENGTH_SHORT);
			toast.show();

		}
	}

	private void exitProcess() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}, 400);

		finish();
	}

	public int getScreenwidth() {
		return screenWidth;
	}
}
