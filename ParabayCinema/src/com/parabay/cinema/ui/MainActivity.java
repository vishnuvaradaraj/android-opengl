package com.parabay.cinema.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parabay.cinema.R;
import com.parabay.cinema.SettingsActivity;
import com.parabay.cinema.data.MediaStream;
import com.parabay.cinema.media.app.Gallery;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.media.app.MovieActivity;
import com.parabay.cinema.test.PlanesActivity;

public class MainActivity extends Base3DActivity {

	private MainRenderer mRenderer;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
			
		mRenderer = new MainRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
				
		super.setRenderer(mRenderer);
		initLoader();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

    public static void playVideo(Activity activity, Uri uri, String title) {
        try {
            Intent intent = new Intent(activity, MovieActivity.class)
                    .setDataAndType(uri, "video/*")
                    .putExtra(Intent.EXTRA_TITLE, title)
                    .putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getString(R.string.video_err),
                    Toast.LENGTH_SHORT).show();
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	        	Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
	        	MainActivity.this.startActivity(myIntent);
	            return true;
	        case R.id.menu_slides:
	        	Intent slidesIntent = new Intent(MainActivity.this, Gallery.class);
	        	MainActivity.this.startActivity(slidesIntent);
	            return true;
	        case R.id.menu_planes:
	        	Intent planesIntent = new Intent(MainActivity.this, PlanesActivity.class);
	        	MainActivity.this.startActivity(planesIntent);
	            return true;	            
	        case R.id.menu_logout:
	        	GalleryApp app = (GalleryApp)MainActivity.this.getApplicationContext();
	        	app.getLoginService().logout();
	        	//playVideo(MainActivity.this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.webm"), "Video Title");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		mRenderer.onResume();
	}

	@Override
	protected void onPause() {
		
		super.onPause();
		mRenderer.onPause();
	}

	@Override
	protected void onStop() {
		
		super.onStop();
	}
	
	
}
