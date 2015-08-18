//package com.youku.player.ui.interf;
//
//import com.youku.player.apiservice.ICacheInfo;
//import com.youku.player.apiservice.IUserInfo;
//import com.youku.player.apiservice.IVideoHistoryInfo;
//
//
//public abstract class IYoukuPlayer {
//	public IMediaPlayerDelegate mMediaPlayerDelegate;
//	protected BaseActivity activity;
//	
//	public abstract IMediaPlayerDelegate getmMediaPlayerDelegate();
//	
//	public abstract void initial(BaseActivity mYoukuBaseActivity);
//	
//	public abstract void setICacheInfo(ICacheInfo mICacheInfo);
//	
//	public abstract void setIUserInfo(IUserInfo iUserInfo);
//	
//	public abstract void setIVideoHistoryInfo(IVideoHistoryInfo iVideoHistoryInfo);
//	
//	public abstract void playVideo(final String vid);
//	
//	public abstract void playVideo(final String vid, final String playlistId);
//	
//	public abstract void playVideoWithPassword(final String vid, final String password);
//	
//	public abstract void playTudouVideo(final String itemCode,int point, boolean noadv);
//	
//	public abstract void playTudouVideo(final String itemCode,int point, final String playlistCode, boolean noadv);
//	
//	public abstract void playTudouVideo(final String itemCode, boolean noadv);
//	
//	public abstract void playTudouVideoWithAlbumID(final String itemCode, final String albumID, boolean noadv);
//	
//	public abstract void playTudouVideoWithPassword(final String itemCode, final String password);
//	
//	public abstract void replayTudouVideo(final String itemCode, boolean noadv);
//	
//	public abstract void playTudouAlbum(final String albumID,int point, boolean noadv);
//	
//	public abstract void playTudouAlbum(final String albumID, boolean noadv);
//	
//	public abstract void playTudouAlbum(final String albumID, final String languageCode, boolean noadv);
//	
//	public abstract void replayTudouAlbum(final String albumID, boolean noadv);
//	
//	public abstract void playVideo(String vid, boolean isCache, int point);
//	
//	public abstract void playLocalVideo(final String vid, String url, String videoTitle);
//	
//	public abstract void playLocalVideo(String vid, String url, String title, int progress);
//	
//	public abstract void playLocalVideo(String url, String title);
//	
//	public abstract void playLocalVideo(String url, String title, int progress);
//	
//	public abstract void replayLocalVideo(final String vid, String url, String title);
//	
//	public abstract void playVideoNoAdv(String vid);
//	
//	public abstract void playVideoWithStage(String id, boolean isCache, int point,int videoStage);
//	
//	public abstract void playVideoWithStageTudou(String id, boolean isCache, int point,int videoStage);
//	
//	public abstract void playVideoAdvext(String id, String adext);
//	
//	public abstract void playHLS(String liveid);
//	
//	public abstract void playYoukuHLS(String liveid);
//}
