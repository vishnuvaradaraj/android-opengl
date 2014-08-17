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

package com.parabay.cinema.media.app;

import android.graphics.Bitmap;

import com.parabay.cinema.media.app.SlideshowPage.Slide;
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

public class SlideshowDataAdapter implements SlideshowPage.Model {
    @SuppressWarnings("unused")
    private static final String TAG = "SlideshowDataAdapter";

    private static final int IMAGE_QUEUE_CAPACITY = 3;

    public interface SlideshowSource {
        public void addContentListener(ContentListener listener);
        public void removeContentListener(ContentListener listener);
        public long reload();
        public MediaItem getMediaItem(int index);
        public int findItemIndex(Path path, int hint);
    }

    private final SlideshowSource mSource;

    private int mLoadIndex = 0;
    private int mNextOutput = 0;
    private boolean mIsActive = false;
    private boolean mNeedReset;
    private boolean mDataReady;
    private Path mInitialPath;

    private final LinkedList<Slide> mImageQueue = new LinkedList<Slide>();

    private Future<Void> mReloadTask;
    private final ThreadPool mThreadPool;

    private long mDataVersion = MediaObject.INVALID_DATA_VERSION;
    private final AtomicBoolean mNeedReload = new AtomicBoolean(false);
    private final SourceListener mSourceListener = new SourceListener();

    // The index is just a hint if initialPath is set
    public SlideshowDataAdapter(GalleryContext app, SlideshowSource source, int index,
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
                synchronized (SlideshowDataAdapter.this) {
                    while (mIsActive && (!mDataReady
                            || mImageQueue.size() >= IMAGE_QUEUE_CAPACITY)) {
                        try {
                            SlideshowDataAdapter.this.wait();
                        } catch (InterruptedException ex) {
                            // ignored.
                        }
                        continue;
                    }
                }
                if (!mIsActive) return null;
                mNeedReset = false;

                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                
                MediaItem item = loadItem();

                if (mNeedReset) {
                    synchronized (SlideshowDataAdapter.this) {
                        mImageQueue.clear();
                        mLoadIndex = mNextOutput;
                    }
                    continue;
                }

                if (item == null) {
                    synchronized (SlideshowDataAdapter.this) {
                        if (!mNeedReload.get()) mDataReady = false;
                        SlideshowDataAdapter.this.notifyAll();
                    }
                    continue;
                }

                Bitmap bitmap = item
                        .requestImage(MediaItem.TYPE_THUMBNAIL)
                        .run(jc);

                if (bitmap != null) {
                    synchronized (SlideshowDataAdapter.this) {
                        mImageQueue.addLast(
                                new Slide(item, mLoadIndex, bitmap));
                        if (mImageQueue.size() == 1) {
                            SlideshowDataAdapter.this.notifyAll();
                        }
                    }
                }
                ++mLoadIndex;
            }
        }
    }

    private class SourceListener implements ContentListener {
        public void onContentDirty() {
            synchronized (SlideshowDataAdapter.this) {
                mNeedReload.set(true);
                mDataReady = true;
                SlideshowDataAdapter.this.notifyAll();
            }
        }
    }

    private synchronized Slide innerNextBitmap() {
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
        Slide ret = mImageQueue.removeFirst();
        Log.i(TAG, ret.toString());
        return ret;
    }

    public Future<Slide> nextSlide(FutureListener<Slide> listener) {
        return mThreadPool.submit(new Job<Slide>() {
            public Slide run(JobContext jc) {
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