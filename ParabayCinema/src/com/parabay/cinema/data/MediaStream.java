package com.parabay.cinema.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.parabay.cinema.R;
import com.parabay.cinema.media.app.FilterUtils;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.media.app.GalleryContext;
import com.parabay.cinema.media.app.Log;
import com.parabay.cinema.media.app.SlideshowDataAdapter;
import com.parabay.cinema.media.app.SlideshowPage.Slide;
import com.parabay.cinema.media.common.Utils;
import com.parabay.cinema.media.data.ContentListener;
import com.parabay.cinema.media.data.DataManager;
import com.parabay.cinema.media.data.MediaItem;
import com.parabay.cinema.media.data.MediaObject;
import com.parabay.cinema.media.data.MediaSet;
import com.parabay.cinema.media.data.Path;
import com.parabay.cinema.media.util.Future;
import com.parabay.cinema.media.util.FutureListener;
import com.parabay.cinema.ui.MainRenderer;

public class MediaStream implements DataStream {

	private static final String TAG = "PhotoStream";

	private DataSource mModel;
	private boolean mIsActive = false;
	private TextureManager mTextureManager;
	private Handler mHandler;
		
	public void init(GalleryApp context, TextureManager textureManager, int typeOf,  boolean random, String path) {
		        
		boolean repeat = true;
		mTextureManager = textureManager;
		
        // We only want to show slideshow for images only, not videos.		
        DataManager manager = context.getDataManager();
        
        int filterType = FilterUtils.FILTER_IMAGE_ONLY;
        if (typeOf == DataManager.INCLUDE_VIDEO) {
        	filterType = FilterUtils.FILTER_VIDEO_ONLY;
        }
        
        String pathString = path;
        if (null == pathString) 
        	pathString = manager.getTopSetPath(typeOf);
        
        Path pathDefault = Path.fromString(pathString);
        String mediaPath = pathDefault.toString();
        mediaPath = FilterUtils.newFilterPath(mediaPath, filterType);
        
        MediaSet mediaSet = context.getDataManager().getMediaSet(mediaPath);

        if (random) {
            mModel = new DataSourceAdapter(context,
                    new ShuffleSource(mediaSet, repeat), 0, null);
        } else {
            mModel = new DataSourceAdapter(context, new SequentialSource(mediaSet, repeat),
                    0, pathDefault);
        }

	}
	
	@Override
	public void pause() {
		
		mIsActive = false;
		mModel.pause();
	}

	@Override
	public void resume() {
		
		mIsActive = true;
		mModel.resume();
	}

	@Override
	public Future<DataItem> nextItem(FutureListener<DataItem> listener) {

		return mModel.nextItem(listener);
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
    
    private static class ShuffleSource implements DataSourceSequencer {
    	
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

    private static class SequentialSource implements DataSourceSequencer {
    	
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
