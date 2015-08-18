///*
// * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
// * 
// * Email:qq81595157@126.com
// * 
// * PROPRIETARY/CONFIDENTIAL.
// */
//
//package com.youku.player.fragment;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;
//import android.annotation.SuppressLint;
//import android.annotation.TargetApi;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.content.DialogInterface;
//import android.content.res.Configuration;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.GridView;
//import com.baseproject.utils.Util;
//import com.youku.player.YoukuPlayerApplication;
//import com.youku.player.adapter.CachingListAdapter;
//import com.youku.player.ui.R;
//import com.youku.player.ui.widget.YoukuLoading;
//import com.youku.service.download.DownloadInfo;
//import com.youku.service.download.DownloadManager;
//import com.youku.ui.activity.CachePageActivity;
//
///**
// * CachingFragment.缓存中的分页
// * 
// * @author 刘仲男 qq81595157@126.com
// * @version v3.5
// * @created time 2012-11-5 下午1:16:28
// */
//public class CachingFragment extends YoukuFragment {
//
//	private GridView gridView;
//	private DownloadManager download;
//
//	private CachingListAdapter adapter;
//	private ArrayList<DownloadInfo> downloadingList;
//	private ArrayList<DownloadInfo>  deleteDownloadingList_show=new ArrayList<DownloadInfo>();
//	/** 可编辑的 */
////	private boolean mEditable = false;
//	private boolean needwait = true;
//	/** 是否是横屏 */
//	private boolean isLand = false;
//	private static long time = 0l;
//
//	private Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			// downloadingList = download.getDownloadingList();
//			getData();
//			if (adapter == null) {
//				adapter = new CachingListAdapter(getActivity(),
//						downloadingList, gridView);
//				adapter.setdeleteDownloadingList_show(deleteDownloadingList_show);
////				adapter.setEdit(mEditable);
//				gridView.setAdapter(adapter);
//			} else {
//				adapter.setData(downloadingList);
//				adapter.setdeleteDownloadingList_show(deleteDownloadingList_show);
//				adapter.notifyDataSetChanged();
//			}
//		}
//
//	};
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		download = DownloadManager.getInstance();
//		View view = inflater.inflate(R.layout.fragment_cache, container, false);
//		gridView = (GridView) view.findViewById(R.id.gridview);
//		gridView.setOnItemClickListener(downloadOnItemClickListener);
//		gridView.setOnScrollListener(downloadOnScrollListener);
//		return view;
//	}
//
//	@Override
//	public void onActivityCreated(Bundle s) {
//		super.onActivityCreated(s);
//		if (s != null) {
//			if (s.containsKey("downloading_editable"))
////				mEditable = s.getBoolean("downloading_editable");
//			if (s.containsKey("downloading_needwait"))
//				needwait = s.getBoolean("downloading_needwait");
//		}
//		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			isLand = true;
//			gridView.setNumColumns(4);
//		} else {
//			isLand = false;
//			gridView.setNumColumns(2);
//		}
//	}
//
//	@Override
//	public void onResume() {
//		if (needwait) {
//			handler.sendEmptyMessageDelayed(0, 100L);
//			needwait = false;
//		} else
//			handler.sendEmptyMessage(0);
//		super.onResume();
////		setEditable(((CachePageActivity)getActivity()).ismIsEditState());
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
////		outState.putBoolean("downloading_editable", mEditable);
//		outState.putBoolean("downloading_needwait", needwait);
//		super.onSaveInstanceState(outState);
//	}
//
//	@SuppressWarnings("rawtypes")
//	private void getData() {
//		downloadingList=new ArrayList<DownloadInfo>();
//		DownloadManager manager = DownloadManager.getInstance();
//		Map<String, DownloadInfo> map = manager.getDownloadingData();
//		if (map == null) {
//			return;
//		}
//		Iterator iter = map.entrySet().iterator(); // 获得map的Iterator
//		while (iter.hasNext()) {
//			Entry entry = (Entry) iter.next();
//			downloadingList.add((DownloadInfo) entry.getValue());
//		}
//		DownloadInfo.compareBySeq = false;
//		Collections.sort(downloadingList);
//	}
//
//	public void setUpdate(DownloadInfo info) {
//		DownloadInfo infos = null;
//		for (int i = 0, n = downloadingList.size(); i < n; i++) {
//			infos = downloadingList.get(i);
//			if (info.taskId.equals(infos.taskId)) {
//				downloadingList.set(i, info);
//				break;
//			}
//		}
//		adapter.setUpdate(info);
//	}
//
//	public void notifyData() {
//		if (!CachePageActivity.mIsEditState) {
//			deleteDownloadingList_show.clear();
//			for (DownloadInfo item:downloadingList) {
//				item.iseditState=0;
//			}
//		}
//		if (adapter != null) {
//			adapter.notifyDataSetChanged();
//		}
//	}
//
//	/** 刷新数据及页面 */
//	public void refresh() {
//		handler.sendEmptyMessageDelayed(0, 500L);
//	}
//
//	private boolean editisshow = true;
//	private int tempItem = 0;
//	private OnScrollListener downloadOnScrollListener = new OnScrollListener() {
//
//		@Override
//		public void onScrollStateChanged(AbsListView view, int scrollState) {
//			// FIXME onScrollStateChanged
//
//		}
//
//		@Override
//		public void onScroll(AbsListView view, int firstVisibleItem,
//				int visibleItemCount, int totalItemCount) {
//			if (!YoukuPlayerApplication.isTablet && isLand) {
//				if (firstVisibleItem != tempItem) {
//					if (firstVisibleItem > tempItem && editisshow) {
//						((CachePageActivity) getActivity()).editGone();
//						editisshow = false;
//					} else if (firstVisibleItem < tempItem && !editisshow) {
//						((CachePageActivity) getActivity()).editShow();
//						editisshow = true;
//					}
//					tempItem = firstVisibleItem;
//				}
//			}
//		}
//	};
//	private OnItemClickListener downloadOnItemClickListener = new OnItemClickListener() {
//
//		@Override
//		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//				long arg3) {
//			if (downloadingList.size() - 1 < position)
//				return;
//			final String pageName = "缓存页-正在缓存页";
//			DownloadInfo info = downloadingList.get(position);
//			if (!CachePageActivity.mIsEditState) {// 非编辑状态时
//				int state = info.getState();
//				if (state == DownloadInfo.STATE_DOWNLOADING
//						|| state == DownloadInfo.STATE_WAITING
//						|| state == DownloadInfo.STATE_INIT
//						|| state == DownloadInfo.STATE_EXCEPTION) {
//					download.pauseDownload(info.taskId);
//				} else if (state == DownloadInfo.STATE_PAUSE) {
//					if (!Util.hasInternet()) {
//						YoukuPlayerApplication.showTips(R.string.tips_no_network);
//						return;
//					}
//					if (!Util.hasSDCard()) {
//						YoukuPlayerApplication.showTips(R.string.download_no_sdcard);
//						return;
//					}
//					if (Util.isWifi() == false
//							&& download.canUse3GDownload() == false) {
//						YoukuPlayerApplication.showTips(R.string.download_cannot_ues_3g);
//						return;
//					}
//					download.startDownload(info.taskId);
//				}
//			} else {// 编辑状态
////				delete(info);
//				if (info.iseditState==0) {
//					info.iseditState=1;
//					deleteDownloadingList_show.add(info);
//				}else {
//					info.iseditState=0;
//					for (DownloadInfo item:deleteDownloadingList_show) {
//						if (info.videoid.equals(item.videoid)) {
//							deleteDownloadingList_show.remove(item);
//						}
//					}
//				}
//				showTopbar_delete_icon();
//				adapter.notifyDataSetChanged();
//			}
//		}
//
//	};
//	void showTopbar_delete_icon(){
///*		if (deleteDownloadingList_show.size()>0) {
//			((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_icon);
//		}else {
//			((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_gray_icon);	
//		}*/
//	}
//
//	/** 删除选中 */
//	private void delete(final DownloadInfo info) {
////		final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
//			
//			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//			@SuppressLint("NewApi")
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				YoukuLoading.show(getActivity());
//				AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						return download.deleteDownloading(info.taskId);
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						YoukuLoading.dismiss();
//						CachePageActivity a = (CachePageActivity) getActivity();
//						if (a != null) {
//							a.setProgressValues(a);
//						}
//						handler.sendEmptyMessage(0);
//						super.onPostExecute(result);
//					}
//
//				};
//				if (Build.VERSION.SDK_INT >= 11)
//					my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							(Void[]) null);
//				else
//					my_task.execute((Void[]) null);
//				YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//			}
//		});
//		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		builder.setMessage(R.string.delete_my_tag_message);
//		builder.setTitle("删除缓存");
//		builder.create().show();
//	}
//
//	/** 删除所有 */
//	public void deleteAll() {
//		if (downloadingList == null || downloadingList.size() == 0) {
//			return;
//		}
//		long now = System.currentTimeMillis();
//		if ((now - time) < 1000l) {
//			return;
//		} else {
//			time = now;
//		}
////		final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
//			
//			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				YoukuLoading.show(getActivity());
//				AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						download.deleteAllDownloading();
//						return false;
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						YoukuLoading.dismiss();
//						CachePageActivity a = (CachePageActivity) getActivity();
//						if (a != null) {
//							a.setProgressValues(a);
//							if (adapter != null) {
//								adapter.notifyDataSetChanged();
//							}
//							a.setEditViewState(false);
//						}
//						handler.sendEmptyMessage(0);
//						super.onPostExecute(result);
//					}
//
//				};
//				if (Build.VERSION.SDK_INT >= 11)
//					my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							(Void[]) null);
//				else
//					my_task.execute((Void[]) null);
//				YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//			}
//		});
//		builder.setNegativeButton(R.string.cancel, null);
//
//		builder.setMessage("您确定要全部删除吗?");
//		builder.setTitle("删除缓存");
//		builder.create().show();
//	}
//	/** 删除选中 */
//	private void delete() {
////		final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
//			
//			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//			@SuppressLint("NewApi")
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				YoukuLoading.show(getActivity());
//				AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						for (int i = 0; i < deleteDownloadingList_show.size(); i++) {
//							DownloadInfo info=deleteDownloadingList_show.get(i);
//							 download.deleteDownloading(info.taskId);
//						}
//						deleteDownloadingList_show.clear();
//						return true;
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						YoukuLoading.dismiss();
//						CachePageActivity a = (CachePageActivity) getActivity();
//						if (a != null) {
//							a.setProgressValues(a);
//						}
//						showTopbar_delete_icon();
//						handler.sendEmptyMessage(0);
//						super.onPostExecute(result);
//					}
//
//				};
//				if (Build.VERSION.SDK_INT >= 11)
//					my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							(Void[]) null);
//				else
//					my_task.execute((Void[]) null);
//				YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//			}
//		});
//		builder.setNegativeButton(R.string.cancel, null);
//		
//		builder.setMessage(R.string.delete_my_tag_message);
//		builder.setTitle("删除缓存");
//		builder.create().show();
//	}
//	public void deleteSelected() {
//		// TODO Auto-generated method stub
//		if (null==deleteDownloadingList_show||deleteDownloadingList_show.size()==0) {
//			return;
//		}
//		delete();
//	}
//
//}
