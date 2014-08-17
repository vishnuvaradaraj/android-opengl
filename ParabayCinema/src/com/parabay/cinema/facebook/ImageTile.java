package com.parabay.cinema.facebook;


import com.parabay.cinema.R;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.widget.ImageView;

public class ImageTile {

	public static final String TAG = "ImageTile";
	
	private Activity activity;
	
	public ImageTile(Activity activity) {
		
		this.activity = activity;
		
	}
	
	public Bitmap getTile(int index) {
		
		Bitmap background = BitmapFactory.decodeResource(activity.getResources(), R.drawable.flickrpics);

		Bitmap result = Bitmap.createBitmap(
                1024, 1024, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        Rect fullSize = new Rect(0,0, 1024, 1024);
        canvas.drawBitmap(background, null, fullSize, null);
        
		int count = 0;
		Cursor mCursor = FacebookGlobals.getUserDatabase().getAllMedias();
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String url = mCursor.getString(5);
                    if (!url.equals("")) {
                    	
                    	//do something
                    	Log.i("FB", "URL=" + url);
                    	
                    	Bitmap source = FacebookGlobals.getImageManager().getBitmap(url);
            		    Rect size = new Rect(0,0, 255, 255);
            		    Bitmap cropped = this.cropImage(source, size);
            		    
            		    this.paintTile(canvas, cropped, count);
                    }
                } while (mCursor.moveToNext() && count++ < 16);
            }
        }
	    
	    ImageView imageView = (ImageView) activity.findViewById(R.id.content_root);
	    BitmapDisplayer bitmapDisplay = new BitmapDisplayer(result, imageView);
        activity.runOnUiThread(bitmapDisplay);

		return result;
	}

	protected void paintTile(Canvas canvas, Bitmap src, int index) {
		
		int row = index /4;
		int column = index % 4;
		
		canvas.drawBitmap(src, column * 256, row * 256, null);

	}
		

	protected Bitmap cropImage(Bitmap source, Rect rect) {
		
        Rect srcRect = new Rect(0, 0, source.getWidth(), source.getHeight());
        Rect dstRect = rect;

        Log.i(TAG, String.format("Source: (%d, %d)-(%d,%d)", srcRect.left, srcRect.top, srcRect.right, srcRect.bottom));
        
        Bitmap result = Bitmap.createBitmap(
        		dstRect.width(), dstRect.height(), Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        float hScale = rect.width()/(float)source.getWidth();
        float vScale = rect.height()/(float)source.getHeight();
        float scale = Math.max(hScale, vScale);
        
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(rect.width()/2 - source.getWidth()/2 * scale, rect.height()/2 - source.getHeight()/2 * scale);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(source, matrix, new Paint());
        
        return result;

	}
}
