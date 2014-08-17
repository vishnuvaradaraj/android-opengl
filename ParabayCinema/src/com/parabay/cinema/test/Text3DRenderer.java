package com.parabay.cinema.test;

import java.io.IOException;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.parabay.cinema.R;
import com.parabay.cinema.text.FontChar;
import com.parabay.cinema.text.FontInfo;
import com.parabay.cinema.ui.Base3DActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.util.Log;
import rajawali.BaseObject3D;
import rajawali.lights.DirectionalLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.primitives.Cube;
import rajawali.primitives.Plane;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;
import rajawali.math.Number3D;
import rajawali.primitives.Line3D;

public class Text3DRenderer extends RajawaliRenderer {
	
	public static final String TAG = "Text3DRenderer";
	
	private DirectionalLight mLight;
	private FontChar fontChar1;

	private FontInfo font = new FontInfo();
	
	public Text3DRenderer(Context context) {
		super(context);
		setFrameRate(60);
		
		
		try {
    		
    		font.initData(context, "Roboto-Regular.ttf");	
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void initScene() {
		
		char c = 'B';
		boolean f = true;
				
		mLight = new DirectionalLight();
		mLight.setPower(7);
		mLight.setPosition(0, 0, 7);
		mCamera.setPosition(0, 0, 7);
		mCamera.setLookAt(0, 0, 0);

		
		float xpos = 0f;
		float charWidth = 0f;
		
		fontChar1 = new FontChar(c, this.font, f);
		charWidth = fontChar1.getGlyphData().getBoundingBox().getWidth();
		
		DiffuseMaterial material1 = new DiffuseMaterial();
		material1.setUseColor(true);
		fontChar1.setMaterial(material1);
		

		fontChar1.addLight(mLight);
		fontChar1.setColor(0xff00ff00);
		fontChar1.setScale(0.001f);
		fontChar1.setX(xpos);
		addChild(fontChar1);

	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		((Base3DActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((Base3DActivity) mContext).hideLoader();
	}

	public void onDrawFrame(GL10 glUnused) {
		
		super.onDrawFrame(glUnused);
		fontChar1.setRotY(fontChar1.getRotY() + 1);
	}
}
