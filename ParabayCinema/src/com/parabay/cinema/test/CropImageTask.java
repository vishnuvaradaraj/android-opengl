package com.parabay.cinema.test;

import com.parabay.cinema.facebook.BitmapDisplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.FloatMath;
import android.util.Log;
import android.widget.ImageView;

public class CropImageTask extends AsyncTask<Void, Void, Boolean> {
	
	public static final String TAG = "MainActivity";
    private static final int FACE_PIXEL_COUNT = 120000; // around 400x300

	private Bitmap cropped;
			
    public static final float UNSPECIFIED = -1f;
    
    private static final int MAX_FACE_COUNT = 3;
    private static final float FACE_EYE_RATIO = 2f;

	private FaceDetector.Face[] mFaces = new FaceDetector.Face[MAX_FACE_COUNT];
	private Bitmap mSrcBitmap = null;
	private Bitmap mFaceBitmap = null;
	private int mFaceCount;
    private float mAspectRatio = UNSPECIFIED;
    
    private RectF mFaceRect;
    private MainActivity activity;
    
    public CropImageTask(Bitmap bmp, MainActivity activity) {
    	
    	this.mSrcBitmap = bmp;
    	this.activity = activity;
    }
    
	@Override
	protected Boolean doInBackground(Void... params) {
        
		Bitmap bitmap = mSrcBitmap;
        
		mFaceRect = this.detectFaces(bitmap);
		
		Rect size = new Rect(0,0, 255, 255);
		//Rect size = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    cropped = this.cropImage(bitmap, size);
	    
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		

		if (success) {
			//finish();
			
			ImageView imageView = null; //TODO:vishnuv set this
			BitmapDisplayer bitmapDisplay = new BitmapDisplayer(cropped, imageView);
            Activity activity = (Activity)imageView.getContext();
            activity.runOnUiThread(bitmapDisplay);
                        
            
		} else {
			
		}
	}

	@Override
	protected void onCancelled() {
		
		
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
        
        /*
        float minScale = Math.min(source.getWidth() / rect.width(), source.getHeight() / rect.height());

        int dx = (srcRect.width() - dstRect.width()) / 2;
        int dy = (srcRect.height() - dstRect.height()) / 2;
        Log.i(TAG, String.format("pre dx: %d, dy: %d, dx/dy: %f", dx, dy, ((float)dx/(float)dy) ));

        if (dx > dy) {
        	dx -= dy;
        	dy = 0;
        }
        else if (dy > dx) {
        	dy -= dx;
        	dx = 0;
        }

        Log.i(TAG, String.format("post dx: %d, dy: %d, dx/dy: %f", dx, dy, ((float)dx/(float)dy) ));

        srcRect.inset(Math.max(0, dx), Math.max(0, dy));
        dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

        Log.i(TAG, String.format("src: (%d, %d)-(%d,%d)", srcRect.left, srcRect.top, srcRect.right, srcRect.bottom));
        Log.i(TAG, String.format("dest: (%d, %d)-(%d,%d)", dstRect.left, dstRect.top, dstRect.right, dstRect.bottom));

        Bitmap result = Bitmap.createBitmap(
        		dstRect.width(), dstRect.height(), Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
               
        canvas.drawBitmap(source, srcRect, dstRect, null);
        
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(0, 0, 0));
        myPaint.setStrokeWidth(3);
        myPaint.setStyle(Paint.Style.STROKE);
        
        canvas.drawRect(this.mFaceRect, myPaint);
        */
        
        return result;

	}
	
    public RectF detectFaces(Bitmap bitmap) {
    	
        int rotation = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = FloatMath.sqrt((float) FACE_PIXEL_COUNT / (width * height));
        
        // faceBitmap is a correctly rotated bitmap, as viewed by a user.
        Bitmap faceBitmap;
        if (((rotation / 90) & 1) == 0) {
            int w = (Math.round(width * scale) & ~1); // must be even
            int h = Math.round(height * scale);
            faceBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
            Canvas canvas = new Canvas(faceBitmap);
            canvas.rotate(rotation, w / 2, h / 2);
            canvas.scale((float) w / width, (float) h / height);
            canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
            	            
        } else {
            int w = (Math.round(height * scale) & ~1); // must be even
            int h = Math.round(width * scale);
            faceBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
            Canvas canvas = new Canvas(faceBitmap);
            canvas.translate(w / 2, h / 2);
            canvas.rotate(rotation);
            canvas.translate(-h / 2, -w / 2);
            canvas.scale((float) w / height, (float) h / width);
            canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        }
        
        //save input to face detector
        mFaceBitmap = bitmap;

        FaceDetector detector = new FaceDetector(
        		faceBitmap.getWidth(), faceBitmap.getHeight(), MAX_FACE_COUNT);
        mFaceCount = detector.findFaces(faceBitmap, mFaces);

        RectF faceRect = new RectF(0f, 0f, width, height);
        for (int i = 0, n = mFaceCount; i < n; ++i) {
        	
        	RectF fr = getFaceRect(mFaces[i]);
        	RectF mfr = mapRect(bitmap, fr);
        	
        	Log.i(TAG, String.format("Found face %d: %s, %s", i, fr.toString(), mfr.toString() ));
       
            faceRect.intersect(mfr);
        }
        
        Log.i(TAG, String.format("Found face rect: %s (%d)", faceRect.toString(), mFaceCount ));	        
        return faceRect;
    }
    
    private RectF mapRect(Bitmap bitmap, RectF input) {
    	
    	RectF output = new RectF();
    	
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        output.set(
                (input.left * width),
                (input.top * height),
                (input.right * width),
                (input.bottom * height));
        return output;
    }
    
    private RectF getFaceRect(FaceDetector.Face face) {
    	
        PointF point = new PointF();
        face.getMidPoint(point);

        int width = mFaceBitmap.getWidth();
        int height = mFaceBitmap.getHeight();
        float rx = face.eyesDistance() * FACE_EYE_RATIO;
        float ry = rx;
        float aspect = mAspectRatio;
        if (aspect != UNSPECIFIED) {
            if (aspect > 1) {
                rx = ry * aspect;
            } else {
                ry = rx / aspect;
            }
        }

        RectF r = new RectF(
                point.x - rx, point.y - ry, point.x + rx, point.y + ry);
        r.intersect(0, 0, width, height);

        if (aspect != UNSPECIFIED) {
            if (r.width() / r.height() > aspect) {
                float w = r.height() * aspect;
                r.left = (r.left + r.right - w) * 0.5f;
                r.right = r.left + w;
            } else {
                float h = r.width() / aspect;
                r.top =  (r.top + r.bottom - h) * 0.5f;
                r.bottom = r.top + h;
            }
        }

        r.left /= width;
        r.right /= width;
        r.top /= height;
        r.bottom /= height;

        return r;
    }
}