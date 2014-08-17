package com.parabay.cinema.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.Animation3DQueue;
import rajawali.animation.RotateAnimation3D;
import rajawali.animation.RotateAroundAnimation3D;
import rajawali.animation.ScaleAnimation3D;
import rajawali.animation.TranslateAnimation3D;
import rajawali.filters.SepiaFilter;
import rajawali.filters.SwirlFilter;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.materials.CubeMapMaterial;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.PhongMaterial;
import rajawali.materials.TextureInfo;
import rajawali.math.AngleAxis;
import rajawali.math.MathUtil;
import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;
import rajawali.math.Quaternion;
import rajawali.parser.ObjParser;
import rajawali.primitives.Cube;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.MeshExporter;
import rajawali.util.MeshExporter.ExportType;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parabay.cinema.R;
import com.parabay.cinema.ui.Base3DActivity;

import android.util.FloatMath;

public class BlenderLoadModelRenderer extends RajawaliRenderer {
	
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final float TRACKBALL_SCALE_FACTOR = 36.0f;
    private float mPreviousX;
    private float mPreviousY;
    
	private PointLight mLight;
	private BaseObject3D mObjectGroup;
	private List<Animation3DQueue>  queueList = new ArrayList<Animation3DQueue>();
	
	public BlenderLoadModelRenderer(Context context) {
		super(context);
		setFrameRate(60);
	}

    @Override public void onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        //switch (e.getAction()) {
        
        //case MotionEvent.ACTION_MOVE:
            float dx = x - mPreviousX;
            float dy = y - mPreviousY; 
            mObjectGroup.rotateAround(Number3D.getAxisVector(Axis.X), dx * TOUCH_SCALE_FACTOR);
            mObjectGroup.rotateAround(Number3D.getAxisVector(Axis.Y), dy * TOUCH_SCALE_FACTOR);
        //default:
        	//Log.i("DEBUG", Integer.toString(e.getAction()));
        //}
        mPreviousX = x;
        mPreviousY = y;
        return;
    }
    
	public Quaternion fromEuler(final float x, final float y, final float z) {
		
		float u0 = FloatMath.sqrt(FloatMath.cos(y)*FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)* FloatMath.cos(z)+1)/2;
		float u1 = (FloatMath.cos(x)*FloatMath.sin(z)+FloatMath.cos(y)*FloatMath.sin(z)+FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.cos(z))/FloatMath.sqrt(FloatMath.cos(y)* FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;
		float u2 = (FloatMath.sin(y)*FloatMath.sin(z)-FloatMath.cos(y)*FloatMath.sin(x)*FloatMath.cos(z)-FloatMath.sin(x))/FloatMath.sqrt(FloatMath.cos(y)*FloatMath.cos(x)+ FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;
		float u3 = (FloatMath.sin(y)*FloatMath.cos(x)+FloatMath.sin(y)*FloatMath.cos(z)+FloatMath.cos(y)*FloatMath.sin(x)*FloatMath.sin(z))/FloatMath.sqrt(FloatMath.cos(y)* FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;

		Quaternion q = new Quaternion(u0, u1, u2, u3);
		return q;
	}
	
	protected void initScene() {
		mLight = new PointLight();
		mLight.setPosition(4.07625f, -1.00545f, 5.90386f);
		mLight.setPower(6);
		
		mCamera.setLookAt(0, 0, 0);
		mCamera.setPosition(7.48113f, 6.50764f, 5.34367f);

		mCamera.setFogNear(10);
		mCamera.setFogFar(30);
		mCamera.setFogColor(0x999999);
		
		setFogEnabled(true);
		setBackgroundColor(0x999999);
		
		ObjParser objParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.test_obj);
		objParser.parse();
		mObjectGroup = objParser.getParsedObject();
		mObjectGroup.addLight(mLight);
		addChild(mObjectGroup);

		MeshExporter exporter = new MeshExporter(mObjectGroup);
		exporter.export("test.ser", ExportType.SERIALIZED);

		InputStream inputStream = this.getContext().getResources().openRawResource(R.raw.test);
		
		JSONObject action = null;
		JSONObject root = null;
		
		
		try {
			
	        byte[] b = new byte[inputStream.available()];
	        inputStream.read(b);
	        String inpStr = new String(b);

			root = new JSONObject(inpStr);

			JSONArray actions = root.getJSONArray("actions");
			for(int i=0; i<actions.length(); i++){
				action = actions.getJSONObject(i);
				String actionId = action.getString("id");
				Log.i("DEBUG", actionId);
				if (actionId.contains("CubeAction")) {
					break;
				}
			}
			
			if (action != null) {
				
				JSONArray channels = action.getJSONArray("channels");
				for(int i=0; i<channels.length(); i++){
					
					JSONObject channel = channels.getJSONObject(i);		
					Animation3DQueue mQueue = new Animation3DQueue();
					queueList.add(mQueue);
					
					String channelId = channel.getString("id");
					Log.i("DEBUG", channelId);
						
						JSONArray keyFrames = channel.getJSONArray("kf");
						Log.i("DEBUG", String.format("Found %d keyframes.", keyFrames.length()));
						for(int j=0; j<keyFrames.length(); j++){
							
							JSONObject kf = keyFrames.getJSONObject(j);
							double t = kf.getDouble("t");
							double x = kf.getDouble("x");
							double y = kf.getDouble("y");
							double z = kf.getDouble("z");
							
							Log.i("DEBUG", String.format("%s %f : (%f, %f, %f)", channelId, t, x, y, z));
							
							if (channelId.contains("location")) {

								TranslateAnimation3D anim = new TranslateAnimation3D(new Number3D(x, y, z));
								anim.setDuration((long)t*1000);
								//anim.setRepeatMode(Animation3D.REVERSE);
								//anim.setRepeatCount(Animation3D.INFINITE);	
								anim.setTransformable3D(mObjectGroup);
								//mQueue.addAnimation(anim);
								
							}
							else if (channelId.contains("rotation")) {
																
								Quaternion q = this.fromEuler((float)x, (float)y, (float)z);
								
								Log.i("DEBUG", String.format("%s %f : (%f, %f, %f)", channelId, t, MathUtil.radiansToDegrees((float)x), MathUtil.radiansToDegrees((float)y), MathUtil.radiansToDegrees((float)z)));
								Log.i("DEBUG", q.toString());
								
								AngleAxis aa = q.toAngleAxis();
								Log.i("DEBUG", aa.toString());
								
								RotateAnimation3D anim = new RotateAnimation3D(aa.getAxis(), aa.getAngle());
								anim.setDuration((long)t*1000);
								//anim.setRepeatMode(Animation3D.REVERSE);
								//anim.setRepeatCount(Animation3D.INFINITE);
								anim.setTransformable3D(mObjectGroup);
								mQueue.addAnimation(anim);
								
							}
							else if (channelId.contains("scale")) {
								
								Animation3D anim = new ScaleAnimation3D(new Number3D(x, y, z));
								anim.setDuration((long)t*1000);
								//anim.setRepeatCount(3);
								//anim.setRepeatMode(Animation3D.REVERSE);
								anim.setTransformable3D(mObjectGroup);
								mQueue.addAnimation(anim);

								
							}
						}

				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
				
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		((Base3DActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((Base3DActivity) mContext).hideLoader();
		
		//for(int i=0; i<queueList.size(); i++)
		//	queueList.get(i).start();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		
	}
	
	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
				
	}
}
