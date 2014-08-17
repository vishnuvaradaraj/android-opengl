package com.parabay.cinema.data;


/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.graphics.Bitmap;
import android.util.Log;

import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.media.app.GalleryContext;
import com.parabay.cinema.media.data.ContentListener;
import com.parabay.cinema.media.data.MediaItem;
import com.parabay.cinema.media.data.MediaObject;
import com.parabay.cinema.media.data.Path;
import com.parabay.cinema.media.util.Future;
import com.parabay.cinema.media.util.FutureListener;
import com.parabay.cinema.media.util.ThreadPool;
import com.parabay.cinema.media.util.ThreadPool.Job;
import com.parabay.cinema.media.util.ThreadPool.JobContext;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataSourceAdapter implements DataSource {
	
    @SuppressWarnings("unused")
    private static final String TAG = "DataSourceAdapter";

    private static final int IMAGE_QUEUE_CAPACITY = 3;

    private final DataSourceSequencer mSource;

    private int mLoadIndex = 0;
    private int mNextOutput = 0;
    private boolean mIsActive = false;
    private boolean mNeedReset;
    private boolean mDataReady;
    private Path mInitialPath;

    private final LinkedList<DataItem> mImageQueue = new LinkedList<DataItem>();

    private Future<Void> mReloadTask;
    private final ThreadPool mThreadPool;

    private long mDataVersion = MediaObject.INVALID_DATA_VERSION;
    private final AtomicBoolean mNeedReload = new AtomicBoolean(false);
    private final SourceListener mSourceListener = new SourceListener();

    // The index is just a hint if initialPath is set
    public DataSourceAdapter(GalleryApp app, DataSourceSequencer source, int index,
            Path initialPath) {
        mSource = source;
        mInitialPath = initialPath;
        mLoadIndex = index;
        mNextOutput = index;
        mThreadPool = app.getThreadPool();
    }

    private MediaItem loadItem() {
        if (mNeedReload.compareAndSet(true, false)) {
            long v = mSource.reload();
            if (v != mDataVersion) {
                mDataVersion = v;
                mNeedReset = true;
                return null;
            }
        }
        int index = mLoadIndex;
        if (mInitialPath != null) {
            index = mSource.findItemIndex(mInitialPath, index);
            mInitialPath = null;
        }
        return mSource.getMediaItem(index);
    }

    private class ReloadTask implements Job<Void> {
        public Void run(JobContext jc) {
            while (true) {
                synchronized (DataSourceAdapter.this) {
                    while (mIsActive && (!mDataReady
                            || mImageQueue.size() >= IMAGE_QUEUE_CAPACITY)) {
                        try {
                            DataSourceAdapter.this.wait();
                        } catch (InterruptedException ex) {
                            // ignored.
                        }
                        continue;
                    }
                }
                if (!mIsActive) return null;
                mNeedReset = false;

                try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                
                MediaItem item = loadItem();

                if (mNeedReset) {
                    synchronized (DataSourceAdapter.this) {
                        mImageQueue.clear();
                        mLoadIndex = mNextOutput;
                    }
                    continue;
                }

                if (item == null) {
                    synchronized (DataSourceAdapter.this) {
                        if (!mNeedReload.get()) mDataReady = false;
                        DataSourceAdapter.this.notifyAll();
                    }
                    continue;
                }

                Bitmap bitmap = item
                        .requestImage(MediaItem.TYPE_THUMBNAIL)
                        .run(jc);

                if (bitmap != null) {
                    synchronized (DataSourceAdapter.this) {
                        mImageQueue.addLast(
                                new DataItem(item, mLoadIndex, bitmap));
                        if (mImageQueue.size() == 1) {
                            DataSourceAdapter.this.notifyAll();
                        }
                    }
                }
                ++mLoadIndex;
            }
        }
    }

    private class SourceListener implements ContentListener {
        public void onContentDirty() {
            synchronized (DataSourceAdapter.this) {
                mNeedReload.set(true);
                mDataReady = true;
                DataSourceAdapter.this.notifyAll();
            }
        }
    }

    private synchronized DataItem innerNextBitmap() {
        while (mIsActive && mDataReady && mImageQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException t) {
                throw new AssertionError();
            }
        }
        if (mImageQueue.isEmpty()) return null;
        mNextOutput++;
        this.notifyAll();
        DataItem ret = mImageQueue.removeFirst();
        return ret;
    }

    public Future<DataItem> nextItem(FutureListener<DataItem> listener) {
        return mThreadPool.submit(new Job<DataItem>() {
            public DataItem run(JobContext jc) {
                jc.setMode(ThreadPool.MODE_NONE);
                return innerNextBitmap();
            }
        }, listener);
    }

    public void pause() {
        synchronized (this) {
            mIsActive = false;
            notifyAll();
        }
        mSource.removeContentListener(mSourceListener);
        mReloadTask.cancel();
        mReloadTask.waitDone();
        mReloadTask = null;
    }

    public synchronized void resume() {
        mIsActive = true;
        mSource.addContentListener(mSourceListener);
        mNeedReload.set(true);
        mDataReady = true;
        mReloadTask = mThreadPool.submit(new ReloadTask());
    }
}
