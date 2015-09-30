package com.bnrc.ui.rjz;

import java.util.ArrayList;
import java.util.List;

import com.bnrc.busapp.R;
import com.bnrc.ui.listviewswipedelete.BlogItem;
import com.bnrc.ui.listviewswipedelete.DelBlogItem;
import com.bnrc.ui.listviewswipedelete.ListViewCompat;
import com.bnrc.ui.listviewswipedelete.SlideAdapter;
import com.bnrc.ui.listviewswipedelete.SlideView;
import com.bnrc.ui.listviewswipedelete.SlideView.OnSlideListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ConcernFragment extends BaseFragment implements
		OnItemClickListener {
	private static final String TAG = ConcernFragment.class.getSimpleName();
	private Context mContext;
	private ListViewCompat mListView;
	private List<DelBlogItem> blogItems;
	private SlideAdapter adapter;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = (Context) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.activity_listview_swipe, null);

		mListView = (ListViewCompat) view.findViewById(R.id.list);
		blogItems = new ArrayList<DelBlogItem>();

		DelBlogItem item = new DelBlogItem();
		item.setStation("明光桥北");
		item.setAlertState("提醒");
		blogItems.add(item);
		DelBlogItem item1 = new DelBlogItem();
		item1.setStation("西单");
		item1.setAlertState("提醒");
		blogItems.add(item1);
		DelBlogItem item2 = new DelBlogItem();
		item2.setStation("天通苑");
		item2.setAlertState("不提醒");
		blogItems.add(item2);
		DelBlogItem item3 = new DelBlogItem();
		item3.setStation("通州北苑");
		item3.setAlertState("不提醒");
		blogItems.add(item3);
		DelBlogItem item4 = new DelBlogItem();
		item4.setStation("大红门 ");
		item4.setAlertState("提醒");
		blogItems.add(item4);
		adapter = new SlideAdapter(mContext, blogItems);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Toast.makeText(mContext, "onItemClick position=" + position,
		// 0).show();

	}

}
