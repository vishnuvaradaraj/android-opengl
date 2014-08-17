package com.parabay.cinema.test;

import com.parabay.cinema.R;
import com.parabay.cinema.ui.Base3DActivity;

import android.os.Bundle;
import android.view.Menu;

public class PlanesActivity extends Base3DActivity {
private PlanesRenderer mRenderer;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRenderer = new PlanesRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		super.setRenderer(mRenderer);
		initLoader();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
