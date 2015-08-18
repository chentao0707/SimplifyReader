///*
// * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
// * 
// * Email:qq81595157@126.com
// * 
// * PROPRIETARY/CONFIDENTIAL.
// */
//
//package com.youku.ui.activity;
//
//import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.TextView;
//
//import com.baseproject.utils.Logger;
//import com.youku.player.ui.R;
//
///**
// * @class A CacheSeriesActivity.
// * @Description: TODO 缓存的剧集列表
// * 
// * @author 刘仲男
// * @version $Revision$
// * @created time 2012-11-13 上午9:59:53
// */
//public class CacheSeriesActivity extends ActionBarActivity {
//
//	public static CacheSeriesActivity instance;
//	private String cats = null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		instance = this;
//		if (getIntent().hasExtra("cats"))
//			cats = getIntent().getStringExtra("cats");
//		else
//			cats = "";
//		Logger.d("cats:" + cats);
//		if (getString(R.string.detail_movie).equals(cats)
//				|| getString(R.string.detail_variety).equals(cats)
//				|| getString(R.string.detail_special).equals(cats)
//				|| getString(R.string.detail_memory).equals(cats)
//				|| getString(R.string.detail_education).equals(cats)
//				|| getString(R.string.detail_entertainment).equals(cats)) {
//			setContentView(R.layout.activity_cacheseries_fortitle);
//		} else {
//			setContentView(R.layout.activity_cacheseries);
//		}
//		// setContentView(R.layout.activity_cacheseries);
//		TextView showname = (TextView) findViewById(R.id.title);
//		showname.setText(getIntent().getStringExtra("showname"));
//		// CachePageActivity.instances.setProgressValues(this);
//	}
//
//	@Override
//	protected void onDestroy() {
//		instance = null;
//		super.onDestroy();
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
///*		if (null!=Youku.mAction_bars&&Youku.mAction_bars.size()>0) {
//			SortActionBars(menu);
//		}else {
//			if (android.os.Build.VERSION.SDK_INT >= 11) {
//				getMenuInflater().inflate(R.menu.home_mainpage, menu);
//			} else {
//				getMenuInflater().inflate(R.menu.home_lowversion, menu);
//			}
//		}*/
////		setupSearchMenuItem(menu);
///*		menu.removeItem(R.id.menu_download);
//		getSupportActionBar().setDisplayUseLogoEnabled(false);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
////		menuItemClicked(item);
//		return super.onOptionsItemSelected(item);
//	}
///*
//	@Override
//	public String getPageName() {
//		return "缓存的剧集列表";
//	}*/
//}
