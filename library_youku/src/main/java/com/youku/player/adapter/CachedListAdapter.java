///*
// * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
// * 
// * Email:qq81595157@126.com
// * 
// * PROPRIETARY/CONFIDENTIAL.
// */
//
//package com.youku.player.adapter;
//
//import java.util.ArrayList;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.drawable.Drawable;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils.TruncateAt;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.baseproject.utils.Logger;
//import com.baseproject.utils.Util;
//import com.youku.player.YoukuPlayerApplication;
//import com.youku.player.ui.R;
//import com.youku.player.util.PlayerUtil;
//import com.youku.service.download.AsyncImageLoader;
//import com.youku.service.download.DownloadInfo;
//import com.youku.service.download.DownloadManager;
//import com.youku.service.download.DownloadUtils;
//import com.youku.ui.activity.CachePageActivity;
//import com.youku.ui.activity.CacheSeriesActivity;
//
///**
// * CachedListAdapter.缓存完成的适配器
// * 
// * @author 刘仲男 qq81595157@126.com
// * @version v3.5
// * @created time 2012-11-5 下午1:24:22
// */
//public class CachedListAdapter extends BaseAdapter {
//	private final String TAG = "CachedListAdapter";
//	private String pageName = "缓存页-缓存完成页";
//	private String videoid, showname, showid, cats;
//	private int videoType, size, showepisode_total;
//	private ArrayList<DownloadInfo> downloadedList_show;
//	private Context context;
//	/** 是否可编辑的 */
////	private boolean editable = false;
//	/** 是否在文件夹内 */
//	private boolean isInner = false;
//	private LayoutInflater inflater;
//	private AsyncImageLoader loader;
//	/** 添加按钮item */
//	private final int TYPE_0 = 0;
//	/** 缓存列表item */
//	private final int TYPE_1 = 1;
//	/** 文件夹item */
//	private final int TYPE_2 = 2;
//
//	private boolean lock = false;
//
//	private Handler handler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			notifyDataSetChanged();
//			super.handleMessage(msg);
//		}
//	};
//
//	public CachedListAdapter(Context context,
//			ArrayList<DownloadInfo> downloadedList_show, String videoId,
//			String showid, String showname, String cats, int videoType,
//			int showepisode_total, GridView gridView) {
//		if (context == null)
//			inflater = LayoutInflater.from(YoukuPlayerApplication.context);
//		else
//			inflater = LayoutInflater.from(context);
//		this.context = context;
//		this.downloadedList_show = downloadedList_show;
//		this.videoid = videoId;
//		this.showid = showid;
//		this.showname = showname;
//		this.cats = cats;
//		this.videoType = videoType;
//		this.showepisode_total = showepisode_total;
//		loader = AsyncImageLoader.getInstance();
//	}
//
//	/** 设置数据 */
//	public void setData(ArrayList<DownloadInfo> downloadedList_show,
//			String videoId, String showid, String showname, String cats,
//			int videoType, int showepisode_total) {
//		this.downloadedList_show = downloadedList_show;
//		this.videoid = videoId;
//		this.showid = showid;
//		this.showname = showname;
//		this.cats = cats;
//		this.videoType = videoType;
//		this.showepisode_total = showepisode_total;
//	}
//
//	/** 设置是否在文件夹内 */
//	public void setInner(boolean isInner) {
//		this.isInner = isInner;
//	}
//
////	/** 设置是否是编辑状态 */
////	public void setEdit(boolean editable) {
////		this.editable = editable;
////	}
//
//	/** 获得剧集总数 */
//	public int getTotal() {
//		return showepisode_total;
//	}
//
//	/** 获得视频id */
//	public String getVideoId() {
//		return videoid;
//	}
//
//	/** 获得视频类型 */
//	public String getCats() {
//		return cats;
//	}
//
//	/** 获得视频类型 */
//	public int getVideoType() {
//		return videoType;
//	}
//
//	@Override
//	public int getCount() {
//		if (downloadedList_show != null) {
//			size = downloadedList_show.size();
//			if (isInner) {
//				if (size > 0) {
//					videoid = downloadedList_show.get(0).videoid;
//					showname = downloadedList_show.get(0).showname;
//					showid = downloadedList_show.get(0).showid;
//					cats = downloadedList_show.get(0).cats;
//					showepisode_total = 0;
//					for (int i = 0; i < size; i++) {
//						if (showepisode_total < downloadedList_show.get(i).showepisode_total)
//							showepisode_total = downloadedList_show.get(i).showepisode_total;
//					}
//					videoType = DownloadInfo.getTypeId(cats, showepisode_total);
//					if (lock == false
//							&& (showepisode_total == 0 || cats == null)) {
//						lock = true;
//						new AsyncTask<Void, Void, Boolean>() {
//
//							@Override
//							protected Boolean doInBackground(Void... params) {
//								// FIXME doInBackground
//								try {
//									for (int i = 0; i < size; i++) {
//										DownloadInfo info = downloadedList_show
//												.get(i);
//										DownloadUtils.getVideoInfo(info);
//										DownloadUtils
//												.makeDownloadInfoFile(info);
//									}
//									cats = downloadedList_show.get(0).cats;
//									showepisode_total = downloadedList_show
//											.get(0).showepisode_total;
//									videoType = DownloadInfo.getTypeId(cats,
//											showepisode_total);
//									return true;
//								} catch (Exception e) {
//									Logger.e(TAG, "getVideoInfo", e);
//								}
//								return false;
//							}
//
//							@Override
//							protected void onPostExecute(Boolean result) {
//								// FIXME onPostExecute
//								if (result)
//									handler.sendEmptyMessageDelayed(0, 1000l);
//								lock = false;
//								super.onPostExecute(result);
//							}
//
//						}.execute();
//					}
//				}
//				return size + 1;
//			} else {
//				return size;
//			}
//		}
//		return 0;
//	}
//
//	@Override
//	public int getItemViewType(int position) {
//		// FIXME getItemViewType
//		if (isInner) {// 先判断是否在文件夹内
//			if (position == 0)
//				return TYPE_0;
//			else
//				return TYPE_1;
//		} else {// 不是文件夹时
//			if (downloadedList_show.get(position).isSeries())
//				return TYPE_2;
//			else
//				return TYPE_1;
//		}
//	}
//
//	@Override
//	public int getViewTypeCount() {
//		// FIXME getViewTypeCount
//		return 3;
//	}
//
//	@Override
//	public Object getItem(int position) {
//		// FIXME getItem
//		return position;
//	}
//
//	@Override
//	public long getItemId(int position) {
//		// FIXME getItemId
//		return position;
//	}
//
//	@Override
//	public View getView(final int position, View convertView, ViewGroup parent) {
//		ViewHolder0 viewHolder0 = null;
//		ViewHolder viewHolder = null;
//		int type = getItemViewType(position);
//		// 无convertView，需要new出各个控件
//		if (convertView == null) {
//			// 按当前所需的样式，确定new的布局
//			switch (type) {
//			case TYPE_0:
//				convertView = inflater.inflate(R.layout.grid_item_cache_add,
//						parent, false);
//				viewHolder0 = new ViewHolder0();
//				viewHolder0.title = (TextView) convertView
//						.findViewById(R.id.title);
//				viewHolder0.title.setEllipsize(TruncateAt.END);
//				viewHolder0.add = convertView.findViewById(R.id.add);
//				convertView.setTag(viewHolder0);
//				break;
//			case TYPE_1:
//				convertView = inflater.inflate(R.layout.grid_item_cache,
//						parent, false);
//				viewHolder = new ViewHolder();
//				viewHolder.thumbnail = (ImageView) convertView
//						.findViewById(R.id.thumbnail);
//				viewHolder.thumbnail_mask = convertView.findViewById(R.id.mask);
//				viewHolder.btn_delete = convertView
//						.findViewById(R.id.btn_delete);
//				viewHolder.title = (TextView) convertView
//						.findViewById(R.id.title);
//				// viewHolder.title.setEllipsize(TruncateAt.END);
//				viewHolder.state = (TextView) convertView
//						.findViewById(R.id.state);
//				viewHolder.progress = (TextView) convertView
//						.findViewById(R.id.progress);
//				viewHolder.format = convertView.findViewById(R.id.format);
//				viewHolder.llview=  convertView
//						.findViewById(R.id.llview);
//				convertView.setTag(viewHolder);
//				break;
//			case TYPE_2:
//				convertView = inflater.inflate(R.layout.grid_item_cache_folder,
//						parent, false);
//				viewHolder = new ViewHolder();
//				viewHolder.thumbnail = (ImageView) convertView
//						.findViewById(R.id.thumbnail);
//				viewHolder.thumbnail_mask = convertView.findViewById(R.id.mask);
//				viewHolder.btn_delete = convertView
//						.findViewById(R.id.btn_delete);
//				viewHolder.title = (TextView) convertView
//						.findViewById(R.id.title);
//				// viewHolder.title.setEllipsize(TruncateAt.END);
//				viewHolder.state = (TextView) convertView
//						.findViewById(R.id.state);
//				viewHolder.progress = (TextView) convertView
//						.findViewById(R.id.progress);
//				viewHolder.format = convertView.findViewById(R.id.format);
//				viewHolder.llview=  convertView
//						.findViewById(R.id.llview);
//				convertView.setTag(viewHolder);
//				break;
//			}
//		} else {
//			// 有convertView，按样式，取得不用的布局
//			switch (type) {
//			case TYPE_0:
//				viewHolder0 = (ViewHolder0) convertView.getTag();
//				break;
//			case TYPE_1:
//				viewHolder = (ViewHolder) convertView.getTag();
//				break;
//			case TYPE_2:
//				viewHolder = (ViewHolder) convertView.getTag();
//				break;
//			}
//		}
//		// 设置资源
//		switch (type) {
//		case TYPE_0:
//			setView(viewHolder0);
//			break;
//		case TYPE_1:
//			if (isInner) {
//				DownloadInfo info = downloadedList_show.get(position - 1);
//				setView(viewHolder, info, position - 1, type);
//			} else {
//				DownloadInfo info = downloadedList_show.get(position);
//				setView(viewHolder, info, position, type);
//			}
//			break;
//		case TYPE_2:
//			DownloadInfo info = downloadedList_show.get(position);
//			setView(viewHolder, info, position, type);
//			break;
//		}
//		return convertView;
//	}
//
//	/**
//	 * TODO 添加更多按钮
//	 * 
//	 * @param viewHolder
//	 */
//	private void setView(ViewHolder0 viewHolder) {
//		viewHolder.title.setText("已缓存完" + size + "集/共" + showepisode_total
//				+ "集");
//		viewHolder.add.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (!Util.hasInternet()) {
//					YoukuPlayerApplication.showTips(R.string.tips_no_network);
//					return;
//				}
//				Intent intent = new Intent(context, CacheSeriesActivity.class);
//				intent.putExtra("videoid", videoid);
//				intent.putExtra("showid", showid);
//				intent.putExtra("showname", showname);
//				intent.putExtra("cats", cats);
//				intent.putExtra("videoType", videoType);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				context.startActivity(intent);
//			}
//		});
//	}
//
//	@SuppressLint("ResourceAsColor")
//	@SuppressWarnings("deprecation")
//	private void setView(final ViewHolder viewHolder, final DownloadInfo info,
//			final int position, int type) {
//		if (isInner || type == TYPE_1) {
//			// viewHolder.title.setEllipsize(TruncateAt.MIDDLE);
//			viewHolder.title.setText(info.title);
//			switch (info.format) {
//			case DownloadInfo.FORMAT_HD2:
//				viewHolder.format.setBackgroundResource(R.drawable.icon_hd2);
//				break;
//			case DownloadInfo.FORMAT_MP4:
//				viewHolder.format.setBackgroundResource(R.drawable.icon_hd);
//				break;
//			default:
//				viewHolder.format.setBackgroundDrawable(null);
//				break;
//			}
//		} else {
//			// viewHolder.title.setEllipsize(TruncateAt.END);
//			viewHolder.title.setText(info.showname);
//			viewHolder.format.setBackgroundDrawable(null);
//		}
//		Drawable d = loader.loadDrawable(viewHolder.thumbnail, info.imgUrl,
//				info.savePath + DownloadManager.THUMBNAIL_NAME, info);
//		if (d == null) {
//			viewHolder.thumbnail.setBackgroundDrawable(null);
//		} else {
//			viewHolder.thumbnail.setBackgroundDrawable(d);
//		}
//		if (CachePageActivity.mIsEditState) {// 编辑状态下
//			if (info.iseditState==1) {
//				viewHolder.state.setVisibility(View.GONE);
//				viewHolder.progress.setVisibility(View.GONE);
//				viewHolder.thumbnail_mask.setVisibility(View.VISIBLE);
//				viewHolder.btn_delete.setVisibility(View.VISIBLE);
//				viewHolder.llview.setBackgroundResource(R.drawable.delete_border);
//			}else {
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.thumbnail_mask.setVisibility(View.GONE);
//				viewHolder.btn_delete.setVisibility(View.GONE);	
//				viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//			}
//		} else if (isInner || type == TYPE_1) {// 非编辑状态且文件夹里
//			viewHolder.btn_delete.setVisibility(View.GONE);
//			viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//			if (info.playTime == 0) {// new 未观看
//				viewHolder.thumbnail_mask.setVisibility(View.GONE);
//				viewHolder.state.setVisibility(View.GONE);
//				viewHolder.progress.setVisibility(View.GONE);
//			} else if (info.playTime > info.seconds - 60) {// 已看完，重播
//				viewHolder.thumbnail_mask.setVisibility(View.VISIBLE);
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.state.setText("重播");
//				viewHolder.progress.setText("已看完 / "
//						+ PlayerUtil.formatTimeForHistory(info.seconds));
//			} else {// 未看完，续播
//				viewHolder.thumbnail_mask.setVisibility(View.VISIBLE);
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.state.setText("续播");
//				viewHolder.progress.setText(PlayerUtil
//						.formatTimeForHistory(info.playTime)
//						+ " / "
//						+ PlayerUtil.formatTimeForHistory(info.seconds));
//			}
//		} else {// 非编辑状态
//			viewHolder.thumbnail_mask.setVisibility(View.GONE);
//			viewHolder.btn_delete.setVisibility(View.GONE);
//			viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//			viewHolder.state.setVisibility(View.GONE);
//			viewHolder.progress.setVisibility(View.GONE);
//		}
//
//	}
//
//	class ViewHolder {
//		/** 缩略图 */
//		private ImageView thumbnail;
//		/** 缩略图遮罩 */
//		private View thumbnail_mask;
//		private TextView title;
//		private View btn_delete;
//		private TextView state;
//		private TextView progress;
//		private View format;
//		private View llview;
//	}
//
//	class ViewHolder0 {
//		private TextView title;
//		private View add;
//	}
//
//}
