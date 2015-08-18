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

package com.github.obsessive.simplifyreader.ui.activity.qrcode.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 */
public class InactivityTimer {

	private static final String TAG = InactivityTimer.class.getSimpleName();

	private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

	private Activity activity;
	private BroadcastReceiver powerStatusReceiver;
	private boolean registered;
	private AsyncTask<Object, Object, Object> inactivityTask;

	public InactivityTimer(Activity activity) {
		this.activity = activity;
		powerStatusReceiver = new PowerStatusReceiver();
		registered = false;
		onActivity();
	}

	@SuppressLint("NewApi")
	public synchronized void onActivity() {
		cancel();
		inactivityTask = new InactivityAsyncTask();
		if (Build.VERSION.SDK_INT >= 11) {
			inactivityTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			inactivityTask.execute();
		}
	}

	public synchronized void onPause() {
		cancel();
		if (registered) {
			activity.unregisterReceiver(powerStatusReceiver);
			registered = false;
		} else {
			Log.w(TAG, "PowerStatusReceiver was never registered?");
		}
	}

	public synchronized void onResume() {
		if (registered) {
			Log.w(TAG, "PowerStatusReceiver was already registered?");
		} else {
			activity.registerReceiver(powerStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			registered = true;
		}
		onActivity();
	}

	private synchronized void cancel() {
		AsyncTask<?, ?, ?> task = inactivityTask;
		if (task != null) {
			task.cancel(true);
			inactivityTask = null;
		}
	}

	public void shutdown() {
		cancel();
	}

	private class PowerStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 0 indicates that we're on battery
				boolean onBatteryNow = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) <= 0;
				if (onBatteryNow) {
					InactivityTimer.this.onActivity();
				} else {
					InactivityTimer.this.cancel();
				}
			}
		}
	}

	private class InactivityAsyncTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... objects) {
			try {
				Thread.sleep(INACTIVITY_DELAY_MS);
				Log.i(TAG, "Finishing activity due to inactivity");
				activity.finish();
			} catch (InterruptedException e) {
				// continue without killing
			}
			return null;
		}
	}

}
