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

package com.github.obsessive.library.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.github.obsessive.library.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;


public class CircularProgressDrawable extends Drawable
        implements Animatable {

    public enum Style {NORMAL, ROUNDED}

    public interface OnEndListener {
        public void onEnd(CircularProgressDrawable drawable);
    }

    private static final ArgbEvaluator COLOR_EVALUATOR = new ArgbEvaluator();
    public static final Interpolator END_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator DEFAULT_ROTATION_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator DEFAULT_SWEEP_INTERPOLATOR = new DecelerateInterpolator();
    private static final int ROTATION_ANIMATOR_DURATION = 2000;
    private static final int SWEEP_ANIMATOR_DURATION = 600;
    private static final int END_ANIMATOR_DURATION = 200;

    private final RectF fBounds = new RectF();

    private ValueAnimator mSweepAppearingAnimator;
    private ValueAnimator mSweepDisappearingAnimator;
    private ValueAnimator mRotationAnimator;
    private ValueAnimator mEndAnimator;
    private OnEndListener mOnEndListener;
    private boolean mModeAppearing;
    private Paint mPaint;
    private boolean mRunning;
    private int mCurrentColor;
    private int mCurrentIndexColor;
    private float mCurrentSweepAngle;
    private float mCurrentRotationAngleOffset = 0;
    private float mCurrentRotationAngle = 0;
    private float mCurrentEndRatio = 1f;

    //params
    private Interpolator mAngleInterpolator;
    private Interpolator mSweepInterpolator;
    private float mBorderWidth;
    private int[] mColors;
    private float mSweepSpeed;
    private float mRotationSpeed;
    private int mMinSweepAngle;
    private int mMaxSweepAngle;
    private boolean mFirstSweepAnimation;

    private CircularProgressDrawable(int[] colors,
                                     float borderWidth,
                                     float sweepSpeed,
                                     float rotationSpeed,
                                     int minSweepAngle,
                                     int maxSweepAngle,
                                     Style style,
                                     Interpolator angleInterpolator,
                                     Interpolator sweepInterpolator) {
        mSweepInterpolator = sweepInterpolator;
        mAngleInterpolator = angleInterpolator;
        mBorderWidth = borderWidth;
        mCurrentIndexColor = 0;
        mColors = colors;
        mCurrentColor = mColors[0];
        mSweepSpeed = sweepSpeed;
        mRotationSpeed = rotationSpeed;
        mMinSweepAngle = minSweepAngle;
        mMaxSweepAngle = maxSweepAngle;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(borderWidth);
        mPaint.setStrokeCap(style == Style.ROUNDED ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        mPaint.setColor(mColors[0]);

        setupAnimations();
    }

    private void reinitValues() {
        mFirstSweepAnimation = true;
        mCurrentEndRatio = 1f;
        mPaint.setColor(mCurrentColor);
    }

    @Override
    public void draw(Canvas canvas) {
        float startAngle = mCurrentRotationAngle - mCurrentRotationAngleOffset;
        float sweepAngle = mCurrentSweepAngle;
        if (!mModeAppearing) {
            startAngle = startAngle + (360 - sweepAngle);
        }
        startAngle %= 360;
        if (mCurrentEndRatio < 1f) {
            float newSweepAngle = sweepAngle * mCurrentEndRatio;
            startAngle = (startAngle + (sweepAngle - newSweepAngle)) % 360;
            sweepAngle = newSweepAngle;
        }
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        fBounds.left = bounds.left + mBorderWidth / 2f + .5f;
        fBounds.right = bounds.right - mBorderWidth / 2f - .5f;
        fBounds.top = bounds.top + mBorderWidth / 2f + .5f;
        fBounds.bottom = bounds.bottom - mBorderWidth / 2f - .5f;
    }

    private void setAppearing() {
        mModeAppearing = true;
        mCurrentRotationAngleOffset += mMinSweepAngle;
    }

    private void setDisappearing() {
        mModeAppearing = false;
        mCurrentRotationAngleOffset = mCurrentRotationAngleOffset + (360 - mMaxSweepAngle);
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////////            Animation

    private void setupAnimations() {
        mRotationAnimator = ValueAnimator.ofFloat(0f, 360f);
        mRotationAnimator.setInterpolator(mAngleInterpolator);
        mRotationAnimator.setDuration((long) (ROTATION_ANIMATOR_DURATION / mRotationSpeed));
        mRotationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = animation.getAnimatedFraction() * 360f;
                setCurrentRotationAngle(angle);
            }
        });
        mRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotationAnimator.setRepeatMode(ValueAnimator.RESTART);

        mSweepAppearingAnimator = ValueAnimator.ofFloat(mMinSweepAngle, mMaxSweepAngle);
        mSweepAppearingAnimator.setInterpolator(mSweepInterpolator);
        mSweepAppearingAnimator.setDuration((long) (SWEEP_ANIMATOR_DURATION / mSweepSpeed));
        mSweepAppearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                float angle;
                if (mFirstSweepAnimation) {
                    angle = animatedFraction * mMaxSweepAngle;
                } else {
                    angle = mMinSweepAngle + animatedFraction * (mMaxSweepAngle - mMinSweepAngle);
                }
                setCurrentSweepAngle(angle);
            }
        });
        mSweepAppearingAnimator.addListener(new Animator.AnimatorListener() {
            boolean cancelled = false;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
                mModeAppearing = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    mFirstSweepAnimation = false;
                    setDisappearing();
                    mSweepDisappearingAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mSweepDisappearingAnimator = ValueAnimator.ofFloat(mMaxSweepAngle, mMinSweepAngle);
        mSweepDisappearingAnimator.setInterpolator(mSweepInterpolator);
        mSweepDisappearingAnimator.setDuration((long) (SWEEP_ANIMATOR_DURATION / mSweepSpeed));
        mSweepDisappearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                setCurrentSweepAngle(mMaxSweepAngle - animatedFraction * (mMaxSweepAngle - mMinSweepAngle));

                long duration = animation.getDuration();
                long played = animation.getCurrentPlayTime();
                float fraction = (float) played / duration;
                if (mColors.length > 1 && fraction > .7f) { //because
                    int prevColor = mCurrentColor;
                    int nextColor = mColors[(mCurrentIndexColor + 1) % mColors.length];
                    int newColor = (Integer) COLOR_EVALUATOR.evaluate((fraction - .7f) / (1 - .7f), prevColor, nextColor);
                    mPaint.setColor(newColor);
                }
            }
        });
        mSweepDisappearingAnimator.addListener(new Animator.AnimatorListener() {
            boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    setAppearing();
                    mCurrentIndexColor = (mCurrentIndexColor + 1) % mColors.length;
                    mCurrentColor = mColors[mCurrentIndexColor];
                    mPaint.setColor(mCurrentColor);
                    mSweepAppearingAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mEndAnimator = ValueAnimator.ofFloat(1f, 0f);
        mEndAnimator.setInterpolator(END_INTERPOLATOR);
        mEndAnimator.setDuration(END_ANIMATOR_DURATION);
        mEndAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEndRatio(1f - animation.getAnimatedFraction());

            }
        });
        mEndAnimator.addListener(new Animator.AnimatorListener() {
            private boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEndRatio(0f);
                if (!cancelled) stop();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        mRunning = true;
        reinitValues();
        mRotationAnimator.start();
        mSweepAppearingAnimator.start();
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }
        mRunning = false;
        stopAnimators();
        invalidateSelf();
    }

    private void stopAnimators() {
        mRotationAnimator.cancel();
        mSweepAppearingAnimator.cancel();
        mSweepDisappearingAnimator.cancel();
        mEndAnimator.cancel();
    }

    public void progressiveStop(OnEndListener listener) {
        if (!isRunning() || mEndAnimator.isRunning()) {
            return;
        }
        mOnEndListener = listener;
        mEndAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mEndAnimator.removeListener(this);
                if (mOnEndListener != null) mOnEndListener.onEnd(CircularProgressDrawable.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mEndAnimator.start();
    }

    public void progressiveStop() {
        progressiveStop(null);
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    public void setCurrentRotationAngle(float currentRotationAngle) {
        mCurrentRotationAngle = currentRotationAngle;
        invalidateSelf();
    }

    public void setCurrentSweepAngle(float currentSweepAngle) {
        mCurrentSweepAngle = currentSweepAngle;
        invalidateSelf();
    }

    private void setEndRatio(float ratio) {
        mCurrentEndRatio = ratio;
        invalidateSelf();
    }

    public static class Builder {
        private int[] mColors;
        private float mSweepSpeed;
        private float mRotationSpeed;
        private float mStrokeWidth;
        private int mMinSweepAngle;
        private int mMaxSweepAngle;
        private Style mStyle;
        private Interpolator mSweepInterpolator = DEFAULT_SWEEP_INTERPOLATOR;
        private Interpolator mAngleInterpolator = DEFAULT_ROTATION_INTERPOLATOR;

        public Builder(Context context) {
            this(context, false);
        }

        public Builder(Context context, boolean editMode) {
            initValues(context, editMode);
        }

        private void initValues(Context context, boolean editMode) {
            mStrokeWidth = context.getResources().getDimension(R.dimen.cpb_default_stroke_width);
            mSweepSpeed = 1f;
            mRotationSpeed = 1f;
            if (editMode) {
                mColors = new int[]{Color.BLUE};
                mMinSweepAngle = 20;
                mMaxSweepAngle = 300;
            } else {
                mColors = new int[]{context.getResources().getColor(R.color.cpb_default_color)};
                mMinSweepAngle = context.getResources().getInteger(R.integer.cpb_default_min_sweep_angle);
                mMaxSweepAngle = context.getResources().getInteger(R.integer.cpb_default_max_sweep_angle);
            }
            mStyle = Style.ROUNDED;
        }

        public Builder color(int color) {
            mColors = new int[]{color};
            return this;
        }

        public Builder colors(int[] colors) {
            CircularProgressBarUtils.checkColors(colors);
            mColors = colors;
            return this;
        }

        public Builder sweepSpeed(float sweepSpeed) {
            CircularProgressBarUtils.checkSpeed(sweepSpeed);
            mSweepSpeed = sweepSpeed;
            return this;
        }

        public Builder rotationSpeed(float rotationSpeed) {
            CircularProgressBarUtils.checkSpeed(rotationSpeed);
            mRotationSpeed = rotationSpeed;
            return this;
        }

        public Builder minSweepAngle(int minSweepAngle) {
            CircularProgressBarUtils.checkAngle(minSweepAngle);
            mMinSweepAngle = minSweepAngle;
            return this;
        }

        public Builder maxSweepAngle(int maxSweepAngle) {
            CircularProgressBarUtils.checkAngle(maxSweepAngle);
            mMaxSweepAngle = maxSweepAngle;
            return this;
        }

        public Builder strokeWidth(float strokeWidth) {
            CircularProgressBarUtils.checkPositiveOrZero(strokeWidth, "StrokeWidth");
            mStrokeWidth = strokeWidth;
            return this;
        }

        public Builder style(Style style) {
            CircularProgressBarUtils.checkNotNull(style, "Style");
            mStyle = style;
            return this;
        }

        public Builder sweepInterpolator(Interpolator interpolator) {
            CircularProgressBarUtils.checkNotNull(interpolator, "Sweep interpolator");
            mSweepInterpolator = interpolator;
            return this;
        }

        public Builder angleInterpolator(Interpolator interpolator) {
            CircularProgressBarUtils.checkNotNull(interpolator, "Angle interpolator");
            mAngleInterpolator = interpolator;
            return this;
        }

        public CircularProgressDrawable build() {
            return new CircularProgressDrawable(mColors,
                    mStrokeWidth,
                    mSweepSpeed,
                    mRotationSpeed,
                    mMinSweepAngle,
                    mMaxSweepAngle,
                    mStyle,
                    mAngleInterpolator,
                    mSweepInterpolator);
        }
    }
}