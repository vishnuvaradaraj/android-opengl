package com.parabay.cinema.test;

import java.io.ObjectInputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.parabay.cinema.R;
import com.parabay.cinema.ui.Base3DActivity;

import rajawali.BaseObject3D;
import rajawali.SerializedObject3D;
import rajawali.lights.DirectionalLight;
import rajawali.materials.PhongMaterial;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;

public class RajawaliUIElementsRenderer extends RajawaliRenderer {
	private DirectionalLight mLight;
	private BaseObject3D mMonkey;

	public RajawaliUIElementsRenderer(Context context) {
		super(context);
		setFrameRate(60);
	}

	protected void initScene() {
		mLight = new DirectionalLight(0, 0, 1);
		mLight.setPower(.8f);
		mCamera.setPosition(0, 0, -7);

		try {
			ObjectInputStream ois = new ObjectInputStream(mContext.getResources().openRawResource(R.raw.monkey_ser));
			SerializedObject3D serializedMonkey = (SerializedObject3D) ois.readObject();
			ois.close();

			mMonkey = new BaseObject3D(serializedMonkey);
			mMonkey.addLight(mLight);
			addChild(mMonkey);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PhongMaterial material = new PhongMaterial();
		material.setUseColor(true);
		mMonkey.setMaterial(material);
		mMonkey.setColor(0xffcfb52b);
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		((Base3DActivity) mContext).showLoader();
		super.onSurfaceCreated(gl, config);
		((Base3DActivity) mContext).hideLoader();
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		mMonkey.setRotY(mMonkey.getRotY() + 1);
	}
}
