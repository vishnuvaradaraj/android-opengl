/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parabay.cinema.media.util;

// This is an array whose index ranges from min to max (inclusive).
public class RangeIntArray {
    private int[] mData;
    private int mOffset;

    public RangeIntArray(int min, int max) {
        mData = new int[max - min + 1];
        mOffset = min;
    }

    // Wraps around an existing array
    public RangeIntArray(int[] src, int min, int max) {
        mData = src;
        mOffset = min;
    }

    public void put(int i, int object) {
        mData[i - mOffset] = object;
    }

    public int get(int i) {
        return mData[i - mOffset];
    }

    public int indexOf(int object) {
        for (int i = 0; i < mData.length; i++) {
            if (mData[i] == object) return i + mOffset;
        }
        return Integer.MAX_VALUE;
    }
}