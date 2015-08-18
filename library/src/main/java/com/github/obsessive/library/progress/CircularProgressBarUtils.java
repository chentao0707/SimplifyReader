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

import android.animation.ValueAnimator;

import static java.lang.Math.min;

/**
 * Created by castorflex on 8/14/14.
 */
class CircularProgressBarUtils {

  private CircularProgressBarUtils() {
  }

  static void checkSpeed(float speed) {
    if (speed <= 0f)
      throw new IllegalArgumentException("Speed must be >= 0");
  }

  static void checkColors(int[] colors) {
    if (colors == null || colors.length == 0)
      throw new IllegalArgumentException("You must provide at least 1 color");
  }

  static void checkAngle(int angle) {
    if (angle < 0 || angle > 360)
      throw new IllegalArgumentException(String.format("Illegal angle %d: must be >=0 and <= 360", angle));
  }

  static void checkPositiveOrZero(float number, String name) {
    if (number < 0)
      throw new IllegalArgumentException(String.format("%s %d must be positive", name, number));
  }

  static void checkPositive(int number, String name) {
    if (number <= 0)
      throw new IllegalArgumentException(String.format("%s must not be null", name));
  }

  static void checkNotNull(Object o, String name) {
    if (o == null)
      throw new IllegalArgumentException(String.format("%s must be not null", name));
  }

  static float getAnimatedFraction(ValueAnimator animator) {
    float fraction = animator.getDuration() > 0 ? ((float) animator.getCurrentPlayTime()) / animator.getDuration() : 0f;

    fraction = min(fraction, 1f);
    fraction = animator.getInterpolator().getInterpolation(fraction);
    return fraction;
  }
}
