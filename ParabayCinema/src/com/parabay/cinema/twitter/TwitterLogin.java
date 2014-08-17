package com.parabay.cinema.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parabay.cinema.LoginService;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.ui.MainActivity;

public class TwitterLogin implements LoginService {

	static String TWITTER_CONSUMER_KEY = "mupHAgTu6vw4at8YZTBmw"; // place your cosumer key here
	static String TWITTER_CONSUMER_SECRET = "eYj3ZjPxh5T3WS3IlsR7eys4jyMLRV1E5eqVMnpwc"; // place your consumer secret here

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "oauth://com.parabay.cinema.twitter";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	private Activity mActivity;

	private static final String TAG = "TwitterLogin";

	private SharedPreferences mSharedPreferences;
	
	// Progress dialog
	ProgressDialog pDialog;

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;
		
	// Internet Connection detector
	private ConnectionDetector cd;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	
	public TwitterLogin() {
		
	}
	
	public void init(FragmentActivity activity, Bundle savedInstanceState) {
		
		mActivity = activity;
		
		this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
		
        if (this.mSharedPreferences.getString("auth", "") == TAG) {
        	login();
        }
        
		cd = new ConnectionDetector(this.mActivity.getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(this.mActivity, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		
	}
	
	public void login() {
		
        if (!isConnected()) {

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 
			
			// Call login twitter function
			loginToTwitter();

        }
        else {
        	Log.i(TAG, "Twitter logged in already");
        	this.onLogin();
        }

	}
	
	public void logout() {
		
    	if (isConnected()) {
    		
			Editor e = this.mSharedPreferences.edit();
        	e.remove("auth");
    		e.remove(PREF_KEY_OAUTH_TOKEN);
    		e.remove(PREF_KEY_OAUTH_SECRET);
    		e.remove(PREF_KEY_TWITTER_LOGIN);
    		e.commit();

    	}  
	}
	
	public boolean isConnected() {
	
		return isTwitterLoggedInAlready();
	}
	
	protected void onLogin() {
		
        if (isConnected()) {
            
        	Editor e = this.mSharedPreferences.edit();
        	e.putString("auth", TAG);
        	e.commit();
        	
        	GalleryApp app = (GalleryApp)this.mActivity.getApplicationContext();
        	app.setLoginService(this);

        	this.mActivity.finish();
        	
    		Intent intent = new Intent(this.mActivity, MainActivity.class);                
    		this.mActivity.startActivity(intent);
        	
        }
	}
	
	private void loginToTwitter() {
		
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();
			
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter
						.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				this.mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			// user already logged into twitter
			
			this.onLogin();
		}
	}

	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}
	
	public void processCallback(Intent intent) {
		
		if (!isTwitterLoggedInAlready()) {
			Uri uri = intent.getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
					
					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getName();
					
					this.onLogin();
					
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}
		
	}
}
