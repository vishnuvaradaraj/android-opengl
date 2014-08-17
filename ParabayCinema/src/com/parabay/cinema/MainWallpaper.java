package com.parabay.cinema;

import rajawali.wallpaper.Wallpaper;
import android.content.Context;

public class MainWallpaper extends Wallpaper {
	private MainWallpaperRenderer mRenderer;

	public Engine onCreateEngine() {
		mRenderer = new MainWallpaperRenderer(this);
		return new WallpaperEngine(this.getSharedPreferences("parabay3dtvsharedprefs",
				Context.MODE_PRIVATE), getBaseContext(), mRenderer, false);
	}
}
