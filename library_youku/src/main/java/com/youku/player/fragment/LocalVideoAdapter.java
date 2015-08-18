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
 * 用于填充本地扫描视频
 * */

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LocalVideoAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}
//	private List<Media> localVideoInfos;
//	private List<Media> deleteLocalVideoInfos;
//	private Context context;
//	private LayoutInflater inflater;
//
//	public LocalVideoAdapter(Context mContext, List<Media> mLocalVideoInfos) {
//		super();
//		localVideoInfos = mLocalVideoInfos;
//		context = mContext;
//		inflater = LayoutInflater.from(context);
//	}
//public void setDeleteLocalVideoInfos(List<Media> deleteLocalVideoInfos){
//	this.deleteLocalVideoInfos=deleteLocalVideoInfos;
//}
//	public void setVideoInfos(List<Media> mlocalVideoInfos) {
//		localVideoInfos = mlocalVideoInfos;
//		notifyDataSetChanged();
//	}
//
//	@Override
//	public int getCount() {
//		if (null == localVideoInfos || localVideoInfos.isEmpty())
//			return 0;
//		// TODO Auto-generated method stub
//		return localVideoInfos.size();
//	}
//
//	@Override
//	public Object getItem(int position) {
//		// TODO Auto-generated method stub
//		return localVideoInfos.get(position);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		// TODO Auto-generated method stub
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		ViewHolder viewHolder = null;
//		Media localVideoInfo = localVideoInfos.get(position);
//		if (null == convertView) {
//			convertView = inflater.inflate(R.layout.grid_item_cache, parent,
//					false);
//			viewHolder = new ViewHolder();
//			viewHolder.durationTextView = (TextView) convertView
//					.findViewById(R.id.duration);
//			viewHolder.nameTextView = (TextView) convertView
//					.findViewById(R.id.title);
//			viewHolder.thumbnail = (ImageView) convertView
//					.findViewById(R.id.thumbnail);
//			viewHolder.thumbnail_mask = convertView.findViewById(R.id.mask);
//			viewHolder.state = (TextView) convertView.findViewById(R.id.state);
//			viewHolder.progress = (TextView) convertView
//					.findViewById(R.id.progress);
//			viewHolder.btn_delete = convertView.findViewById(R.id.btn_delete);
//			viewHolder.llview=  convertView.findViewById(R.id.llview);
//			convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//		viewHolder.btn_delete.setVisibility(View.GONE);
//		viewHolder.durationTextView.setVisibility(View.GONE);
//		if (0 < localVideoInfo.getDuration()) {
//			viewHolder.durationTextView.setVisibility(View.VISIBLE);
//			viewHolder.durationTextView.setText(YoukuUtil
//					.formatTimeForHistory(localVideoInfo.getDuration() / 1000));
//		}
//		viewHolder.nameTextView.setText(localVideoInfo.getTitle());
//		viewHolder.thumbnail.setImageDrawable(localVideoInfo
//				.getDrawable(context));
//		if (0 < localVideoInfo.getDuration()) {
//			if (localVideoInfo.getProgress() == 0) {// new 未观看
//				viewHolder.thumbnail_mask.setVisibility(View.GONE);
//				viewHolder.state.setVisibility(View.GONE);
//				viewHolder.progress.setVisibility(View.GONE);
//				viewHolder.durationTextView.setVisibility(View.VISIBLE);
//			} else if (localVideoInfo.getProgress() > localVideoInfo
//					.getDuration() / 1000 - 60) {// 已看完，重播
//				viewHolder.thumbnail_mask.setVisibility(View.VISIBLE);
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.state.setText("重播");
//				viewHolder.progress.setText("已看完 / "
//						+ YoukuUtil.formatTimeForHistory(localVideoInfo
//								.getDuration() / 1000));
//			} else {// 未看完，续播
//				viewHolder.thumbnail_mask.setVisibility(View.VISIBLE);
//				viewHolder.state.setVisibility(View.VISIBLE);
//				viewHolder.progress.setVisibility(View.VISIBLE);
//				viewHolder.state.setText("续播");
//				viewHolder.progress.setText(YoukuUtil
//						.formatTimeForHistory(localVideoInfo.getProgress())
//						+ " / "
//						+ YoukuUtil.formatTimeForHistory(localVideoInfo
//								.getDuration() / 1000));
//			}
//		}
//		if (CachePageActivity.mIsEditState && null != deleteLocalVideoInfos
//				&& deleteLocalVideoInfos.contains(localVideoInfo)) {
//			viewHolder.btn_delete.setVisibility(View.VISIBLE);
//			viewHolder.llview.setBackgroundResource(R.drawable.delete_border);
//		} else {
//			viewHolder.btn_delete.setVisibility(View.GONE);
//			viewHolder.llview.setBackgroundResource(R.drawable.delete_border_white);
//		}
//		return convertView;
//	}
//
//	class ViewHolder {
//		private TextView durationTextView;
//		private TextView nameTextView;
//		/** 缩略图 */
//		private ImageView thumbnail;
//		/** 缩略图遮罩 */
//		private View thumbnail_mask;
//		private TextView state;
//		private TextView progress;
//		private View btn_delete;
//		private View llview;
//	}

}
