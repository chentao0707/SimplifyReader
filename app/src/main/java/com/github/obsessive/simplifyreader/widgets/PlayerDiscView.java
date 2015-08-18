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

package com.github.obsessive.simplifyreader.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.utils.ImageLoaderHelper;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PlayerDiscView extends RelativeLayout {

    private static final int NEEDLE_ANIMATOR_TIME = 350;
    private static final float NEEDLE_ROTATE_CIRCLE = -30.0f;

    private static final int DISC_ANIMATOR_TIME = 20 * 1000;
    private static final int DISC_ANIMATOR_REPEAT_COUNT = -1;

    private static final int DISC_REVERSE_ANIMATOR_TIME = 500;

    private ImageView mNeedle;
    private ImageView mAlbumCover;
    private RelativeLayout mDiscLayout;

    private ObjectAnimator mNeedleAnimator;
    private ObjectAnimator mDiscLayoutAnimator;

    private float mDiscLayoutAnimatorValue;

    private float mNeedlePivotX = 0.0f;
    private float mNeedlePivotY = 0.0f;

    private static final float X_FRACTION = 184.0f / 212.0f;
    private static final float Y_FRACTION = 25.0f / 259.0f;

    private boolean isPlaying = false;

    private Context mContext;

    public boolean isPlaying() {
        return isPlaying;
    }

    public PlayerDiscView(Context context, AttributeSet attrs) {
        super(context);
        init(context);
    }

    public PlayerDiscView(Context context) {
        super(context);
        init(context);
    }

    public PlayerDiscView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stick);

        mNeedlePivotX = bitmap.getWidth() * X_FRACTION;
        mNeedlePivotY = bitmap.getHeight() * Y_FRACTION;

        bitmap.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNeedle = (ImageView) findViewById(R.id.player_needle);
        mAlbumCover = (ImageView) findViewById(R.id.player_disc_image);
        mDiscLayout = (RelativeLayout) findViewById(R.id.player_disc_container);

        ViewHelper.setPivotX(mNeedle, mNeedlePivotX);
        ViewHelper.setPivotY(mNeedle, mNeedlePivotY);
    }

    public void startPlay() {
        if (isPlaying) {
            return;
        }

        startNeedleAnimator();
        startDiscAnimator(0.0f);

        isPlaying = true;
    }

    public void rePlay() {
        if (isPlaying) {
            return;
        }

        startNeedleAnimator();
        startDiscAnimator(mDiscLayoutAnimatorValue);

        isPlaying = true;
    }

    public void pause() {
        if (!isPlaying) {
            return;
        }

        startNeedleAnimator();

        if (mDiscLayoutAnimator.isRunning() || mDiscLayoutAnimator.isStarted()) {
            mDiscLayoutAnimator.cancel();
        }

        isPlaying = false;
    }

    public void next() {
        if (isPlaying) {
            startNeedleAnimator();
        }
        mDiscLayoutAnimator.cancel();
        isPlaying = false;

        reverseDiscAnimator();
    }

    private void startNeedleAnimator() {
        if (isPlaying) {
            mNeedleAnimator = ObjectAnimator.ofFloat(mNeedle, "rotation", 0, NEEDLE_ROTATE_CIRCLE);
        } else {
            mNeedleAnimator = ObjectAnimator.ofFloat(mNeedle, "rotation", NEEDLE_ROTATE_CIRCLE, 0);
        }

        mNeedleAnimator.setDuration(NEEDLE_ANIMATOR_TIME);
        mNeedleAnimator.setInterpolator(new DecelerateInterpolator());

        if (mNeedleAnimator.isRunning() || mNeedleAnimator.isStarted()) {
            mNeedleAnimator.cancel();
        }

        mNeedleAnimator.start();
    }

    private void startDiscAnimator(float animatedValue) {
        mDiscLayoutAnimator = ObjectAnimator.ofFloat(mDiscLayout, "rotation", animatedValue, 360 + animatedValue);
        mDiscLayoutAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                mDiscLayoutAnimatorValue = (Float) arg0.getAnimatedValue();
            }
        });
        mDiscLayoutAnimator.setDuration(DISC_ANIMATOR_TIME);
        mDiscLayoutAnimator.setRepeatCount(DISC_ANIMATOR_REPEAT_COUNT);
        mDiscLayoutAnimator.setInterpolator(new LinearInterpolator());

        if (mDiscLayoutAnimator.isRunning() || mDiscLayoutAnimator.isStarted()) {
            mDiscLayoutAnimator.cancel();
        }

        mDiscLayoutAnimator.start();
    }

    private void reverseDiscAnimator() {
        mDiscLayoutAnimator = ObjectAnimator.ofFloat(mDiscLayout, "rotation", mDiscLayoutAnimatorValue, 360);
        mDiscLayoutAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                mDiscLayoutAnimatorValue = (Float) arg0.getAnimatedValue();
            }
        });
        mDiscLayoutAnimator.setDuration(DISC_REVERSE_ANIMATOR_TIME);
        mDiscLayoutAnimator.setInterpolator(new AccelerateInterpolator());

        if (mDiscLayoutAnimator.isRunning() || mDiscLayoutAnimator.isStarted()) {
            mDiscLayoutAnimator.cancel();
        }

        mDiscLayoutAnimator.start();
    }

    public void loadAlbumCover(String imageUrl) {
        ImageLoader.getInstance().displayImage(imageUrl, mAlbumCover,
                ImageLoaderHelper.getInstance
                        (mContext).getDisplayOptions(100));
    }

}
