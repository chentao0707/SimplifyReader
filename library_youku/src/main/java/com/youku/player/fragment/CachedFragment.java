///*
//* Copyright © 2012-2013 LiuZhongnan. All rights reserved.
//*
//* Email:qq81595157@126.com
//*
//* PROPRIETARY/CONFIDENTIAL.
//*/
//
//package com.youku.player.fragment;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map.Entry;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.res.Configuration;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.GridView;
//
//import com.baseproject.utils.Logger;
//import com.youku.player.YoukuPlayerApplication;
//import com.youku.player.adapter.CachedListAdapter;
//import com.youku.player.ui.R;
//import com.youku.player.ui.widget.YoukuLoading;
//import com.youku.player.util.DetailUtil;
//import com.youku.service.download.DownloadInfo;
//import com.youku.service.download.DownloadManager;
//import com.youku.service.download.DownloadUtils;
//import com.youku.service.download.IDownload;
//import com.youku.ui.activity.CachePageActivity;
//
///**
//* CachedFragment.缓存完成的分页
//*
//* @author 刘仲男 qq81595157@126.com
//* @version v3.5
//* @created time 2012-11-5 下午1:16:02
//*/
//@SuppressLint("NewApi")
//public class CachedFragment extends YoukuFragment {
//
//	private GridView gridView;
//	private DownloadManager download;
//	private CachedListAdapter adapter;
//	/** 用于当前显示的下载列表 */
//	private ArrayList<DownloadInfo> downloadedList_show = new ArrayList<DownloadInfo>();
//	private ArrayList<DownloadInfo> deleteDownloadedList_show = new ArrayList<DownloadInfo>();
//	/** showid为key的Map，用于存储分类文件夹的内容 */
//	private HashMap<String, ArrayList<DownloadInfo>> downloadedList_Map = new HashMap<String, ArrayList<DownloadInfo>>();
//	/** 可编辑的 */
//	private boolean editable2 = false;
//	/** 是否在文件夹内 */
//	private boolean isInner = false;
//	private String videoid, showId, showName, cats;
//	private int showepisode_total;// 总集数
//	private int showItemNum = 0;// show所在item的位置
//	private int videoType;
//	private boolean needwait = true;
//	/** 是否是横屏 */
//	private boolean isLand = false;
//	private static long time = 0l;
//	private static long time2 = 0l;
//
//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			initData();
//			if (adapter == null) {
//				adapter = new CachedListAdapter(YoukuPlayerApplication.context,
//						downloadedList_show, videoid, showId, showName, cats,
//						videoType, showepisode_total, gridView);
//				// adapter.setEdit(editable);
//				adapter.setInner(isInner);
//				gridView.setAdapter(adapter);
//				// setEditable(((CachePageActivity)getActivity()).ismIsEditState());
//			} else {
//				adapter.setData(downloadedList_show, videoid, showId, showName,
//						cats, videoType, showepisode_total);
//				adapter.notifyDataSetChanged();
//				// setEditable(((CachePageActivity)getActivity()).ismIsEditState());
//			}
//			// YoukuUtil.showTips(""+((CachePageActivity)getActivity()).ismIsEditState());
//			YoukuLoading.dismiss();
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
//		// TODO Auto-generated method stub
//		super.onActivityCreated(s);
//		if (s != null) {
//			if (s.containsKey("downloaded_editable"))
//				// editable = s.getBoolean("downloaded_editable");
//				if (s.containsKey("downloaded_isInner"))
//					isInner = s.getBoolean("downloaded_isInner");
//			if (s.containsKey("downloaded_showId"))
//				showId = s.getString("downloaded_showId");
//			if (s.containsKey("downloaded_showName"))
//				showName = s.getString("downloaded_showName");
//			if (s.containsKey("downloaded_videoid"))
//				videoid = s.getString("downloaded_videoid");
//			if (s.containsKey("downloaded_cats"))
//				cats = s.getString("downloaded_cats");
//			if (s.containsKey("downloaded_videoType"))
//				videoType = s.getInt("downloaded_videoType");
//			if (s.containsKey("downloaded_showepisode_total"))
//				showepisode_total = s.getInt("downloaded_showepisode_total");
//			if (s.containsKey("downloaded_needwait"))
//				needwait = s.getBoolean("downloaded_needwait");
//			if (s.containsKey("showItemNum"))
//				showItemNum = s.getInt("showItemNum");
//		}
//		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			gridView.setNumColumns(4);
//		} else {
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
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle s) {
//		// s.putBoolean("downloaded_editable", editable);
//		s.putBoolean("downloaded_isInner", isInner);
//		s.putString("downloaded_showId", showId);
//		s.putString("downloaded_showName", showName);
//		if (adapter != null) {
//			videoid = adapter.getVideoId();
//			s.putString("downloaded_videoid", videoid);
//			cats = adapter.getCats();
//			s.putString("downloaded_cats", cats);
//			videoType = adapter.getVideoType();
//			s.putInt("downloaded_videoType", videoType);
//			showepisode_total = adapter.getTotal();
//			s.putInt("downloaded_showepisode_total", showepisode_total);
//		}
//		s.putBoolean("downloaded_needwait", needwait);
//		s.putInt("showItemNum", showItemNum);
//		super.onSaveInstanceState(s);
//	}
//
//	@SuppressWarnings("rawtypes")
//	private void initData() {
//		Iterator iter = download.getDownloadedData().entrySet().iterator(); // 获得map的Iterator
//		downloadedList_show.clear();
//		downloadedList_Map.clear();
//		while (iter.hasNext()) {
//			Entry entry = (Entry) iter.next();
//			DownloadInfo info = (DownloadInfo) entry.getValue();
//			String showId = info.showid;
//			if (info.isSeries()) {
//				if (downloadedList_Map.containsKey(showId)) {
//					downloadedList_Map.get(showId).add(info);
//				} else {
//					ArrayList<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
//					downloadInfos.add(info);
//					downloadedList_Map.put(showId, downloadInfos);
//					downloadedList_show.add(info);
//				}
//			} else {
//				downloadedList_show.add(info);
//			}
//		}
//		if (isInner) {
//			setInnerData();
//		} else {
//			DownloadInfo.compareBySeq = false;
//			Collections.sort(downloadedList_show);
//		}
//	}
//
//	private void setInnerData() {
//		showItemNum = 0;
//		if (downloadedList_Map.get(showId) == null) {
//			downloadedList_show.clear();
//		} else {
//			for (int i = 0, n = downloadedList_show.size(); i < n; i++) {
//				if (showId.equals(downloadedList_show.get(i).showid)) {
//					showItemNum = i + 1;
//					break;
//				}
//			}
//			downloadedList_show = downloadedList_Map.get(showId);
//			showName = downloadedList_show.get(0).showname;
//			DownloadInfo.compareBySeq = true;
//			Collections.sort(downloadedList_show);
//		}
//		CachePageActivity a = (CachePageActivity) getActivity();
//		if (a != null) {
//			a.setFolderState(isInner, showName);
//		}
//	}
//
//	public void notifyData() {
//		if (CachePageActivity.mIsEditState == false) {
//			deleteDownloadedList_show.clear();
//			for (DownloadInfo item : downloadedList_show) {
//				item.iseditState = 0;
//			}
//		}
//		if (adapter != null) {
//			adapter.notifyDataSetChanged();
//		}
//	}
//
//	// public boolean getEditable() {
//	// return editable;
//	// }
//
//	public boolean getIsInner() {
//		return isInner;
//	}
//
//	/** 刷新数据及页面 */
//	public void refresh() {
//		handler.sendEmptyMessageDelayed(0, 500L);
//	}
//
//	private void refreshAdapter() {
//		if (adapter != null) {
//			adapter.setData(downloadedList_show, videoid, showId, showName,
//					cats, videoType, showepisode_total);
//			adapter.setInner(isInner);
//			// editable = false;
//			// adapter.setEdit(editable);
//			// setEditable(((CachePageActivity)getActivity()).ismIsEditState());
//			// YoukuUtil.showTips(""+((CachePageActivity)getActivity()).ismIsEditState());
//			gridView.setAdapter(adapter);
//		}
//	}
//
//	/** 进入文件夹内 */
//	private void goInner(String showId) {
//		if (isInner) {
//			return;
//		}
//		isInner = true;
//		this.showId = showId;
//		setInnerData();
//		refreshAdapter();
//	}
//
//	/**
//	 * TODO 从文件夹内返回
//	 *
//	 * @return 是否成功
//	 */
//	public boolean returnOuter() {
//		if (!isInner) {
//			return false;
//		}
//		isInner = false;
//		((CachePageActivity) getActivity()).setFolderState(isInner, null);
//		initData();
//		refreshAdapter();
//		gridView.setSelection(scrollPos);
//		((CachePageActivity) getActivity()).setEditViewState(false);
//		return true;
//	}
//
//	/** 删除选中 */
//	@SuppressWarnings("unused")
//	private void delete(final DownloadInfo info) {
//		// final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						YoukuLoading.show(getActivity());
//						AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//							@Override
//							protected Boolean doInBackground(Void... params) {
//								if (isInner || !info.isSeries()) {
//									return download.deleteDownloaded(info);
//								} else {
//									return download
//											.deleteDownloadeds(downloadedList_Map
//													.get(info.showid));
//								}
//							}
//
//							@Override
//							protected void onPostExecute(Boolean result) {
//								YoukuLoading.dismiss();
//								if (result) {
//									CachePageActivity a = (CachePageActivity) getActivity();
//									if (a != null) {
//										a.setProgressValues(a);
//									}
//									handler.sendEmptyMessageDelayed(0, 500l);
//								} else {
//								}
//								super.onPostExecute(result);
//							}
//
//						};
//						if (Build.VERSION.SDK_INT >= 11)
//							my_task.executeOnExecutor(
//									AsyncTask.THREAD_POOL_EXECUTOR,
//									(Void[]) null);
//						else
//							my_task.execute((Void[]) null);
//						YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//
//					}
//				});
//		builder.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//					}
//				});
//
//		builder.setMessage(R.string.delete_my_tag_message).setTitle("删除缓存");
//		builder.create().show();
//	}
//
//	/** 删除所有 */
//	public void deleteAll() {
//		if (downloadedList_show == null || downloadedList_show.size() == 0) {
//			return;
//		}
//		long now = System.currentTimeMillis();
//		if ((now - time) < 1000l) {
//			return;
//		} else {
//			time = now;
//		}
//		// final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						YoukuLoading.show(getActivity());
//						AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//							@Override
//							protected Boolean doInBackground(Void... params) {
//								return download.deleteAllDownloaded();
//							}
//
//							@Override
//							protected void onPostExecute(Boolean result) {
//								YoukuLoading.dismiss();
//								CachePageActivity a = (CachePageActivity) getActivity();
//								if (a != null) {
//									a.setProgressValues(a);
//									// setEditable(false);
//									// setEditable(((CachePageActivity)getActivity()).ismIsEditState());
//									// a.setEditViewState(false);
//								}
//								handler.sendEmptyMessage(0);
//								super.onPostExecute(result);
//							}
//
//						};
//						if (Build.VERSION.SDK_INT >= 11)
//							my_task.executeOnExecutor(
//									AsyncTask.THREAD_POOL_EXECUTOR,
//									(Void[]) null);
//						else
//							my_task.execute((Void[]) null);
//						YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//					}
//
//				});
//
//		builder.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//
//					}
//				});
//		builder.setMessage("您确定要全部删除吗?");
//		builder.setTitle("删除缓存");
//		builder.create().show();
//	}
//
//	/** 删除选中 */
//	private void delete() {
//		// final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setPositiveButton(R.string.confirm,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						YoukuLoading.show(getActivity());
//						AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//							@Override
//							protected Boolean doInBackground(Void... params) {
//								for (int i = 0; i < deleteDownloadedList_show
//										.size(); i++) {
//									DownloadInfo info = deleteDownloadedList_show
//											.get(i);
//
//									if (isInner || !info.isSeries()) {
//										download.deleteDownloaded(info);
//									} else {
//										download.deleteDownloadeds(downloadedList_Map
//												.get(info.showid));
//									}
//								}
//								deleteDownloadedList_show.clear();
//								return true;
//							}
//
//							@Override
//							protected void onPostExecute(Boolean result) {
//								if (result) {
//									CachePageActivity a = (CachePageActivity) getActivity();
//									if (a != null) {
//										a.setProgressValues(a);
//									}
//									showTopbar_delete_icon();
//									handler.sendEmptyMessageDelayed(0, 500l);
//								} else {
//								}
//								super.onPostExecute(result);
//							}
//
//						};
//						if (Build.VERSION.SDK_INT >= 11)
//							my_task.executeOnExecutor(
//									AsyncTask.THREAD_POOL_EXECUTOR,
//									(Void[]) null);
//						else
//							my_task.execute((Void[]) null);
//						YoukuPlayerApplication.isMyYoukuNeedRefresh = true;
//					}
//				});
//
//		builder.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//
//					}
//				});
//
//		builder.setMessage(R.string.delete_my_tag_message);
//		builder.setTitle("删除缓存");
//		builder.create().show();
//	}
//
//	private boolean editisshow = true;
//	private int tempItem = 0;
//	private int scrollTop = 0, scrollPos = 0;
//	private OnScrollListener downloadOnScrollListener = new OnScrollListener() {
//
//		@Override
//		public void onScrollStateChanged(AbsListView view, int scrollState) {
//			// 不滚动时保存当前滚动到的位置
//			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//				// ListPos记录当前可见的List顶端的一行的位置
//				scrollPos = gridView.getFirstVisiblePosition();
//			}
//			if (downloadedList_show != null) {
//				View v = gridView.getChildAt(0);
//				scrollTop = (v == null) ? 0 : v.getTop();
//			}
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
//
//	private OnItemClickListener downloadOnItemClickListener = new OnItemClickListener() {
//
//		@Override
//		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//				long arg3) {
//			long now = System.currentTimeMillis();
//			if ((now - time2) < 500l) {
//				return;
//			} else {
//				time2 = now;
//			}
//			final String pageName = "缓存页-缓存完成页";
//			try {
//				DownloadInfo info;
//				if (!CachePageActivity.mIsEditState) {// 非编辑状态时
//					if (isInner) {
//						info = downloadedList_show.get(position - 1);
//					} else {
//						info = downloadedList_show.get(position);
//						if (info.isSeries()) {
//							goInner(info.showid);
//							return;
//						}
//					}
//
//					if (info.playTime == 0) {// new 未观看
//						goLocalPlayerWithPoint(getActivity(),
//								info.videoid, info.title, 0);
//					} else if (info.playTime > info.seconds - 60) {// 已看完，重播
//						goLocalPlayerWithPoint(getActivity(),
//								info.videoid, info.title, -1);
//					} else {// 未看完，续播
//						goLocalPlayerWithPoint(getActivity(),
//								info.videoid, info.title, info.playTime * 1000);
//					}
//					/*
//					 * if (isInner) {
//					 * IStaticsManager.cachedVideoClick(showItemNum, null,
//					 * info.showid); } else {
//					 * IStaticsManager.cachedVideoClick(position + 1,
//					 * info.videoid, null); }
//					 */
//				} else {// 编辑（可删除）状态时
//					if (isInner) {
//						info = downloadedList_show.get(position - 1);
//						if (info.iseditState == 0) {
//							info.iseditState = 1;
//							deleteDownloadedList_show.add(info);
//						} else {
//							info.iseditState = 0;
//							deleteDownloadedList_show.remove(info);
//						}
//						adapter.notifyDataSetChanged();
//						// delete(info);
//
//					} else {
//						info = downloadedList_show.get(position);
//						if (info.iseditState == 0) {
//							info.iseditState = 1;
//							deleteDownloadedList_show.add(info);
//						} else {
//							info.iseditState = 0;
//							deleteDownloadedList_show.remove(info);
//						}
//						showTopbar_delete_icon();
//						adapter.notifyDataSetChanged();
//						// delete(info);
//					}
//				}
//
//			} catch (Exception e) {
//				Logger.e("CachedFragment", e);
//				return;
//			}
//		}
//
//	};
//
//	public void deleteSelected() {
//		// TODO Auto-generated method stub
//		if (null == deleteDownloadedList_show
//				|| deleteDownloadedList_show.size() == 0) {
//			return;
//		}
//		delete();
//	}
//
//	void showTopbar_delete_icon() {
//		// if (deleteDownloadedList_show.size()>0) {
//		// ((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_icon);
//		// }else {
//		// ((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_gray_icon);
//		// }
//	}
//
//	public void goLocalPlayerWithPoint(Context context, String vid,
//			String title, int point) {
//		IDownload download = DownloadManager.getInstance();
//		DownloadInfo info = download.getDownloadInfo(vid);
//		if (info == null) {
//			return;
//		}
//		DownloadUtils.makeM3U8File(info);
//		if (info.format == DownloadInfo.FORMAT_HD2) {
//			for (int i = 0; i < info.segsSeconds.length; i++) {
//				File f = new File(info.savePath + (i + 1) + ".flv");
//				if (f.exists() && f.isFile()) {
//					f.renameTo(new File(info.savePath + (i + 1) + "."
//							+ DownloadInfo.FORMAT_POSTFIX[info.format]));
//				}
//			}
//		}
//	}
//}
