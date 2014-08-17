package com.parabay.cinema.ui;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.parabay.cinema.R;
import com.parabay.cinema.data.DataItem;
import com.parabay.cinema.data.MediaStream;
import com.parabay.cinema.media.app.GalleryApp;
import com.parabay.cinema.media.app.GalleryContext;
import com.parabay.cinema.media.app.SlideshowPage.Slide;
import com.parabay.cinema.media.data.DataManager;
import com.parabay.cinema.media.util.Future;
import com.parabay.cinema.media.util.FutureListener;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.Animation3DQueue;
import rajawali.animation.RotateAnimation3D;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager;
import rajawali.math.Number3D;
import rajawali.parser.ObjParser;
import rajawali.primitives.Cube;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;

public class MainRenderer extends RajawaliRenderer {

	private static final String TAG = "MainRenderer";
	
    public static final int MSG_LOAD_NEXT_BITMAP = 1;
    public static final int MSG_SHOW_PENDING_BITMAP = 2;

    private static final int MAX_TEXTURES = 2;
    
	private Animation3D mAnim;
	private MediaStream mPhotoStream;
	private Handler mHandler;
	private boolean mIsActive = false;
			
	private Cube mCube;
	private SimpleMaterial mMaterial;
	
	private int mIndex = 0;
	private TextureInfo textureInfo;
	private List<MediaCacheItem> mCachedItems = new ArrayList<MediaCacheItem>();
	
	private int processedItemCount = 0;
	
	private PointLight mLight;
	private BaseObject3D mObjectGroup;
	private List<Animation3DQueue>  queueList = new ArrayList<Animation3DQueue>();
	
	private SceneParser mParser;
	
	public MainRenderer(Context context) {
		
		super(context);
		
		mPhotoStream = new MediaStream();		
		GalleryApp gc = (GalleryApp)context.getApplicationContext();
		mPhotoStream.init(gc, mTextureManager, DataManager.INCLUDE_IMAGE, true, null);
		
		mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                	case MSG_SHOW_PENDING_BITMAP:
                		updateTexture();
                    	
                    break;
                    case MSG_LOAD_NEXT_BITMAP:
                    	nextTexture();
                        break;
                    default: throw new AssertionError();
                }
            }
        };
        
		setFrameRate(30);
	}
	
	protected void loadModel() {
	
		mCamera.setLookAt(0, 0, 0);
		mCamera.setPosition(8.57499f, -4.98123f, 4.71415f);

		setBackgroundColor(0x999999);
		
		this.mParser = new SceneParser(getContext().getResources(), this.mTextureManager, R.raw.scene);
		this.mParser.loadLights();
		
		BaseObject3D obj = this.mParser.serializeModel(R.raw.book_obj, "book");
		addChild(obj);
		obj = this.mParser.serializeModel(R.raw.photo_obj, "photo");
		addChild(obj);

		this.mParser.parse(mCamera);
	}
	
	public void nextTexture() {
	
		this.mPhotoStream.nextItem(new FutureListener<DataItem>() {
	        public void onFutureDone(Future<DataItem> future) {
	            DataItem item = future.get();
	            if (null != item) {
	            	
	            	Log.i(TAG, "Found next item:" + item.toString());
	            	MediaCacheItem cacheItem = new MediaCacheItem(
	            			(Bitmap)item.getData(), 
	            			MainRenderer.this.mTextureManager);
	            	MainRenderer.this.mCachedItems.add(cacheItem);
	            	
	                mHandler.sendEmptyMessage(MSG_LOAD_NEXT_BITMAP);
	            }
	        }
	    });
	
	}

	public void updateTexture() {
		
		MediaCacheItem mci = MainRenderer.this.getItem(MainRenderer.this.mIndex);
		if (this.textureInfo != null && mci != null && mci.textureInfo != null) {
			
			Log.i(TAG, "Removing texture: " + this.textureInfo.toString());
			mMaterial.getTextureInfoList().remove(this.textureInfo);

			MainRenderer.this.mIndex = (MainRenderer.this.mIndex+1) % this.processedItemCount;
			
			Log.i(TAG, "Adding texture: " + mci.textureInfo.toString());
			mMaterial.getTextureInfoList().add(mci.textureInfo);
			this.textureInfo = mci.textureInfo;
			
		}

		if (MainRenderer.this.mIsActive)
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_PENDING_BITMAP, 3000);

	}
	
	protected void initScene() {
		
		/*
    	setBackgroundColor(0xffffff);
    	
    	mMaterial = new SimpleMaterial();
    	
    	
    	mCamera.setZ(-6);
    	
    	mCube = new Cube(1);
    	mCube.setMaterial(mMaterial);
    	addChild(mCube);
		
    	Number3D axis = new Number3D(3, 1, 6);
		axis.normalize();
		mAnim = new RotateAnimation3D(axis, 360);
		mAnim.setDuration(8000);
		mAnim.setRepeatCount(Animation3D.INFINITE);
		mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
		mAnim.setTransformable3D(mCube);
    	
    	MediaCacheItem cacheItem1 = new MediaCacheItem(
    			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.flickrpics), 
    			MainRenderer.this.mTextureManager);
    	cacheItem1.update(false);
    	this.mCachedItems.add(cacheItem1);
    	
    	MediaCacheItem cacheItem2 = new MediaCacheItem(
    			BitmapFactory.decodeResource(getContext().getResources(), R.drawable.rajawali_tex), 
    			MainRenderer.this.mTextureManager);
    	this.mCachedItems.add(cacheItem2);

    	
		this.textureInfo = cacheItem1.textureInfo;
		this.mMaterial.addTexture(this.textureInfo, false, true);
		*/
    	
		this.loadModel();
	}
	
    
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
				
		((Base3DActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((Base3DActivity) mContext).hideLoader();
		
		this.mParser.start();
		
		/*
		mAnim.start();
		
		mHandler.sendEmptyMessageDelayed(MSG_LOAD_NEXT_BITMAP, 2000);
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_PENDING_BITMAP, 3000);
		*/
	}
	
    public void onDrawFrame(GL10 glUnused) {
    	
    	if (this.processedItemCount != this.mCachedItems.size()) {
    		
    		Log.i(TAG, "Processing texture list.");
	    	for(MediaCacheItem mci : this.mCachedItems) {
	    		mci.update(false);
	    	}
	    	this.processedItemCount = this.mCachedItems.size();
    	}
    	
        super.onDrawFrame(glUnused);
    }

	public void onPause() {
		
		mIsActive = false;
		this.mPhotoStream.pause();
	}
	
	public void onResume() {
		
		mIsActive = true;
		this.mPhotoStream.resume();		
	}

	public MediaCacheItem getItem(int index) {

		MediaCacheItem mci = null;
		if (this.mCachedItems.size() > 0) {
			mci = this.mCachedItems.get(index % this.mCachedItems.size());
		}
		return mci;
	}

	public static class MediaCacheItem {

		public Bitmap dataItem;
		public TextureInfo textureInfo;
		private TextureManager textureManager;
		
		public MediaCacheItem(Bitmap b, TextureManager texMgr) {
			
			this.dataItem = b;
			this.textureManager = texMgr;			
		}
		
		
		public boolean update(boolean forceUpdate) {
			
			boolean ret = false;
			
			if (this.textureInfo == null) {
				
				Log.i(TAG, "Creating textureInfo..");
				ret = true;
				this.textureInfo = this.textureManager.addTexture((Bitmap)this.dataItem, false, true);  		
			}
			else {
				
				if (forceUpdate)
					this.textureManager.updateTexture(this.textureInfo, this.dataItem);
			}
			
			return ret;
		}
	}
}
