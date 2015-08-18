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

package com.github.obsessive.simplifyreader.common;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/5/28.
 * Description:
 */
public class Constants {

    public static final String KEY_MUSIC_PARCELABLE_DATA = "KEY_MUSIC_PARCELABLE_DATA";
    public static final String KEY_MUSIC_TOTAL_DURATION = "KEY_MUSIC_TOTAL_DURATION";
    public final static String KEY_MUSIC_CURRENT_DUTATION = "KEY_MUSIC_CURRENT_DUTATION";
    public final static String KEY_MUSIC_SECOND_PROGRESS = "KEY_MUSIC_SECOND_PROGRESS";
    public final static String KEY_PLAYER_SEEK_TO_PROGRESS = "KEY_PLAYER_SEEK_TO_PROGRESS";

    public static final String ACTION_MUSIC_BUNDLE_BROADCAST = "ACTION_MUSIC_BUNDLE_BROADCAST";
    public final static String ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST = "ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST";
    public final static String ACTION_MUSIC_SECOND_PROGRESS_BROADCAST = "ACTION_MUSIC_SECOND_PROGRESS_BROADCAST";

    public static final int EVENT_BEGIN = 0X100;
    public static final int EVENT_REFRESH_DATA = EVENT_BEGIN + 10;
    public static final int EVENT_LOAD_MORE_DATA = EVENT_BEGIN + 20;
    public static final int EVENT_START_PLAY_MUSIC = EVENT_BEGIN + 30;
    public static final int EVENT_STOP_PLAY_MUSIC = EVENT_BEGIN + 40;
}
