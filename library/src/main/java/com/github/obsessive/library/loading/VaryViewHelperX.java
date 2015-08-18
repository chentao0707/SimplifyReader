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
package com.github.obsessive.library.loading;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;


public class VaryViewHelperX implements IVaryViewHelper {

    private IVaryViewHelper helper;
    private View view;

    public VaryViewHelperX(View view) {
        super();
        this.view = view;
        ViewGroup group = (ViewGroup) view.getParent();
        LayoutParams layoutParams = view.getLayoutParams();
        FrameLayout frameLayout = new FrameLayout(view.getContext());
        group.removeView(view);
        group.addView(frameLayout, layoutParams);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        View floatView = new View(view.getContext());
        frameLayout.addView(view, params);
        frameLayout.addView(floatView, params);
        helper = new VaryViewHelper(floatView);
    }

    @Override
    public View getCurrentLayout() {
        return helper.getCurrentLayout();
    }

    @Override
    public void restoreView() {
        helper.restoreView();
    }

    @Override
    public void showLayout(View view) {
        helper.showLayout(view);
    }

    @Override
    public View inflate(int layoutId) {
        return helper.inflate(layoutId);
    }

    @Override
    public Context getContext() {
        return helper.getContext();
    }

    @Override
    public View getView() {
        return view;
    }
}
