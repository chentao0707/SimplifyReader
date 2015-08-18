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
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBarActivity;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import com.baseproject.utils.UIUtils;
//import com.baseproject.utils.Util;
//import com.youku.player.YoukuPlayerApplication;
//import com.youku.player.adapter.CachePageAdapter;
//import com.youku.player.ui.R;
//import com.youku.player.ui.widget.YoukuLoading;
//import com.youku.player.util.PlayerUtil;
//import com.youku.service.download.DownloadInfo;
//import com.youku.service.download.DownloadManager;
//import com.youku.service.download.DownloadService;
//import com.youku.service.download.IDownload;
//import com.youku.service.download.OnChangeListener;
//import com.youku.service.download.SDCardManager;
//
///**
// * CachePageActivity.缓存页面
// * 
// * @author 刘仲男 qq81595157@126.com
// * @version v3.5
// * @created time 2012-11-2 下午4:50:34
// */
//public class CachePageActivity extends ActionBarActivity{
//
//	public static CachePageActivity instance;
//	private DownloadManager download;
//	private ViewPager viewpager;
//	private View btn_caching, btn_cached, edit, delete, complete, edit_folder,
//			complete_folder, outer, folder, item_edit, mem_info, btn_local;
//	private TextView title_folder;
//	private CachePageAdapter adapter;
//	private String showname, tag0, tag1;
//	private int selectTab = 0;
//	private boolean editable = false;
//	private boolean isFirstStart = true;
//	private boolean isNoFrom = false;
//	private boolean isLand = false;
//	public static boolean mIsEditState=false;
//
////	public boolean ismIsEditState() {
////		return mIsEditState;
////	}
////
////	public void setmIsEditState(boolean mIsEditState) {
////		this.mIsEditState = mIsEditState;
////	}
//	
//	public interface StateChangedCallback {
//		public void StateChanged(int selecttab);
//	}
//
//	private StateChangedCallback callback = new StateChangedCallback() {
//
//		@Override
//		public void StateChanged(final int selecttabed) {
//			selectTab = selecttabed;
//			switchTab(selecttabed);
//			adapter.notifyData();
//			setEditViewState(false);
//			setEditViewState(mIsEditState);
//			if (selecttabed == 0) {
//
//			} else {
//
//			}
//		}
//	};
//	/** 广播监听(SD卡事件、刷新数据事件) */
//	private BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			final String action = intent.getAction();
//			if (action.equals(DownloadManager.ACTION_DOWNLOAD_FINISH)) {
//				// adapter.fragment_downloading.refresh();
//				// adapter.fragment_downloaded.refresh();
//				setProgressValues(CachePageActivity.this);
//			} else if (action.equals(DownloadManager.ACTION_SDCARD_CHANGED)) {
//				adapter.fragment_downloading.refresh();
//				adapter.fragment_downloaded.refresh();
//				setProgressValues(CachePageActivity.this);
//			} else if (action
//					.equals(DownloadManager.ACTION_SDCARD_PATH_CHANGED)) {
//				setProgressValues(CachePageActivity.this);
//			} else if (action.equals(DownloadManager.ACTION_THUMBNAIL_COMPLETE)) {
//				adapter.fragment_downloading.refresh();
//				adapter.fragment_downloaded.refresh();
//			} else if (action
//					.equals(DownloadManager.ACTION_CREATE_DOWNLOAD_ALL_READY)) {
//				boolean value = intent.getBooleanExtra(
//						IDownload.KEY_CREATE_DOWNLOAD_IS_NEED_REFRESH, true);
//				if (value)
//					adapter.fragment_downloading.refresh();
//			}
//		}
//	};
//
//	@Override
//	protected void onCreate(Bundle b) {
//		super.onCreate(b);
//		setContentView(R.layout.activity_cachepage);
//		YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//		instance = this;
//		findView();
//		if (b != null) {
//			if (b.containsKey("download_tab"))
//				selectTab = b.getInt("download_tab");
//			if (b.containsKey("download_editable"))
//				editable = b.getBoolean("download_editable");
//			if (b.containsKey("showname"))
//				showname = b.getString("showname");
//			if (b.containsKey("tag0"))
//				tag0 = b.getString("tag0");
//			if (b.containsKey("tag1"))
//				tag1 = b.getString("tag1");
//			if (b.containsKey("isFirstStart"))
//				isFirstStart = b.getBoolean("isFirstStart");
//			if (b.containsKey("progress"))
//				progress = b.getInt("progress");
//			if (b.containsKey("secondaryProgress"))
//				secondaryProgress = b.getInt("secondaryProgress");
//			if (b.containsKey("Total_mem"))
//				Total_mem = b.getString("Total_mem");
//			if (b.containsKey("Other_used"))
//				Other_used = b.getString("Other_used");
//			if (b.containsKey("Download_used"))
//				Download_used = b.getString("Download_used");
//			if (b.containsKey("Free_mem"))
//				Free_mem = b.getString("Free_mem");
//		}
//		// 统计
//		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			isLand = true;
//			if (isFirstStart == false) {
//
//			}
//		} else {
//			isLand = false;
//			if (isFirstStart == false) {
//
//			}
//		}
//		adapter = new CachePageAdapter(this, viewpager, callback, tag0, tag1);
//		Intent intent = getIntent();
//		if (isFirstStart) {// 首次进入缓存页
//			YoukuLoading.show(this);
//			if (Util.hasSDCard()) {
//				selectTab = 0;
//				if (intent == null) {
//					isNoFrom = true;
//				} else if (intent.hasExtra("go")) {
//					if ("downloading".equals(intent.getStringExtra("go"))) {
//						selectTab = 0;
//					} else {
//						selectTab = 1;
//					}
//				} else if (isFromH5(intent)) {// 首次启动，并来自H5页
//					toDownload(intent);
//				} else {
//					isNoFrom = true;
//				}
//			} else {
//				YoukuPlayerApplication.showTips(R.string.download_no_sdcard);
//			}
//			// if(intent.hasExtra("from")){
//			// if("welcome".equals(intent.getStringExtra("from"))){
//			//
//			// }
//			// }
////			if (HomePageActivity.isLocalMode) {
////				HomePageActivity.initSomeData();
////				HomePageActivity.excuteInitTask();
////				HomePageActivity.excuteHomePageData(null);
////			}
//
//		} else {
//
//		}
//
//		switchTab(selectTab);
//		if (selectTab == 0) {
//			setFolderState(false, showname);
//		} else if (adapter.getIsInner()) {
//			setFolderState(true, showname);
//		} else {
//			setFolderState(false, showname);
//		}
//		setEditViewState(editable);
//		setDefauleProgressValues(instance);
//		download = DownloadManager.getInstance();
//		registBroadCastReciver();
//		new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				if (Util.hasSDCard()) {
//					download = DownloadManager.getInstance();
//					if (isFirstStart) {
//						isFirstStart = false;
//						if (isNoFrom) {
//							if (download.getDownloadingData() != null
//									&& download.getDownloadingData().size() == 0) {
//								selectTab = 1;
//								switchTab(selectTab);
//								if (download.getDownloadedData().size() == 0) {
//									YoukuPlayerApplication.showTips(R.string.tips_no_cache);
//								}
//							}
//						}
//					}
//
//					download.setOnChangeListener(new OnChangeListener() {
//
//						@Override
//						public void onFinish() {
//							if (adapter != null) {
//								adapter.fragment_downloading.refresh();
//								adapter.fragment_downloaded.refresh();
//							}
//						}
//
//						@Override
//						public void onChanged(DownloadInfo info) {
//							if (adapter != null)
//								adapter.fragment_downloading.setUpdate(info);
//						}
//					});
//					download.startNewTask();
//				} else {
//					isFirstStart = false;
//				}
//			}
//		}, 500L);
//		// 延迟载入空间进度
//		new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				setProgressValues(instance);
//			}
//		}, 1000L);
//
//		if (UIUtils.hasKitKat()
//				&& SDCardManager.getExternalStorageDirectory().size() > 0
//				&& !YoukuPlayerApplication.getPreferenceBoolean("44ExternalSDCardTips")) {
//			YoukuPlayerApplication.showTips("此前在外置SD卡中缓存的视频无法观看，建议到电脑上删除后重新缓存");
//			YoukuPlayerApplication.savePreference("44ExternalSDCardTips", true);
//		}
//		mIsEditState=false;
//	}
//	private boolean isFromH5(Intent intent) {
//		if (intent != null && intent.getData() != null) {
//			String vid = intent.getData().getQueryParameter("vid");
//			if (vid != null) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private void toDownload(Intent intent) {
//		String vid = intent.getData().getQueryParameter("vid");
//		String title = intent.getData().getQueryParameter("title");
//		String source = intent.getData().getQueryParameter("source");
//		String cookieid = intent.getData().getQueryParameter("cookieid");
//		title = URLDecoder(title);
//		Intent in = new Intent(instance, DownloadService.class);
//		in.setAction(DownloadService.ACTION_CREATE);
//		in.putExtra("videoId", vid);
//		if (title == null)
//			in.putExtra("videoName", "");
//		else
//			in.putExtra("videoName", title);
//		startService(in);
////		IStaticsManager.detailCall2CacheStatics(source, cookieid);
//	}
//
//	private String URLDecoder(String s) {
//		if (s == null || s.length() == 0)
//			return "";
//		try {
//			s = URLDecoder.decode(s, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			return "";
//		} catch (NullPointerException e) {
//			return "";
//		}
//		return s;
//	}
//
//	@Override
//	protected void onNewIntent(Intent intent) {
//		if (intent != null && intent.hasExtra("go")) {
//			if ("downloading".equals(intent.getStringExtra("go"))) {
//				if (viewpager.getCurrentItem() == 1) {
//					adapter.fragment_downloaded.returnOuter();
//					selectTab = 0;
//					switchTab(selectTab);
//				}
//			} else if ("downloaded".equals(intent.getStringExtra("go"))) {
//				if (viewpager.getCurrentItem() == 1) {
//					adapter.fragment_downloaded.returnOuter();
//				} else {
//					selectTab = 1;
//					switchTab(selectTab);
//				}
//			}
//		} else if (isFromH5(intent)) {
//			if (viewpager.getCurrentItem() == 1) {
//				adapter.fragment_downloaded.returnOuter();
//			}
//			if (Util.hasSDCard()) {
//				toDownload(intent);
//				selectTab = 0;
//				switchTab(selectTab);
//			} else {
//				YoukuPlayerApplication.showTips(R.string.download_no_sdcard);
//			}
//		}
//		super.onNewIntent(intent);
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		outState.putInt("download_tab", viewpager.getCurrentItem());
////		outState.putBoolean("download_editable",ismIsEditState());
//		outState.putString("showname", showname);
//		outState.putString("tag0", adapter.fragment_downloading.getTag());
//		outState.putString("tag1", adapter.fragment_downloaded.getTag());
//		outState.putBoolean("isFirstStart", isFirstStart);
//		if (Total_mem != null) {
//			outState.putInt("progress", progress);
//			outState.putInt("secondaryProgress", secondaryProgress);
//			outState.putString("Total_mem", Total_mem);
//			outState.putString("Other_used", Other_used);
//			outState.putString("Download_used", Download_used);
//			outState.putString("Free_mem", Free_mem);
//		}
//		super.onSaveInstanceState(outState);
//	}
//
//	@Override
//	protected void onStop() {
//		// 页面跳走，清除编辑状态；横竖屏切换的时候不改变编辑状态
//		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			if (isLand) {
//				adapter.notifyData();
//				setEditViewState(false);
//			}
//		} else {
//			if (!isLand) {
//				adapter.notifyData();
//				setEditViewState(false);
//			}
//		}
//		super.onStop();
//	}
//
//	@Override
//	protected void onDestroy() {
//		YoukuLoading.dismiss();
//		if (broadCastReceiver != null)
//			unregisterReceiver(broadCastReceiver);
//		if (download != null) {
//			download.setOnChangeListener(null);
//		}
//		adapter = null;
//		instance = null;
//		super.onDestroy();
//	}
//
//	private void registBroadCastReciver() {
//		IntentFilter f = new IntentFilter();
//		f.addAction(DownloadManager.ACTION_SDCARD_CHANGED);
//		f.addAction(DownloadManager.ACTION_SDCARD_PATH_CHANGED);
//		f.addAction(DownloadManager.ACTION_THUMBNAIL_COMPLETE);
//		f.addAction(DownloadManager.ACTION_DOWNLOAD_FINISH);
//		f.addAction(DownloadManager.ACTION_CREATE_DOWNLOAD_ALL_READY);
//		registerReceiver(broadCastReceiver, f);
//	}
//
//	private void findView() {
//		viewpager = (ViewPager) findViewById(R.id.cachepage);
//		btn_caching = findViewById(R.id.caching);
//		btn_cached = findViewById(R.id.cached);
//		btn_local = findViewById(R.id.local);
//		btn_local.setVisibility(View.GONE);
//		item_edit = findViewById(R.id.item_edit);
//		edit = findViewById(R.id.edit);
//		// allstart = findViewById(R.id.allstart);
//		delete = findViewById(R.id.delete);
//		complete = findViewById(R.id.complete);
//		edit_folder = findViewById(R.id.edit_folder);
//		// delete_folder = findViewById(R.id.delete_folder);
//		complete_folder = findViewById(R.id.complete_folder);
//		outer = findViewById(R.id.outer);
//		folder = findViewById(R.id.folder);
//		title_folder = (TextView) findViewById(R.id.title_folder);
//		mem_info = findViewById(R.id.mem_info);
//		
//		setClickListener();
//
////		btn_caching.setOnClickListener(this);
////		btn_cached.setOnClickListener(this);
////		btn_local.setOnClickListener(this);
////		edit.setOnClickListener(this);
////		// allstart.setOnClickListener(this);
////		delete.setOnClickListener(this);
////		complete.setOnClickListener(this);
////		edit_folder.setOnClickListener(this);
////		// delete_folder.setOnClickListener(this);
////		complete_folder.setOnClickListener(this);
//	}
//	
//	private void setClickListener(){
//		edit.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setEditViewState(true);
//				adapter.notifyData();
//			}
//		});
//		
//		edit_folder.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setEditViewState(true);
//				adapter.notifyData();
//			}
//		});
//		delete.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				adapter.deleteAll();
//			}
//		});
//		
//		complete.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setEditViewState(false);
//				adapter.notifyData();
//			}
//		});
//		
//		complete_folder.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setEditViewState(false);
//				adapter.notifyData();
//			}
//		});
//		
//		btn_caching.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				switchTab(0);
//			}
//		});
//		
//		btn_cached.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				switchTab(1);
//			}
//		});
//		
//		btn_local.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				switchTab(2);
//			}
//		});
//	}
//
////	@Override
////	public void onClick(View v) {
////		switch (v.getId()) {
////		case R.id.edit:// 编辑
////		case R.id.edit_folder:// 编辑
////			setEditViewState(true);
////			adapter.notifyData();
////			if (selectTab == 0) {
////
////			} else {
////
////			}
////			break;
////		case R.id.allstart:// 全部开始
////			break;
////		case R.id.delete:// 全部删除
////		case R.id.delete_folder:// 全部删除
////			adapter.deleteAll();
////			if (selectTab == 0) {
////			} else {
////			}
////			break;
////		case R.id.complete:// 完成
////		case R.id.complete_folder:// 完成
////			setEditViewState(false);
////			adapter.notifyData();
////			if (selectTab == 0) {
////			} else {
////			}
////			break;
////		case R.id.caching:// tab1
////			switchTab(0);
//////			setEditViewState(mIsEditState);
////			break;
////		case R.id.cached:// tab2
////			switchTab(1);
//////			setEditViewState(mIsEditState);
////			break;
////		case R.id.local:// tab3
////			switchTab(2);
////			break;
////		}
////	}
//
//	/** 设置编辑按钮的状态 */
//	public void setEditViewState(boolean editable) {
////		if (editable) {
////			edit.setVisibility(View.GONE);
////			delete.setVisibility(View.VISIBLE);
////			complete.setVisibility(View.VISIBLE);
////			edit_folder.setVisibility(View.GONE);
////			// delete_folder.setVisibility(View.VISIBLE);
////			complete_folder.setVisibility(View.VISIBLE);
////		} else {
////			edit.setVisibility(View.VISIBLE);
////			delete.setVisibility(View.GONE);
////			complete.setVisibility(View.GONE);
////			edit_folder.setVisibility(View.VISIBLE);
////			// delete_folder.setVisibility(View.GONE);
////			complete_folder.setVisibility(View.GONE);
////		}
////		if (selectTab == 2) {
////			edit.setVisibility(View.GONE);
////		}
//
//		// if (selectTab == 0) {
//		// if (editable) {
//		// allstart.setVisibility(View.GONE);
//		// } else {
//		// allstart.setVisibility(View.VISIBLE);
//		// }
//		// } else {
//		// allstart.setVisibility(View.GONE);
//		// }
//	}
//
//	/** 设置文件夹页面 */
//	public void setFolderState(boolean isInner, String showname) {
//		if (isInner) {
//			outer.setVisibility(View.GONE);
//			folder.setVisibility(View.VISIBLE);
//			this.showname = showname;
//			title_folder.requestFocus();
//			title_folder.setText(showname);
//			viewpager.setEnabled(false);
//		} else {
//			outer.setVisibility(View.VISIBLE);
//			folder.setVisibility(View.GONE);
//			viewpager.setEnabled(true);
//		}
//	}
//
//	/**
//	 * 选择viewpager 某个tab.
//	 * 
//	 * @param tab
//	 *            0 下载中 1 已完成2本地视频
//	 */
//	private void switchTab(int tab) {
//		switch (tab) {
//		case 0:
//			edit.setVisibility(View.VISIBLE);
//			btn_caching.setSelected(true);
//			btn_cached.setSelected(false);
//			btn_local.setSelected(false);
//			btn_caching.setEnabled(false);
//			btn_local.setEnabled(true);
//			btn_cached.setEnabled(true);
//			break;
//		case 1:
//			edit.setVisibility(View.VISIBLE);
//			btn_caching.setSelected(false);
//			btn_cached.setSelected(true);
//			btn_local.setSelected(false);
//			btn_caching.setEnabled(true);
//			btn_local.setEnabled(true);
//			btn_cached.setEnabled(false);
//			break;
//		case 2:
//			edit.setVisibility(View.GONE);
//			btn_caching.setSelected(false);
//			btn_cached.setSelected(false);
//			btn_local.setSelected(true);
//			btn_local.setEnabled(false);
//			btn_cached.setEnabled(true);
//			btn_caching.setEnabled(true);
//			break;
//		}
//
//		viewpager.setCurrentItem(tab);
//	}
//
//	private int progress, secondaryProgress;
//	private String Total_mem, Other_used, Download_used, Free_mem;
//
//	/**
//	 * TODO 设置初始SD卡空间进度
//	 */
//	private void setDefauleProgressValues(Activity v) {
//		if (v == null || Total_mem == null)
//			return;
//		ProgressBar mProgressBar = (ProgressBar) v.findViewById(R.id.mem_used);
//		TextView mTotal_mem = (TextView) v.findViewById(R.id.total_mem);
//		TextView mOther_used = (TextView) v.findViewById(R.id.other_used);
//		TextView mDownload_used = (TextView) v.findViewById(R.id.download_used);
//		TextView mFree_mem = (TextView) v.findViewById(R.id.free_mem);
//		mProgressBar.setProgress(progress);
//		mProgressBar.setSecondaryProgress(secondaryProgress);
//		mTotal_mem.setText(Total_mem);
//		mOther_used.setText(Other_used);
//		mDownload_used.setText(Download_used);
//		mFree_mem.setText(Free_mem);
//	}
//
//	/**
//	 * TODO 设置SD卡空间进度
//	 */
//	public void setProgressValues(Activity v) {
//		if (v == null)
//			return;
//		ProgressBar mProgressBar = (ProgressBar) v.findViewById(R.id.mem_used);
//		TextView mTotal_mem = (TextView) v.findViewById(R.id.total_mem);
//		TextView mOther_used = (TextView) v.findViewById(R.id.other_used);
//		TextView mDownload_used = (TextView) v.findViewById(R.id.download_used);
//		TextView mFree_mem = (TextView) v.findViewById(R.id.free_mem);
//		mProgressBar.setMax(1000);
//		DownloadManager download = DownloadManager.getInstance();
//		SDCardManager m = new SDCardManager(
//				download.getCurrentDownloadSDCardPath());
//		if (!m.exist()) {// 无sd卡
//			progress = 0;
//			secondaryProgress = 0;
//			mProgressBar.setProgress(0);
//			mProgressBar.setSecondaryProgress(0);
//			Total_mem = null;
//			Other_used = null;
//			Download_used = null;
//			Free_mem = null;
//			mTotal_mem.setText(null);
//			mOther_used.setText(null);
//			mDownload_used.setText(null);
//			mFree_mem.setText(null);
//			return;
//		}
//
//		int mProgress = m.getOtherProgrss();
//		progress = mProgress;
//		mProgressBar.setProgress(mProgress);
//		mProgress += m.getYoukuProgrss();
//		secondaryProgress = mProgress;
//		mProgressBar.setSecondaryProgress(mProgress);
//		Total_mem = PlayerUtil.formatSize(m.getTotalSize());
//		Other_used = PlayerUtil.formatSize(m.getOtherSpace());
//		Download_used = PlayerUtil.formatSize(m.getYoukuVideoSpace());
//		Free_mem = PlayerUtil.formatSize(m.getFreeSize());
//		mTotal_mem.setText(Total_mem);
//		mOther_used.setText(Other_used);
//		mDownload_used.setText(Download_used);
//		mFree_mem.setText(Free_mem);
//	}
//
//	public void editShow() {
//		item_edit.setVisibility(View.VISIBLE);
//		mem_info.setVisibility(View.VISIBLE);
//	}
//
//	public void editGone() {
//		item_edit.setVisibility(View.GONE);
//		mem_info.setVisibility(View.GONE);
//	}
//
///*	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		if (null!=Youku.mAction_bars&&Youku.mAction_bars.size()>0) {
//			SortActionBars(menu);
//		}else {
//			if (android.os.Build.VERSION.SDK_INT >= 11) {
//				getMenuInflater().inflate(R.menu.home_mainpage, menu);
//			} else {
//				getMenuInflater().inflate(R.menu.home_lowversion, menu);
//			}
//		}
//		menu.findItem(R.id.menu_edit).setVisible(true);
//		setupSearchMenuItem(menu);
//		menu.removeItem(R.id.menu_download);
//		getSupportActionBar().setDisplayUseLogoEnabled(false);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		for (int j = 0; j < mMenuListId.length; j++) {
//			if (mMenuListId[j]!=R.id.menu_search&&mMenuListId[j]!=R.id.menu_edit) {
//				menu.removeItem(mMenuListId[j]);
//			}
//		}
//		return true;
//	}*/
//	public void  onMenuDeleteClick(){
//		
//		adapter.deleteSelected();
//		if (selectTab == 0) {
//		} else {
//		}
//	}
//	
//	public void onMenuEditClick() {
//		// TODO Auto-generated method stub
//		mIsEditState=true;
//		adapter.notifyData();
//		if (selectTab == 0) {
//
//		} else {
//
//		}
//	}
//	public void OnDestroyActionEvent() {
//		// TODO Auto-generated method stub
//		mIsEditState=false;
//		adapter.notifyData();
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
///*		switch (item.getItemId()) {
//		case android.R.id.home:
//			if (getIsSearchOpen()) {
//				closeSearchView();
//				return true;
//			}
//			back();
//			return true;
//		}*/
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onBackPressed() {
//		back();
//	}
//
//	/**
//	 * 点击返回按钮
//	 */
//	public void onBackClick(View v) {
//		back();
//	}
//
//	/**
//	 * 返回
//	 */
//	private void back() {
//		if ((viewpager.getCurrentItem() != 1)
//				|| (viewpager.getCurrentItem() == 1 && adapter.fragment_downloaded
//						.returnOuter() == false)) {
///*			if (HomePageActivity.isLocalMode) {
//				Intent intent = new Intent(this, HomePageActivity.class);
//				startActivity(intent);
//			}*/
//			finish();
//		}
//	}
//
//
//}
