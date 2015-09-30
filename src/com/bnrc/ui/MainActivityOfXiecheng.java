package com.bnrc.ui;

import com.bnrc.busapp.R;
import com.bnrc.busapp.SettingView;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewFlipper;

public class MainActivityOfXiecheng extends ActivityGroup implements
		OnCheckedChangeListener {

	public static MainActivityOfXiecheng instance = null;
	private ViewFlipper container;// viewflipper������ĻҶ���л�
	private RadioGroup radio_group;//
	private Intent mIntent;
	private RadioButton radio_near, radio_concern, radio_buscircle, radio_help;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		switchPage(0);
		radio_group.setOnCheckedChangeListener(this);
	}

	private void switchPage(int positoon) {
		switch (positoon) {
		case 0:
			mIntent = new Intent(this, NearActivity.class);
			break;
		case 1:
			mIntent = new Intent(this, ConcernActivity.class);
			break;
		case 2:
			mIntent = new Intent(this, BuscircleActivity.class);
			break;
		case 3:
			mIntent = new Intent(this, SettingView.class);
			break;

		default:
			break;
		}

		container.removeAllViews();
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Window subActivity = getLocalActivityManager().startActivity(
				"subActivity", mIntent);
		container.addView(subActivity.getDecorView(),
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
	}

	/**
	 * ��ʼ�����ֿؼ�
	 */
	private void initView() {
		container = (ViewFlipper) findViewById(R.id.container);
		radio_group = (RadioGroup) findViewById(R.id.radio_group);

		radio_near = (RadioButton) findViewById(R.id.radio_near);
		radio_concern = (RadioButton) findViewById(R.id.radio_concern);
		radio_buscircle = (RadioButton) findViewById(R.id.radio_buscircle);
		radio_help = (RadioButton) findViewById(R.id.radio_setting);

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_near:

			switchPage(0);

			break;

		case R.id.radio_concern:

			switchPage(1);
			break;

		case R.id.radio_buscircle:

			switchPage(2);
			break;

		case R.id.radio_setting:

			switchPage(3);
			break;

		default:
			break;
		}

	}

	private void getScreenDensity() {
		Display currDisplay = getWindowManager().getDefaultDisplay();// ��ȡ��Ļ��ǰ�ֱ���
		int displayWidth = currDisplay.getWidth();
		int displayHeight = currDisplay.getHeight();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
