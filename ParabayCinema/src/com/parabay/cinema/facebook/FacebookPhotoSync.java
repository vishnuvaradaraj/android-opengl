package com.parabay.cinema.facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import com.facebook.android.R;

import com.facebook.android.FacebookError;

public class FacebookPhotoSync {

	public static final String TAG = "FacebookPhotoSync";
	
	private ProgressDialog dialog;
	private Activity activity;
	
	public FacebookPhotoSync(Activity activity) {
		this.activity = activity;		
	}
	
	public void loadPhotos() {
		
		if (null != this.activity) {
			dialog = ProgressDialog.show(this.activity, "",
               "Please wait", true, true);
		}
		
        String query = "SELECT pid, src_small, src_big, modified FROM photo " +
			"WHERE aid IN ( " +
			"  SELECT aid FROM album WHERE owner = me() " +
			"  OR owner IN (SELECT uid2 FROM friend WHERE uid1 = me()) " +
			") ORDER BY modified DESC LIMIT 16 offset 0";
        
        Bundle params = new Bundle();
        params.putString("method", "fql.query");
        params.putString("query", query);
        
        Utility.mAsyncRunner.request(null, params, new FQLRequestListener());

	}
	
    public class FQLRequestListener extends BaseRequestListener {
    	
    	protected Bitmap cropImage(Bitmap source, Rect rect) {
    		
            int outputX = rect.width();
            int outputY = rect.height();

            // (rect.width() * scaleX, rect.height() * scaleY) =
            // the size of drawing area in output bitmap
            float scaleX = 1;
            float scaleY = 1;
            
            Rect dest = new Rect(0, 0, outputX, outputY);
            scaleX = (float) outputX / rect.width();
            scaleY = (float) outputY / rect.height();

            // Keep the content in the center (or crop the content)
            int rectWidth = Math.round(rect.width() * scaleX);
            int rectHeight = Math.round(rect.height() * scaleY);
            dest.set(Math.round((outputX - rectWidth) / 2f),
                    Math.round((outputY - rectHeight) / 2f),
                    Math.round((outputX + rectWidth) / 2f),
                    Math.round((outputY + rectHeight) / 2f));

            
            Bitmap result = Bitmap.createBitmap(
                    outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(source, rect, dest, null);
            
            return result;

    	}
    	
    	protected void processResult(JSONArray result) throws JSONException {
    		
    		for (int i = 0; i < result.length(); i++) {
    			
    		    JSONObject row = result.getJSONObject(i);
    		    String url = row.getString("src_big");
    		    
    		    FacebookGlobals.getUserDatabase().insertMedia(row.getString("pid"), 
    		    		Utility.MEDIA_SOURCE_FACEBOOK, 
    		    		"me", 
    		    		row.getString("src_small"), 
    		    		row.getString("src_big"), 
    		    		Utility.MEDIA_TYPE_PHOTO,
    		    		row.getInt("modified"));
    		    
    		    FacebookGlobals.getImageManager().getBitmap(url);

    		}
    	}
    	
        @Override
        public void onComplete(final String response, final Object state) {
        	
        	if (null != FacebookPhotoSync.this.activity) {
        		dialog.dismiss();
        	}
        	
            /*
             * Output can be a JSONArray or a JSONObject.
             * Try JSONArray and if there's a JSONException, parse to JSONObject
             */
            try {
                JSONArray json = new JSONArray(response);
                
                this.processResult(json);
                
            } catch (JSONException e) {
                try {
                    /*
                     * JSONObject probably indicates there was some error
                     * Display that error, but for end user you should parse the
                     * error and show appropriate message
                     */
                    JSONObject json = new JSONObject(response);
                    
                } catch (JSONException e1) {
                    Log.e("FB", e1.getMessage());
                }
            }
        }

        public void onFacebookError(FacebookError error) {
            dialog.dismiss();
            Log.e("FB", error.getMessage());
        }
    }


}
