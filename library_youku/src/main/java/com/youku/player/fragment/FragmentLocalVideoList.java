/*
 * Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.youku.player.fragment;
/**
 * 缓存页中本地扫描视频类
 * 主要功能：
 * 1、调用youkupalyer中的扫描程序，扫描设备中视频显示
 * 2、读取本地存储的扫描视频播放历史显示
 * 3、点击视频进入详情页播放视频
 * */

public class FragmentLocalVideoList extends YoukuFragment {
//	private static ArrayList<Media> localVideoInfos = new ArrayList<Media>();
//	private GridView gridView;
//	public final int MSG_FIND_VIDEO_COMPLETED = 0;
//	public final int MSG_GET_PROGRESS_COMPLETED = 1;
//	public final int MSG_GET_REFRESH_ADAPTER = 4;
//	public final int MSG_SELECT_VIDEO_PLAY = 3;
//	private LocalVideoAdapter localVideoAdapter;
//	private final String TAG = "FragmentLocalVideoList";
//	private Context context;
//	private boolean needRefreshHistory = false;
//	private TextView tipsTextView;
//	private int selPositon = 0;// 记录gridview位置
//	private Scanner scanner;
//	private boolean needStop= false;
//	private List<Media> deleteLocalVideoInfos=new ArrayList<Media>();
//	@Override
//	public void onResume() {
//		needStop=false;
//		super.onResume();
//		Logger.e(TAG, "onResume()");
//		if (null != gridView && null != localVideoAdapter) {
//			gridView.setAdapter(localVideoAdapter);
//			gridView.setSelection(selPositon);
//		}
//		if (needRefreshHistory)// 需要刷新播放历史
//		{
//			getItemHistory();
//		}
//	}
//
//	@Override
//	public void onHiddenChanged(boolean hidden) {
//		// TODO Auto-generated method stub
//		super.onHiddenChanged(hidden);
//	}
//
//	/**
//	 * 获取下一个视频
//	 * */
//	public static Media getNextVideo(String path) {
//		if (null == localVideoInfos || localVideoInfos.isEmpty())
//			return null;
//		for (int i = 0; i < localVideoInfos.size() - 1; i++) {
//			Media media = localVideoInfos.get(i);
//			if (null != media) {
//				if (media.getLocation().equals(path)) {
//					return localVideoInfos.get(i + 1);
//				}
//			}
//		}
//		return null;
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		context = getActivity();
//		Logger.e(TAG, "onCreate()");
//		setRetainInstance(true);
//		initScanner();
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		Logger.e(TAG, "onCreateView()");
//		View view = inflater.inflate(R.layout.fragment_cache, container, false);
//		gridView = (GridView) view.findViewById(R.id.gridview);
//		int numColumns = getResources().getInteger(R.integer.local_video_numColumns);
//		gridView.setNumColumns(numColumns);
//		gridView.setOnItemClickListener(onVideoClickListener);
//		tipsTextView = (TextView) view.findViewById(R.id.tv_local_tips);
//		return view;
//	}
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		Logger.e(TAG, "onActivityCreated()");
//		context = getActivity();
//		// if(null!=savedInstanceState)
//		// {
//		// localVideoInfos =
//		// savedInstanceState.getParcelableArrayList("videoList");
//		// }
//		init();
//	}
//
//	@Override
//	public void onDestroy() {
//		needStop=true;
//		super.onDestroy();
//		Logger.e(TAG, "onDestroy()");
//		localVideoAdapter = null;
//		if (null != scanner)
//			scanner.clearListener();
//		scanner = null;
//		if (null != mHandler)
//			mHandler.removeCallbacksAndMessages(null);
//		mHandler = null;
//		if (null != getPlayHistoryThread)
//			getPlayHistoryThread.interrupt();
//		getPlayHistoryThread = null;
//		localVideoInfos = null;
//	}
//
//	@Override
//	public void onPause() {
//		needStop=true;
//		selPositon = gridView.getFirstVisiblePosition();
//		super.onPause();
//		Logger.e(TAG, "onPause()");
//
//	}
//
//	/**
//	 * 初始化扫描器
//	 * */
//	private void initScanner() {
//		scanner = Scanner.getInstance(context);
//		scanner.setScanListener(new IScanListener() {
//
//			@Override
//			public void onThumbnailUpdate(Media media) {
//				localVideoAdapter.notifyDataSetChanged();
//			}
//
//			/*
//			 * 本地视频扫描完成
//			 * */
//			@Override
//			public void onScanStop(List<Media> list) {
//				endtime = System.currentTimeMillis();
//				//Log.e(TAG, (endtime) + "------endtime---");
//				//Log.e(TAG, (endtime - startime) / 1000 + "S------耗时---");
//				dismissScanning();
//				if (null == list || list.isEmpty()) {
//					setEmptyView();
//					return;
//				}
//				Collections.sort(list);
//				localVideoInfos = (ArrayList<Media>) list;
//				localVideoAdapter.setVideoInfos(localVideoInfos);
//				getItemHistory();
//
//			}
//
//			@Override
//			public void onScanStart() {
//
//			}
//
//			@Override
//			public void onItemAdded(int count, int total) {
//				// TODO Auto-generated method stub
//
//			}
//		}, mHandler);
//		Thumbnailer.setThumbnailSize(160, 90);
//	}
//
//	private void init() {
//		if (null == localVideoAdapter)
//			localVideoAdapter = new LocalVideoAdapter(context, localVideoInfos);
//		if (null != gridView)
//			gridView.setAdapter(localVideoAdapter);
//		localVideoAdapter.setDeleteLocalVideoInfos(deleteLocalVideoInfos);
//		startime = System.currentTimeMillis();
//		//Log.e(TAG, (startime) + "------startime---");
//		if (null == localVideoInfos || localVideoInfos.isEmpty()) {
//			showScanning();
//			scanner.loadMediaItems();
//		}
//		gridView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				if (CachePageActivity.mIsEditState) {
//					if (deleteLocalVideoInfos.contains(localVideoInfos.get(arg2))) {
//						deleteLocalVideoInfos.remove(localVideoInfos.get(arg2));
//					}else {
//						deleteLocalVideoInfos.add(localVideoInfos.get(arg2));
//					}
//					showTopbar_delete_icon();
//					localVideoAdapter.notifyDataSetChanged();
//				}
//			}
//		});
//	}
//
//	long startime, endtime;
//	private Media selVideoMedia = null;
//	/**
//	 * 点击视频去详情页播放
//	 * */
//	private OnItemClickListener onVideoClickListener = new OnItemClickListener() {
//
//		@Override
//		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//				long arg3) {
//			needRefreshHistory = true;
//			selVideoMedia = (Media) (localVideoAdapter.getItem(arg2));
//			itemClickUserAction();
//			IStaticsManager.localVideoClick(arg2+1, selVideoMedia.getTitle());
//		}
//	};
//
//	/**
//	 * 用户点击事件处理，延时500ms除去快速点击
//	 * */
//	private void itemClickUserAction() {
//
//		if (null != mHandler) {
//			mHandler.removeCallbacks(mItemClickRunnable);
//			mHandler.postDelayed(mItemClickRunnable, 500);
//		}
//
//	}
//
//	private Runnable mItemClickRunnable = new Runnable() {
//
//		@Override
//		public void run() {
//			if (null != mHandler) {
//				if (null != selVideoMedia) {
//					Message message = Message.obtain();
//					message.what = MSG_SELECT_VIDEO_PLAY;
//					message.obj = selVideoMedia;
//					mHandler.sendMessage(message);
//					Logger.e(TAG, "detailContentHandler.sendMessageDelayed");
//				}
//				// detailContentHandler.postDelayed(r, delayMillis);
//			}
//		}
//	};
//
//	@Override
//	public void onSaveInstanceState(Bundle s) {
//		super.onSaveInstanceState(s);
//		Logger.e(TAG, "onSaveInstanceState()");
//		// s.putParcelableArrayList("videoList", localVideoInfos);
//	}
//
//	/**
//	 * 显示正在扫描
//	 * */
//	private void showScanning() {
//		if (null != tipsTextView) {
//			tipsTextView.setVisibility(View.VISIBLE);
//			tipsTextView.setText("正在为您扫描本地视频，请稍后");
//		}
//		if (null != gridView) {
//			gridView.setVisibility(View.GONE);
//		}
//	}
//
//	/**
//	 * 隐藏正在扫描
//	 * */
//	private void dismissScanning() {
//		if (null != tipsTextView) {
//			tipsTextView.setVisibility(View.GONE);
//		}
//		if (null != gridView) {
//			gridView.setVisibility(View.VISIBLE);
//		}
//	}
//
//	/**
//	 * 没有找到本地视频
//	 * */
//	private void setEmptyView() {
//		if (null != tipsTextView) {
//			tipsTextView.setVisibility(View.VISIBLE);
//			tipsTextView.setText("本地木有任何视频");
//		}
//		if (null != gridView) {
//			gridView.setVisibility(View.GONE);
//		}
//	}
//
//	private long[] progress;// 临时存储播放历史
//	private Thread getPlayHistoryThread = null;
//
//	/**
//	 * 获取播放历史
//	 * */
//	private void getItemHistory() {
//		if (needStop)
//			return;
//		if (null == localVideoInfos || localVideoInfos.isEmpty())
//			return;
//		progress = new long[localVideoInfos.size()];
//		if (null == getPlayHistoryThread
//				|| getPlayHistoryThread.getState() == State.TERMINATED) {
//			getPlayHistoryThread = new Thread(new GetItemHistoryRunnable());
//		}
//		if(needStop) return;
//		getPlayHistoryThread.start();
//	}
//
//	private class GetItemHistoryRunnable implements Runnable {
//
//		@Override
//		public void run() {
//			if(needStop) return;
//			if (null == localVideoInfos)
//				return;
//			for (int i = 0; i < localVideoInfos.size(); i++) {
//				if(needStop) return;
//				if (null == localVideoInfos.get(i))
//					return;
//				progress[i] = SQLiteManager
//						.getLocalVideoPlayProgress(localVideoInfos.get(i)
//								.getLocation());
//			}
//			if (null != mHandler) {
//				Message message = Message.obtain();
//				message.what = MSG_GET_PROGRESS_COMPLETED;
//				mHandler.sendMessage(message);
//			}
//		}
//
//	};
//
//	private Handler mHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_GET_REFRESH_ADAPTER:
//				YoukuLoading.dismiss();
//				localVideoInfos.removeAll(deleteLocalVideoInfos);
//				deleteLocalVideoInfos.clear();
//				localVideoAdapter.setVideoInfos(localVideoInfos);
//				showTopbar_delete_icon();
//				localVideoAdapter.notifyDataSetChanged();
//				break;
//			case MSG_GET_PROGRESS_COMPLETED:
//				if (null == localVideoInfos || localVideoInfos.isEmpty())
//					return;
//				for (int i = 0; i < progress.length; i++) {
//					if (null == localVideoInfos.get(i))
//						return;
//					localVideoInfos.get(i).setProgress(progress[i]);
//				}
//				localVideoAdapter.notifyDataSetChanged();
//				if (null != gridView)
//					gridView.setSelection(selPositon);
//				needStop=false;
//				break;
//			case MSG_SELECT_VIDEO_PLAY:
//				Intent detailIntent = new Intent(context, DetailActivity.class);
//				detailIntent.putExtra("videoPath", selVideoMedia.getLocation());
//				if (selVideoMedia.getProgress() > selVideoMedia.getDuration() / 1000 - 60) {
//					detailIntent.putExtra("point", 0);
//				} else {
//					detailIntent.putExtra("point",
//							(int) selVideoMedia.getProgress() * 1000);
//				}
//
//				detailIntent.putExtra("title", selVideoMedia.getTitle());
//				detailIntent.putExtra("isfromLocalVideo", true);
//				startActivity(detailIntent);
//				break;
//
//			default:
//				break;
//			}
//		}
//
//	};
//	private void  delete(final List<Media> videoInfos){
//		
//		new Thread() {
//			public void run() {
//				for (Media info : videoInfos) {
//					YoukuUtil.deleteFile(new File(info.getLocation()));
//				}
//			};
//		}.start();
//	}
//	/** 删除选中 */
//	private void delete() {
//		final YoukuDialog d = new YoukuDialog(getActivity(), TYPE.normal);
//		d.setNormalNegtiveBtn(R.string.confirm, new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				d.dismiss();
//				YoukuLoading.show(getActivity());
//				AsyncTask<Void, Void, Boolean> my_task = new AsyncTask<Void, Void, Boolean>() {
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						delete(deleteLocalVideoInfos);
//					return true;
//					}
//
//					@Override
//					protected void onPostExecute(Boolean result) {
//						if (result) {
//							CachePageActivity a = (CachePageActivity) getActivity();
//							if (a != null) {
//								a.setProgressValues(a);
//							}
//							mHandler.sendEmptyMessageDelayed(MSG_GET_REFRESH_ADAPTER, 500l);
//						} else {
//						}
//						super.onPostExecute(result);
//					}
//
//				};
//				if (Build.VERSION.SDK_INT >= 11)
//					my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							(Void[]) null);
//				else
//					my_task.execute((Void[]) null);
//				Youku.isMyYoukuNeedRefresh = true;
//			}
//		});
//		d.setNormalPositiveBtn(R.string.cancel, new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				d.dismiss();
//			}
//		});
//		d.setMessage(R.string.delete_my_tag_message);
//		d.setTitle("删除缓存");
//		d.show();
//	}
//	public void deleteSelected() {
//		// TODO Auto-generated method stub
//		if (null==deleteLocalVideoInfos||deleteLocalVideoInfos.size()==0) {
//			return;
//		}
//		delete();
//	}
//	void showTopbar_delete_icon(){
//		if (deleteLocalVideoInfos.size()>0) {
//			((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_icon);
//		}else {
//			((BaseActivity)getActivity()).getEditModeDeleteMenu().setIcon(R.drawable.topbar_delete_gray_icon);	
//		}
//	}
//	public void notifyData() {
//		// TODO Auto-generated method stub
//		if (!CachePageActivity.mIsEditState) {
//			deleteLocalVideoInfos.clear();
//		}
//		if (localVideoAdapter != null) {
//			localVideoAdapter.notifyDataSetChanged();
//		}
//	}

}
