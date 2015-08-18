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

import android.content.Context;

import com.github.obsessive.library.base.BaseLazyFragment;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.bean.NavigationEntity;
import com.github.obsessive.simplifyreader.interactor.HomeInteractor;
import com.github.obsessive.simplifyreader.ui.fragment.ImagesContainerFragment;
import com.github.obsessive.simplifyreader.ui.fragment.MusicsFragment;
import com.github.obsessive.simplifyreader.ui.fragment.VideosContainerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/12.
 * Description:
 */
public class HomeInteractorImpl implements HomeInteractor {

    @Override
    public List<BaseLazyFragment> getPagerFragments() {
        List<BaseLazyFragment> fragments = new ArrayList<>();
        fragments.add(new ImagesContainerFragment());
        fragments.add(new VideosContainerFragment());
        fragments.add(new MusicsFragment());

        return fragments;
    }

    @Override
    public List<NavigationEntity> getNavigationListData(Context context) {
        List<NavigationEntity> navigationEntities = new ArrayList<>();
        String[] navigationArrays = context.getResources().getStringArray(R.array.navigation_list);
        navigationEntities.add(new NavigationEntity("", navigationArrays[0], R.drawable.ic_picture));
        navigationEntities.add(new NavigationEntity("", navigationArrays[1], R.drawable.ic_video));
        navigationEntities.add(new NavigationEntity("", navigationArrays[2], R.drawable.ic_music));
        return navigationEntities;
    }
}
