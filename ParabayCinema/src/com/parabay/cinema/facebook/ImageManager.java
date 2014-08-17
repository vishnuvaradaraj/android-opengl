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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.parabay.cinema.R;
import com.parabay.cinema.facebook.Utility;

/**
 * Download multiple images asynchronously in the background
 */
public class ImageManager {
    private static final String TAG = "ImageManager";

    private final int mResId = R.drawable.background;

    private MemoryCache mMemoryCache = new MemoryCache();

    private FileCache mFileCache;

    private Map<ImageView, String> mImageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService mExecutorService;

    public ImageManager(Context context) {
        mFileCache = new FileCache(context);
        // Pre-allocate 5 Download Threads
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Get the image from cache if it's not empty ,otherwise get it from image
     * queue.
     * 
     * @param url The image url
     * @param imageView The ImageView to display the content
     */
    public void showImage(String url, ImageView imageView) {
        mImageViews.put(imageView, url);
        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            queueImage(url, imageView);
            imageView.setImageResource(mResId);
        }
    }

    /**
     * Put the images in queue.
     * 
     * @param url The image url
     * @param imageView The ImageView to display the content
     */
    private void queueImage(String url, ImageView imageView) {
        ImagesToLoad mImages = new ImagesToLoad(url, imageView);
        mExecutorService.submit(new ImageLoader(mImages));
    }

    /**
     * Get the image from SD cache ,otherwise obtain it from web
     * 
     * @param url The path of image url
     * @return Return bitmap or null if it's empty
     */
    public Bitmap getBitmap(String url) {
        File file = mFileCache.getFile(url);
        InputStream inStream = null;
        OutputStream outStream = null;

        // Get image from SD cache
        Bitmap mBitmap = decodeFile(file);
        if (mBitmap != null) {
            return mBitmap;
        }
        // Get image from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            inStream = conn.getInputStream();
            outStream = new FileOutputStream(file);
            // copy the image to local cache
            Utility.copyStream(inStream, outStream);
            outStream.close();
            bitmap = decodeFile(file);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Could not obtain the content", e);
            return null;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not obtain the content", e);
            }

        }
    }

    /**
     * Decodes image and scales it to reduce memory consumption and thus avoid
     * Out of Memory errors
     * 
     * @param file The file to decode
     * @return Returns scaled and decoded bitmap ,otherwise null
     */
    private Bitmap decodeFile(File file) {
        try {

            // decode image size
            BitmapFactory.Options optios1 = new BitmapFactory.Options();
            optios1.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, optios1);

            // Find the correct scale value. It should be a power of 2.
            final int REQUIRED_SIZE = 256;
            int tmp_width = optios1.outWidth, tmp_height = optios1.outHeight;
            int scale = 1;
            while (true) {
                if (tmp_width / 2 < REQUIRED_SIZE || tmp_height / 2 < REQUIRED_SIZE) {
                    break;
                }
                tmp_width /= 2;
                tmp_height /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, options2);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot decode file: " + file);
            Log.e(TAG, "Exception" + e);
        }
        return null;
    }

    /**
     * Reuse the ImageView
     * 
     * @param imageToLoad The image queue
     * @return Return true if the url is empty ,false otherwise
     */
    private boolean imageViewReused(ImagesToLoad imageToLoad) {
        String tag = mImageViews.get(imageToLoad.mImageView);
        if (tag == null || !tag.equals(imageToLoad.url)) {
            return true;
        }
        return false;
    }

    /**
     * Clear the caches
     */
    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

    /**
     * Task for the image queue
     */
    private class ImagesToLoad {
        public String url;

        public ImageView mImageView;

        public ImagesToLoad(String u, ImageView i) {
            url = u;
            mImageView = i;
        }
    }

    /**
     * Load the images in a separate thread
     */
    private class ImageLoader implements Runnable {
        ImagesToLoad mImageToLoad;

        ImageLoader(ImagesToLoad photoToLoad) {
            this.mImageToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(mImageToLoad)) {
                return;
            }
            Bitmap bitmap = getBitmap(mImageToLoad.url);
            mMemoryCache.put(mImageToLoad.url, bitmap);
            if (imageViewReused(mImageToLoad)) {
                return;
            }
            BitmapDisplayer bitmapDisplay = new BitmapDisplayer(bitmap, mImageToLoad);
            Activity activity = (Activity)mImageToLoad.mImageView.getContext();
            activity.runOnUiThread(bitmapDisplay);
        }
    }

    /**
     * Display bitmap in the UI thread
     */
    private class BitmapDisplayer implements Runnable {
        Bitmap mBitmap;

        ImagesToLoad mImageToLoad;

        public BitmapDisplayer(Bitmap bitmap, ImagesToLoad image) {
            mBitmap = bitmap;
            mImageToLoad = image;
        }

        public void run() {
            if (imageViewReused(mImageToLoad)) {
                return;
            }
            if (mBitmap != null) {
                mImageToLoad.mImageView.setImageBitmap(mBitmap);
            } else {
                mImageToLoad.mImageView.setImageResource(mResId);
            }
        }
    }

}
