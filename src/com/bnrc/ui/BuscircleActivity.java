package com.bnrc.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.global.AbConstant;
import com.bnrc.busapp.MyAlertStationView;
import com.bnrc.busapp.MyFavoriteBuslineView;
import com.bnrc.busapp.MyFavoriteStationView;
import com.bnrc.busapp.NewRootView;
import com.bnrc.busapp.R;
import com.bnrc.busapp.SearchSomethingView;
import com.bnrc.util.collectwifi.BaseActivity;

/**
 * @author zj
 */

public class BuscircleActivity extends BaseActivity {

	private TextView gongjiaochuxing;
	private TextView dianyingBtn;
	private TextView cantingBtn;
	private TextView yinhangBtn;
	private TextView chaoshiBtn;
	private TextView tixingBtn;
	private TextView favStationBtn;
	private TextView favBuslineBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_buscircle);
		setTitleText("公交圈");
		setLogo(R.drawable.button_selector_back);
		setTitleLayoutBackground(R.drawable.top_bg);
		setTitleLayoutGravity(Gravity.CENTER, Gravity.CENTER);
		findViewById();
		setListener();
	}

	private void findViewById() {
		gongjiaochuxing = (TextView) findViewById(R.id.gongjiaochuxing);
		dianyingBtn = (TextView) findViewById(R.id.dianying);
		cantingBtn = (TextView) findViewById(R.id.canting);
		yinhangBtn = (TextView) findViewById(R.id.yinhang);
		chaoshiBtn = (TextView) findViewById(R.id.chaoshi);
		tixingBtn = (TextView) findViewById(R.id.tixingzhandian);
		favStationBtn = (TextView) findViewById(R.id.shoucangzhandian);
		favBuslineBtn = (TextView) findViewById(R.id.shoucangxianlu);
	}

	private void setListener() {
		gongjiaochuxing.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		tixingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BuscircleActivity.this,
						MyAlertStationView.class);
				intent.putExtra("TEXT", BuscircleActivity.this.getResources()
						.getString(R.string.title_transparent_desc));
				// ���ñ�����͸��
				intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
						AbConstant.TITLE_TRANSPARENT);
				startActivity(intent);
			}
		});

		favStationBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BuscircleActivity.this,
						MyFavoriteStationView.class);
				intent.putExtra("TEXT", BuscircleActivity.this.getResources()
						.getString(R.string.title_transparent_desc));
				// ���ñ�����͸��
				intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
						AbConstant.TITLE_TRANSPARENT);
				startActivity(intent);

			}
		});

		favBuslineBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BuscircleActivity.this,
						MyFavoriteBuslineView.class);
				intent.putExtra("TEXT", BuscircleActivity.this.getResources()
						.getString(R.string.title_transparent_desc));
				// ���ñ�����͸��
				intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
						AbConstant.TITLE_TRANSPARENT);
				startActivity(intent);

			}
		});
		dianyingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ
				if (isNetworkConnected(BuscircleActivity.this)) {
					Intent intent = new Intent(BuscircleActivity.this,
							SearchSomethingView.class);
					intent.putExtra(
							"TEXT",
							BuscircleActivity.this.getResources().getString(
									R.string.title_transparent_desc));
					// ���ñ�����͸��
					intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
							AbConstant.TITLE_TRANSPARENT);
					intent.putExtra("keyword", "酒店");
					startActivity(intent);
					// overridePendingTransition(R.anim.out_alpha,
					// R.anim.enter_alpha);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"您的网络有问题，请检查~", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		});

		cantingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (isNetworkConnected(BuscircleActivity.this)) {
					Intent intent = new Intent(BuscircleActivity.this,
							SearchSomethingView.class);
					intent.putExtra(
							"TEXT",
							BuscircleActivity.this.getResources().getString(
									R.string.title_transparent_desc));
					intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
							AbConstant.TITLE_TRANSPARENT);
					intent.putExtra("keyword", "小吃");
					startActivity(intent);
					// overridePendingTransition(R.anim.out_alpha,
					// R.anim.enter_alpha);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"您的网络有问题，请检查~", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		});

		yinhangBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ
				if (isNetworkConnected(BuscircleActivity.this)) {
					Intent intent = new Intent(BuscircleActivity.this,
							SearchSomethingView.class);
					intent.putExtra(
							"TEXT",
							BuscircleActivity.this.getResources().getString(
									R.string.title_transparent_desc));
					// ���ñ�����͸��
					intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
							AbConstant.TITLE_TRANSPARENT);
					intent.putExtra("keyword", "银行");
					startActivity(intent);
					// overridePendingTransition(R.anim.out_alpha,
					// R.anim.enter_alpha);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"您的网络有问题，请检查~", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		});

		chaoshiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ
				if (isNetworkConnected(BuscircleActivity.this)) {
					Intent intent = new Intent(BuscircleActivity.this,
							SearchSomethingView.class);
					intent.putExtra(
							"TEXT",
							BuscircleActivity.this.getResources().getString(
									R.string.title_transparent_desc));
					// ���ñ�����͸��
					intent.putExtra(AbConstant.TITLE_TRANSPARENT_FLAG,
							AbConstant.TITLE_TRANSPARENT);
					intent.putExtra("keyword", "超市");
					startActivity(intent);
					// overridePendingTransition(R.anim.out_alpha,
					// R.anim.enter_alpha);
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"您的网络有问题，请检查~", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		});

	}

	private boolean isNetworkConnected(Context context) {
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
}
