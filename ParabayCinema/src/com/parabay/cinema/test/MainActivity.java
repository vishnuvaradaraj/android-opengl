package com.parabay.cinema.test;


import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import com.parabay.cinema.R;
import com.parabay.cinema.facebook.BaseRequestListener;
import com.parabay.cinema.facebook.FacebookPhotoSync;
import com.parabay.cinema.facebook.FacebookGlobals;
import com.parabay.cinema.facebook.LoginButton;
import com.parabay.cinema.facebook.SessionEvents;
import com.parabay.cinema.facebook.SessionStore;
import com.parabay.cinema.facebook.Utility;
import com.parabay.cinema.facebook.SessionEvents.AuthListener;
import com.parabay.cinema.facebook.SessionEvents.LogoutListener;
import com.parabay.cinema.text.FontInfo;
import com.parabay.cinema.text.tess.PGLU;
import com.parabay.cinema.text.tess.PGLUtessellator;
import com.parabay.cinema.text.tess.PGLUtessellatorCallbackAdapter;
import com.parabay.cinema.text.ttf.TTFParser;
import com.parabay.cinema.text.ttf.TrueTypeFont;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    /*
     * Your Facebook Application ID must be set before running this example See
     * http://www.facebook.com/developers/createapp.php
     */
    public static final String APP_ID = "485152904830339";

    private LoginButton mLoginButton;
    private Button mPlayButton;
    private Button mLettersButton;
    
    private TextView mText;
    private ImageView mUserPic;
    private Handler mHandler;
    ProgressDialog dialog;
    
    final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    final static int PICK_EXISTING_PHOTO_RESULT_CODE = 1;

    String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
            "photo_upload" };	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_login);
        
        FacebookGlobals.init(this);
                
        mText = (TextView) MainActivity.this.findViewById(R.id.txt);
        mUserPic = (ImageView) MainActivity.this.findViewById(R.id.user_pic);

        mHandler = new Handler();

        // Create the Facebook Object using the app id.
        Utility.mFacebook = new Facebook(APP_ID);
        // Instantiate the asynrunner object for asynchronous api calls.
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);

        mLoginButton = (LoginButton) findViewById(R.id.login);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	FontInfo font = new FontInfo();
            	
            	try {
            		
            		font.initData(MainActivity.this, "Roboto-Bold.ttf");
            		
			        
				} catch (IOException e) {
					e.printStackTrace();
				}
            			
            	
            	FacebookGlobals.getFacebookPhotoSync().loadPhotos();
            	
            	FacebookGlobals.getImageTile().getTile(0);
            	
            	Intent myIntent = new Intent(MainActivity.this, PlanesActivity.class);
            	MainActivity.this.startActivity(myIntent);
            	
            }
        });

        // restore session if one exists
        SessionStore.restore(Utility.mFacebook, this);
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());

        mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);

        if (Utility.mFacebook.isSessionValid()) {
            requestUserData();
        }

       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(Utility.mFacebook != null) {
            if (!Utility.mFacebook.isSessionValid()) {
                mText.setText("Please login.");
                mUserPic.setImageBitmap(null);
            } else {
                Utility.mFacebook.extendAccessTokenIfNeeded(this, null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_RESULT_CODE: {
                Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
                break;
            }
 
        }
    }

    /*
     * Request user name, and picture to show on the main screen.
     */
    public void requestUserData() {
        mText.setText("Fetching user name, profile pic...");
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");
        Utility.mAsyncRunner.request("me", params, new UserRequestListener());
    }
    
    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            try {
            	//{"name":"Parabaye Helpe","id":"100002270876575","picture":{"data":{"url":"http:\/\/profile.ak.fbcdn.net\/hprofile-ak-snc4\/372344_100002270876575_285317821_q.jpg","is_silhouette":false}}}
            	
                jsonObject = new JSONObject(response);

                String url = "";
                final JSONObject pic = jsonObject.getJSONObject("picture");
                if (null != pic) {
                	final JSONObject dat = pic.getJSONObject("data");
                	if (null != dat) {
                		url = dat.getString("url");
                	}
                }
                
                final String picURL = url;
                final String name = jsonObject.getString("name");
                Utility.userUID = jsonObject.getString("id");

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    	
                    	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .build());
                    	
                        mText.setText("Welcome " + name + "!");
                        mUserPic.setImageBitmap(Utility.getBitmap(picURL));
                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /*
     * The Callback for notifying the application when authorization succeeds or
     * fails.
     */

    public class FbAPIsAuthListener implements AuthListener {

        @Override
        public void onAuthSucceed() {
            requestUserData();
        }

        @Override
        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        @Override
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        @Override
        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
            mUserPic.setImageBitmap(null);
        }
    }
}
