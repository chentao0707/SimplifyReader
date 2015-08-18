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

package com.github.obsessive.simplifyreader.bean;

import java.util.List;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/16.
 * Description:
 */
public class ResponseMusicsListentity {
    private int r;
    private int version_max;
    private int is_show_quick_start;
    private List<MusicsListEntity> song;

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getVersion_max() {
        return version_max;
    }

    public void setVersion_max(int version_max) {
        this.version_max = version_max;
    }

    public int getIs_show_quick_start() {
        return is_show_quick_start;
    }

    public void setIs_show_quick_start(int is_show_quick_start) {
        this.is_show_quick_start = is_show_quick_start;
    }

    public List<MusicsListEntity> getSong() {
        return song;
    }

    public void setSong(List<MusicsListEntity> song) {
        this.song = song;
    }
}
