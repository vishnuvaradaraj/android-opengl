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

package com.parabay.cinema.facebook;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache the images in Memory
 */
class MemoryCache {

    // SoftReference are guaranteed to be cleared before the VM will throw an
    // OutOfMemoryError.

    private Map<String, SoftReference<Bitmap>> mCache = Collections
            .synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

    /**
     * @param key The image url
     * @return Return bitmap if the key is present ,otherwise null
     */
    public Bitmap get(String key) {
        if (!mCache.containsKey(key)) {
            return null;
        }
        SoftReference<Bitmap> ref = mCache.get(key);
        return ref.get();
    }

    /**
     * @param key The image url
     * @param bitmap The bitmap to put in HashMap
     */
    public void put(String key, Bitmap bitmap) {
        mCache.put(key, new SoftReference<Bitmap>(bitmap));
    }

    /**
     * Clear the cache
     */
    public void clear() {
        mCache.clear();
    }

}
