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

package com.github.obsessive.simplifyreader.interactor.impl;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.github.obsessive.simplifyreader.bean.ResponseImagesListEntity;
import com.github.obsessive.simplifyreader.interactor.CommonListInteractor;
import com.github.obsessive.simplifyreader.listeners.BaseMultiLoadedListener;
import com.github.obsessive.simplifyreader.utils.UriHelper;
import com.github.obsessive.simplifyreader.utils.VolleyHelper;
import com.google.gson.reflect.TypeToken;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/2.
 * Description:
 */
public class ImagesListInteractorImpl implements CommonListInteractor {

    private BaseMultiLoadedListener<ResponseImagesListEntity> loadedListener = null;

    public ImagesListInteractorImpl(BaseMultiLoadedListener<ResponseImagesListEntity> loadedListener) {
        this.loadedListener = loadedListener;
    }

    @Override
    public void getCommonListData(final String requestTag, final int event_tag, String keywords, int page) {
        GsonRequest<ResponseImagesListEntity> gsonRequest = new GsonRequest<ResponseImagesListEntity>(
                UriHelper.getInstance().getImagesListUrl(keywords, page),
                null,
                new TypeToken<ResponseImagesListEntity>() {
                }.getType(),
                new Response.Listener<ResponseImagesListEntity>() {
                    @Override
                    public void onResponse(ResponseImagesListEntity response) {
                        loadedListener.onSuccess(event_tag, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadedListener.onException(error.getMessage());
                    }
                }
        );

        gsonRequest.setShouldCache(true);
        gsonRequest.setTag(requestTag);

        VolleyHelper.getInstance().getRequestQueue().add(gsonRequest);
    }

}
