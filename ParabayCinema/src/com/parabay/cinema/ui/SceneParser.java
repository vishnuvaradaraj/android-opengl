package com.parabay.cinema.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rajawali.ATransformable3D;
import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.SerializedObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.Animation3DQueue;
import rajawali.animation.RotateAnimation3D;
import rajawali.animation.ScaleAnimation3D;
import rajawali.animation.TranslateAnimation3D;
import rajawali.lights.PointLight;
import rajawali.materials.TextureManager;
import rajawali.math.AngleAxis;
import rajawali.math.MathUtil;
import rajawali.math.Number3D;
import rajawali.math.Quaternion;
import rajawali.parser.AParser;
import rajawali.parser.ObjParser;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.MeshExporter;
import rajawali.util.MeshExporter.ExportType;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.FloatMath;
import android.util.Log;

import com.parabay.cinema.R;

public class SceneParser  {

	private static final String TAG = "SceneParser";
			
	private Resources mResources;
	private TextureManager mTextureManager;
	private int mParseResourceId;
	
	private PointLight mLight1;
	private PointLight mLight2;
	private PointLight mLight3;
	private Camera mCamera;
	
	private Map<String, BaseObject3D> mapObjects = new HashMap<String, BaseObject3D>();
	private Map<String, String> mapActions = new HashMap<String, String>();
	private Map<String, List<Animation3DQueue> > mapAnimations = new HashMap<String, List<Animation3DQueue> >();
	    
	private List<Animation3DQueue>  queueList = new ArrayList<Animation3DQueue>();
	
	public SceneParser(Resources resources, TextureManager textureManager, int resourceId) {
		
		this.mResources = resources;
		this.mTextureManager = textureManager;
		this.mParseResourceId = resourceId;
		
		this.loadLights();
	}
		
	public void loadLights() {
		
		mLight1 = new PointLight();
		mLight1.setPosition(4.07625f, 1.00545f, 5.90386f);
		mLight1.setPower(1);
		
		mLight2 = new PointLight();
		mLight2.setPosition(-1.99457f, 4.13236f, 1.55009f);
		mLight2.setPower(1);
		
		mLight3 = new PointLight();
		mLight3.setPosition(-7.75055f, -1.33129f, -6.09659f);
		mLight3.setPower(1);
	}
	
	public void addLights(BaseObject3D obj) {
		
		obj.addLight(mLight1);
		obj.addLight(mLight2);
		obj.addLight(mLight3);
	}
	
	public void addActionMap(String actionName, String objName) {
		
	}
	
	public BaseObject3D loadModel(int resourceId, String name) throws StreamCorruptedException, NotFoundException, IOException, ClassNotFoundException {
		
		ObjectInputStream ois = new ObjectInputStream(mResources.openRawResource(resourceId));
		SerializedObject3D serializedObj = (SerializedObject3D) ois.readObject();
		ois.close();

		BaseObject3D obj = new BaseObject3D(serializedObj);
		this.addLights(obj);
		
		this.mapObjects.put(name, obj);
		
		return obj;
	}
	
	public BaseObject3D serializeModel(int id, String name) {
				
		ObjParser objParser = new ObjParser(mResources, mTextureManager, id);
		objParser.parse();
		
		BaseObject3D obj = objParser.getParsedObject();
		this.addLights(obj);
		
		//MeshExporter exporter = new MeshExporter(obj);
		//exporter.export(name + "_ser", ExportType.SERIALIZED);

		return obj;
	}
	
	public Quaternion fromEuler(final float x, final float y, final float z) {
		
		float u0 = FloatMath.sqrt(FloatMath.cos(y)*FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)* FloatMath.cos(z)+1)/2;
		float u1 = (FloatMath.cos(x)*FloatMath.sin(z)+FloatMath.cos(y)*FloatMath.sin(z)+FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.cos(z))/FloatMath.sqrt(FloatMath.cos(y)* FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;
		float u2 = (FloatMath.sin(y)*FloatMath.sin(z)-FloatMath.cos(y)*FloatMath.sin(x)*FloatMath.cos(z)-FloatMath.sin(x))/FloatMath.sqrt(FloatMath.cos(y)*FloatMath.cos(x)+ FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;
		float u3 = (FloatMath.sin(y)*FloatMath.cos(x)+FloatMath.sin(y)*FloatMath.cos(z)+FloatMath.cos(y)*FloatMath.sin(x)*FloatMath.sin(z))/FloatMath.sqrt(FloatMath.cos(y)* FloatMath.cos(x)+FloatMath.cos(y)*FloatMath.cos(z)-FloatMath.sin(y)*FloatMath.sin(x)*FloatMath.sin(z)+FloatMath.cos(x)*FloatMath.cos(z)+1)/2;

		Quaternion q = new Quaternion(u0, u1, u2, u3);
		return q;
	}
	
	protected List<Animation3DQueue> parseAction(JSONObject action, ATransformable3D obj) throws JSONException {
				
		JSONArray channels = action.getJSONArray("channels");
		for(int i=0; i<channels.length(); i++){
			
			JSONObject channel = channels.getJSONObject(i);		
			Animation3DQueue mQueue = new Animation3DQueue();
			queueList.add(mQueue);
			
			String channelId = channel.getString("id");
			Log.i(TAG, channelId);
				
				JSONArray keyFrames = channel.getJSONArray("kf");
				Log.i(TAG, String.format("Found %d keyframes.", keyFrames.length()));
				
				double t0 = 0;
				for(int j=0; j<keyFrames.length(); j++){
					
					JSONObject kf = keyFrames.getJSONObject(j);
					double t = kf.getDouble("t");
					double x = kf.getDouble("x");
					double y = kf.getDouble("y");
					double z = kf.getDouble("z");
					
					double d = (t-t0);
					t0 = t;
					
					Log.i(TAG, String.format("%s %f : (%f, %f, %f)", channelId, t, x, y, z));
					
					if (channelId.contains("location")) {

						TranslateAnimation3D anim = new TranslateAnimation3D(new Number3D(x, y, z));
						anim.setDuration((long)d*1000);
						anim.setTransformable3D(obj);
						mQueue.addAnimation(anim);
						
					}
					else if (channelId.contains("rotation")) {
														
						Quaternion q = this.fromEuler((float)x, (float)y, (float)z);
						
						Log.i(TAG, String.format("%s %f : (%f, %f, %f)", channelId, t, MathUtil.radiansToDegrees((float)x), MathUtil.radiansToDegrees((float)y), MathUtil.radiansToDegrees((float)z)));
						Log.i(TAG, q.toString());
						
						AngleAxis aa = q.toAngleAxis();
						Log.i(TAG, aa.toString());
						
						RotateAnimation3D anim = new RotateAnimation3D(aa.getAxis(), aa.getAngle());
						anim.setDuration((long)d*1000);
						anim.setTransformable3D(obj);
						mQueue.addAnimation(anim);
						
					}
					else if (channelId.contains("scale")) {
						
						Log.i(TAG, String.format("%s %f : (%f, %f, %f)", channelId, t, x, y, z));

						Animation3D anim = new ScaleAnimation3D(new Number3D(x, y, z));
						anim.setDuration((long)d*1000);
						anim.setTransformable3D(obj);
						mQueue.addAnimation(anim);
						
					}
				}
		}

		String actionId = action.getString("id");
		mapAnimations.put(actionId, queueList);
		return queueList;
	}
	
	public void parse(ATransformable3D camera) {
						
		InputStream inputStream = mResources.openRawResource(this.mParseResourceId);
		
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
				Log.i(TAG, actionId);
				if (actionId.contains("CameraAction")) {
					break;
				}
			}
			
			if (action != null) {
				
				this.parseAction(action, camera);
			}
			else {
				Log.i(TAG, "No actions found.");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
	}
	
	public void start() {
		
		for(Animation3DQueue q : queueList)
			q.start();
	}

}
