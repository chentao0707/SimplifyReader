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
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils.TruncateAt;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.TextView;
//
//import com.youku.player.YoukuPlayerApplication;
//import com.youku.player.ui.R;
//import com.youku.player.ui.widget.YoukuImageView;
//import com.youku.service.download.AsyncImageLoader;
//import com.youku.service.download.BaseDownload;
//import com.youku.service.download.DownloadInfo;
//import com.youku.service.download.DownloadUtils;
//import com.youku.ui.activity.CachePageActivity;
//
///**
// * CachingListAdapter.正在缓存中的适配器
// * 
// * @author 刘仲男 qq81595157@126.com
// * @version v3.5
// * @created time 2012-11-5 下午1:24:07
// */
//public class CachingListAdapter extends BaseAdapter {
//
//	private LayoutInflater mInflater;
//	private ArrayList<DownloadInfo> downloadinfoList;
//	private GridView gridView;
//	private AsyncImageLoader loader;
//	/** 是否可编辑的 */
////	private boolean editable = false;
//
//	public CachingListAdapter(Context context,
//			ArrayList<DownloadInfo> downloadinfoList, GridView gridView) {
//		if (context == null)
//			mInflater = LayoutInflater.from(YoukuPlayerApplication.context);
//		else
//			mInflater = LayoutInflater.from(context);
//		this.downloadinfoList = downloadinfoList;
//		this.gridView = gridView;
//		loader = AsyncImageLoader.getInstance();
//	}
//
//	public void setUpdate(DownloadInfo info) {
//		Message message = Message.obtain();
//		message.obj = info;
//		handler.sendMessage(message);
//	}
//
//	/** 重新装载数据 */
//	public void setData(ArrayList<DownloadInfo> downloadinfoList) {
//		this.downloadinfoList = downloadinfoList;
//	}
//
////	/** 设置是否是编辑状态 */
////	public void setEdit(boolean editable) {
////		this.editable = editable;
////	}
//
//	@Override
//	public int getCount() {
//		if (downloadinfoList != null)
//			return downloadinfoList.size();
//		return 0;
//	}
//
//	@Override
//	public Object getItem(int position) {
//		return position;
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@SuppressWarnings("deprecation")
//	@Override
//	public View getView(final int position, View convertView, ViewGroup parent) {
//		if (position > downloadinfoList.size() - 1)
//			return null;
//		final ViewHolder viewHolder;
//		if (convertView == null) {
//			convertView = mInflater.inflate(R.layout.grid_item_cache, null);
//			viewHolder = new ViewHolder();
//			viewHolder.thumbnail = (YoukuImageView) convertView
//					.findViewById(R.id.thumbnail);
//			viewHolder.btn_delete = convertView.findViewById(R.id.btn_delete);
//			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
//			viewHolder.title.setEllipsize(TruncateAt.END);
//			viewHolder.format = convertView.findViewById(R.id.format);
//			viewHolder.state = (TextView) convertView.findViewById(R.id.state);
//			viewHolder.progress = (TextView) convertView
//					.findViewById(R.id.progress);
//			viewHolder.llview=  convertView
//					.findViewById(R.id.llview);
//			convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//
//		final DownloadInfo info = downloadinfoList.get(position);
//		viewHolder.title.setText(info.title);
//		Drawable d = loader.loadDrawable(viewHolder.thumbnail, info.imgUrl,
//				info.savePath + BaseDownload.THUMBNAIL_NAME, info);
//		if (d == null) {
//			viewHolder.thumbnail.setBackgroundDrawable(null);
//		} else {
//			viewHolder.thumbnail.setBackgroundDrawable(d);
//		}
//		switch (info.format) {
//		case DownloadInfo.FORMAT_HD2:
//			viewHolder.format.setBackgroundResource(R.drawable.icon_hd2);
//			break;
//		case DownloadInfo.FORMAT_MP4:
//			viewHolder.format.setBackgroundResource(R.drawable.icon_hd);
//			break;
//		default:
//			viewHolder.format.setBackgroundDrawable(null);
//			break;
//		}
//		if (CachePageActivity.mIsEditState) {// 编辑状态下
//			if (info.iseditState==0) {
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.btn_delete.setVisibility(View.GONE);	
////				for (DownloadInfo i:deleteDownloadingList_show) {
////					if (i.videoid.equals(info.videoid)) {
////						viewHolder.state.setVisibility(View.GONE);
////						viewHolder.progress.setVisibility(View.GONE);
////						viewHolder.btn_delete.setVisibility(View.VISIBLE);
////					}
////					
////				}
//				viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//			}
//			else {
//				viewHolder.state.setVisibility(View.GONE);
//				viewHolder.progress.setVisibility(View.GONE);
//				viewHolder.btn_delete.setVisibility(View.VISIBLE);
//				viewHolder.llview.setBackgroundResource(R.drawable.delete_border);
//			}
//		} else {// 非编辑状态下
//			viewHolder.state.setVisibility(View.VISIBLE);
//			viewHolder.progress.setVisibility(View.VISIBLE);
//			viewHolder.btn_delete.setVisibility(View.GONE);
//			viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//			viewHolder.state.setTag("stateTextView" + info.videoid);
//			viewHolder.progress.setTag("progressTextView" + info.videoid);
//			setStateChange(viewHolder.state, viewHolder.progress, info);
//		}
//		return convertView;
//	}
//
//	/** 设置更新状态的改变 */
//	private void setStateChange(TextView state, TextView progress,
//			DownloadInfo info) {
//		switch (info.getState()) {
//		case DownloadInfo.STATE_DOWNLOADING:
//			state.setText(DownloadUtils.getProgress(info) + "%");
//			progress.setText(getProgress(info));
//			break;
//		case DownloadInfo.STATE_PAUSE:
//			state.setText(R.string.pause);
//			progress.setText(getProgress(info));
//			break;
//		case DownloadInfo.STATE_INIT:
//		case DownloadInfo.STATE_WAITING:
//		case DownloadInfo.STATE_EXCEPTION:
//			state.setText(R.string.wait);
//			progress.setText(getProgress(info));
//			break;
//		}
//	}
//
//	/** 格式化对象 */
//	private final DecimalFormat df = new DecimalFormat("0.#");
//
//	/** 得到下载大小 例如26M/60M */
//	private String getProgress(DownloadInfo info) {
//		final String totalSize = df.format(info.size / (1024 * 1024));
//		final String downloadedSize = df.format(info.downloadedSize
//				/ (1024 * 1024));
//		return downloadedSize + "M/" + totalSize + "M";
//	}
//
//	private Handler handler = new Handler() {
//		public void handleMessage(Message msg) {
//			DownloadInfo info = (DownloadInfo) msg.obj;
//			TextView stateText = (TextView) gridView
//					.findViewWithTag("stateTextView" + info.videoid);
//			TextView progressText = (TextView) gridView
//					.findViewWithTag("progressTextView" + info.videoid);
//			if (stateText == null || progressText == null)
//				return;
//			setStateChange(stateText, progressText, info);
//		}
//	};
//
//	class ViewHolder {
//		private YoukuImageView thumbnail;
//		private TextView title;
//		private View btn_delete;
//		private TextView state;
//		private TextView progress;
//		private View format;
//		private View llview;
//	}
//	ArrayList<DownloadInfo> deleteDownloadingList_show;
//	public void setdeleteDownloadingList_show(
//			ArrayList<DownloadInfo> deleteDownloadingList_show) {
//		// TODO Auto-generated method stub
//		this.deleteDownloadingList_show=deleteDownloadingList_show;
//	}
//}
