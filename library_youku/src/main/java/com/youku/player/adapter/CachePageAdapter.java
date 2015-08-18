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
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.view.ViewGroup;
//
//import com.youku.player.fragment.CachedFragment;
//import com.youku.player.fragment.CachingFragment;
//import com.youku.player.fragment.FragmentLocalVideoList;
//import com.youku.ui.activity.CachePageActivity.StateChangedCallback;
//
///**
// * CachePageAdapter.缓存页适配器
// * 
// * @author 刘仲男 qq81595157@126.com
// * @version v3.5
// * @created time 2012-11-2 下午6:04:17
// */
//public class CachePageAdapter extends FragmentPagerAdapter implements
//		ViewPager.OnPageChangeListener {
//
//	public CachingFragment fragment_downloading;
//	public CachedFragment fragment_downloaded;
////	public FragmentLocalVideoList fragment_local;
//	private ViewPager pager;
//	private StateChangedCallback callback;
//
//	public CachePageAdapter(FragmentManager fm) {
//		super(fm);
//		// TODO Auto-generated constructor stub
//	}
//
//	public CachePageAdapter(FragmentActivity activity, ViewPager pager,
//			StateChangedCallback callback, String tag0, String tag1) {
//		super(activity.getSupportFragmentManager());
//		this.pager = pager;
//		this.callback = callback;
//		pager.setAdapter(this);
//		pager.setOnPageChangeListener(this);
//		if (tag0 != null) {
//			fragment_downloading = (CachingFragment) activity
//					.getSupportFragmentManager().findFragmentByTag(tag0);
//			if (fragment_downloading == null) {
//				fragment_downloading = (CachingFragment) Fragment.instantiate(
//						activity, CachingFragment.class.getName());
//			}
//		} else {
//			fragment_downloading = (CachingFragment) Fragment.instantiate(
//					activity, CachingFragment.class.getName());
//		}
//		if (tag1 != null) {
//			fragment_downloaded = (CachedFragment) activity
//					.getSupportFragmentManager().findFragmentByTag(tag1);
//			if (fragment_downloaded == null) {
//				fragment_downloaded = (CachedFragment) Fragment.instantiate(
//						activity, CachedFragment.class.getName());
//			}
//		} else {
//			fragment_downloaded = (CachedFragment) Fragment.instantiate(
//					activity, CachedFragment.class.getName());
//		}
////		fragment_local = (FragmentLocalVideoList)activity
////				.getSupportFragmentManager().findFragmentByTag(FragmentLocalVideoList.class.getName());
////		if(null==fragment_local)
////			fragment_local = (FragmentLocalVideoList) Fragment.instantiate(
////					activity, FragmentLocalVideoList.class.getName());
//	}
//
//	public void notifyData() {
//		switch (pager.getCurrentItem()) {
//		case 0:
//			fragment_downloading.notifyData();
//			break;
//		case 1:
//			fragment_downloaded.notifyData();
//			break;
//		case 2:
////			fragment_local.notifyData();
//			break;
//		}
//	}
//
////	public boolean getEditable() {
////		switch (pager.getCurrentItem()) {
////		case 0:
////			return fragment_downloading.getEditable();
////		case 1:
////			return fragment_downloaded.getEditable();
////		}
////		return false;
////	}
//
//	public boolean getIsInner() {
//		return fragment_downloaded.getIsInner();
//	}
//
//	public void deleteAll() {
//		switch (pager.getCurrentItem()) {
//		case 0:
//			fragment_downloading.deleteAll();
//			break;
//		case 1:
//			fragment_downloaded.deleteAll();
//			break;
//		}
//	}
//	public void deleteSelected() {
//		switch (pager.getCurrentItem()) {
//		case 0:
//			fragment_downloading.deleteSelected();
//			break;
//		case 1:
//			fragment_downloaded.deleteSelected();
//			break;
//		case 2:
////			fragment_local.deleteSelected();
//			break;
//		}
//	}
//
//	/** 刷新数据及页面 */
//	public synchronized void refresh() {
//		switch (pager.getCurrentItem()) {
//		case 0:
//			fragment_downloading.refresh();
//			break;
//		case 1:
//			fragment_downloaded.refresh();
//			break;
//		}
//	}
//
//	@Override
//	public Fragment getItem(int arg0) {
//		if (arg0 == 0) {
//			return fragment_downloading;
//		} else if (arg0 == 1) {
//			return fragment_downloaded;
//		}
//		else if(arg0 == 2)
//		{
////			return fragment_local;
//		}
//		return null;
//	}
//
//	@Override
//	public int getCount() {
//		// FIXME getCount
//		return 3;
//	}
//
//	@Override
//	public void onPageScrollStateChanged(int arg0) {
//		if (callback != null) {
//			if (arg0 == 2) {
//				callback.StateChanged(pager.getCurrentItem() % 3);
//			}
//		}
//	}
//
//	@Override
//	public void destroyItem(ViewGroup container, int position, Object object) {
//		// FIXME destroyItem
//		position = position % 3;
//		// super.destroyItem(container, position, object);
//	}
//
//	@Override
//	public Object instantiateItem(ViewGroup container, int position) {
//		// FIXME instantiateItem
//		position = position % 3;
//		return super.instantiateItem(container, position);
//	}
//
//	@Override
//	public void onPageScrolled(int arg0, float arg1, int arg2) {
//		// FIXME onPageScrolled
//
//	}
//
//	@Override
//	public void onPageSelected(int arg0) {
//		// FIXME onPageSelected
//
//	}
//
//}
