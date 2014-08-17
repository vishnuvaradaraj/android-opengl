package com.parabay.cinema.facebook;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.facebook.android.R;

public class BitmapDisplayer implements Runnable {
    Bitmap mBitmap;

    ImageView mImageView;

    public BitmapDisplayer(Bitmap bitmap, ImageView imageView) {
        mBitmap = bitmap;
        mImageView = imageView;
    }

    public void run() {
        
        if (mBitmap != null) {
            mImageView.setImageBitmap(mBitmap);
        } else {
            //mImageView.setImageResource(R.drawable.background);
        }
    }
}