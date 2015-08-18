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

package com.youku.player.util;

public interface DetailMessage {
    public final int LOAD_NEXT_PAGE_SUCCESS = 100;
    public final int LOAD_NEXT_PAGE_FAILED = 101;

    public final int REFRESH_SUCCESS = 102;
    public final int REFRESH_FAILED = 103;

    public final int GET_SERIES_SUCCESS = 104;
    public final int GET_VARIETY_SERIES_SUCCESS = 1040;
    public final int TITLE_RQC_CACHE_LOGIN = 105;
    public final int LAYOUT_INIT_FINISH = 106;
    public final int WAIT = 107;
    public final static int MSG_UP_SUCCESS = 10;
    public final static int MSG_UP_FAIL = 11;
    public final static int MSG_DOWN_SUCCESS = 20;
    public final static int MSG_DOWN_FAIL = 21;

    public final static int MSG_RQC_FAV_LOG = 201;
    public final static int MSG_FAV_SUCCESS = 202;
    public final static int MSG_FAV_FAIL = 203;

    public final static int MSG_RQC_CACHE_LOGIN = 204;
    public final static int MSG_RQC_CACHE_LOCAL_BACK = 2041;

    public final static int GET_LAYOUT_DATA_FAIL = 206;
    public final static int DETAIL_PLAY_TASK = 207;

    public final static int SERIES_ITEM_TO_PLAY = 301;
    public final static int SHOW_CURRENT_PLAY = 302;
    public final static int SHOW_CACHED_ITEM = 401;
    public final static int SEEK_TO_POINT = 501;
    public final static int CACHE_START_DOWNLAOD = 502;
    public final static int GO_CACHED_LIST = 503;
    public final static int GO_RELATED_VIDEO = 504;
    public final static int WEAK_NETWORK = 506;
    public final static int SHOW_NETWORK_ERROR_DIALOG = 507;
    public final static int CACHED_ALREADY = 508;
    public final static int GET_CACHED_LIST = 509;
    public final static int DOWN_lOAD_SUCCESS = 610;
    public final static int DOWN_lOAD_FAILED = 611;
    public final static int GET_HIS_FINISH = 612;
    public final static int MSG_GET_PLAY_INFO_SUCCESS = 6130;
    public final static int MSG_GET_PLAY_INFO_FAIL = 6131;
    public final static int MSG_UPDATE_COMMENT = 6132;
    public final static int MSG_CANNOT_CACHE = 20120;
    public final static int MSG_CANNOT_CACHE_VARIETY = 20121;
    public final static int MSG_VIDEO_PLAY_CHANGE = 2013;// 播放视频更改
    public final static int UPDATE_CACHE_ITEM = 201304;

    //用在刷新插件上
    public static final int PLUGIN_SHOW_AD_PLAY = 1;
    public static final int PLUGIN_SHOW_SMALL_PLAYER = 2;
    public static final int PLUGIN_SHOW_FULLSCREEN_PLAYER = 3;
    public static final int PLUGIN_SHOW_FULLSCREEN_ENDPAGE = 4;
    public static final int PLUGIN_SHOW_IMAGE_AD = 5;
    public static final int PLUGIN_SHOW_INVESTIGATE = 6;
    public static final int PLUGIN_SHOW_NOT_SET = 7;
}
