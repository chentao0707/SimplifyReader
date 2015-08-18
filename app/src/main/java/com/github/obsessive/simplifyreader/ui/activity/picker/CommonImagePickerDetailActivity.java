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

package com.github.obsessive.simplifyreader.ui.activity.picker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.github.obsessive.library.adapter.ListViewDataAdapter;
import com.github.obsessive.library.adapter.ViewHolderBase;
import com.github.obsessive.library.adapter.ViewHolderCreator;
import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.picker.ImageItem;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseActivity;
import com.github.obsessive.simplifyreader.utils.ImageLoaderHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/8/10 15:45
 * PkgName: com.github.obsessive.simplifyreader.ui.activity.picker
 * Description:
 */
public class CommonImagePickerDetailActivity extends BaseActivity {

    public static final String KEY_BUNDLE_RESULT_IMAGE_PATH = "KEY_BUNDLE_RESULT_IMAGE_PATH";

    @InjectView(R.id.common_image_picker_detail_grid_view)
    GridView commonImagePickerDetailGridView;

    private ListViewDataAdapter<ImageItem> mGridViewAdapter = null;
    private List<ImageItem> mGridListData = null;

    @Override
    protected boolean isApplyKitKatTranslucency() {
        return true;
    }

    @Override
    protected void getBundleExtras(Bundle extras) {
        mGridListData = extras.getParcelableArrayList(CommonImagePickerListActivity
                .KEY_BUNDLE_ALBUM_PATH);

        String title = extras.getString(CommonImagePickerListActivity.KEY_BUNDLE_ALBUM_NAME);
        if (!CommonUtils.isEmpty(title)) {
            setTitle(title);
        }
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_common_image_picker_detail;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void initViewsAndEvents() {
        mGridViewAdapter = new ListViewDataAdapter<>(new ViewHolderCreator<ImageItem>() {
            @Override
            public ViewHolderBase<ImageItem> createViewHolder(int position) {
                return new ViewHolderBase<ImageItem>() {

                    ImageView mItemImage;

                    @Override
                    public View createView(LayoutInflater layoutInflater) {
                        View convertView = layoutInflater.inflate(R.layout.grid_item_common_image_picker, null);
                        mItemImage = ButterKnife.findById(convertView, R.id.grid_item_common_image_picker_image);
                        return convertView;
                    }

                    @Override
                    public void showData(int position, ImageItem itemData) {
                        if (null != itemData) {
                            String imagePath = itemData.getImagePath();
                            if (!CommonUtils.isEmpty(imagePath)) {
                                ImageLoader.getInstance().displayImage("file://" + imagePath,
                                        mItemImage, ImageLoaderHelper.getInstance(mContext).getDisplayOptions());
                            }
                        }
                    }
                };
            }
        });
        mGridViewAdapter.getDataList().addAll(mGridListData);
        commonImagePickerDetailGridView.setAdapter(mGridViewAdapter);

        commonImagePickerDetailGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mGridViewAdapter && null != mGridViewAdapter.getDataList() &&
                        !mGridViewAdapter.getDataList().isEmpty() &&
                        position < mGridViewAdapter.getDataList().size()) {

                    Intent intent = new Intent();
                    intent.putExtra(KEY_BUNDLE_RESULT_IMAGE_PATH,
                            mGridViewAdapter.getDataList().get(position).getImagePath());

                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected boolean toggleOverridePendingTransition() {
        return true;
    }

    @Override
    protected TransitionMode getOverridePendingTransitionMode() {
        return TransitionMode.BOTTOM;
    }
}
