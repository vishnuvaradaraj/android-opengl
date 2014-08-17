package com.parabay.cinema.facebook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.parabay.cinema.LoginActivity;
import com.parabay.cinema.LoginService;
import com.parabay.cinema.R;
import com.parabay.cinema.SplashActivity;
import com.parabay.cinema.facebook.SessionEvents.AuthListener;
import com.parabay.cinema.facebook.SessionEvents.LogoutListener;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.ui.MainActivity;

public class FacebookLogin implements LoginService {

    public static final String APP_ID = "485152904830339";

    private Activity mActivity;
    private Button mLoginButton;
    private Handler mHandler;
    ProgressDialog dialog;
    
    final public static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    final public static int PICK_EXISTING_PHOTO_RESULT_CODE = 1;

	private static final String TAG = "FacebookLogin";

    private String[] mPermissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
            "photo_upload" };	

	private SharedPreferences preferences;
	
	public FacebookLogin() {
		
	}
	
	public void init(FragmentActivity activity, Bundle savedInstanceState) {
		
		mActivity = activity;
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
		
        FacebookGlobals.init(mActivity);       
        mHandler = new Handler();

        // Create the Facebook Object using the app id.
        Utility.mFacebook = new Facebook(APP_ID);
        // Instantiate the asynrunner object for asynchronous api calls.
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
        
        // restore session if one exists
        SessionStore.restore(Utility.mFacebook, mActivity);
        SessionEvents.addAuthListener(new FbAPIsAuthListener());
        SessionEvents.addLogoutListener(new FbAPIsLogoutListener());

        if (this.preferences.getString("auth", "") == TAG) {
        	login();
        }
	}
	
	public void login() {
		
        if (!isConnected()) {
         	Utility.mFacebook.authorize(mActivity, mPermissions, AUTHORIZE_ACTIVITY_RESULT_CODE, new LoginDialogListener());
        }
        else {
        	Log.i(TAG, "Facebook logged in already");
        	this.onLogin();
        }

	}
	
	public void logout() {
		
    	if (isConnected()) {
    		
			Editor e = this.preferences.edit();
        	e.remove("auth");
        	e.commit();

            SessionEvents.onLogoutBegin();
            AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
            asyncRunner.logout(mActivity, new LogoutRequestListener());
    	}  
	}
	
	public boolean isConnected() {
	
		return Utility.mFacebook.isSessionValid();
	}
	
	protected void onLogin() {
		
        if (Utility.mFacebook.isSessionValid()) {
            
        	Editor e = this.preferences.edit();
        	e.putString("auth", TAG);
        	e.commit();
        	
        	GalleryApp app = (GalleryApp)this.mActivity.getApplicationContext();
        	app.setLoginService(this);

        	this.mActivity.finish();
        	
    		Intent intent = new Intent(this.mActivity, MainActivity.class);                
    		this.mActivity.startActivity(intent);
        	
        }
	}

	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
     * The Callback for notifying the application when authorization succeeds or
     * fails.
     */

    public class FbAPIsAuthListener implements AuthListener {

        @Override
        public void onAuthSucceed() {
        	SessionStore.save(Utility.mFacebook, mActivity);
            onLogin();
        }

        @Override
        public void onAuthFail(String error) {
        	
        }
    }

    /*
     * The Callback for notifying the application when log out starts and
     * finishes.
     */
    public class FbAPIsLogoutListener implements LogoutListener {
        @Override
        public void onLogoutBegin() {
            
        }

        @Override
        public void onLogoutFinish() {
        	SessionStore.clear(mActivity);
        }
    }

    private final class LoginDialogListener implements DialogListener {
        @Override
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        @Override
        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        @Override
        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        @Override
        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }

    private class LogoutRequestListener extends BaseRequestListener {
        @Override
        public void onComplete(String response, final Object state) {
            /*
             * callback should be run in the original thread, not the background
             * thread
             */
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }

}
