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

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.widget.ImageView;

import com.parabay.cinema.media.common.Utils;
import com.parabay.cinema.media.data.ContentListener;
import com.parabay.cinema.media.data.DataManager;
import com.parabay.cinema.media.data.MediaItem;
import com.parabay.cinema.media.data.MediaObject;
import com.parabay.cinema.media.data.MediaSet;
import com.parabay.cinema.media.data.Path;
import com.parabay.cinema.media.ui.SlideshowView;
import com.parabay.cinema.media.util.Future;
import com.parabay.cinema.media.util.FutureListener;


public class SlideshowPage extends ActivityState {
	
    private static final String TAG = "SlideshowPage";

    public static final String KEY_SET_PATH = "media-set-path";
    public static final String KEY_ITEM_PATH = "media-item-path";
    public static final String KEY_PHOTO_INDEX = "photo-index";
    public static final String KEY_RANDOM_ORDER = "random-order";
    public static final String KEY_REPEAT = "repeat";
    public static final String KEY_DREAM = "dream";

    private static final long SLIDESHOW_DELAY = 3000; // 3 seconds

    private static final int MSG_LOAD_NEXT_BITMAP = 1;
    private static final int MSG_SHOW_PENDING_BITMAP = 2;

    private Handler mHandler;
    private Model mModel;
    private Slide mPendingSlide = null;
    private boolean mIsActive = false;
    private SlideshowView mSlideshowView;
    
    public static interface Model {
        public void pause();

        public void resume();

        public Future<Slide> nextSlide(FutureListener<Slide> listener);
    }

    public static class Slide {
    	
        public Bitmap bitmap;
        public MediaItem item;
        public int index;

        public Slide(MediaItem item, int index, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.item = item;
            this.index = index;
        }
        
        public String toString() {
        	StringBuilder result = new StringBuilder();
            String NEW_LINE = "\n";

            result.append(this.getClass().getName() + " Object {" + NEW_LINE);
            result.append(" item: " + item.toString() + NEW_LINE);
            result.append(" index: " + String.valueOf(index) + NEW_LINE );
            result.append("}");

            return result.toString();
        }
    }


	@Override
	protected void onCreate(Bundle data, Bundle restoreState) {
		super.onCreate(data, restoreState);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW_PENDING_BITMAP:
                        showPendingBitmap();
                        break;
                    case MSG_LOAD_NEXT_BITMAP:
                        loadNextBitmap();
                        break;
                    default: throw new AssertionError();
                }
            }
        };
        initializeData(data);
        initializeViews();

        //mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, SLIDESHOW_DELAY);
    }

    private void initializeViews() {
    	
    	ImageView imageView = (ImageView)mActivity.getGLRoot();
        mSlideshowView = new SlideshowView( (imageView) );
    }
    
    private void loadNextBitmap() {
        mModel.nextSlide(new FutureListener<Slide>() {
            public void onFutureDone(Future<Slide> future) {
                mPendingSlide = future.get();
                if (null != mPendingSlide) {
                	mHandler.sendEmptyMessage(MSG_SHOW_PENDING_BITMAP);
                }
            }
        });
    }

    private void showPendingBitmap() {
        // mPendingBitmap could be null, if
        // 1.) there is no more items
        // 2.) mModel is paused
        final Slide slide = mPendingSlide;
        if (slide == null) {
        	mIsActive = false;
            return;
        }

        mSlideshowView.next(slide.bitmap, slide.item.getRotation());

        mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, SLIDESHOW_DELAY);
    }


    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        mModel.pause();
        mSlideshowView.release();

        mHandler.removeMessages(MSG_LOAD_NEXT_BITMAP);
        mHandler.removeMessages(MSG_SHOW_PENDING_BITMAP);
    }
	
    
    @Override
    public void onResume() {
        super.onResume();
        mIsActive = true;
        mModel.resume();

        if (mPendingSlide != null) {
            showPendingBitmap();
        } else {
            loadNextBitmap();
        }
    }
    
    private void initializeData(Bundle data) {
        boolean random = true;
        
        if (null != data)
        	random = data.getBoolean(KEY_RANDOM_ORDER, false);
        
        // We only want to show slideshow for images only, not videos.		
        DataManager manager = mActivity.getDataManager();
        Path pathDefault = Path.fromString(
                manager.getTopSetPath(DataManager.INCLUDE_IMAGE));
        String mediaPath = pathDefault.toString();
        if (null != data)
        	mediaPath = data.getString(KEY_SET_PATH);
        
        mediaPath = FilterUtils.newFilterPath(mediaPath, FilterUtils.FILTER_IMAGE_ONLY);
        MediaSet mediaSet = mActivity.getDataManager().getMediaSet(mediaPath);

        if (random) {
            boolean repeat = true;
            
            if (null != data)
            	repeat = data.getBoolean(KEY_REPEAT);
            mModel = new SlideshowDataAdapter(mActivity,
                    new ShuffleSource(mediaSet, repeat), 0, null);
            //index=0;
        } else {
        	boolean repeat = true;
            int index = 0;//data.getInt(KEY_PHOTO_INDEX);
            String itemPath = null; //data.getString(KEY_ITEM_PATH);
            Path path = itemPath != null ? Path.fromString(itemPath) : pathDefault;
            if (null != data)
            	repeat = data.getBoolean(KEY_REPEAT);
            mModel = new SlideshowDataAdapter(mActivity, new SequentialSource(mediaSet, repeat),
                    index, path);
            //index = index
        }
    }
    
    private static MediaItem findMediaItem(MediaSet mediaSet, int index) {
        for (int i = 0, n = mediaSet.getSubMediaSetCount(); i < n; ++i) {
            MediaSet subset = mediaSet.getSubMediaSet(i);
            int count = subset.getTotalMediaItemCount();
            if (index < count) {
                return findMediaItem(subset, index);
            }
            index -= count;
        }
        ArrayList<MediaItem> list = mediaSet.getMediaItem(index, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    private static class ShuffleSource implements SlideshowDataAdapter.SlideshowSource {
        private static final int RETRY_COUNT = 5;
        private final MediaSet mMediaSet;
        private final Random mRandom = new Random();
        private int mOrder[] = new int[0];
        private final boolean mRepeat;
        private long mSourceVersion = MediaSet.INVALID_DATA_VERSION;
        private int mLastIndex = -1;

        public ShuffleSource(MediaSet mediaSet, boolean repeat) {
            mMediaSet = Utils.checkNotNull(mediaSet);
            mRepeat = repeat;
        }

        public int findItemIndex(Path path, int hint) {
            return hint;
        }

        public MediaItem getMediaItem(int index) {
            if (!mRepeat && index >= mOrder.length) return null;
            if (mOrder.length == 0) return null;
            mLastIndex = mOrder[index % mOrder.length];
            MediaItem item = findMediaItem(mMediaSet, mLastIndex);
            for (int i = 0; i < RETRY_COUNT && item == null; ++i) {
                Log.w(TAG, "fail to find image: " + mLastIndex);
                mLastIndex = mRandom.nextInt(mOrder.length);
                item = findMediaItem(mMediaSet, mLastIndex);
            }
            return item;
        }

        public long reload() {
            long version = mMediaSet.reload();
            if (version != mSourceVersion) {
                mSourceVersion = version;
                int count = mMediaSet.getTotalMediaItemCount();
                if (count != mOrder.length) generateOrderArray(count);
            }
            return version;
        }

        private void generateOrderArray(int totalCount) {
            if (mOrder.length != totalCount) {
                mOrder = new int[totalCount];
                for (int i = 0; i < totalCount; ++i) {
                    mOrder[i] = i;
                }
            }
            for (int i = totalCount - 1; i > 0; --i) {
                Utils.swap(mOrder, i, mRandom.nextInt(i + 1));
            }
            if (mOrder[0] == mLastIndex && totalCount > 1) {
                Utils.swap(mOrder, 0, mRandom.nextInt(totalCount - 1) + 1);
            }
        }

        public void addContentListener(ContentListener listener) {
            mMediaSet.addContentListener(listener);
        }

        public void removeContentListener(ContentListener listener) {
            mMediaSet.removeContentListener(listener);
        }
    }

    private static class SequentialSource implements SlideshowDataAdapter.SlideshowSource {
        private static final int DATA_SIZE = 32;

        private ArrayList<MediaItem> mData = new ArrayList<MediaItem>();
        private int mDataStart = 0;
        private long mDataVersion = MediaObject.INVALID_DATA_VERSION;
        private final MediaSet mMediaSet;
        private final boolean mRepeat;

        public SequentialSource(MediaSet mediaSet, boolean repeat) {
            mMediaSet = mediaSet;
            mRepeat = repeat;
        }

        public int findItemIndex(Path path, int hint) {
            return mMediaSet.getIndexOfItem(path, hint);
        }

        public MediaItem getMediaItem(int index) {
            int dataEnd = mDataStart + mData.size();

            if (mRepeat) {
                int count = mMediaSet.getMediaItemCount();
                if (count == 0) return null;
                index = index % count;
            }
            if (index < mDataStart || index >= dataEnd) {
                mData = mMediaSet.getMediaItem(index, DATA_SIZE);
                mDataStart = index;
                dataEnd = index + mData.size();
            }

            return (index < mDataStart || index >= dataEnd) ? null : mData.get(index - mDataStart);
        }

        public long reload() {
            long version = mMediaSet.reload();
            if (version != mDataVersion) {
                mDataVersion = version;
                mData.clear();
            }
            return mDataVersion;
        }

        public void addContentListener(ContentListener listener) {
            mMediaSet.addContentListener(listener);
        }

        public void removeContentListener(ContentListener listener) {
            mMediaSet.removeContentListener(listener);
        }
    }
}
