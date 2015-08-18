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

package com.github.obsessive.simplifyreader.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.obsessive.library.adapter.ListViewDataAdapter;
import com.github.obsessive.library.adapter.ViewHolderBase;
import com.github.obsessive.library.adapter.ViewHolderCreator;
import com.github.obsessive.library.base.BaseWebActivity;
import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.library.utils.TLog;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.bean.AboutListEntity;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseSwipeBackActivity;
import com.github.obsessive.simplifyreader.ui.activity.qrcode.decode.DecodeUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/7/23
 * Description:
 */
public class AboutUsActivity extends BaseSwipeBackActivity {

    @InjectView(R.id.about_us_pay)
    ImageView mPayCode;

    @InjectView(R.id.about_us_list)
    ListView mListView;

    private String[] mAboutArray = null;
    private AboutListEntity mItemData = null;
    private ListViewDataAdapter<AboutListEntity> mListViewDataAdapter = null;

    @Override
    protected boolean isApplyKitKatTranslucency() {
        return true;
    }

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_about_us;
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
        mAboutArray = getResources().getStringArray(R.array.about_list);

        mListViewDataAdapter = new ListViewDataAdapter<>(new ViewHolderCreator<AboutListEntity>() {
            @Override
            public ViewHolderBase<AboutListEntity> createViewHolder(int position) {
                return new ViewHolderBase<AboutListEntity>() {

                    TextView mTitle;
                    TextView mSubTitle;

                    @Override
                    public View createView(LayoutInflater layoutInflater) {
                        View convertView = layoutInflater.inflate(R.layout.list_item_about, null);
                        mTitle = ButterKnife.findById(convertView, R.id.list_item_about_title);
                        mSubTitle = ButterKnife.findById(convertView, R.id.list_item_about_sub_title);
                        return convertView;
                    }

                    @Override
                    public void showData(int position, AboutListEntity itemData) {
                        if (null != itemData) {
                            if (!CommonUtils.isEmpty(itemData.getTitle())) {
                                mTitle.setText(itemData.getTitle());
                            }

                            if (!CommonUtils.isEmpty(itemData.getSubTitle())) {
                                mSubTitle.setText(itemData.getSubTitle());
                            }
                        }
                    }
                };
            }
        });

        mListView.setAdapter(mListViewDataAdapter);

        mItemData = new AboutListEntity();
        mItemData.setTitle(mAboutArray[0]);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            String version = String.format(getResources().getString(R.string.splash_version), packageInfo.versionName);
            if (!CommonUtils.isEmpty(version)) {
                mItemData.setSubTitle(version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mListViewDataAdapter.getDataList().add(mItemData);

        mItemData = new AboutListEntity();
        mItemData.setTitle(mAboutArray[1]);
        mItemData.setSubTitle("https://github.com/SkillCollege");
        mListViewDataAdapter.getDataList().add(mItemData);

        mItemData = new AboutListEntity();
        mItemData.setTitle(mAboutArray[2]);
        mItemData.setSubTitle("https://github.com/SkillCollege/SimplifyReader");
        mListViewDataAdapter.getDataList().add(mItemData);

        mItemData = new AboutListEntity();
        mItemData.setTitle(mAboutArray[3]);
        mListViewDataAdapter.getDataList().add(mItemData);

        mItemData = new AboutListEntity();
        mItemData.setTitle(mAboutArray[4]);
        mListViewDataAdapter.getDataList().add(mItemData);

        mListViewDataAdapter.notifyDataSetChanged();

        mPayCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                DecodeUtils decodeUtils = new DecodeUtils(DecodeUtils.DECODE_DATA_MODE_ALL);
                String url = decodeUtils.decodeWithZxing(BitmapFactory.decodeResource(getResources(), R.drawable.pay_qrcode));
                if (!CommonUtils.isEmpty(url)) {
                    TLog.d(TAG_LOG, url);
                    intent.setData(Uri.parse(url));
                }
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle extras = new Bundle();
                switch (position) {
                    case 0:
                        // version
                        break;
                    case 1:
                        // author
                        extras.putString(BaseWebActivity.BUNDLE_KEY_TITLE, mAboutArray[1]);
                        extras.putBoolean(BaseWebActivity.BUNDLE_KEY_SHOW_BOTTOM_BAR, true);
                        extras.putString(BaseWebActivity.BUNDLE_KEY_URL, "https://github.com/SkillCollege");

                        readyGo(BaseWebActivity.class, extras);
                        break;
                    case 2:
                        // project index
                        extras.putString(BaseWebActivity.BUNDLE_KEY_TITLE, mAboutArray[2]);
                        extras.putBoolean(BaseWebActivity.BUNDLE_KEY_SHOW_BOTTOM_BAR, true);
                        extras.putString(BaseWebActivity.BUNDLE_KEY_URL, "https://github.com/SkillCollege/SimplifyReader");

                        readyGo(BaseWebActivity.class, extras);
                        break;
                    case 3:
                        // project description
                        extras.putString(BaseWebActivity.BUNDLE_KEY_TITLE, mAboutArray[3]);
                        extras.putBoolean(BaseWebActivity.BUNDLE_KEY_SHOW_BOTTOM_BAR, false);
                        extras.putString(BaseWebActivity.BUNDLE_KEY_URL, "file:///android_asset/project_description.html");

                        readyGo(BaseWebActivity.class, extras);
                        break;
                    case 4:
                        // open source description
                        extras.putString(BaseWebActivity.BUNDLE_KEY_TITLE, mAboutArray[4]);
                        extras.putBoolean(BaseWebActivity.BUNDLE_KEY_SHOW_BOTTOM_BAR, false);
                        extras.putString(BaseWebActivity.BUNDLE_KEY_URL, "file:///android_asset/open_source.html");

                        readyGo(BaseWebActivity.class, extras);
                        break;

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
        return TransitionMode.RIGHT;
    }
}
