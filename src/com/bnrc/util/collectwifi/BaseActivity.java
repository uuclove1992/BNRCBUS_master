package com.bnrc.util.collectwifi;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.ab.activity.AbActivity;
import com.bnrc.busapp.R;

public class BaseActivity extends AbActivity {
	protected static final String TAG = BaseActivity.class.getSimpleName();
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
				new Thread_send_json(BaseActivity.this, mThreadHandler).start();
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
		return true;
	}
}
