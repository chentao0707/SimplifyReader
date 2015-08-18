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

package com.github.obsessive.simplifyreader.utils;

import com.github.obsessive.simplifyreader.api.ApiConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/20.
 * Description:
 */
public class UriHelper {

    private static volatile UriHelper instance = null;

    /**
     * 20 datas per page
     */
    public static final int PAGE_LIMIT = 20;

    public static final String URL_MUSICS_LIST_CHANNEL_ID = "0";

    private UriHelper() {
    }

    public static UriHelper getInstance() {
        if (null == instance) {
            synchronized (UriHelper.class) {
                if (null == instance) {
                    instance = new UriHelper();
                }
            }
        }
        return instance;
    }

    public int calculateTotalPages(int totalNumber) {
        if (totalNumber > 0) {
            return totalNumber % PAGE_LIMIT != 0 ? (totalNumber / PAGE_LIMIT + 1) : totalNumber / PAGE_LIMIT;
        } else {
            return 0;
        }
    }

    public String getImagesListUrl(String category, int pageNum) {
        StringBuffer sb = new StringBuffer();
        sb.append(ApiConstants.Urls.BAIDU_IMAGES_URLS);
        sb.append("?col=");
        try {
            sb.append(URLEncoder.encode(category, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("&tag=");
        try {
            sb.append(URLEncoder.encode("全部", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("&pn=");
        sb.append(pageNum * PAGE_LIMIT);
        sb.append("&rn=");
        sb.append(PAGE_LIMIT);
        sb.append("&from=1");
        return sb.toString();
    }

    public String getVideosListUrl(String category, int pageNum) {
        StringBuffer sb = new StringBuffer();
        sb.append(ApiConstants.Urls.YOUKU_VIDEOS_URLS);
        sb.append("?keyword=");
        try {
            sb.append(URLEncoder.encode(category, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("&page=");
        sb.append(pageNum);
        sb.append("&count=");
        sb.append(PAGE_LIMIT);
        sb.append("&public_type=all&paid=0&period=today&orderby=published&client_id=6ecd6970268b4c53");
        return sb.toString().trim();
    }

    public String getVideoUserUrl(int userId) {
        StringBuffer sb = new StringBuffer();
        sb.append(ApiConstants.Urls.YOUKU_USER_URLS);
        sb.append("?user_id=");
        sb.append(userId);
        sb.append("&client_id=6ecd6970268b4c53");
        return sb.toString().trim();
    }

    public String getDoubanPlayListUrl(String channelId) {
        StringBuffer sb = new StringBuffer();
        sb.append(ApiConstants.Urls.DOUBAN_PLAY_LIST_URLS);
        sb.append("?channel=");
        sb.append(channelId);
        sb.append("&app_name=radio_desktop_win&version=100&type=&sid=0");
        return sb.toString().trim();
    }
}
