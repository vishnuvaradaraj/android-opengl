package com.parabay.cinema.facebook;

import android.app.Activity;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.parabay.cinema.facebook.FacebookPhotoSync;

public class FacebookGlobals {

	static private FacebookPhotoSync facebookPhotoSync;
	static private UserDatabase userDatabase;
	static private ImageManager imageManager;
	static private ImageTile imageTile;
    static private Facebook facebook;
    static private AsyncFacebookRunner asyncRunner;

	static private Activity activity;
	
	static public void init(Activity activity) {
		
		facebookPhotoSync = new FacebookPhotoSync(activity);
		userDatabase = new UserDatabase(activity);
		imageManager = new ImageManager(activity);
		imageTile = new ImageTile(activity);
		
        userDatabase.open();

	}
	
	static public void shutdown() {
		
		facebookPhotoSync = null;
		userDatabase = null;
		imageManager = null;
		imageTile = null;
	}
	
	static public FacebookPhotoSync getFacebookPhotoSync() {
		return facebookPhotoSync;
	}
	
	static public UserDatabase getUserDatabase() {
		return userDatabase;
	}
	
	static public ImageManager getImageManager() {
		return imageManager;
	}

	public static ImageTile getImageTile() {
		return imageTile;
	}

	public static Facebook getFacebook() {
		return facebook;
	}

	public static AsyncFacebookRunner getAsyncRunner() {
		return asyncRunner;
	}
	
}
