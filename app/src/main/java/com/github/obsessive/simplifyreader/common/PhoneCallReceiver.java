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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.github.obsessive.library.eventbus.EventCenter;

import de.greenrobot.event.EventBus;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/6/8.
 * Description:
 */
public class PhoneCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent) {
            return;
        }

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            EventBus.getDefault().post(new EventCenter(Constants.EVENT_STOP_PLAY_MUSIC));
        } else {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    switch (state) {
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                        case TelephonyManager.CALL_STATE_RINGING:
                            EventBus.getDefault().post(new EventCenter(Constants.EVENT_STOP_PLAY_MUSIC));

                            break;

                        case TelephonyManager.CALL_STATE_IDLE:
                            EventBus.getDefault().post(new EventCenter(Constants.EVENT_START_PLAY_MUSIC));

                            break;
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
