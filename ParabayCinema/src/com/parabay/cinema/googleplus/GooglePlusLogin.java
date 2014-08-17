package com.parabay.cinema.googleplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.parabay.cinema.LoginService;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.ui.MainActivity;

public class GooglePlusLogin implements 
	ConnectionCallbacks, OnConnectionFailedListener, LoginService  {

	private static final String TAG = "GooglePlusLogin";
	
    public static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    
	private FragmentActivity mActivity;
	private SharedPreferences preferences;

	public GooglePlusLogin() {
		
	}
	
	public Activity getActivity() {
		return this.mActivity;
	}
	
	public void init(FragmentActivity activity, Bundle savedInstanceState) {
		
		this.mActivity = activity;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this.mActivity);

        mPlusClient = new PlusClient(this.mActivity, this, this, Scopes.PLUS_PROFILE);
        // Progress bar to be displayed if the connection failure is not resolved.
        mConnectionProgressDialog = new ProgressDialog(this.mActivity);
        mConnectionProgressDialog.setMessage("Signing in...");
        
        if (this.preferences.getString("auth", "") == TAG) {
        	login();
        }
	}
	
	protected void onLogin() {
		
		if (isConnected()) {
			
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
	
	public boolean isConnected() {
		
		return ((this.mPlusClient != null) && (this.mPlusClient.isConnected()));
	}

    public void login() {
    	
    	if (!isConnected()) {
    		mPlusClient.connect();
    	}
    	else {
        	Log.i(TAG, "G+ logged in already");
        	this.onLogin();
        }
    }

    public void logout() {
    	
    	if (isConnected()) {
    		
			Editor e = this.preferences.edit();
        	e.remove("auth");
        	e.commit();

        	mPlusClient.disconnect();
    	}    		
    }
	
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
    	
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == this.mActivity.RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }
	
    @Override
    public void onConnectionFailed(ConnectionResult result) {
    	
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this.mActivity, REQUEST_CODE_RESOLVE_ERR);
            } catch (SendIntentException e) {
                mPlusClient.connect();
            }
        }
        // Save the result and resolve the connection failure upon a user click.
        mConnectionResult = result;
    }

    @Override
    public void onConnected() {
        String accountName = mPlusClient.getAccountName();        
        this.onLogin();
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "disconnected");
    }
}
