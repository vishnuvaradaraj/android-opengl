package com.parabay.cinema.test;

import com.parabay.cinema.ui.Base3DActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class BlenderLoadModelActivity extends Base3DActivity implements OnTouchListener{
	private BlenderLoadModelRenderer mRenderer;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRenderer = new BlenderLoadModelRenderer(this);
        mRenderer.setSurfaceView(mSurfaceView);
        super.setRenderer(mRenderer);
        mSurfaceView.setOnTouchListener(this);
        initLoader();
        
    }
    
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mRenderer.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
