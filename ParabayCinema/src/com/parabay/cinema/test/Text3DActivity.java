package com.parabay.cinema.test;

import com.parabay.cinema.ui.Base3DActivity;

import android.content.Intent;
import android.os.Bundle;

public class Text3DActivity extends Base3DActivity {
	private Text3DRenderer mRenderer;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		mRenderer = new Text3DRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
				
		super.setRenderer(mRenderer);
		initLoader();
	}
}
