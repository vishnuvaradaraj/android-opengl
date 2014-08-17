package com.parabay.cinema;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.parabay.cinema.ui.MainActivity;

public class SplashActivity extends Activity {

	protected static final String TAG = "SplashActivity";
	
	private Thread mWorkerThread;	
	private AccountManager accountManager;
	
	
	  private static final String AUTH_TOKEN_TYPE = "lh2";
	
	  private static final int REQUEST_AUTHENTICATE = 0;
	
	  private static final String PREF = "MyPrefs";
	
	  private static final int DIALOG_ACCOUNTS = 0;
	
	  private String authToken;

	  private static HttpTransport transport;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
			    
		//transport = GoogleTransport.create();
	    //GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
	    
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
				
    	mWorkerThread =  new Thread() {
    		
    		@Override
    		public void run() {
    			try {
    				synchronized(this)
    				{   					
    					wait(2000);
    				}
    			} 
    			catch(InterruptedException ex)
    			{    				
    			}

    			finish();
    			
    			//MainActivity.playVideo(SplashActivity.this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.webm"), "Video Title");
    			    			     			
    		}
    	};
    	
    	mWorkerThread.start();

		Intent intent = new Intent(SplashActivity.this, MainActivity.class);                
		startActivity(intent);
		
		//accountManager = AccountManager.get(SplashActivity.this);
		//Account[] accounts = accountManager.getAccountsByType("com.google");
		
		//showDialog(DIALOG_ACCOUNTS);

	}
	  
    @Override
    public boolean onTouchEvent(MotionEvent evt)
    {
    	if(evt.getAction() == MotionEvent.ACTION_DOWN)
    	{
    		synchronized(mWorkerThread) {
    			mWorkerThread.notifyAll();
    		}
    	}
    	return true;
    }
}
