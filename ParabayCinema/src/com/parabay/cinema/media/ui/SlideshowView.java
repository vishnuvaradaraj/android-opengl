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

package com.parabay.cinema.media.ui;

import java.util.Random;

import com.parabay.cinema.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class SlideshowView  {
	
    public SlideshowView(ImageView imageView) {	
		
    	mImageView = imageView;
	}

	@SuppressWarnings("unused")
    private static final String TAG = "SlideshowView";

    private static final float SCALE_SPEED = 0.20f ;
    private static final float MOVE_SPEED = SCALE_SPEED;

    private ImageView mImageView;
    
    private int mCurrentRotation;
    private Bitmap mCurrentTexture;

    private int mPrevRotation;
    private Bitmap mPrevTexture;

     private Random mRandom = new Random();

    public void next(Bitmap bitmap, int rotation) {

    	Log.i(TAG, "Rotation = " + String.valueOf(rotation));

        if (mPrevTexture != null) {
            mPrevTexture.recycle();
        }

        mPrevTexture = mCurrentTexture;
        mPrevRotation = mCurrentRotation;

        mCurrentRotation = rotation;
        mCurrentTexture = bitmap;

        //this.setImageResource(R.drawable.splash);
        mImageView.setImageBitmap(bitmap);
        mImageView.invalidate();
    }

    public void release() {
        if (mPrevTexture != null) {
            mPrevTexture.recycle();
            mPrevTexture = null;
        }
        if (mCurrentTexture != null) {
            mCurrentTexture.recycle();
            mCurrentTexture = null;
        }
    }

}
