package com.bnrc.busapp;

import org.json.JSONObject;

import com.ab.global.AbConstant;
import com.bnrc.ui.MainActivityOfXiecheng;
import com.bnrc.ui.rjz.MainActivity;
import com.bnrc.util.LocationUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

public class splashScreen extends Activity {
	/**
	 * Called when the activity is first created.
	 */

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		setContentView(R.layout.splashscreen);

		com.umeng.socialize.utils.Log.LOG = true;
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent
				.setOnlineConfigureListener(new UmengOnlineConfigureListener() {
					@Override
					public void onDataReceived(JSONObject data) {
					}
				});

		LocationUtil mApplication = LocationUtil.getInstance(splashScreen.this);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				/* Create an Intent that will start the Main WordPress Activity. */
				Intent mainIntent = new Intent(splashScreen.this,
						MainActivity.class);
				mainIntent.putExtra("TEXT", splashScreen.this.getResources()
						.getString(R.string.title_transparent_desc));
				mainIntent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
						AbConstant.TITLE_TRANSPARENT);
				splashScreen.this.startActivity(mainIntent);
				splashScreen.this.finish();
			}
		}, 2900); // 2900 for release

	}
}
