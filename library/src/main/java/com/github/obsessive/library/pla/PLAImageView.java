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

package com.github.obsessive.library.pla;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * This view will auto determine the width or height by determining if the
 * height or width is set and scale the other dimension depending on the images
 * dimension
 * <p/>
 * This view also contains an ImageChangeListener which calls changed(boolean
 * isEmpty) once a change has been made to the ImageView
 *
 * @author Maurycy Wojtowicz
 */
public class PLAImageView extends ImageView {
    private Bitmap currentBitmap;
    private ImageChangeListener imageChangeListener;
    private boolean scaleToWidth = false; // this flag determines if should
    // measure height manually dependent
    // of width

    public PLAImageView(Context context) {
        super(context);
        init();
    }

    public PLAImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PLAImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setScaleType(ScaleType.CENTER_INSIDE);
    }

    public void recycle() {
        setImageBitmap(null);
        if ((this.currentBitmap == null) || (this.currentBitmap.isRecycled()))
            return;
        this.currentBitmap.recycle();
        this.currentBitmap = null;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        currentBitmap = bm;
        super.setImageBitmap(currentBitmap);
        if (imageChangeListener != null)
            imageChangeListener.changed((currentBitmap == null));
    }

    @Override
    public void setImageDrawable(Drawable d) {
        super.setImageDrawable(d);
        if (imageChangeListener != null)
            imageChangeListener.changed((d == null));
    }

    @Override
    public void setImageResource(int id) {
        super.setImageResource(id);
    }

    public interface ImageChangeListener {
        // a callback for when a change has been made to this imageView
        void changed(boolean isEmpty);
    }

    public ImageChangeListener getImageChangeListener() {
        return imageChangeListener;
    }

    public void setImageChangeListener(ImageChangeListener imageChangeListener) {
        this.imageChangeListener = imageChangeListener;
    }

    private int imageWidth;
    private int imageHeight;

    public void setImageWidth(int w) {
        imageWidth = w;
    }

    public void setImageHeight(int h) {
        imageHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        /**
         * if both width and height are set scale width first. modify in future
         * if necessary
         */

        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            scaleToWidth = true;
        } else if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            scaleToWidth = false;
        } else
            throw new IllegalStateException("width or height needs to be set to match_parent or a specific dimension");

        if (imageWidth == 0) {
            // nothing to measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        } else {
            if (scaleToWidth) {
                int iw = imageWidth;
                int ih = imageHeight;
                int heightC = width * ih / iw;
                if (height > 0)
                    if (heightC > height) {
                        // dont let hegiht be greater then set max
                        heightC = height;
                        width = heightC * iw / ih;
                    }

                this.setScaleType(ScaleType.CENTER_CROP);
                setMeasuredDimension(width, heightC);

            } else {
                // need to scale to height instead
                int marg = 0;
                if (getParent() != null) {
                    if (getParent().getParent() != null) {
                        marg += ((RelativeLayout) getParent().getParent()).getPaddingTop();
                        marg += ((RelativeLayout) getParent().getParent()).getPaddingBottom();
                    }
                }

                int iw = imageWidth;
                int ih = imageHeight;

                width = height * iw / ih;
                height -= marg;
                setMeasuredDimension(width, height);
            }

        }
    }

}
