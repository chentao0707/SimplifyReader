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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseIntArray;

/**
 * Created by juyeong on 7/15/14.
 */
public class ParcelableSparseIntArray extends SparseIntArray implements Parcelable {

    public ParcelableSparseIntArray() {
    }

    public ParcelableSparseIntArray(int initialCapacity) {
        super(initialCapacity);
    }

    @SuppressWarnings("unchecked")
    private ParcelableSparseIntArray(Parcel in) {
        append(in.readSparseArray (ClassLoader.getSystemClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSparseArray(toSparseArray());
    }

    private SparseArray<Object> toSparseArray() {
        SparseArray<Object> sparseArray = new SparseArray<Object>();
        for (int i = 0, size = size(); i < size; i++)
            sparseArray.append(keyAt(i), valueAt(i));
        return sparseArray;
    }

    private void append(SparseArray<Integer> sparseArray) {
        for (int i = 0, size = sparseArray.size(); i < size; i++)
            put(sparseArray.keyAt(i), sparseArray.valueAt(i));
    }

    public static final Creator<ParcelableSparseIntArray> CREATOR = new Creator<ParcelableSparseIntArray>() {
        public ParcelableSparseIntArray createFromParcel(Parcel source) {
            return new ParcelableSparseIntArray(source);
        }

        public ParcelableSparseIntArray[] newArray(int size) {
            return new ParcelableSparseIntArray[size];
        }
    };
}
